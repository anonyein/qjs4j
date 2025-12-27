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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AtomicsObject synchronization methods.
 */
public class AtomicsObjectTest extends BaseTest {

    @Test
    public void testNotify() {
        // Create ArrayBuffer and Int32Array
        JSArrayBuffer ab = new JSArrayBuffer(4);
        JSInt32Array arr = new JSInt32Array(ab, 0, 1);

        // Store initial value
        assertNotNull(arr.getBuffer().getBuffer());
        arr.getBuffer().getBuffer().putInt(0, 0);

        // Test 1: Notify with non-shared buffer - should still return 0
        JSValue result = AtomicsObject.notify(ctx, null, new JSValue[]{arr, new JSNumber(0)});
        assertTrue(result.isNumber());
        assertEquals(0, result.asNumber().map(JSNumber::value).orElse(-1D));

        // Test 2: Invalid arguments
        JSValue error = AtomicsObject.notify(ctx, null, new JSValue[]{});
        assertTypeError(error);

        // Test 3: Non-TypedArray argument
        error = AtomicsObject.notify(ctx, null, new JSValue[]{new JSNumber(1)});
        assertTypeError(error);

        // Test 4: Out of bounds index
        error = AtomicsObject.notify(ctx, null, new JSValue[]{arr, new JSNumber(10)});
        assertRangeError(error);

        // Test 5: Negative count
        error = AtomicsObject.notify(ctx, null, new JSValue[]{arr, new JSNumber(0), new JSNumber(-1)});
        assertRangeError(error);
    }

    @Test
    public void testNotifyMultipleWaiters() throws InterruptedException {
        // Create ArrayBuffer and Int32Array
        JSArrayBuffer ab = new JSArrayBuffer(4);
        JSInt32Array arr = new JSInt32Array(ab, 0, 1);

        // Store initial value
        assertNotNull(arr.getBuffer().getBuffer());
        arr.getBuffer().getBuffer().putInt(0, 200);

        int waiterCount = 3;
        CountDownLatch allWaitersStarted = new CountDownLatch(waiterCount);
        CountDownLatch allWaitersFinished = new CountDownLatch(waiterCount);
        AtomicInteger successCount = new AtomicInteger(0);

        // Create multiple waiters
        for (int i = 0; i < waiterCount; i++) {
            Thread waiter = new Thread(() -> {
                JSContext waiterCtx = new JSContext(new JSRuntime());
                allWaitersStarted.countDown();
                JSValue result = AtomicsObject.wait(waiterCtx, null, new JSValue[]{
                        arr, new JSNumber(0), new JSNumber(200), new JSNumber(5000)
                });

                if (result instanceof JSString && "ok".equals(((JSString) result).value())) {
                    successCount.incrementAndGet();
                }
                allWaitersFinished.countDown();
                waiterCtx.close();
            });
            waiter.start();
        }

        // Wait for all waiters to start
        assertTrue(allWaitersStarted.await(2, TimeUnit.SECONDS));

        // Give waiters time to enter wait state
        Thread.sleep(100);

        // Notify all waiters
        JSValue notifyResult = AtomicsObject.notify(ctx, null, new JSValue[]{
                arr, new JSNumber(0), new JSNumber(waiterCount)
        });

        assertTrue(notifyResult.isNumber());
        assertEquals(waiterCount, notifyResult.asNumber().map(JSNumber::value).orElse(-1D).intValue());

        // Wait for all waiters to finish
        assertTrue(allWaitersFinished.await(2, TimeUnit.SECONDS));
        assertEquals(waiterCount, successCount.get(), "All waiters should have been notified");
    }

    @Test
    public void testNotifyWithInfinity() throws InterruptedException {
        // Create ArrayBuffer and Int32Array
        JSArrayBuffer ab = new JSArrayBuffer(4);
        JSInt32Array arr = new JSInt32Array(ab, 0, 1);

        assertNotNull(arr.getBuffer().getBuffer());
        arr.getBuffer().getBuffer().putInt(0, 300);

        CountDownLatch waiterStarted = new CountDownLatch(1);
        CountDownLatch waiterFinished = new CountDownLatch(1);

        Thread waiter = new Thread(() -> {
            JSContext waiterCtx = new JSContext(new JSRuntime());
            waiterStarted.countDown();
            AtomicsObject.wait(waiterCtx, null, new JSValue[]{
                    arr, new JSNumber(0), new JSNumber(300), new JSNumber(5000)
            });
            waiterFinished.countDown();
            waiterCtx.close();
        });

        waiter.start();
        assertTrue(waiterStarted.await(1, TimeUnit.SECONDS));
        Thread.sleep(100);

        // Notify with Integer.MAX_VALUE (equivalent to +Infinity in the spec)
        JSValue notifyResult = AtomicsObject.notify(ctx, null, new JSValue[]{
                arr, new JSNumber(0) // No count parameter means notify all
        });

        assertTrue(notifyResult.isNumber());
        assertEquals(1, notifyResult.asNumber().map(JSNumber::value).orElse(0D).intValue());
        assertTrue(waiterFinished.await(2, TimeUnit.SECONDS));
    }

    @Test
    public void testPause() {
        // Atomics.pause() should return undefined
        JSValue result = AtomicsObject.pause(ctx, null, new JSValue[]{});
        assertTrue(result.isUndefined());
    }

    @Test
    public void testWait() {
        // Create ArrayBuffer and Int32Array
        JSArrayBuffer ab = new JSArrayBuffer(4);
        JSInt32Array arr = new JSInt32Array(ab, 0, 1);

        // Store initial value
        assertNotNull(arr.getBuffer().getBuffer());
        arr.getBuffer().getBuffer().putInt(0, 42);

        // Test 1: Wait with non-matching value - should return "not-equal"
        JSValue result = AtomicsObject.wait(ctx, null, new JSValue[]{
                arr, new JSNumber(0), new JSNumber(0), new JSNumber(0)
        });
        assertTrue(result.isString());
        assertEquals("not-equal", result.asString().map(JSString::value).orElse(""));

        // Test 2: Wait with matching value and immediate timeout - should return "timed-out"
        result = AtomicsObject.wait(ctx, null, new JSValue[]{
                arr, new JSNumber(0), new JSNumber(42), new JSNumber(0)
        });
        assertTrue(result.isString());
        assertEquals("timed-out", result.asString().map(JSString::value).orElse(""));

        // Test 3: Invalid arguments
        JSValue error = AtomicsObject.wait(ctx, null, new JSValue[]{arr, new JSNumber(0)});
        assertTypeError(error);

        // Test 4: Non-TypedArray argument
        error = AtomicsObject.wait(ctx, null, new JSValue[]{new JSNumber(1), new JSNumber(0), new JSNumber(0)});
        assertTypeError(error);

        // Test 5: Out of bounds index
        error = AtomicsObject.wait(ctx, null, new JSValue[]{arr, new JSNumber(10), new JSNumber(0)});
        assertRangeError(error);
    }

    @Test
    public void testWaitAndNotifyMultithreaded() throws InterruptedException {
        // Create ArrayBuffer and Int32Array
        JSArrayBuffer ab = new JSArrayBuffer(4);
        JSInt32Array arr = new JSInt32Array(ab, 0, 1);

        // Store initial value
        assertNotNull(arr.getBuffer().getBuffer());
        arr.getBuffer().getBuffer().putInt(0, 100);

        CountDownLatch waitStarted = new CountDownLatch(1);
        CountDownLatch waitFinished = new CountDownLatch(1);
        AtomicBoolean waitSuccess = new AtomicBoolean(false);

        // Thread 1: Wait
        Thread waiter = new Thread(() -> {
            JSContext waiterCtx = new JSContext(new JSRuntime());
            waitStarted.countDown();
            JSValue result = AtomicsObject.wait(waiterCtx, null, new JSValue[]{
                    arr, new JSNumber(0), new JSNumber(100), new JSNumber(5000) // 5 second timeout
            });

            if (result instanceof JSString && "ok".equals(((JSString) result).value())) {
                waitSuccess.set(true);
            }
            waitFinished.countDown();
            waiterCtx.close();
        });

        waiter.start();

        // Wait for waiter to start
        assertTrue(waitStarted.await(1, TimeUnit.SECONDS));

        // Give waiter time to enter wait state
        Thread.sleep(100);

        // Thread 2: Notify
        JSValue notifyResult = AtomicsObject.notify(ctx, null, new JSValue[]{
                arr, new JSNumber(0), new JSNumber(1)
        });

        // Should notify 1 waiter
        assertTrue(notifyResult.isNumber());
        assertEquals(1, notifyResult.asNumber().map(JSNumber::value).orElse(0D).intValue());

        // Wait for waiter to finish
        assertTrue(waitFinished.await(2, TimeUnit.SECONDS));
        assertTrue(waitSuccess.get(), "Wait should have been notified successfully");
    }

    @Test
    public void testWaitAsync() {
        // Create ArrayBuffer and Int32Array
        JSArrayBuffer ab = new JSArrayBuffer(4);
        JSInt32Array arr = new JSInt32Array(ab, 0, 1);

        // Store initial value
        assertNotNull(arr.getBuffer().getBuffer());
        arr.getBuffer().getBuffer().putInt(0, 42);

        // Test 1: waitAsync with non-matching value - should return {async: false, value: "not-equal"}
        JSValue result = AtomicsObject.waitAsync(ctx, null, new JSValue[]{
                arr, new JSNumber(0), new JSNumber(0)
        });
        JSObject resultObj = result.asObject().orElse(null);
        assertNotNull(resultObj);
        assertEquals(JSBoolean.FALSE, resultObj.get("async"));
        assertEquals("not-equal", ((JSString) resultObj.get("value")).value());

        // Test 2: waitAsync with matching value - should return {async: true, value: ...}
        result = AtomicsObject.waitAsync(ctx, null, new JSValue[]{
                arr, new JSNumber(0), new JSNumber(42)
        });
        resultObj = result.asObject().orElse(null);
        assertNotNull(resultObj);
        assertEquals(JSBoolean.TRUE, resultObj.get("async"));

        // Test 3: Invalid arguments
        JSValue error = AtomicsObject.waitAsync(ctx, null, new JSValue[]{arr, new JSNumber(0)});
        assertTypeError(error);

        // Test 4: Non-TypedArray argument
        error = AtomicsObject.waitAsync(ctx, null, new JSValue[]{new JSNumber(1), new JSNumber(0), new JSNumber(0)});
        assertTypeError(error);

        // Test 5: Out of bounds index
        error = AtomicsObject.waitAsync(ctx, null, new JSValue[]{arr, new JSNumber(10), new JSNumber(0)});
        assertRangeError(error);
    }
}
