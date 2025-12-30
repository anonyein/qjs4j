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

import com.caoccao.qjs4j.BaseJavetTest;
import com.caoccao.qjs4j.core.JSBoolean;
import com.caoccao.qjs4j.core.JSBooleanObject;
import com.caoccao.qjs4j.core.JSValue;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Boolean constructor.
 */
public class BooleanConstructorTest extends BaseJavetTest {

    @Test
    public void testBooleanConstructorWithDifferentValues() {
        Stream.of(
                // Test with truthy values
                "new Boolean(1)", "new Boolean('hello')",
                // Test with falsy values
                "new Boolean(0)", "new Boolean('')", "new Boolean(null)", "new Boolean(undefined)"
        ).forEach(code -> assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeBoolean(),
                () -> {
                    JSValue result = context.eval(code);
                    assertThat(result.isBooleanObject()).isTrue();
                    return result.asBooleanObject().map(JSBooleanObject::getValue).map(JSBoolean::value).orElseThrow();
                }
        ));
    }

    @Test
    public void testBooleanObjectToString() {
        Stream.of(
                "(new Boolean(true)).toString()",
                "(new Boolean(false)).toString()").forEach(code -> {
            assertWithJavet(
                    () -> v8Runtime.getExecutor(code).executeString(),
                    () -> context.eval(code).toJavaObject());
        });
    }

    @Test
    public void testBooleanObjectTypeof() {
        String code = "typeof new Boolean(true)";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeString(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testBooleanObjectValueOf() {
        String code = "(new Boolean(true)).valueOf();";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeBoolean(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testBooleanWithoutNewReturnsPrimitive() {
        Stream.of(
                // Test Boolean(true) without new returns primitive
                "Boolean(true);",
                // Test Boolean(false) without new returns primitive
                "Boolean(false);"
        ).forEach(code -> assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeBoolean(),
                () -> context.eval(code).toJavaObject()));
    }

    @Test
    public void testNewBooleanCreatesJSBooleanObject() {
        Stream.of(
                // Test new Boolean(true) creates JSBooleanObject
                "new Boolean(true);",
                // Test new Boolean(false) creates JSBooleanObject
                "new Boolean(false);"
        ).forEach(code ->
                assertWithJavet(
                        () -> v8Runtime.getExecutor(code).executeBoolean(),
                        () -> context.eval(code).asBooleanObject().map(JSBooleanObject::toJavaObject).orElseThrow()));
    }
}
