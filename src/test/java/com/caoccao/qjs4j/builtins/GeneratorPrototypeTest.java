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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for GeneratorPrototype methods.
 */
public class GeneratorPrototypeTest extends BaseTest {

    @Test
    public void testCustomGenerator() {
        // Create a custom generator that yields specific values
        final int[] counter = {0};
        JSGenerator generator = JSGenerator.fromIteratorFunction(() -> {
            counter[0]++;
            if (counter[0] == 1) {
                return JSIterator.IteratorResult.of(new JSString("first"));
            } else if (counter[0] == 2) {
                return JSIterator.IteratorResult.of(new JSString("second"));
            } else {
                return JSIterator.IteratorResult.done();
            }
        });

        // Test the custom generator
        JSValue result = GeneratorPrototype.next(context, generator, new JSValue[]{});
        JSObject iteratorResult = result.asObject().orElseThrow();
        assertThat(iteratorResult.get("value")).isInstanceOfSatisfying(JSString.class, str -> assertThat(str.value()).isEqualTo("first"));
        assertThat(iteratorResult.get("done")).isEqualTo(JSBoolean.FALSE);

        result = GeneratorPrototype.next(context, generator, new JSValue[]{});
        iteratorResult = result.asObject().orElseThrow();
        assertThat(iteratorResult.get("value")).isInstanceOfSatisfying(JSString.class, str -> assertThat(str.value()).isEqualTo("second"));
        assertThat(iteratorResult.get("done")).isEqualTo(JSBoolean.FALSE);

        result = GeneratorPrototype.next(context, generator, new JSValue[]{});
        iteratorResult = result.asObject().orElseThrow();
        assertThat(iteratorResult.get("value")).isEqualTo(JSUndefined.INSTANCE);
        assertThat(iteratorResult.get("done")).isEqualTo(JSBoolean.TRUE);
    }

    @Test
    public void testEmptyGenerator() {
        // Create an empty generator
        JSArray emptyArray = new JSArray();
        JSGenerator generator = JSGenerator.fromArray(emptyArray);

        // Normal case: next() on empty generator
        JSValue result = GeneratorPrototype.next(context, generator, new JSValue[]{});
        JSObject iteratorResult = result.asObject().orElseThrow();
        assertThat(iteratorResult.get("value")).isEqualTo(JSUndefined.INSTANCE);
        assertThat(iteratorResult.get("done")).isEqualTo(JSBoolean.TRUE);

        // Normal case: return on empty generator
        JSGenerator generator2 = JSGenerator.fromArray(emptyArray);
        result = GeneratorPrototype.returnMethod(context, generator2, new JSValue[]{new JSString("done")});
        iteratorResult = result.asObject().orElseThrow();
        assertThat(iteratorResult.get("value")).isInstanceOfSatisfying(JSString.class, str -> assertThat(str.value()).isEqualTo("done"));
        assertThat(iteratorResult.get("done")).isEqualTo(JSBoolean.TRUE);
    }

    @Test
    public void testNext() {
        // Create a simple generator from array
        JSArray array = new JSArray();
        array.push(new JSNumber(1));
        array.push(new JSNumber(2));
        array.push(new JSNumber(3));
        JSGenerator generator = JSGenerator.fromArray(array);

        // Normal case: first next() call
        JSValue result = GeneratorPrototype.next(context, generator, new JSValue[]{});
        JSObject iteratorResult = result.asObject().orElseThrow();
        assertThat(iteratorResult.get("value")).isInstanceOfSatisfying(JSNumber.class, num -> assertThat(num.value()).isEqualTo(1.0));
        assertThat(iteratorResult.get("done")).isEqualTo(JSBoolean.FALSE);

        // Normal case: second next() call
        result = GeneratorPrototype.next(context, generator, new JSValue[]{});
        iteratorResult = result.asObject().orElseThrow();
        assertThat(iteratorResult.get("value")).isInstanceOfSatisfying(JSNumber.class, num -> assertThat(num.value()).isEqualTo(2.0));
        assertThat(iteratorResult.get("done")).isEqualTo(JSBoolean.FALSE);

        // Normal case: third next() call
        result = GeneratorPrototype.next(context, generator, new JSValue[]{});
        iteratorResult = result.asObject().orElseThrow();
        assertThat(iteratorResult.get("value")).isInstanceOfSatisfying(JSNumber.class, num -> assertThat(num.value()).isEqualTo(3.0));
        assertThat(iteratorResult.get("done")).isEqualTo(JSBoolean.FALSE);

        // Normal case: fourth next() call (done)
        result = GeneratorPrototype.next(context, generator, new JSValue[]{});
        iteratorResult = result.asObject().orElseThrow();
        assertThat(iteratorResult.get("value")).isEqualTo(JSUndefined.INSTANCE);
        assertThat(iteratorResult.get("done")).isEqualTo(JSBoolean.TRUE);

        // Normal case: subsequent calls after done
        result = GeneratorPrototype.next(context, generator, new JSValue[]{});
        iteratorResult = result.asObject().orElseThrow();
        assertThat(iteratorResult.get("value")).isEqualTo(JSUndefined.INSTANCE);
        assertThat(iteratorResult.get("done")).isEqualTo(JSBoolean.TRUE);

        // Normal case: next() with value argument (ignored in this simple implementation)
        result = GeneratorPrototype.next(context, generator, new JSValue[]{new JSString("ignored")});
        iteratorResult = result.asObject().orElseThrow();
        assertThat(iteratorResult.get("value")).isEqualTo(JSUndefined.INSTANCE);
        assertThat(iteratorResult.get("done")).isEqualTo(JSBoolean.TRUE);

        // Edge case: called on non-generator
        result = GeneratorPrototype.next(context, new JSString("not a generator"), new JSValue[]{});
        assertThat(result).isInstanceOfSatisfying(JSObject.class, error -> {
            assertThat(error.get("name")).isInstanceOfSatisfying(JSString.class, name ->
                    assertThat(name.value()).isEqualTo("TypeError"));
        });
        assertThat(context.getPendingException()).isNotNull();

        // Edge case: called on null
        result = GeneratorPrototype.next(context, JSNull.INSTANCE, new JSValue[]{});
        assertThat(result).isInstanceOfSatisfying(JSObject.class, error -> {
            assertThat(error.get("name")).isInstanceOfSatisfying(JSString.class, name ->
                    assertThat(name.value()).isEqualTo("TypeError"));
        });
        assertThat(context.getPendingException()).isNotNull();
    }

    @Test
    public void testReturn() {
        // Create a generator
        JSArray array = new JSArray();
        array.push(new JSNumber(1));
        array.push(new JSNumber(2));
        array.push(new JSNumber(3));
        JSGenerator generator = JSGenerator.fromArray(array);

        // Normal case: return with value
        JSValue result = GeneratorPrototype.returnMethod(context, generator, new JSValue[]{new JSString("returned")});
        JSObject iteratorResult = result.asObject().orElseThrow();
        assertThat(iteratorResult.get("value")).isInstanceOfSatisfying(JSString.class, str -> assertThat(str.value()).isEqualTo("returned"));
        assertThat(iteratorResult.get("done")).isEqualTo(JSBoolean.TRUE);

        // Normal case: subsequent next() calls after return
        result = GeneratorPrototype.next(context, generator, new JSValue[]{});
        iteratorResult = result.asObject().orElseThrow();
        assertThat(iteratorResult.get("value")).isInstanceOfSatisfying(JSString.class, str -> assertThat(str.value()).isEqualTo("returned"));
        assertThat(iteratorResult.get("done")).isEqualTo(JSBoolean.TRUE);

        // Normal case: return without value (undefined)
        JSGenerator generator2 = JSGenerator.fromArray(array);
        result = GeneratorPrototype.returnMethod(context, generator2, new JSValue[]{});
        iteratorResult = result.asObject().orElseThrow();
        assertThat(iteratorResult.get("value")).isEqualTo(JSUndefined.INSTANCE);
        assertThat(iteratorResult.get("done")).isEqualTo(JSBoolean.TRUE);

        // Edge case: called on non-generator
        result = GeneratorPrototype.returnMethod(context, new JSObject(), new JSValue[]{new JSNumber(42)});
        assertThat(result).isInstanceOfSatisfying(JSObject.class, error -> {
            assertThat(error.get("name")).isInstanceOfSatisfying(JSString.class, name ->
                    assertThat(name.value()).isEqualTo("TypeError"));
        });
        assertThat(context.getPendingException()).isNotNull();

        // Edge case: called on undefined
        result = GeneratorPrototype.returnMethod(context, JSUndefined.INSTANCE, new JSValue[]{new JSNumber(42)});
        assertThat(result).isInstanceOfSatisfying(JSObject.class, error -> {
            assertThat(error.get("name")).isInstanceOfSatisfying(JSString.class, name ->
                    assertThat(name.value()).isEqualTo("TypeError"));
        });
        assertThat(context.getPendingException()).isNotNull();
    }

    @Test
    public void testThrow() {
        // Create a generator
        JSArray array = new JSArray();
        array.push(new JSNumber(1));
        array.push(new JSNumber(2));
        JSGenerator generator = JSGenerator.fromArray(array);

        // Normal case: throw with exception
        JSValue result = GeneratorPrototype.throwMethod(context, generator, new JSValue[]{new JSString("test exception")});
        // In this simplified implementation, throw completes the generator and returns an error
        assertThat(result).isInstanceOfSatisfying(JSObject.class, error -> {
            assertThat(error.get("name")).isNotNull();
            assertThat(error.get("message")).isNotNull();
        });
        assertThat(context.getPendingException()).isNotNull();

        // Normal case: throw without exception (undefined)
        JSGenerator generator2 = JSGenerator.fromArray(array);
        result = GeneratorPrototype.throwMethod(context, generator2, new JSValue[]{});
        assertThat(result).isInstanceOfSatisfying(JSObject.class, error -> {
            assertThat(error.get("name")).isNotNull();
            assertThat(error.get("message")).isNotNull();
        });
        assertThat(context.getPendingException()).isNotNull();

        // Edge case: called on non-generator
        result = GeneratorPrototype.throwMethod(context, new JSNumber(123), new JSValue[]{new JSString("error")});
        assertThat(result).isInstanceOfSatisfying(JSObject.class, error -> {
            assertThat(error.get("name")).isInstanceOfSatisfying(JSString.class, name ->
                    assertThat(name.value()).isEqualTo("TypeError"));
        });
        assertThat(context.getPendingException()).isNotNull();

        // Edge case: called on null
        result = GeneratorPrototype.throwMethod(context, JSNull.INSTANCE, new JSValue[]{new JSString("error")});
        assertThat(result).isInstanceOfSatisfying(JSObject.class, error -> {
            assertThat(error.get("name")).isInstanceOfSatisfying(JSString.class, name ->
                    assertThat(name.value()).isEqualTo("TypeError"));
        });
        assertThat(context.getPendingException()).isNotNull();
    }
}