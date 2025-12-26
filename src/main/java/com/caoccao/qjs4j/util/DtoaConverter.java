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

package com.caoccao.qjs4j.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

/**
 * Double-to-ASCII converter.
 * Implements proper JavaScript number-to-string conversion.
 * Based on QuickJS dtoa.c implementation.
 * <p>
 * Supports JavaScript's Number.prototype methods:
 * - toString() - free format
 * - toFixed(fractionDigits) - fixed decimal notation
 * - toExponential(fractionDigits) - exponential notation
 * - toPrecision(precision) - significant digits
 */
public final class DtoaConverter {

    public static final double FIXED_THRESHOLD = 1e21;
    public static final int MAX_DIGITS = 100;
    public static final int MAX_PRECISION = 100;

    /**
     * Clean up number string by removing trailing zeros and unnecessary decimal points.
     */
    private static String cleanupNumberString(String str) {
        if (!str.contains(".")) {
            return str;
        }

        // Remove trailing zeros after decimal point
        int i = str.length() - 1;
        while (i >= 0 && str.charAt(i) == '0') {
            i--;
        }

        // Remove decimal point if no fractional part remains
        if (i >= 0 && str.charAt(i) == '.') {
            i--;
        }

        return str.substring(0, i + 1);
    }

    /**
     * Convert a double to string using free format (automatic best representation).
     * This is the default JavaScript toString() behavior.
     */
    public static String convert(double value) {
        return convert(value, false);
    }

    /**
     * Convert a double to string with optional minus zero handling.
     *
     * @param value         The value to convert
     * @param showMinusZero If true, show "-0" for negative zero
     */
    public static String convert(double value, boolean showMinusZero) {
        // Handle special values
        if (Double.isNaN(value)) {
            return "NaN";
        }
        if (Double.isInfinite(value)) {
            return value > 0 ? "Infinity" : "-Infinity";
        }
        if (value == 0.0) {
            // Check for negative zero
            if (showMinusZero && Double.doubleToRawLongBits(value) == Double.doubleToRawLongBits(-0.0)) {
                return "-0";
            }
            return "0";
        }

        // JavaScript uses exponential notation when exponent < -6 or >= 21
        // Java's Double.toString uses different rules, so we need custom logic

        // Calculate the exponent
        double absValue = Math.abs(value);
        int exponent = 0;
        if (absValue != 0) {
            exponent = (int) Math.floor(Math.log10(absValue));
        }

        // Use exponential notation for very small or very large numbers
        if (absValue != 0 && (exponent < -6 || exponent >= 21)) {
            // Use exponential notation
            String result = Double.toString(value);
            // Convert uppercase 'E' to lowercase 'e' for JavaScript standard
            result = result.replace('E', 'e');
            return result;
        }

        // Use decimal notation
        // For numbers in the safe range, use BigDecimal for accurate representation
        String result;
        if (value == Math.floor(value) && absValue < 1e15) {
            // Integer value - no decimal point
            result = String.format("%.0f", value);
        } else {
            // Use BigDecimal to avoid precision issues
            result = Double.toString(value);
            // If Java's toString used exponential notation, we need to convert it
            if (result.contains("E") || result.contains("e")) {
                // Convert from exponential to decimal
                result = String.format("%.15f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
            }
            // Remove trailing zeros after decimal point
            if (result.contains(".")) {
                result = result.replaceAll("0+$", "").replaceAll("\\.$", "");
            }
        }

        return result;
    }

    /**
     * Convert to exponential notation.
     * Implements Number.prototype.toExponential(fractionDigits).
     *
     * @param value          The value to convert
     * @param fractionDigits Number of digits after decimal point (0-100)
     * @return The formatted string in exponential notation
     * @throws IllegalArgumentException if fractionDigits is out of range
     */
    public static String convertExponential(double value, int fractionDigits) {
        if (fractionDigits < 0 || fractionDigits > MAX_DIGITS) {
            throw new IllegalArgumentException("fractionDigits must be between 0 and " + MAX_DIGITS);
        }

        // Handle special values
        if (Double.isNaN(value)) {
            return "NaN";
        }
        if (Double.isInfinite(value)) {
            return value > 0 ? "Infinity" : "-Infinity";
        }

        return formatExponential(value, fractionDigits);
    }

    /**
     * Convert to fixed-point notation.
     * Implements Number.prototype.toFixed(fractionDigits).
     *
     * @param value          The value to convert
     * @param fractionDigits Number of digits after decimal point (0-100)
     * @return The formatted string in fixed notation
     * @throws IllegalArgumentException if fractionDigits is out of range
     */
    public static String convertFixed(double value, int fractionDigits) {
        if (fractionDigits < 0 || fractionDigits > MAX_DIGITS) {
            throw new IllegalArgumentException("fractionDigits must be between 0 and " + MAX_DIGITS);
        }

        // Handle special values
        if (Double.isNaN(value)) {
            return "NaN";
        }
        if (Double.isInfinite(value)) {
            return value > 0 ? "Infinity" : "-Infinity";
        }

        // JavaScript uses exponential notation for very large numbers in toFixed
        if (Math.abs(value) >= FIXED_THRESHOLD) {
            return convert(value);
        }

        // Use BigDecimal for precise fixed-point formatting
        try {
            BigDecimal bd = BigDecimal.valueOf(value);
            bd = bd.setScale(fractionDigits, RoundingMode.HALF_UP);
            return bd.toPlainString();
        } catch (NumberFormatException e) {
            // Fallback to String.format
            return String.format(Locale.US, "%." + fractionDigits + "f", value);
        }
    }

    /**
     * Convert integer to string (optimized path).
     */
    public static String convertInt(int value) {
        return Integer.toString(value);
    }

    /**
     * Convert integer to string with specified radix (2-36).
     */
    public static String convertIntRadix(int value, int radix) {
        if (radix < 2 || radix > 36) {
            throw new IllegalArgumentException("radix must be between 2 and 36");
        }
        return Integer.toString(value, radix);
    }

    /**
     * Convert long to string (optimized path).
     */
    public static String convertLong(long value) {
        return Long.toString(value);
    }

    /**
     * Convert long to string with specified radix (2-36).
     */
    public static String convertLongRadix(long value, int radix) {
        if (radix < 2 || radix > 36) {
            throw new IllegalArgumentException("radix must be between 2 and 36");
        }
        return Long.toString(value, radix);
    }

    /**
     * Convert with specified precision (significant digits).
     * Implements Number.prototype.toPrecision(precision).
     *
     * @param value     The value to convert
     * @param precision Number of significant digits (1-100)
     * @return The formatted string
     * @throws IllegalArgumentException if precision is out of range
     */
    public static String convertWithPrecision(double value, int precision) {
        if (precision < 1 || precision > MAX_DIGITS) {
            throw new IllegalArgumentException("precision must be between 1 and " + MAX_DIGITS);
        }

        // Handle special values
        if (Double.isNaN(value)) {
            return "NaN";
        }
        if (Double.isInfinite(value)) {
            return value > 0 ? "Infinity" : "-Infinity";
        }

        // Use BigDecimal for accurate formatting
        if (value == 0.0) {
            // Special case for zero
            StringBuilder result = new StringBuilder("0");
            if (precision > 1) {
                result.append(".");
                for (int i = 1; i < precision; i++) {
                    result.append("0");
                }
            }
            return result.toString();
        }

        // Determine if we should use exponential or fixed notation
        int exponent = (int) Math.floor(Math.log10(Math.abs(value)));

        if (exponent < -6 || exponent >= precision) {
            // Use exponential notation
            String format = "%." + (precision - 1) + "e";
            return String.format(format, value).replaceAll("([eE][+-])0+", "$1");
        } else {
            // Use fixed notation
            BigDecimal bd = BigDecimal.valueOf(value);
            bd = bd.round(new java.math.MathContext((int) precision, RoundingMode.HALF_UP));
            return bd.stripTrailingZeros().toPlainString();
        }
    }

    /**
     * Format a value in exponential notation.
     */
    private static String formatExponential(double value, int fractionDigits) {
        if (value == 0.0) {
            // Special case for zero
            StringBuilder sb = new StringBuilder("0");
            if (fractionDigits > 0) {
                sb.append('.');
                for (int i = 0; i < fractionDigits; i++) {
                    sb.append('0');
                }
            }
            sb.append("e+0");
            return sb.toString();
        }

        // Use scientific notation
        String format = String.format(Locale.US, "%." + fractionDigits + "e", value);

        // Convert Java's format to JavaScript's format
        // Java: 1.234e+00, JavaScript: 1.234e+0
        return normalizeExponentialFormat(format);
    }

    /**
     * Get the base-10 exponent of a value.
     */
    private static int getExponent(double value) {
        if (value == 0.0) {
            return 0;
        }
        return (int) Math.floor(Math.log10(Math.abs(value)));
    }

    /**
     * Normalize exponential format from Java to JavaScript style.
     * Java uses "e+00", "e-00", JavaScript uses "e+0", "e-0"
     */
    private static String normalizeExponentialFormat(String str) {
        // Find 'e' or 'E'
        int eIndex = str.indexOf('e');
        if (eIndex == -1) {
            eIndex = str.indexOf('E');
        }
        if (eIndex == -1) {
            return str;
        }

        String mantissa = str.substring(0, eIndex);
        String exponentPart = str.substring(eIndex + 1);

        // Parse and reformat exponent
        char sign = '+';
        int startIndex = 0;
        if (exponentPart.charAt(0) == '+' || exponentPart.charAt(0) == '-') {
            sign = exponentPart.charAt(0);
            startIndex = 1;
        }

        int exponent = Integer.parseInt(exponentPart.substring(startIndex));
        return mantissa + "e" + sign + exponent;
    }
}
