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
import com.caoccao.qjs4j.core.JSNumber;
import com.caoccao.qjs4j.core.JSSharedArrayBuffer;
import com.caoccao.qjs4j.core.JSString;
import com.caoccao.qjs4j.core.JSValue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SharedArrayBuffer.prototype methods.
 */
public class SharedArrayBufferPrototypeTest extends BaseTest {

    @Test
    public void testGetByteLength() {
        JSSharedArrayBuffer sab = new JSSharedArrayBuffer(64);

        // Normal case: get byte length
        JSValue result = SharedArrayBufferPrototype.getByteLength(ctx, sab, new JSValue[]{});
        assertEquals(64.0, result.asNumber().map(JSNumber::value).orElse(0.0));

        // Normal case: empty buffer
        JSSharedArrayBuffer emptySab = new JSSharedArrayBuffer(0);
        result = SharedArrayBufferPrototype.getByteLength(ctx, emptySab, new JSValue[]{});
        assertEquals(0.0, result.asNumber().map(JSNumber::value).orElse(0.0));

        // Edge case: called on non-SharedArrayBuffer
        assertTypeError(SharedArrayBufferPrototype.getByteLength(ctx, new JSString("not sab"), new JSValue[]{}));
        assertPendingException(ctx);
    }

    @Test
    public void testSlice() {
        JSSharedArrayBuffer sab = new JSSharedArrayBuffer(16);

        // Normal case: slice entire buffer
        JSValue result = SharedArrayBufferPrototype.slice(ctx, sab, new JSValue[]{});
        assertInstanceOf(JSSharedArrayBuffer.class, result);
        JSSharedArrayBuffer sliced = (JSSharedArrayBuffer) result;
        assertEquals(16, sliced.getByteLength());

        // Normal case: slice with start only
        result = SharedArrayBufferPrototype.slice(ctx, sab, new JSValue[]{new JSNumber(4)});
        assertInstanceOf(JSSharedArrayBuffer.class, result);
        sliced = (JSSharedArrayBuffer) result;
        assertEquals(12, sliced.getByteLength()); // 16 - 4

        // Normal case: slice with start and end
        result = SharedArrayBufferPrototype.slice(ctx, sab, new JSValue[]{new JSNumber(4), new JSNumber(12)});
        assertInstanceOf(JSSharedArrayBuffer.class, result);
        sliced = (JSSharedArrayBuffer) result;
        assertEquals(8, sliced.getByteLength()); // 12 - 4

        // Normal case: negative start (from end)
        result = SharedArrayBufferPrototype.slice(ctx, sab, new JSValue[]{new JSNumber(-8)});
        assertInstanceOf(JSSharedArrayBuffer.class, result);
        sliced = (JSSharedArrayBuffer) result;
        assertEquals(8, sliced.getByteLength()); // 16 - 8

        // Normal case: negative end (from end)
        result = SharedArrayBufferPrototype.slice(ctx, sab, new JSValue[]{new JSNumber(4), new JSNumber(-4)});
        assertInstanceOf(JSSharedArrayBuffer.class, result);
        sliced = (JSSharedArrayBuffer) result;
        assertEquals(8, sliced.getByteLength()); // 12 - 4

        // Edge case: start >= end (empty slice)
        result = SharedArrayBufferPrototype.slice(ctx, sab, new JSValue[]{new JSNumber(8), new JSNumber(4)});
        assertInstanceOf(JSSharedArrayBuffer.class, result);
        sliced = (JSSharedArrayBuffer) result;
        assertEquals(0, sliced.getByteLength());

        // Edge case: called on non-SharedArrayBuffer
        assertTypeError(SharedArrayBufferPrototype.slice(ctx, new JSString("not sab"), new JSValue[]{}));
        assertPendingException(ctx);
    }
}