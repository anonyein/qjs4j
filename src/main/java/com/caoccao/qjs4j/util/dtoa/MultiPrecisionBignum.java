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

/**
 * Multi-precision bignum using 32-bit limbs.
 * Based on QuickJS dtoa.c implementation.
 * 
 * The represented number is sum(i, limbs[i] * 2^(32 * i))
 */
public class MultiPrecisionBignum {
    private static final int LIMB_BITS = 32;
    private static final long LIMB_MASK = 0xFFFFFFFFL;
    
    private int[] limbs;
    private int length;
    
    public MultiPrecisionBignum(int capacity) {
        this.limbs = new int[capacity];
        this.length = 0;
    }
    
    public MultiPrecisionBignum(long value) {
        this(2);
        setU64(value);
    }
    
    public void setU64(long value) {
        if (value == 0) {
            length = 1;
            limbs[0] = 0;
        } else {
            limbs[0] = (int) (value & LIMB_MASK);
            limbs[1] = (int) ((value >>> 32) & LIMB_MASK);
            length = (limbs[1] != 0) ? 2 : 1;
        }
    }
    
    public long getU64() {
        if (length == 0) return 0;
        if (length == 1) return limbs[0] & LIMB_MASK;
        return (limbs[0] & LIMB_MASK) | ((limbs[1] & LIMB_MASK) << 32);
    }
    
    public int getLength() {
        return length;
    }
    
    public int getLimb(int index) {
        return index < length ? limbs[index] : 0;
    }
    
    public void setLimb(int index, int value) {
        if (index < limbs.length) {
            limbs[index] = value;
            if (index >= length) {
                length = index + 1;
            }
        }
    }
    
    /**
     * Renormalize: adjust length to exclude leading zero limbs
     */
    public void renormalize() {
        while (length > 1 && limbs[length - 1] == 0) {
            length--;
        }
        if (length == 0) {
            length = 1;
            limbs[0] = 0;
        }
    }
    
    /**
     * Add unsigned integer to limbs
     */
    public int addUI(long b) {
        long carry = b;
        for (int i = 0; i < length && carry != 0; i++) {
            long sum = (limbs[i] & LIMB_MASK) + carry;
            limbs[i] = (int) (sum & LIMB_MASK);
            carry = sum >>> 32;
        }
        if (carry != 0 && length < limbs.length) {
            limbs[length++] = (int) (carry & LIMB_MASK);
        }
        return (int) carry;
    }
    
    /**
     * Multiply limbs by single limb value and add carry
     * result[i] = a[i] * b + carry
     */
    public static void mul1(int[] result, int[] a, int aLen, int b, long carry) {
        long bLong = b & LIMB_MASK;
        for (int i = 0; i < aLen; i++) {
            long product = (a[i] & LIMB_MASK) * bLong + carry;
            result[i] = (int) (product & LIMB_MASK);
            carry = product >>> 32;
        }
        if (carry != 0 && aLen < result.length) {
            result[aLen] = (int) (carry & LIMB_MASK);
        }
    }
    
    /**
     * Divide limbs by single limb value
     * Returns remainder
     */
    public int div1(int divisor) {
        long divisorLong = divisor & LIMB_MASK;
        long remainder = 0;
        
        for (int i = length - 1; i >= 0; i--) {
            long dividend = (remainder << 32) | (limbs[i] & LIMB_MASK);
            limbs[i] = (int) ((dividend / divisorLong) & LIMB_MASK);
            remainder = dividend % divisorLong;
        }
        
        renormalize();
        return (int) (remainder & LIMB_MASK);
    }
    
    /**
     * Shift right by n bits with rounding
     */
    public void shrRound(int shift, RoundingMode mode) {
        if (shift == 0) return;
        
        int limbShift = shift / LIMB_BITS;
        int bitShift = shift % LIMB_BITS;
        
        if (limbShift >= length) {
            length = 1;
            limbs[0] = 0;
            return;
        }
        
        // Shift limbs
        if (bitShift == 0) {
            for (int i = 0; i < length - limbShift; i++) {
                limbs[i] = limbs[i + limbShift];
            }
            length -= limbShift;
        } else {
            int invBitShift = LIMB_BITS - bitShift;
            for (int i = 0; i < length - limbShift - 1; i++) {
                int low = limbs[i + limbShift] >>> bitShift;
                int high = limbs[i + limbShift + 1] << invBitShift;
                limbs[i] = low | high;
            }
            limbs[length - limbShift - 1] = limbs[length - 1] >>> bitShift;
            length -= limbShift;
        }
        
        renormalize();
    }
    
    /**
     * Multiply by power: a *= (radix1 * 2^radix_shift)^f
     */
    public int mulPow(int radix1, int radixShift, int f) {
        if (f == 0) return 0;
        
        int eOffset = -f * radixShift;
        
        if (radix1 != 1) {
            // Multiply by radix1^f
            int absF = Math.abs(f);
            for (int i = 0; i < absF; i++) {
                int[] temp = new int[length + 1];
                mul1(temp, limbs, length, radix1, 0);
                System.arraycopy(temp, 0, limbs, 0, Math.min(temp.length, limbs.length));
                length = Math.min(temp.length, limbs.length);
                renormalize();
            }
        }
        
        return eOffset;
    }
    
    /**
     * Compare with another bignum
     */
    public int compareTo(MultiPrecisionBignum other) {
        if (length != other.length) {
            return Integer.compare(length, other.length);
        }
        for (int i = length - 1; i >= 0; i--) {
            int a = limbs[i] & 0x7FFFFFFF;
            int b = other.limbs[i] & 0x7FFFFFFF;
            if (a != b) {
                return Integer.compare(a, b);
            }
        }
        return 0;
    }
    
    public enum RoundingMode {
        ROUND_DOWN,      // Round toward zero
        ROUND_NEAREST,   // Round to nearest, ties to even
        ROUND_NEAREST_AWAY  // Round to nearest, ties away from zero
    }
}
