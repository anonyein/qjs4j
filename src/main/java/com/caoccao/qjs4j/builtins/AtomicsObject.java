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

import com.caoccao.qjs4j.core.*;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implementation of Atomics object static methods.
 * Based on ES2017 Atomics specification.
 *
 * The Atomics object provides atomic operations on SharedArrayBuffer and TypedArray views.
 * These operations guarantee atomic read-modify-write sequences and memory ordering.
 */
public final class AtomicsObject {

    /**
     * Atomics.add(typedArray, index, value)
     * ES2017 24.4.3
     * Atomically adds value to the element at index and returns the old value.
     */
    public static JSValue add(JSContext ctx, JSValue thisArg, JSValue[] args) {
        if (args.length < 3) {
            return ctx.throwError("TypeError", "Atomics.add requires typedArray, index, and value");
        }

        // Validate typed array
        if (!(args[0] instanceof JSTypedArray typedArray)) {
            return ctx.throwError("TypeError", "Atomics.add requires a TypedArray");
        }

        // Only Int32Array and Uint32Array support atomic operations
        if (!(typedArray instanceof JSInt32Array) && !(typedArray instanceof JSUint32Array)) {
            return ctx.throwError("TypeError", "Atomics.add only works on Int32Array or Uint32Array");
        }

        // Check if backed by SharedArrayBuffer
        if (!typedArray.getBuffer().isShared()) {
            return ctx.throwError("TypeError", "Atomics operations require SharedArrayBuffer");
        }

        int index = (int) ((JSNumber) args[1]).value();
        int value = (int) ((JSNumber) args[2]).value();

        if (index < 0 || index >= typedArray.getLength()) {
            return ctx.throwError("RangeError", "Index out of bounds");
        }

        // Perform atomic add
        ByteBuffer buffer = typedArray.getBuffer().getBuffer();
        int byteOffset = typedArray.getByteOffset() + (index * 4); // 4 bytes per int32

        synchronized (buffer) {
            int oldValue = buffer.getInt(byteOffset);
            buffer.putInt(byteOffset, oldValue + value);
            return new JSNumber(oldValue);
        }
    }

    /**
     * Atomics.sub(typedArray, index, value)
     * ES2017 24.4.12
     * Atomically subtracts value from the element at index and returns the old value.
     */
    public static JSValue sub(JSContext ctx, JSValue thisArg, JSValue[] args) {
        if (args.length < 3) {
            return ctx.throwError("TypeError", "Atomics.sub requires typedArray, index, and value");
        }

        if (!(args[0] instanceof JSTypedArray typedArray)) {
            return ctx.throwError("TypeError", "Atomics.sub requires a TypedArray");
        }

        if (!(typedArray instanceof JSInt32Array) && !(typedArray instanceof JSUint32Array)) {
            return ctx.throwError("TypeError", "Atomics.sub only works on Int32Array or Uint32Array");
        }

        if (!typedArray.getBuffer().isShared()) {
            return ctx.throwError("TypeError", "Atomics operations require SharedArrayBuffer");
        }

        int index = (int) ((JSNumber) args[1]).value();
        int value = (int) ((JSNumber) args[2]).value();

        if (index < 0 || index >= typedArray.getLength()) {
            return ctx.throwError("RangeError", "Index out of bounds");
        }

        ByteBuffer buffer = typedArray.getBuffer().getBuffer();
        int byteOffset = typedArray.getByteOffset() + (index * 4);

        synchronized (buffer) {
            int oldValue = buffer.getInt(byteOffset);
            buffer.putInt(byteOffset, oldValue - value);
            return new JSNumber(oldValue);
        }
    }

    /**
     * Atomics.and(typedArray, index, value)
     * ES2017 24.4.4
     * Atomically computes bitwise AND and returns the old value.
     */
    public static JSValue and(JSContext ctx, JSValue thisArg, JSValue[] args) {
        if (args.length < 3) {
            return ctx.throwError("TypeError", "Atomics.and requires typedArray, index, and value");
        }

        if (!(args[0] instanceof JSTypedArray typedArray)) {
            return ctx.throwError("TypeError", "Atomics.and requires a TypedArray");
        }

        if (!(typedArray instanceof JSInt32Array) && !(typedArray instanceof JSUint32Array)) {
            return ctx.throwError("TypeError", "Atomics.and only works on Int32Array or Uint32Array");
        }

        if (!typedArray.getBuffer().isShared()) {
            return ctx.throwError("TypeError", "Atomics operations require SharedArrayBuffer");
        }

        int index = (int) ((JSNumber) args[1]).value();
        int value = (int) ((JSNumber) args[2]).value();

        if (index < 0 || index >= typedArray.getLength()) {
            return ctx.throwError("RangeError", "Index out of bounds");
        }

        ByteBuffer buffer = typedArray.getBuffer().getBuffer();
        int byteOffset = typedArray.getByteOffset() + (index * 4);

        synchronized (buffer) {
            int oldValue = buffer.getInt(byteOffset);
            buffer.putInt(byteOffset, oldValue & value);
            return new JSNumber(oldValue);
        }
    }

    /**
     * Atomics.or(typedArray, index, value)
     * ES2017 24.4.8
     * Atomically computes bitwise OR and returns the old value.
     */
    public static JSValue or(JSContext ctx, JSValue thisArg, JSValue[] args) {
        if (args.length < 3) {
            return ctx.throwError("TypeError", "Atomics.or requires typedArray, index, and value");
        }

        if (!(args[0] instanceof JSTypedArray typedArray)) {
            return ctx.throwError("TypeError", "Atomics.or requires a TypedArray");
        }

        if (!(typedArray instanceof JSInt32Array) && !(typedArray instanceof JSUint32Array)) {
            return ctx.throwError("TypeError", "Atomics.or only works on Int32Array or Uint32Array");
        }

        if (!typedArray.getBuffer().isShared()) {
            return ctx.throwError("TypeError", "Atomics operations require SharedArrayBuffer");
        }

        int index = (int) ((JSNumber) args[1]).value();
        int value = (int) ((JSNumber) args[2]).value();

        if (index < 0 || index >= typedArray.getLength()) {
            return ctx.throwError("RangeError", "Index out of bounds");
        }

        ByteBuffer buffer = typedArray.getBuffer().getBuffer();
        int byteOffset = typedArray.getByteOffset() + (index * 4);

        synchronized (buffer) {
            int oldValue = buffer.getInt(byteOffset);
            buffer.putInt(byteOffset, oldValue | value);
            return new JSNumber(oldValue);
        }
    }

    /**
     * Atomics.xor(typedArray, index, value)
     * ES2017 24.4.14
     * Atomically computes bitwise XOR and returns the old value.
     */
    public static JSValue xor(JSContext ctx, JSValue thisArg, JSValue[] args) {
        if (args.length < 3) {
            return ctx.throwError("TypeError", "Atomics.xor requires typedArray, index, and value");
        }

        if (!(args[0] instanceof JSTypedArray typedArray)) {
            return ctx.throwError("TypeError", "Atomics.xor requires a TypedArray");
        }

        if (!(typedArray instanceof JSInt32Array) && !(typedArray instanceof JSUint32Array)) {
            return ctx.throwError("TypeError", "Atomics.xor only works on Int32Array or Uint32Array");
        }

        if (!typedArray.getBuffer().isShared()) {
            return ctx.throwError("TypeError", "Atomics operations require SharedArrayBuffer");
        }

        int index = (int) ((JSNumber) args[1]).value();
        int value = (int) ((JSNumber) args[2]).value();

        if (index < 0 || index >= typedArray.getLength()) {
            return ctx.throwError("RangeError", "Index out of bounds");
        }

        ByteBuffer buffer = typedArray.getBuffer().getBuffer();
        int byteOffset = typedArray.getByteOffset() + (index * 4);

        synchronized (buffer) {
            int oldValue = buffer.getInt(byteOffset);
            buffer.putInt(byteOffset, oldValue ^ value);
            return new JSNumber(oldValue);
        }
    }

    /**
     * Atomics.load(typedArray, index)
     * ES2017 24.4.7
     * Atomically loads and returns the value at index.
     */
    public static JSValue load(JSContext ctx, JSValue thisArg, JSValue[] args) {
        if (args.length < 2) {
            return ctx.throwError("TypeError", "Atomics.load requires typedArray and index");
        }

        if (!(args[0] instanceof JSTypedArray typedArray)) {
            return ctx.throwError("TypeError", "Atomics.load requires a TypedArray");
        }

        if (!(typedArray instanceof JSInt32Array) && !(typedArray instanceof JSUint32Array)) {
            return ctx.throwError("TypeError", "Atomics.load only works on Int32Array or Uint32Array");
        }

        if (!typedArray.getBuffer().isShared()) {
            return ctx.throwError("TypeError", "Atomics operations require SharedArrayBuffer");
        }

        int index = (int) ((JSNumber) args[1]).value();

        if (index < 0 || index >= typedArray.getLength()) {
            return ctx.throwError("RangeError", "Index out of bounds");
        }

        ByteBuffer buffer = typedArray.getBuffer().getBuffer();
        int byteOffset = typedArray.getByteOffset() + (index * 4);

        synchronized (buffer) {
            int value = buffer.getInt(byteOffset);
            return new JSNumber(value);
        }
    }

    /**
     * Atomics.store(typedArray, index, value)
     * ES2017 24.4.11
     * Atomically stores value at index and returns the value.
     */
    public static JSValue store(JSContext ctx, JSValue thisArg, JSValue[] args) {
        if (args.length < 3) {
            return ctx.throwError("TypeError", "Atomics.store requires typedArray, index, and value");
        }

        if (!(args[0] instanceof JSTypedArray typedArray)) {
            return ctx.throwError("TypeError", "Atomics.store requires a TypedArray");
        }

        if (!(typedArray instanceof JSInt32Array) && !(typedArray instanceof JSUint32Array)) {
            return ctx.throwError("TypeError", "Atomics.store only works on Int32Array or Uint32Array");
        }

        if (!typedArray.getBuffer().isShared()) {
            return ctx.throwError("TypeError", "Atomics operations require SharedArrayBuffer");
        }

        int index = (int) ((JSNumber) args[1]).value();
        int value = (int) ((JSNumber) args[2]).value();

        if (index < 0 || index >= typedArray.getLength()) {
            return ctx.throwError("RangeError", "Index out of bounds");
        }

        ByteBuffer buffer = typedArray.getBuffer().getBuffer();
        int byteOffset = typedArray.getByteOffset() + (index * 4);

        synchronized (buffer) {
            buffer.putInt(byteOffset, value);
            return new JSNumber(value);
        }
    }

    /**
     * Atomics.compareExchange(typedArray, index, expectedValue, replacementValue)
     * ES2017 24.4.5
     * Atomically compares and exchanges if equal, returns the old value.
     */
    public static JSValue compareExchange(JSContext ctx, JSValue thisArg, JSValue[] args) {
        if (args.length < 4) {
            return ctx.throwError("TypeError", "Atomics.compareExchange requires typedArray, index, expectedValue, and replacementValue");
        }

        if (!(args[0] instanceof JSTypedArray typedArray)) {
            return ctx.throwError("TypeError", "Atomics.compareExchange requires a TypedArray");
        }

        if (!(typedArray instanceof JSInt32Array) && !(typedArray instanceof JSUint32Array)) {
            return ctx.throwError("TypeError", "Atomics.compareExchange only works on Int32Array or Uint32Array");
        }

        if (!typedArray.getBuffer().isShared()) {
            return ctx.throwError("TypeError", "Atomics operations require SharedArrayBuffer");
        }

        int index = (int) ((JSNumber) args[1]).value();
        int expectedValue = (int) ((JSNumber) args[2]).value();
        int replacementValue = (int) ((JSNumber) args[3]).value();

        if (index < 0 || index >= typedArray.getLength()) {
            return ctx.throwError("RangeError", "Index out of bounds");
        }

        ByteBuffer buffer = typedArray.getBuffer().getBuffer();
        int byteOffset = typedArray.getByteOffset() + (index * 4);

        synchronized (buffer) {
            int oldValue = buffer.getInt(byteOffset);
            if (oldValue == expectedValue) {
                buffer.putInt(byteOffset, replacementValue);
            }
            return new JSNumber(oldValue);
        }
    }

    /**
     * Atomics.exchange(typedArray, index, value)
     * ES2017 24.4.6
     * Atomically exchanges the value at index and returns the old value.
     */
    public static JSValue exchange(JSContext ctx, JSValue thisArg, JSValue[] args) {
        if (args.length < 3) {
            return ctx.throwError("TypeError", "Atomics.exchange requires typedArray, index, and value");
        }

        if (!(args[0] instanceof JSTypedArray typedArray)) {
            return ctx.throwError("TypeError", "Atomics.exchange requires a TypedArray");
        }

        if (!(typedArray instanceof JSInt32Array) && !(typedArray instanceof JSUint32Array)) {
            return ctx.throwError("TypeError", "Atomics.exchange only works on Int32Array or Uint32Array");
        }

        if (!typedArray.getBuffer().isShared()) {
            return ctx.throwError("TypeError", "Atomics operations require SharedArrayBuffer");
        }

        int index = (int) ((JSNumber) args[1]).value();
        int value = (int) ((JSNumber) args[2]).value();

        if (index < 0 || index >= typedArray.getLength()) {
            return ctx.throwError("RangeError", "Index out of bounds");
        }

        ByteBuffer buffer = typedArray.getBuffer().getBuffer();
        int byteOffset = typedArray.getByteOffset() + (index * 4);

        synchronized (buffer) {
            int oldValue = buffer.getInt(byteOffset);
            buffer.putInt(byteOffset, value);
            return new JSNumber(oldValue);
        }
    }

    /**
     * Atomics.isLockFree(size)
     * ES2017 24.4.2
     * Returns whether operations on a given size are lock-free.
     */
    public static JSValue isLockFree(JSContext ctx, JSValue thisArg, JSValue[] args) {
        if (args.length == 0) {
            return JSBoolean.FALSE;
        }

        int size = (int) ((JSNumber) args[0]).value();

        // In Java, operations on 1, 2, 4 bytes are typically lock-free on modern hardware
        // 8 bytes (long) is also lock-free with AtomicLong
        boolean lockFree = size == 1 || size == 2 || size == 4 || size == 8;
        return JSBoolean.valueOf(lockFree);
    }
}
