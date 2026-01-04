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

package com.caoccao.qjs4j.compiler;

import com.caoccao.qjs4j.core.JSContext;
import com.caoccao.qjs4j.core.JSRuntime;
import com.caoccao.qjs4j.core.JSValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Test class declaration compilation and execution.
 */
public class ClassCompilerTest {

    @Test
    public void testSimpleClass() throws Exception {
        String source = """
            class Point {
            }
            Point
            """;

        try (JSContext context = new JSContext(new JSRuntime())) {
            JSValue result = context.eval(source);
            assertThat(result).isNotNull();
            System.out.println("Simple class test passed: " + result);
        }
    }

    @Test
    public void testClassWithMethod() throws Exception {
        String source = """
            class Counter {
                increment() {
                    return 42;
                }
            }
            const c = new Counter();
            c.increment()
            """;

        try (JSContext context = new JSContext(new JSRuntime())) {
            JSValue result = context.eval(source);
            assertThat(result).isNotNull();
            System.out.println("Class with method test result: " + result);
        }
    }

    @Test
    public void testClassWithConstructor() throws Exception {
        String source = """
            class Point {
                constructor(x, y) {
                    this.x = x;
                    this.y = y;
                }
            }
            const p = new Point(1, 2);
            p.x + p.y
            """;

        try (JSContext context = new JSContext(new JSRuntime())) {
            JSValue result = context.eval(source);
            assertThat(result).isNotNull();
            System.out.println("Class with constructor test result: " + result);
        }
    }
}
