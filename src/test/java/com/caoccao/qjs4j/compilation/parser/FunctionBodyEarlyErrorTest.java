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
 * The declarations of a function body, arrow body and class static block are checked as one scope (ES2024 15.2.1).
 * <p>
 * Lexical and var-declared names used to be compared only at the top level of a script or module and inside blocks, so
 * {@code function f() { let x; var x; }} compiled and the {@code var} silently won. Arrow bodies and static blocks went
 * through the block check instead, which treats a function declaration as lexical, so the legal
 * {@code () => { function f() {} var f; }} was refused. Every case is compared against V8.
 */
public class FunctionBodyEarlyErrorTest extends BaseJavetTest {
    @Test
    public void testFunctionDeclarationsAreVarScopedAtTheTopOfABody() {
        assertStringWithJavet("(() => { function f() {} var f; return typeof f })()",
                "(() => { function f() {} function f() {} return typeof f })()",
                "class S { static { function f() {} var f; globalThis.s = typeof f } } globalThis.s",
                "function g() { var f; function f() {} return typeof f } g()",
                "function g() { function f() {} function f() {} return typeof f } g()",
                "function g() { { let x; } var x = 1; return typeof x } g()",
                "function g() { var x; { let x; } return typeof x } g()",
                "function g({a}) { var a; return typeof a } g({})", "function g(x) { var x; return typeof x } g()",
                "function g() { try {} catch (e) { var e; } return 'ok' } g()");
    }

    @Test
    public void testLabeledFunctionDeclarationsAreVarScoped() {
        // Annex B: a labelled function declaration is sloppy-mode only, so these are not run in strict mode.
        assertStringWithJavet("(function () { label: function f() {} var f; return typeof f })()");
        assertBooleanWithJavet("""
                try { eval('(function () { label: function f() {} let f; })'); false }
                catch (e) { e instanceof SyntaxError }""");
    }

    @Test
    public void testLexicalAndVarNamesMustNotClash() {
        assertErrorWithJavet("function lv() { let x = 1; var x = 2; return x } lv()",
                "function lv() { var x = 1; let x = 2; return x } lv()", "function lv() { let x = 1; { var x = 2; } }",
                "function lv() { let x = 1; for (var x = 0;;) break; }", "function lv() { let x = 1; function x() {} }",
                "function lv() { function x() {} let x = 1; }", "function lv() { const x = 1; var x; }",
                "function lv() { class x {} var x; }", "function lv() { let x; let x; }",
                "function lv() { class x {} let x; }", "function lv() { let x; while (true) { var x; break; } }",
                "function lv() { let x; switch (0) { default: var x; } }",
                "function lv() { const [a, {b}] = [1, {b: 2}]; var b }", "async function lv() { let x; var x }",
                "function* lv() { let x; var x }", "var o = { m() { let x; var x } }",
                "var o = { get p() { let x; var x; return 1 } }", "class A { m() { let x; var x; } }",
                "class S { static { let x; var x; } }", "class S { static { function f() {} let f; } }",
                "(() => { function f() {} let f; })", "(() => { let x; var x; })", "(async () => { let x; var x; })");
    }

    @Test
    public void testParametersMustNotClashWithLexicalNames() {
        assertErrorWithJavet("function g(x) { let x; }", "function g(a = 1) { let a }", "function g(...a) { let a }",
                "function g(a) { class a {} }", "function g(a) { const a = 1 }", "((a) => { let a; })",
                "(a => { const a = 1; })", "((a, b) => { class b {} })", "((a, ...r) => { let r; })",
                "(async (a) => { let a; })", "(async a => { let a; })");
    }
}
