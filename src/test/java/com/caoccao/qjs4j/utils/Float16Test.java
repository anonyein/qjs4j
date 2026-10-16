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

package com.caoccao.qjs4j.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The half-precision helpers agree with float arithmetic on every value they can represent.
 */
public class Float16Test {
    private static final float[] SAMPLES = {0f, -0f, 0.25f, -0.25f, 0.5f, -0.5f, 0.75f, 1f, -1f, 1.5f, -1.5f, 2.5f,
            -2.5f, 3.75f, 1024.5f, -1024.5f, 2049f, 65504f, -65504f, 5.96e-8f, 6.1e-5f, Float.POSITIVE_INFINITY,
            Float.NEGATIVE_INFINITY, Float.NaN};

    @Test
    public void testClassification() {
        assertThat(Float16.isNaN(Float16.NaN)).isTrue();
        assertThat(Float16.isNaN(Float16.POSITIVE_INFINITY)).isFalse();
        assertThat(Float16.isInfinite(Float16.NEGATIVE_INFINITY)).isTrue();
        assertThat(Float16.isInfinite(Float16.MAX_VALUE)).isFalse();
        assertThat(Float16.isNormalized(Float16.MIN_NORMAL)).isTrue();
        assertThat(Float16.isNormalized(Float16.MIN_VALUE)).isFalse();
        assertThat(Float16.isNormalized(Float16.POSITIVE_INFINITY)).isFalse();
    }

    @Test
    public void testComparisons() {
        for (float left : SAMPLES) {
            for (float right : SAMPLES) {
                short x = Float16.toHalf(left);
                short y = Float16.toHalf(right);
                float a = Float16.toFloat(x);
                float b = Float16.toFloat(y);
                assertThat(Float16.less(x, y)).as(a + " < " + b).isEqualTo(a < b);
                assertThat(Float16.lessEquals(x, y)).as(a + " <= " + b).isEqualTo(a <= b);
                assertThat(Float16.greater(x, y)).as(a + " > " + b).isEqualTo(a > b);
                assertThat(Float16.greaterEquals(x, y)).as(a + " >= " + b).isEqualTo(a >= b);
                assertThat(Float16.equals(x, y)).as(a + " == " + b).isEqualTo(a == b);
                assertThat(Integer.signum(Float16.compare(x, y))).as("compare " + a + ", " + b)
                        .isEqualTo(Integer.signum(Float.compare(a, b)));
                assertSameFloat(Float16.toFloat(Float16.min(x, y)), Math.min(a, b), "min " + a + ", " + b);
                assertSameFloat(Float16.toFloat(Float16.max(x, y)), Math.max(a, b), "max " + a + ", " + b);
            }
        }
    }

    @Test
    public void testConversions() {
        for (float sample : SAMPLES) {
            short half = Float16.toHalf(sample);
            assertThat(Float16.toHalf((double) sample)).as("double " + sample).isEqualTo(half);
            assertSameFloat(Float16.toFloat(Float16.toHalf(Float16.toFloat(half))), Float16.toFloat(half),
                    "round trip " + sample);
        }
        assertThat(Float16.toFloat(Float16.toHalf(1e10f))).isEqualTo(Float.POSITIVE_INFINITY);
        assertThat(Float16.toFloat(Float16.toHalf(1e-10f))).isEqualTo(0f);
        assertThat(Float16.toFloat(Float16.toHalf(1.0009765625f))).isEqualTo(1.0009765625f);
    }

    @Test
    public void testRounding() {
        for (float sample : SAMPLES) {
            short half = Float16.toHalf(sample);
            float value = Float16.toFloat(half);
            assertSameFloat(Float16.toFloat(Float16.ceil(half)), (float) Math.ceil(value), "ceil " + value);
            assertSameFloat(Float16.toFloat(Float16.floor(half)), (float) Math.floor(value), "floor " + value);
            assertSameFloat(Float16.toFloat(Float16.rint(half)), (float) Math.rint(value), "rint " + value);
            float truncated = value < 0 ? (float) Math.ceil(value) : (float) Math.floor(value);
            assertSameFloat(Float16.toFloat(Float16.trunc(half)), truncated, "trunc " + value);
        }
    }

    /**
     * Same value, as Float.compare sees it: NaN equals NaN and -0 differs from 0.
     */
    private static void assertSameFloat(float actual, float expected, String label) {
        assertThat(Float.compare(actual, expected)).as(label + ": " + actual + " vs " + expected).isZero();
    }
}
