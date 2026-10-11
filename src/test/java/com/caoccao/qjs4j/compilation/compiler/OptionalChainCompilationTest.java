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

package com.caoccao.qjs4j.compilation.compiler;

import com.caoccao.qjs4j.BaseJavetTest;
import org.junit.jupiter.api.Test;

/**
 * An optional chain short-circuits as one unit, ends at a closing parenthesis, and never hides what an ordinary link
 * produced.
 * <p>
 * Each link used to be compiled with a null check of its own, which went wrong in several ways at once: a parenthesized
 * chain still skipped the call outside it ({@code (a?.b)()} returned {@code undefined} instead of throwing) and could
 * not be a template tag; an {@code undefined} returned by {@code f?.()} was taken for a short circuit, so
 * {@code f?.().x} hid a {@code TypeError}; {@code a?.b(...args)} was not optional at all; and a short-circuited call in
 * tail position fell through to the statement after the {@code return}. Every case is compared against V8.
 */
public class OptionalChainCompilationTest extends BaseJavetTest {
    @Test
    public void testArgumentsAndKeysAreSkippedOrCompiledAsTheirOwnChains() {
        assertIntegerWithJavet("var x = 0; var a; a?.b(x++); x", "var x = 0; var a; a?.[x++]; x",
                "var x = 0; var a; a?.b.c(x++)(x++); x",
                "var a = {b(v) { return v === undefined ? 1 : 2 }}; var c; a?.b(c?.d)",
                "var a = {b(v) { return v }}; var c = {d: 3}; a?.b(c?.d)", "var a = {x: 4}; var k; a?.[k?.y ?? 'x']");
    }

    @Test
    public void testDeleteThroughAnOptionalChain() {
        assertBooleanWithJavet("var a; delete a?.b", "var a; delete a?.b.c", "var a; delete a?.[0]",
                "var a; delete (a?.b)", "var a = {b: 1}; delete a?.b && !('b' in a)",
                "var a = {b: {c: 1}}; delete a?.b.c && !('c' in a.b)",
                "var a = {b: [1]}; delete a?.b?.[0] && !(0 in a.b)", "var f = () => ({b: 1}); delete f?.().b");
    }

    @Test
    public void testLinksAfterACallSeeWhatTheCallReturned() {
        // An undefined or null returned by a call is a value, not a short circuit.
        assertBooleanWithJavet("var f = () => null; try { f?.().x; false } catch (e) { e instanceof TypeError }",
                "var a = () => undefined; try { a?.().b; false } catch (e) { e instanceof TypeError }",
                "var a = {b: () => undefined}; try { a?.b().c; false } catch (e) { e instanceof TypeError }",
                "var a = {b() { return null }}; try { a.b?.().c; false } catch (e) { e instanceof TypeError }");
        assertUndefinedWithJavet("var a; a?.().b()", "var a; a?.().b", "var a = {}; a.b?.().c",
                "var f = () => null; f?.()?.x", "var a = {b: null}; a?.b?.c.d.e()");
        assertIntegerWithJavet("var a = () => ({b() { return 7 }}); a?.().b()",
                "var a = {b: {c() { return 9 }}}; a?.b.c()", "var o = {f() { return {g: {h: 5}} }}; o?.f().g?.h");
    }

    @Test
    public void testParenthesesEndAnOptionalChain() {
        // The call outside the parentheses is made even when the chain inside short-circuited.
        assertBooleanWithJavet("var a; try { (a?.b)(); false } catch (e) { e instanceof TypeError }",
                "var a = {}; try { (a?.b)(); false } catch (e) { e instanceof TypeError }",
                "var a; try { (a?.b.c)(); false } catch (e) { e instanceof TypeError }",
                "var a; try { (a?.['b'])(); false } catch (e) { e instanceof TypeError }",
                "var a; try { (a?.().b)(); false } catch (e) { e instanceof TypeError }",
                "var a; try { (a?.b)(...[1]); false } catch (e) { e instanceof TypeError }",
                "var a; try { (a?.b).c; false } catch (e) { e instanceof TypeError }",
                "var a = { b() { return this === a } }; (a?.b)()",
                "var a = { b() { return this === a } }; (a?.['b'])()",
                "var a = { b: { c() { return this === a.b } } }; (a?.b.c)()",
                "var a = { b() { return this === a } }; (a?.b)(...[1])",
                "var a = { b() { return this === a } }; (a?.b)?.()", "var a; (a?.b)?.() === undefined");
        assertIntegerWithJavet("var a = {b: {c() { return 3 }}}; (a?.b).c()",
                "var a = () => ({b() { return 7 }}); (a?.().b)()");
    }

    @Test
    public void testParenthesizedOptionalChainIsATemplateTag() {
        assertBooleanWithJavet("var a = { b() { return this === a } }; (a?.b)`x`",
                "var a; try { (a?.b)`x`; false } catch (e) { e instanceof TypeError }",
                "try { eval('var a = {}; a?.b`x`'); false } catch (e) { e instanceof SyntaxError }",
                "try { eval('var a = {}; a?.b.c`x`'); false } catch (e) { e instanceof SyntaxError }",
                "try { eval('var x = async function () {}?.a`t`'); false } catch (e) { e instanceof SyntaxError }",
                "var a = { b: { c() { return this === a.b } } }; (a?.b).c`x`");
        assertStringWithJavet("var t = async function () { return 1 }`x`; typeof t",
                "var o = { C: function () { this.v = 'built' } }; new o['C']().v",
                // A member of a `new` expression inside the callee of another `new`.
                "function C() { this.k = function K() { this.v = 'inner' } } new new C()['k']().v",
                "function C() { this.k = function K() { this.v = 'dot' } } new new C().k().v");
        assertIntegerWithJavet("var a = { b() { return 1 } }; (a?.b)`x`",
                // A tag in the callee of `new`: new (T`x`)().
                "function T(s) { return function () { this.ok = s.length } } new T`x`().ok",
                "var a = { b(s) { return function () { this.ok = s.length } } }; new (a?.b)`x`().ok");
    }

    @Test
    public void testPrivateAndSuperMembersAsTagsAndCallees() {
        assertIntegerWithJavet("class C { #m() { return 5 } t() { return this.#m`x` } } new C().t()",
                "class C { #m() { return 6 } t() { return this?.#m() } } new C().t()",
                "class A { m() { return 1 } } class B extends A { t() { return super.m`x` } } new B().t()",
                "class A { m() { return {x: 2} } } class B extends A { t() { return super.m?.().x } } new B().t()",
                "class A { m() { return 3 } } class B extends A { t() { return super['m']?.() } } new B().t()");
        assertUndefinedWithJavet("class A { } class B extends A { t() { return super.m?.() } } new B().t()",
                "class A { } class B extends A { t() { return super.m?.().x } } new B().t()",
                "class C { #m() { return 6 } t(o) { return o?.#m() } } new C().t(null)");
    }

    @Test
    public void testReferencesThatAreNotAssignable() {
        assertBooleanWithJavet("try { eval('var a = {}; a?.b = 1'); false } catch (e) { e instanceof SyntaxError }",
                "try { eval('var a = {}; a?.b++'); false } catch (e) { e instanceof SyntaxError }",
                "try { eval('var a = {}; ++a?.b'); false } catch (e) { e instanceof SyntaxError }",
                "try { eval('var a = {}; (a?.b) = 1'); false } catch (e) { e instanceof SyntaxError }");
    }

    @Test
    public void testSpreadCallsInAnOptionalChain() {
        assertUndefinedWithJavet("var a; a?.b(...[1, 2])", "var f; f?.(...[1])", "var a = {}; a.b?.(...[1])");
        assertIntegerWithJavet("var a = {b(...x) { return x.length }}; a?.b(...[1, 2])",
                "var f = (...x) => x.length; f?.(...[1, 2, 3])",
                "var a = {b: {c(...x) { return x[0] }}}; a?.b.c(...[4])");
    }

    @Test
    public void testTailCallsInAnOptionalChainStillReturn() {
        // In strict mode these are tail calls; a short circuit used to skip the return after them.
        assertUndefinedWithJavet("function f(a) { return a?.b(); return 5; } f(undefined)",
                "function f(a) { return a?.(); return 5; } f(undefined)",
                "function f(a) { return a?.b.c(); return 5; } f(undefined)",
                "function f(a) { return a?.b(...[1]); return 5; } f(undefined)");
        assertIntegerWithJavet(
                // Calls a tail call cannot be: a spread call and a super call.
                "function g(...x) { return x.length } function f(...a) { return g(...a) } f(1, 2)",
                "class A { constructor() { this.v = 6 } } class B extends A { constructor() { return super() } } new B().v",
                "function f(a) { return a?.b(); } f({b() { return 3 }})",
                "function f(a) { return (a?.b)(); } f({b() { return 4 }})",
                "function f(a, k) { return k === 0 ? 8 : a?.f(a, k - 1); } f({f}, 3)");
    }

    @Test
    public void testWithObjectIsTheReceiverOfAnOptionalCall() {
        // The function body is sloppy whatever the outer script is, so `with` is legal in both runs.
        assertBooleanWithJavet("var o = {f() { return this === o }}; new Function('o', 'with (o) { return f?.() }')(o)",
                "var f; var o = {}; new Function('o', 'with (o) { return f?.() === undefined }')(o)", """
                        var o = {f(...x) { return this === o && x.length === 2 }};
                        new Function('o', 'with (o) { return f?.(...[1, 2]) }')(o)""",
                "var f; var o = {}; new Function('o', 'with (o) { return f?.(...[1]) === undefined }')(o)");
    }
}
