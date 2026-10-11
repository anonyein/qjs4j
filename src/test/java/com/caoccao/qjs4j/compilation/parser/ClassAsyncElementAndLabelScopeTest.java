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

package com.caoccao.qjs4j.compilation.parser;

import com.caoccao.qjs4j.BaseJavetTest;
import org.junit.jupiter.api.Test;

/**
 * Two classes of valid program the parser used to reject.
 * <p>
 * A class element named {@code async} — {@code async() {}}, static or not — was read as an async method with its name
 * missing, and {@code async} at the end of a line was taken as a modifier of the next element instead of a field.
 * Labels were tracked across function boundaries, so a function could not reuse a label of the code around it.
 */
public class ClassAsyncElementAndLabelScopeTest extends BaseJavetTest {
    @Test
    public void testAsyncAsAClassElementName() {
        assertStringWithJavet("(class { static async() { return 'a' } async() { return 'b' } }).async()",
                "new (class { static async() { return 'a' } async() { return 'b' } })().async()",
                "(class { async() { return 'b' } }).prototype.async()", """
                        (class {
                          static async
                          () { return 'nl' }
                        }).async()""", """
                        var C = class {
                          static async
                          m() { return 1 }
                        };
                        typeof C.m + ':' + typeof C.prototype.m + ':' + Object.hasOwn(C, 'async')""", """
                        var C = class {
                          async
                          *g() { yield 1 }
                        };
                        typeof C.prototype.g + ':' + Object.hasOwn(new C(), 'async')""",
                "var C = class { async }; Object.hasOwn(new C(), 'async') + ''",
                "(class { static async *gen() { yield 1 } }).gen().next().constructor.name",
                "(class { static async m() { return 1 } }).m().constructor.name",
                "(class { static async = 5 }).async + ''", "(class { static get async() { return 'g' } }).async");
    }

    @Test
    public void testLabelsArePerFunction() {
        assertStringWithJavet("foo: { function f() { foo: { } } } 'ok'",
                "foo: { (function () { foo: { break foo; } })() } 'ok'",
                "foo: { (() => { foo: { break foo; } })() } 'ok'", "foo: { class C { m() { foo: {} } } } 'ok'",
                "foo: { class C { static { foo: {} } } } 'ok'", "foo: { class C { x = (() => { foo: {} })() } } 'ok'",
                "foo: { function* g() { foo: { yield 1 } } } 'ok'", "foo: { async function f() { foo: {} } } 'ok'",
                "foo: { ({ m() { foo: {} } }) } 'ok'", "foo: { ({ get x() { foo: {} } }) } 'ok'",
                "foo: { (function (a = () => { foo: {} }) {})() } 'ok'", """
                        var log = [];
                        outer: for (var i = 0; i < 2; i++) {
                          (function () { outer: for (;;) { log.push(i); break outer; } })();
                          if (i === 1) break outer;
                        }
                        log.join()""");
        // A label still cannot be reused inside its own statement, nor reached from a nested function.
        assertErrorWithJavet("foo: { foo: {} }", "foo: { (function () { break foo; })() }",
                "foo: for (;;) { (function () { while (true) { continue foo; } }); break; }",
                "foo: { function f() { foo: { foo: {} } } }");
    }
}
