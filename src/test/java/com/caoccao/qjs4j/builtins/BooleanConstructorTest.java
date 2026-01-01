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
import org.junit.jupiter.api.Test;

/**
 * Unit tests for Boolean constructor.
 */
public class BooleanConstructorTest extends BaseJavetTest {

    @Test
    public void testBooleanConstructorWithDifferentValues() {
        assertBooleanObjectWithJavet(
                // Test with truthy values
                "new Boolean(1)",
                "new Boolean('hello')",
                // Test with falsy values
                "new Boolean(0)",
                "new Boolean('')",
                "new Boolean(null)",
                "new Boolean(undefined)"
        );
    }

    @Test
    public void testBooleanObjectToString() {
        assertStringWithJavet(
                "(new Boolean(true)).toString()",
                "(new Boolean(false)).toString()");
    }

    @Test
    public void testBooleanObjectTypeof() {
        assertStringWithJavet("typeof new Boolean(true)");
    }

    @Test
    public void testBooleanObjectValueOf() {
        assertBooleanWithJavet("(new Boolean(true)).valueOf();");
    }

    @Test
    public void testBooleanWithoutNewReturnsPrimitive() {
        assertBooleanWithJavet(
                // Test Boolean(true) without new returns primitive
                "Boolean(true);",
                // Test Boolean(false) without new returns primitive
                "Boolean(false);");
    }

    @Test
    public void testNewBooleanCreatesJSBooleanObject() {
        assertBooleanObjectWithJavet(
                // Test new Boolean(true) creates JSBooleanObject
                "new Boolean(true);",
                // Test new Boolean(false) creates JSBooleanObject
                "new Boolean(false);");
    }
}
