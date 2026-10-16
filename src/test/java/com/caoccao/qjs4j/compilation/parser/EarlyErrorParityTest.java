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
 * Programs V8 refuses are refused, with V8's messages.
 * <p>
 * Each of these parsed: a parenthesized private name before {@code in}; {@code eval} or {@code arguments} as a class
 * name; {@code yield} in a class field initializer inside a generator; a {@code var} redeclaring a destructuring catch
 * parameter; a line break before an arrow's {@code =>}; {@code ()} as an expression (it evaluated to an empty array); a
 * parenthesized arrow parameter; and, in {@code v}-mode classes, {@code &&&} and mixed set operators.
 */
public class EarlyErrorParityTest extends BaseJavetTest {
    @Test
    public void testAcceptedFormsStillParse() {
        assertStringWithJavet("""
                class C { #x = 1; static m(o) { return #x in o } }
                String(C.m(new C()))""", "typeof ((a, b) => a + b)", "typeof (() => 1)", "typeof ((...a) => a)",
                "typeof ((a,) => a)", "typeof (async x => 1)", "typeof (async (x) => 1)", "String((1, 2))",
                "typeof ((a = 1) => a)", "try {} catch (e) { var e } 'ok'", "try {} catch ({ e }) { var f } 'ok'", """
                        function* g() { class C { x = 1; [(yield 2)] = 3 } return new C().x }
                        var it = g(); String(it.next().value)""");
    }

    @Test
    public void testArrowFunctionsAndParentheses() {
        assertErrorWithJavet("()", "(...a)", "(a, ...b)", "(a,)", "1 + ()", "a = ()", "()\n=> 1", "(x)\n=> 1",
                "async x\n=> 1", "async (x)\n=> 1", "((a)) => 1", "((a), b) => 1", "debugger\n()", "function f(){}\n()",
                "class C{}\n()");
    }

    @Test
    public void testClassesAndCatchParameters() {
        assertErrorWithJavet("class C { #x; static m(o) { return (#x) in o } }", "class eval {}", "class arguments {}",
                "(class eval {})", "(class arguments {})", "function* g() { class C { x = yield } }",
                "function* g() { class C { x = () => yield } }", "try {} catch ([e]) { var e }",
                "try {} catch ({e}) { var e }", "try {} catch ([e]) { for (var e of []) {} }",
                "try {} catch ({ a: [b] }) { if (true) { var b } }");
    }

    @Test
    public void testFunctionBodiesAndClassElementNames() {
        assertErrorWithJavet("function f() {", "() => {", "(a + b) => 1", "class A { static {", "x = { a() {",
                "class A { async, }", "class A { async: 1 }", "'use strict'; function eval() {}",
                "'use strict'; function arguments() {}", "(a, b)\n=> 1", "(a, (b)) => 1");
        assertStringWithJavet("String((1, 2) + 1)", "String(((1)) + 1)",
                "const async = (a, b) => a + b; String(async(1, 2))", """
                        class A { async = 1; static async; 'async'() {} }
                        [new A().async, Object.hasOwn(A, 'async')].join()""");
    }

    @Test
    public void testPrivateNamesBeginOnlyABrandCheck() {
        // A private name can begin a relational operand followed by `in`, parenthesized or not; anywhere else it is an
        // error. A blanket check on `(#` once rejected the valid parenthesized forms.
        assertStringWithJavet("""
                class C {
                  #x;
                  static m(o) {
                    return [
                      (#x in o),
                      ((#x in o)),
                      (#x in o) && 1,
                      (1, #x in o),
                      (#x in o, 2),
                      (#x in o in o),
                      (a => #x in a)(o),
                      #x in o ? 'y' : 'n',
                      [#x in o][0],
                      { a: #x in o }.a,
                      !(#x in o),
                    ].join();
                  }
                }
                [C.m(new C()), C.m({})].join(' | ')""");
        assertErrorWithJavet("class C { #x; static m(o) { return ((#x)) in o } }",
                "class C { #x; static m(o) { return #x } }", "class C { #x; static m(o) { return (#x) } }",
                "class C { #x; static m(o) { return (#x + 1) } }", "class C { #x; static m(o) { return (#x, 1) } }",
                "class C { #x; static m(o) { return (#x.y) } }", "class C { #x; static m(o) { return 1 + #x in o } }",
                "class C { #x; static m(o) { return a < #x in o } }",
                "class C { #x; static m(o) { return #x in #x in o } }",
                "class C { #x; static m(o) { for (#x in o); } }");
        // Outside every class the name is undeclared. V8's message ("Private field '#x' must be declared in an
        // enclosing class") is not qjs4j's, so only the error type is compared.
        assertStringWithJavet("try { eval('function f(o) { return #x in o }'); 'none' } catch (e) { e.name }");
    }

    @Test
    public void testRegExpEarlyErrorsAndCaseFolding() {
        assertErrorWithJavet("new RegExp('[a&&&b]', 'v')", "new RegExp('[a&&b&&]', 'v')", "new RegExp('[&&a]', 'v')",
                "new RegExp('[a&&&]', 'v')", "new RegExp('[a!!b]', 'v')", "new RegExp('[a&&b--c]', 'v')",
                "new RegExp('[ab&&c]', 'v')", "new RegExp('[a-z--b]', 'v')", "new RegExp('[(]', 'v')",
                "new RegExp('a', 'zz')", "new RegExp('(')", "/[(]/v");
        assertBooleanWithJavet("/[İ]/iu.test('i')", "/İ/iu.test('i')", "/[ı]/iu.test('i')", "/[ı]/iu.test('I')",
                "/[i]/iu.test('İ')", "/[I]/iu.test('ı')", "/[İ]/iu.test('İ')", "/[^İ]/iu.test('i')",
                "/[ſ]/iu.test('S')", "/K/iu.test('\\u212A')", "/[]/v.test('a')", "/[^]/v.test('a')",
                "/[\\q{abc|d}--d]/v.test('abc')", "/[[a-z]--[aeiou]]/v.test('e')", "/[[a-z]&&[aeiou]]/v.test('e')",
                "/[a&&b&&c]/v.test('a')", "/[a--b--c]/v.test('a')", "/[a-c]/v.test('b')");
    }
}
