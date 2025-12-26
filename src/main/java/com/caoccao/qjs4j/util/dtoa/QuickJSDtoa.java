/*
 * Copyright (c) 2025-2026. caoccao.com Sam Cao
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.caoccao.qjs4j.util.dtoa;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * QuickJS-compatible dtoa (double-to-ASCII) implementation.
 * Based on QuickJS dtoa.c by Fabrice Bellard.
 * 
 * This implementation uses BigDecimal for arbitrary precision arithmetic
 * to match JavaScript's toString(radix) behavior exactly.
 */
public class QuickJSDtoa {
    
    private static final int MAX_RADIX = 36;
    
    // Maximum number of digits for each radix to represent a double (53-bit mantissa)
    // Based on: ceil(53 * log(2) / log(radix))
    private static final int[] MAX_DIGITS_TABLE = {
        0, 0,  // radix 0, 1 (invalid)
        53, 34, 27, 23, 21, 19, 18, 17, 16, 16,  // radix 2-11
        15, 15, 14, 14, 14, 14, 13, 13, 13, 13,  // radix 12-21
        13, 13, 12, 12, 12, 12, 12, 12, 12, 12,  // radix 22-31
        12, 12, 12, 12, 12                       // radix 32-36
    };
    
    /**
     * Convert double to string in specified radix using QuickJS algorithm.
     * Uses FREE format: finds minimum digits for unique representation.
     */
    public static String toString(double d, int radix) {
        if (radix < 2 || radix > MAX_RADIX) {
            throw new IllegalArgumentException("radix must be between 2 and 36");
        }
        
        // Handle special values
        if (Double.isNaN(d)) {
            return "NaN";
        }
        if (Double.isInfinite(d)) {
            return d > 0 ? "Infinity" : "-Infinity";
        }
        if (d == 0.0) {
            // Check for negative zero
            if (Double.doubleToRawLongBits(d) == Double.doubleToRawLongBits(-0.0)) {
                return "-0";
            }
            return "0";
        }
        
        StringBuilder result = new StringBuilder();
        
        // Extract IEEE 754 double components
        long bits = Double.doubleToRawLongBits(d);
        boolean negative = (bits >>> 63) != 0;
        
        if (negative) {
            result.append('-');
            d = -d;
            bits = Double.doubleToRawLongBits(d);
        }
        
        int exponent = (int) ((bits >>> 52) & 0x7FF);
        long mantissa = bits & 0x000FFFFFFFFFFFFFL;
        
        // Denormalized number handling
        if (exponent == 0) {
            if (mantissa == 0) {
                return negative ? "-0" : "0";
            }
            // Denormal: adjust exponent and normalize mantissa
            int leadingZeros = Long.numberOfLeadingZeros(mantissa) - 11;
            exponent -= leadingZeros - 1;
            mantissa <<= leadingZeros;
        } else {
            // Normal number: add implicit leading 1
            mantissa |= 0x0010000000000000L;
        }
        
        // Remove bias
        exponent -= 1022;
        
        // d = 2^(exponent - 53) * mantissa
        
        // For small integers that fit exactly, use fast path
        if (exponent >= 1 && exponent <= 53) {
            long shifted = mantissa >>> (53 - exponent);
            if ((mantissa & ((1L << (53 - exponent)) - 1)) == 0) {
                // Exact integer
                result.append(Long.toString(shifted, radix));
                return result.toString();
            }
        }
        
        // Use BigDecimal for arbitrary precision conversion
        return convertWithBigDecimal(result.toString(), d, radix);
    }
    
    /**
     * Convert using BigDecimal arbitrary precision arithmetic.
     * This ensures we maintain exactness matching JavaScript behavior.
     */
    private static String convertWithBigDecimal(String prefix, double d, int radix) {
        StringBuilder result = new StringBuilder(prefix);
        
        // Use BigDecimal to capture the EXACT IEEE 754 binary value
        // This is what JavaScript uses internally
        BigDecimal bd = new BigDecimal(d);
        
        // Split into integer and fractional parts
        BigDecimal intPart = bd.setScale(0, RoundingMode.DOWN);
        BigDecimal fracPart = bd.subtract(intPart);
        
        // Convert integer part
        long intValue = intPart.longValue();
        if (intValue == 0) {
            result.append('0');
        } else {
            result.append(Long.toString(intValue, radix));
        }
        
        // Convert fractional part if present
        if (fracPart.compareTo(BigDecimal.ZERO) > 0) {
            result.append('.');
            
            // Calculate maximum fractional digits
            // Total significant digits is limited by MAX_DIGITS_TABLE
            int maxDigits = MAX_DIGITS_TABLE[radix];
            String intStr = Long.toString(Math.max(Math.abs(intValue), 1), radix);
            // Fractional digits = total - integer digits
            // Add a small margin for rounding decisions
            int maxFracDigits = maxDigits - intStr.length() + 5;
            if (maxFracDigits < 1) maxFracDigits = 1;
            
            // Generate fractional digits
            MathContext mc = new MathContext(250, RoundingMode.HALF_EVEN);
            BigDecimal radixBD = BigDecimal.valueOf(radix);
            BigDecimal remaining = fracPart;
            StringBuilder fracDigits = new StringBuilder();
            
            // Generate digits until we hit maxFracDigits or remaining becomes zero
            for (int i = 0; i < maxFracDigits && remaining.compareTo(BigDecimal.ZERO) > 0; i++) {
                remaining = remaining.multiply(radixBD, mc);
                int digit = remaining.intValue();
                remaining = remaining.subtract(BigDecimal.valueOf(digit), mc);
                
                if (digit < 10) {
                    fracDigits.append((char) ('0' + digit));
                } else {
                    fracDigits.append((char) ('a' + digit - 10));
                }
            }
            
            // Now find the shortest representation that round-trips correctly
            // First, remove trailing zeros
            String fracStr = fracDigits.toString();
            while (fracStr.length() > 0 && fracStr.charAt(fracStr.length() - 1) == '0') {
                fracStr = fracStr.substring(0, fracStr.length() - 1);
            }
            
            // Then find shortest prefix that round-trips correctly
            String shortestFrac = findShortestRepresentation(fracStr, intValue, radix, d);
            result.append(shortestFrac);
        }
        
        return result.toString();
    }
    
    /**
     * Find the shortest fractional representation that round-trips to the same double value.
     * This implements the "shortest representation" algorithm similar to QuickJS.
     */
    private static String findShortestRepresentation(String fracDigits, long intValue, int radix, double originalValue) {
        if (fracDigits.isEmpty()) {
            return "";
        }
        
        // Start from the full representation and try shorter versions
        // Work backwards, removing one digit at a time
        String bestCandidate = fracDigits;
        
        for (int len = 1; len <= fracDigits.length(); len++) {
            String candidate = fracDigits.substring(0, len);
            
            // Convert this candidate back to double and see if it matches
            double reconstructed = reconstructDouble(intValue, candidate, radix);
            
            if (reconstructed == originalValue) {
                // This length works - try rounding
                bestCandidate = candidate;
                
                if (len < fracDigits.length()) {
                    // There are more digits - check if we should round up
                    int nextDigit = Character.digit(fracDigits.charAt(len), radix);
                    if (nextDigit >= (radix + 1) / 2) {
                        // Round up
                        String rounded = roundUpFractional(candidate, radix);
                        double roundedValue = reconstructDouble(intValue, rounded, radix);
                        if (roundedValue == originalValue) {
                            bestCandidate = rounded;
                        }
                    }
                }
                
                // Found a match - this is the shortest
                break;
            }
        }
        
        return bestCandidate;
    }
    
    /**
     * Reconstruct a double value from integer and fractional parts in the given radix.
     */
    private static double reconstructDouble(long intPart, String fracPart, int radix) {
        // Use BigDecimal for accurate reconstruction
        BigDecimal result = BigDecimal.valueOf(intPart);
        BigDecimal factor = BigDecimal.ONE;
        BigDecimal radixBD = BigDecimal.valueOf(radix);
        
        for (int i = 0; i < fracPart.length(); i++) {
            factor = factor.divide(radixBD, MathContext.DECIMAL128);
            int digit = Character.digit(fracPart.charAt(i), radix);
            result = result.add(BigDecimal.valueOf(digit).multiply(factor));
        }
        
        return result.doubleValue();
    }
    
    /**
     * Round up the last digit of a fractional string.
     */
    private static String roundUpFractional(String fracDigits, int radix) {
        StringBuilder result = new StringBuilder(fracDigits);
        boolean carry = true;
        
        for (int i = result.length() - 1; i >= 0 && carry; i--) {
            int digit = Character.digit(result.charAt(i), radix);
            digit++;
            
            if (digit < radix) {
                result.setCharAt(i, Character.forDigit(digit, radix));
                carry = false;
            } else {
                result.setCharAt(i, '0');
            }
        }
        
        if (carry) {
            // Would need to increment integer part - return original
            return fracDigits;
        }
        
        return result.toString();
    }
}
