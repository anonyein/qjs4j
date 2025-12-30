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
import com.caoccao.qjs4j.core.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for Date constructor and Date.prototype methods.
 */
public class DateConstructorTest extends BaseTest {

    @Test
    public void testGetDate() {
        JSDate date = new JSDate(1750000000000L);

        JSValue result = DatePrototype.getDate(context, date, new JSValue[]{});
        assertThat(result).isInstanceOfSatisfying(JSNumber.class, jsNum -> {
            double day = jsNum.value();
            assertThat(day).isBetween(1.0, 31.0);
        });

        // Edge case: called on non-Date
        assertTypeError(DatePrototype.getDate(context, new JSNumber(123), new JSValue[]{}));
        assertPendingException(context);
    }

    @Test
    public void testGetDay() {
        JSDate date = new JSDate(1750000000000L);

        JSValue result = DatePrototype.getDay(context, date, new JSValue[]{});
        assertThat(result).isInstanceOfSatisfying(JSNumber.class, jsNum -> {
            double day = jsNum.value();
            assertThat(day).isBetween(0.0, 6.0);
        });

        // Edge case: called on non-Date
        assertTypeError(DatePrototype.getDay(context, JSNull.INSTANCE, new JSValue[]{}));
        assertPendingException(context);
    }

    @Test
    public void testGetFullYear() {
        // Normal case: 2025-06-15
        JSDate date = new JSDate(1750000000000L); // Approximately 2025-06-15

        JSValue result = DatePrototype.getFullYear(context, date, new JSValue[]{});
        // We don't assert exact year as it depends on timezone
        assertThat(result).isInstanceOf(JSNumber.class);

        // Edge case: called on non-Date
        assertTypeError(DatePrototype.getFullYear(context, new JSObject(), new JSValue[]{}));
        assertPendingException(context);
    }

    @Test
    public void testGetHours() {
        JSDate date = new JSDate(1750000000000L);

        JSValue result = DatePrototype.getHours(context, date, new JSValue[]{});
        assertThat(result).isInstanceOfSatisfying(JSNumber.class, jsNum -> {
            double hours = jsNum.value();
            assertThat(hours).isBetween(0.0, 23.0);
        });

        // Edge case: called on non-Date
        assertTypeError(DatePrototype.getHours(context, new JSArray(), new JSValue[]{}));
        assertPendingException(context);
    }

    @Test
    public void testGetMilliseconds() {
        JSDate date = new JSDate(1750000000123L); // 123 milliseconds

        JSValue result = DatePrototype.getMilliseconds(context, date, new JSValue[]{});
        assertThat(result).isInstanceOfSatisfying(JSNumber.class, jsNum -> {
            double ms = jsNum.value();
            assertThat(ms).isBetween(0.0, 999.0);
        });

        // Edge case: called on non-Date
        assertTypeError(DatePrototype.getMilliseconds(context, new JSObject(), new JSValue[]{}));
        assertPendingException(context);
    }

    @Test
    public void testGetMinutes() {
        JSDate date = new JSDate(1750000000000L);

        JSValue result = DatePrototype.getMinutes(context, date, new JSValue[]{});
        assertThat(result).isInstanceOfSatisfying(JSNumber.class, jsNum -> {
            double minutes = jsNum.value();
            assertThat(minutes).isBetween(0.0, 59.0);
        });

        // Edge case: called on non-Date
        assertTypeError(DatePrototype.getMinutes(context, new JSString("not date"), new JSValue[]{}));
        assertPendingException(context);
    }

    @Test
    public void testGetMonth() {
        JSDate date = new JSDate(1750000000000L);

        JSValue result = DatePrototype.getMonth(context, date, new JSValue[]{});
        assertThat(result).isInstanceOfSatisfying(JSNumber.class, jsNum -> {
            double month = jsNum.value();
            assertThat(month).isBetween(0.0, 11.0);
        });

        // Edge case: called on non-Date
        assertTypeError(DatePrototype.getMonth(context, JSUndefined.INSTANCE, new JSValue[]{}));
        assertPendingException(context);
    }

    @Test
    public void testGetSeconds() {
        JSDate date = new JSDate(1750000000000L);

        JSValue result = DatePrototype.getSeconds(context, date, new JSValue[]{});
        assertThat(result).isInstanceOfSatisfying(JSNumber.class, jsNum -> {
            double seconds = jsNum.value();
            assertThat(seconds).isBetween(0.0, 59.0);
        });

        // Edge case: called on non-Date
        assertTypeError(DatePrototype.getSeconds(context, JSBoolean.TRUE, new JSValue[]{}));
        assertPendingException(context);
    }

    @Test
    public void testGetTime() {
        // Normal case
        long timestamp = System.currentTimeMillis();
        JSDate date = new JSDate(timestamp);

        JSValue result = DatePrototype.getTime(context, date, new JSValue[]{});
        assertThat(result).isInstanceOfSatisfying(JSNumber.class, jsNum -> assertThat(jsNum.value()).isEqualTo(timestamp));

        // Edge case: called on non-Date
        assertTypeError(DatePrototype.getTime(context, new JSString("not date"), new JSValue[]{}));
        assertPendingException(context);
    }

    @Test
    public void testGetUTCDate() {
        // Known timestamp: 2025-01-01T00:00:00.000Z
        JSDate date = new JSDate(1735689600000L);

        JSValue result = DatePrototype.getUTCDate(context, date, new JSValue[]{});
        assertThat(result).isInstanceOfSatisfying(JSNumber.class, jsNum -> assertThat(jsNum.value()).isEqualTo(1.0));

        // Edge case: called on non-Date
        assertTypeError(DatePrototype.getUTCDate(context, new JSNumber(42), new JSValue[]{}));
        assertPendingException(context);
    }

    @Test
    public void testGetUTCFullYear() {
        // Known timestamp: 2025-01-01T00:00:00.000Z = 1735689600000
        JSDate date = new JSDate(1735689600000L);

        JSValue result = DatePrototype.getUTCFullYear(context, date, new JSValue[]{});
        assertThat(result).isInstanceOfSatisfying(JSNumber.class, jsNum -> assertThat(jsNum.value()).isEqualTo(2025.0));

        // Edge case: called on non-Date
        assertTypeError(DatePrototype.getUTCFullYear(context, new JSString("not date"), new JSValue[]{}));
        assertPendingException(context);
    }

    @Test
    public void testGetUTCHours() {
        // Known timestamp: 2025-01-01T00:00:00.000Z
        JSDate date = new JSDate(1735689600000L);

        JSValue result = DatePrototype.getUTCHours(context, date, new JSValue[]{});
        assertThat(result).isInstanceOfSatisfying(JSNumber.class, jsNum -> assertThat(jsNum.value()).isEqualTo(0.0));

        // Edge case: called on non-Date
        assertTypeError(DatePrototype.getUTCHours(context, JSNull.INSTANCE, new JSValue[]{}));
        assertPendingException(context);
    }

    @Test
    public void testGetUTCMonth() {
        // Known timestamp: 2025-01-01T00:00:00.000Z
        JSDate date = new JSDate(1735689600000L);

        JSValue result = DatePrototype.getUTCMonth(context, date, new JSValue[]{});
        assertThat(result).isInstanceOfSatisfying(JSNumber.class, jsNum -> assertThat(jsNum.value()).isEqualTo(0.0)); // January = 0

        // Edge case: called on non-Date
        assertTypeError(DatePrototype.getUTCMonth(context, JSUndefined.INSTANCE, new JSValue[]{}));
        assertPendingException(context);
    }

    @Test
    public void testNow() {
        // Normal case
        long before = System.currentTimeMillis();
        JSValue result = DateConstructor.now(context, JSUndefined.INSTANCE, new JSValue[]{});
        long after = System.currentTimeMillis();

        assertThat(result).isInstanceOfSatisfying(JSNumber.class, jsNum -> {
            double timestamp = jsNum.value();
            assertThat(timestamp).isBetween((double) before, (double) after);
        });
    }

    @Test
    public void testParse() {
        // Normal case: ISO 8601 format
        JSValue result = DateConstructor.parse(context, JSUndefined.INSTANCE, new JSValue[]{
                new JSString("2025-01-01T00:00:00.000Z")
        });
        assertThat(result).isInstanceOfSatisfying(JSNumber.class, jsNum -> {
            double timestamp = jsNum.value();
            assertThat(timestamp).isGreaterThan(0.0);
        });

        // Edge case: no arguments
        result = DateConstructor.parse(context, JSUndefined.INSTANCE, new JSValue[]{});
        assertThat(result).isInstanceOfSatisfying(JSNumber.class, jsNum -> {
            double nanValue = jsNum.value();
            assertThat(nanValue).isNaN();
        });

        // Edge case: invalid date string
        result = DateConstructor.parse(context, JSUndefined.INSTANCE, new JSValue[]{
                new JSString("not a date")
        });
        assertThat(result).isInstanceOfSatisfying(JSNumber.class, jsNum -> {
            double invalidValue = jsNum.value();
            assertThat(invalidValue).isNaN();
        });
    }

    @Test
    public void testToISOString() {
        // Known timestamp: 2025-01-01T00:00:00.000Z
        JSDate date = new JSDate(1735689600000L);

        JSValue result = DatePrototype.toISOString(context, date, new JSValue[]{});
        assertThat(result).isInstanceOfSatisfying(JSString.class, jsStr -> {
            String isoString = jsStr.value();
            assertThat(isoString).contains("2025-01-01");
            assertThat(isoString).endsWith("Z");
        });

        // Edge case: called on non-Date
        assertTypeError(DatePrototype.toISOString(context, new JSArray(), new JSValue[]{}));
        assertPendingException(context);
    }

    @Test
    public void testToJSON() {
        // Known timestamp: 2025-01-01T00:00:00.000Z
        JSDate date = new JSDate(1735689600000L);

        JSValue result = DatePrototype.toJSON(context, date, new JSValue[]{});
        assertThat(result).isInstanceOfSatisfying(JSString.class, jsStr -> {
            String jsonString = jsStr.value();
            assertThat(jsonString).contains("2025-01-01");
        });

        // Edge case: called on non-Date
        assertTypeError(DatePrototype.toJSON(context, JSBoolean.FALSE, new JSValue[]{}));
        assertPendingException(context);
    }

    @Test
    public void testToString() {
        JSDate date = new JSDate(1735689600000L);

        JSValue result = DatePrototype.toStringMethod(context, date, new JSValue[]{});
        assertThat(result).isInstanceOfSatisfying(JSString.class, jsStr -> {
            String str = jsStr.value();
            assertThat(str).isNotEmpty();
        });

        // Edge case: called on non-Date
        assertTypeError(DatePrototype.toStringMethod(context, new JSString("not date"), new JSValue[]{}));
        assertPendingException(context);
    }

    @Test
    public void testUTC() {
        // Normal case: Date.UTC(2025, 0, 1, 0, 0, 0)
        JSValue result = DateConstructor.UTC(context, JSUndefined.INSTANCE, new JSValue[]{
                new JSNumber(2025),
                new JSNumber(0), // January
                new JSNumber(1),
                new JSNumber(0),
                new JSNumber(0),
                new JSNumber(0)
        });
        assertThat(result).isInstanceOfSatisfying(JSNumber.class, jsNum -> {
            double timestamp = jsNum.value();
            assertThat(timestamp).isGreaterThan(0.0);
        });

        // Edge case: only year
        result = DateConstructor.UTC(context, JSUndefined.INSTANCE, new JSValue[]{new JSNumber(2025)});
        assertThat(result).isInstanceOf(JSNumber.class);

        // Edge case: year and month
        result = DateConstructor.UTC(context, JSUndefined.INSTANCE, new JSValue[]{
                new JSNumber(2025),
                new JSNumber(5) // June
        });
        assertThat(result).isInstanceOf(JSNumber.class);

        // Edge case: no arguments (should return NaN)
        result = DateConstructor.UTC(context, JSUndefined.INSTANCE, new JSValue[]{});
        assertThat(result).isInstanceOfSatisfying(JSNumber.class, jsNum -> {
            double nanValue = jsNum.value();
            assertThat(nanValue).isNaN();
        });

        // Edge case: invalid date (should return NaN)
        result = DateConstructor.UTC(context, JSUndefined.INSTANCE, new JSValue[]{
                new JSNumber(2025),
                new JSNumber(12), // Invalid month
                new JSNumber(32)  // Invalid day
        });
        assertThat(result).isInstanceOfSatisfying(JSNumber.class, jsNum -> {
            double invalidValue = jsNum.value();
            assertThat(invalidValue).isNaN();
        });
    }

    @Test
    public void testValueOf() {
        long timestamp = 1735689600000L;
        JSDate date = new JSDate(timestamp);

        JSValue result = DatePrototype.valueOf(context, date, new JSValue[]{});
        assertThat(result).isInstanceOfSatisfying(JSNumber.class, jsNum -> assertThat(jsNum.value()).isEqualTo(timestamp));

        // Edge case: called on non-Date
        assertTypeError(DatePrototype.valueOf(context, new JSObject(), new JSValue[]{}));
        assertPendingException(context);
    }
}
