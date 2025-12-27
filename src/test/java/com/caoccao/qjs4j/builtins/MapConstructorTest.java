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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Map constructor static methods.
 */
public class MapConstructorTest extends BaseTest {

    @Test
    public void testGroupBy() {
        JSArray items = new JSArray();
        items.push(new JSNumber(1));
        items.push(new JSNumber(2));
        items.push(new JSNumber(3));
        items.push(new JSNumber(4));

        // Callback function: group by even/odd
        JSFunction callback = new JSNativeFunction("testCallback", 3, (ctx, thisArg, args) -> {
            double num = args[0].asNumber().map(JSNumber::value).orElse(0.0);
            return (num % 2 == 0) ? new JSString("even") : new JSString("odd");
        });

        // Normal case: group by even/odd
        JSValue result = MapConstructor.groupBy(ctx, JSUndefined.INSTANCE, new JSValue[]{items, callback});
        assertInstanceOf(JSMap.class, result);
        JSMap map = (JSMap) result;

        // Check even group
        JSValue evenGroup = map.mapGet(new JSString("even"));
        assertInstanceOf(JSArray.class, evenGroup);
        JSArray evenArray = (JSArray) evenGroup;
        assertEquals(2, evenArray.getLength());
        assertEquals(2.0, evenArray.get(0).asNumber().map(JSNumber::value).orElse(0.0));
        assertEquals(4.0, evenArray.get(1).asNumber().map(JSNumber::value).orElse(0.0));

        // Check odd group
        JSValue oddGroup = map.mapGet(new JSString("odd"));
        assertInstanceOf(JSArray.class, oddGroup);
        JSArray oddArray = (JSArray) oddGroup;
        assertEquals(2, oddArray.getLength());
        assertEquals(1.0, oddArray.get(0).asNumber().map(JSNumber::value).orElse(0.0));
        assertEquals(3.0, oddArray.get(1).asNumber().map(JSNumber::value).orElse(0.0));

        // Edge case: empty array
        JSArray emptyItems = new JSArray();
        result = MapConstructor.groupBy(ctx, JSUndefined.INSTANCE, new JSValue[]{emptyItems, callback});
        assertInstanceOf(JSMap.class, result);
        map = (JSMap) result;
        assertEquals(0, map.size());

        // Edge case: insufficient arguments
        assertTypeError(MapConstructor.groupBy(ctx, JSUndefined.INSTANCE, new JSValue[]{items}));
        assertPendingException(ctx);

        // Edge case: non-array items
        assertTypeError(MapConstructor.groupBy(ctx, JSUndefined.INSTANCE, new JSValue[]{new JSString("not array"), callback}));
        assertPendingException(ctx);

        // Edge case: non-function callback
        assertTypeError(MapConstructor.groupBy(ctx, JSUndefined.INSTANCE, new JSValue[]{items, new JSString("not function")}));
        assertPendingException(ctx);
    }
}