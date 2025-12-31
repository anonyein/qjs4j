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

package com.caoccao.qjs4j.builtins;

import com.caoccao.qjs4j.BaseTest;
import com.caoccao.qjs4j.core.JSDate;
import com.caoccao.qjs4j.core.JSNumber;
import com.caoccao.qjs4j.core.JSString;
import com.caoccao.qjs4j.core.JSValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for DatePrototype methods.
 */
public class DatePrototypeTest extends BaseTest {

    @Test
    public void testGetDate() {
        // Test with 2024-01-01 00:00:00 UTC
        JSDate date = new JSDate(1704067200000L);

        JSValue result = DatePrototype.getDate(context, date, new JSValue[]{});
        assertThat(result).isInstanceOfSatisfying(JSNumber.class, jsNum -> assertThat(jsNum.value()).isEqualTo(1.0));

        // Edge case: called on non-Date
        result = DatePrototype.getDate(context, new JSString("not a date"), new JSValue[]{});
        assertTypeError(result);
        assertPendingException(context);
    }

    @Test
    public void testGetDay() {
        // Test with 2024-01-01 00:00:00 UTC
        JSDate date = new JSDate(1704067200000L);

        JSValue result = DatePrototype.getDay(context, date, new JSValue[]{});
        assertThat(result).isInstanceOfSatisfying(JSNumber.class, jsNum -> {
            double day = jsNum.value();
            assertThat(day).isBetween(0.0, 6.0);
        });

        // Edge case: called on non-Date
        result = DatePrototype.getDay(context, new JSString("not a date"), new JSValue[]{});
        assertTypeError(result);
        assertPendingException(context);
    }

    @Test
    public void testGetFullYear() {
        // Test with 2024-01-01 00:00:00 UTC
        JSDate date = new JSDate(1704067200000L);

        JSValue result = DatePrototype.getFullYear(context, date, new JSValue[]{});
        assertThat(result).isInstanceOfSatisfying(JSNumber.class, jsNum -> assertThat(jsNum.value()).isEqualTo(2024.0));

        // Edge case: called on non-Date
        result = DatePrototype.getFullYear(context, new JSString("not a date"), new JSValue[]{});
        assertTypeError(result);
        assertPendingException(context);
    }

    @Test
    public void testGetHours() {
        // Test with 2024-01-01 00:00:00 UTC
        JSDate date = new JSDate(1704067200000L);

        JSValue result = DatePrototype.getHours(context, date, new JSValue[]{});
        assertThat(result).isInstanceOfSatisfying(JSNumber.class, jsNum -> {
            double hours = jsNum.value();
            assertThat(hours).isBetween(0.0, 23.0);
        });

        // Edge case: called on non-Date
        result = DatePrototype.getHours(context, new JSString("not a date"), new JSValue[]{});
        assertTypeError(result);
        assertPendingException(context);
    }

    @Test
    public void testGetMilliseconds() {
        // Test with 2024-01-01 00:00:00 UTC
        JSDate date = new JSDate(1704067200000L);

        JSValue result = DatePrototype.getMilliseconds(context, date, new JSValue[]{});
        assertThat(result).isInstanceOfSatisfying(JSNumber.class, jsNum -> assertThat(jsNum.value()).isEqualTo(0.0));

        // Edge case: called on non-Date
        result = DatePrototype.getMilliseconds(context, new JSString("not a date"), new JSValue[]{});
        assertTypeError(result);
        assertPendingException(context);
    }

    @Test
    public void testGetMinutes() {
        // Test with 2024-01-01 00:00:00 UTC
        JSDate date = new JSDate(1704067200000L);

        JSValue result = DatePrototype.getMinutes(context, date, new JSValue[]{});
        assertThat(result).isInstanceOfSatisfying(JSNumber.class, jsNum -> assertThat(jsNum.value()).isEqualTo(0.0));

        // Edge case: called on non-Date
        result = DatePrototype.getMinutes(context, new JSString("not a date"), new JSValue[]{});
        assertTypeError(result);
        assertPendingException(context);
    }

    @Test
    public void testGetMonth() {
        // Test with 2024-01-01 00:00:00 UTC (January = month 0)
        JSDate date = new JSDate(1704067200000L);

        JSValue result = DatePrototype.getMonth(context, date, new JSValue[]{});
        assertThat(result).isInstanceOfSatisfying(JSNumber.class, jsNum -> assertThat(jsNum.value()).isEqualTo(0.0)); // January is 0

        // Edge case: called on non-Date
        result = DatePrototype.getMonth(context, new JSString("not a date"), new JSValue[]{});
        assertTypeError(result);
        assertPendingException(context);
    }

    @Test
    public void testGetSeconds() {
        // Test with 2024-01-01 00:00:00 UTC
        JSDate date = new JSDate(1704067200000L);

        JSValue result = DatePrototype.getSeconds(context, date, new JSValue[]{});
        assertThat(result).isInstanceOfSatisfying(JSNumber.class, jsNum -> assertThat(jsNum.value()).isEqualTo(0.0));

        // Edge case: called on non-Date
        result = DatePrototype.getSeconds(context, new JSString("not a date"), new JSValue[]{});
        assertTypeError(result);
        assertPendingException(context);
    }

    @Test
    public void testGetTime() {
        // Test with a known timestamp: 2024-01-01 00:00:00 UTC (1704067200000L)
        JSDate date = new JSDate(1704067200000L);

        JSValue result = DatePrototype.getTime(context, date, new JSValue[]{});
        assertThat(result).isInstanceOfSatisfying(JSNumber.class, jsNum -> assertThat(jsNum.value()).isEqualTo(1704067200000.0));

        // Edge case: called on non-Date
        result = DatePrototype.getTime(context, new JSString("not a date"), new JSValue[]{});
        assertTypeError(result);
        assertPendingException(context);
    }

    @Test
    public void testGetUTCDate() {
        // Test with 2024-01-01 00:00:00 UTC
        JSDate date = new JSDate(1704067200000L);

        JSValue result = DatePrototype.getUTCDate(context, date, new JSValue[]{});
        assertThat(result).isInstanceOfSatisfying(JSNumber.class, jsNum -> assertThat(jsNum.value()).isEqualTo(1.0));

        // Edge case: called on non-Date
        result = DatePrototype.getUTCDate(context, new JSString("not a date"), new JSValue[]{});
        assertTypeError(result);
        assertPendingException(context);
    }

    @Test
    public void testGetUTCFullYear() {
        // Test with 2024-01-01 00:00:00 UTC
        JSDate date = new JSDate(1704067200000L);

        JSValue result = DatePrototype.getUTCFullYear(context, date, new JSValue[]{});
        assertThat(result).isInstanceOfSatisfying(JSNumber.class, jsNum -> assertThat(jsNum.value()).isEqualTo(2024.0));

        // Edge case: called on non-Date
        result = DatePrototype.getUTCFullYear(context, new JSString("not a date"), new JSValue[]{});
        assertTypeError(result);
        assertPendingException(context);
    }

    @Test
    public void testGetUTCHours() {
        // Test with 2024-01-01 00:00:00 UTC
        JSDate date = new JSDate(1704067200000L);

        JSValue result = DatePrototype.getUTCHours(context, date, new JSValue[]{});
        assertThat(result).isInstanceOfSatisfying(JSNumber.class, jsNum -> assertThat(jsNum.value()).isEqualTo(0.0));

        // Edge case: called on non-Date
        result = DatePrototype.getUTCHours(context, new JSString("not a date"), new JSValue[]{});
        assertTypeError(result);
        assertPendingException(context);
    }

    @Test
    public void testGetUTCMonth() {
        // Test with 2024-01-01 00:00:00 UTC
        JSDate date = new JSDate(1704067200000L);

        JSValue result = DatePrototype.getUTCMonth(context, date, new JSValue[]{});
        assertThat(result).isInstanceOfSatisfying(JSNumber.class, jsNum -> assertThat(jsNum.value()).isEqualTo(0.0)); // January is 0

        // Edge case: called on non-Date
        result = DatePrototype.getUTCMonth(context, new JSString("not a date"), new JSValue[]{});
        assertTypeError(result);
        assertPendingException(context);
    }

    @Test
    public void testToISOString() {
        // Test with 2024-01-01 00:00:00 UTC
        JSDate date = new JSDate(1704067200000L);

        JSValue result = DatePrototype.toISOString(context, date, new JSValue[]{});
        assertThat(result).isInstanceOfSatisfying(JSString.class, jsStr -> {
            String isoString = jsStr.value();
            assertThat(isoString).startsWith("2024-01-01T00:00:00");
        });

        // Edge case: called on non-Date
        result = DatePrototype.toISOString(context, new JSString("not a date"), new JSValue[]{});
        assertTypeError(result);
        assertPendingException(context);
    }

    @Test
    public void testToJSON() {
        // Test with 2024-01-01 00:00:00 UTC
        JSDate date = new JSDate(1704067200000L);

        JSValue result = DatePrototype.toJSON(context, date, new JSValue[]{});
        assertThat(result).isInstanceOfSatisfying(JSString.class, jsStr -> {
            String jsonString = jsStr.value();
            assertThat(jsonString).startsWith("2024-01-01T00:00:00");
        });

        // Edge case: called on non-Date
        result = DatePrototype.toJSON(context, new JSString("not a date"), new JSValue[]{});
        assertTypeError(result);
        assertPendingException(context);
    }

    @Test
    public void testToStringMethod() {
        // Test with 2024-01-01 00:00:00 UTC
        JSDate date = new JSDate(1704067200000L);

        JSValue result = DatePrototype.toStringMethod(context, date, new JSValue[]{});
        // Should return a string representation
        assertThat(result).isInstanceOfSatisfying(JSString.class, jsStr -> {
            String str = jsStr.value();
            assertThat(str).isNotNull();
            assertThat(str.length()).isGreaterThan(0);
        });

        // Edge case: called on non-Date
        result = DatePrototype.toStringMethod(context, new JSString("not a date"), new JSValue[]{});
        assertTypeError(result);
        assertPendingException(context);
    }

    @Test
    public void testValueOf() {
        // Test with 2024-01-01 00:00:00 UTC
        JSDate date = new JSDate(1704067200000L);

        JSValue result = DatePrototype.valueOf(context, date, new JSValue[]{});
        assertThat(result).isInstanceOfSatisfying(JSNumber.class, jsNum -> assertThat(jsNum.value()).isEqualTo(1704067200000.0));

        // Edge case: called on non-Date
        result = DatePrototype.valueOf(context, new JSString("not a date"), new JSValue[]{});
        assertTypeError(result);
        assertPendingException(context);
    }
}