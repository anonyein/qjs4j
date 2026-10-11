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
 * Built-ins treat an empty or missing argument the way the specification and V8 do.
 * <ul>
 * <li>An error constructed with {@code ""} has its own {@code message}; only {@code undefined} leaves it out.</li>
 * <li>{@code Array.prototype.toString} falls back to {@code Object.prototype.toString} when {@code join} is not
 * callable, {@code undefined} included; {@code join} accepts a primitive receiver and renders a cycle as "".</li>
 * <li>A missing argument to a string method is {@code undefined}: {@code "abc".at()} is {@code "a"}, and a missing
 * search string is the string {@code "undefined"}.</li>
 * </ul>
 */
public class BuiltinArgumentEdgeCaseTest extends BaseJavetTest {
    @Test
    public void testArrayIsGuardedOnlyOnceItsLengthAndSeparatorAreRead() {
        // V8 registers an array as being joined after reading its length and converting the separator, so a separator
        // or a length getter that joins the same array still joins it. Registering first rendered the inner join as "".
        assertStringWithJavet("""
                const a = [1, 2];
                const viaSeparator = a.join({ toString() { return a.join('-'); } });
                const b = [1, 2];
                const viaToLocaleString = b.join({ toString() { return b.toLocaleString(); } });
                let joinInner;
                let joinFirst = true;
                const o = { 0: 'x', 1: 'y', get length() {
                  if (joinFirst) { joinFirst = false; joinInner = Array.prototype.join.call(o, '+'); }
                  return 2;
                } };
                const joinOuter = Array.prototype.join.call(o, '/');
                let localeInner;
                let localeFirst = true;
                const p = { 0: 'x', 1: 'y', get length() {
                  if (localeFirst) { localeFirst = false; localeInner = Array.prototype.toLocaleString.call(p); }
                  return 2;
                } };
                const localeOuter = Array.prototype.toLocaleString.call(p);
                const c = [1, 2];
                const cycleInSeparator = c.join({ toString() { return String(c.join(c)); } });
                JSON.stringify([viaSeparator, viaToLocaleString, joinInner, joinOuter, localeInner, localeOuter,
                  cycleInSeparator, c.join(undefined), c.join()])""");
    }

    @Test
    public void testArrayJoinAndToString() {
        assertStringWithJavet(
                """
                        var c = [1];
                        c.push(c);
                        JSON.stringify([c.join(), c.toString(), c.toLocaleString(), String([1, [2, [3]]]), [c, c].join('|')])""",
                """
                        var a = [1, 2];
                        a.join = undefined;
                        var b = [1, 2];
                        b.join = 5;
                        [String(a), String(b), Array.prototype.toString.call({ join: () => 'J' }),
                          Array.prototype.toString.call({})].join(' ')""",
                """
                        [Array.prototype.join.call('abc'), Array.prototype.join.call(5), Array.prototype.join.call(true),
                          Array.prototype.join.call({ length: 2, 0: 'x' })].join(' ')""",
                """
                        var d = [1];
                        var e = [d];
                        d.push(e);
                        var o = { length: 2, 0: 1 };
                        o[1] = o;
                        [d.join('-'), e.join('+'), Array.prototype.join.call(o)].join(' ')""", """
                        var f = [1, 2];
                        f.toString = function () { return 'F' };
                        [f, f].join()""",
                """
                        try { [{ toString() { throw new Error('element') } }, 2].join(); 'no' } catch (x) { x.message }""",
                """
                        try { [1, 2].join({ toString() { throw new Error('separator') } }); 'no' } catch (x) { x.message }""",
                """
                        try { Array.prototype.join.call({ get length() { throw new Error('length') } }); 'no' }
                        catch (x) { x.message }""");
    }

    @Test
    public void testErrorsWithAnEmptyMessage() {
        assertStringWithJavet("""
                JSON.stringify([Error(''), Error(undefined), TypeError(''), Error(0), AggregateError([], ''),
                  SuppressedError(1, 2, ''), RangeError(''), new EvalError(''), new URIError(''), new SyntaxError(''),
                  new ReferenceError(''), Error(), new Error('', { cause: 1 }), AggregateError([])]
                  .map(e => Object.hasOwn(e, 'message')))""", """
                JSON.stringify([Object.getOwnPropertyNames(new Error('', { cause: 1 })), String(new Error('')),
                  Object.getOwnPropertyDescriptor(new TypeError(''), 'message')])""");
    }

    @Test
    public void testErrorsWithAnUndefinedMessageAndOptions() {
        // An undefined message defines none, even when options follow.
        assertStringWithJavet("""
                ['Error', 'TypeError', 'RangeError', 'ReferenceError', 'SyntaxError', 'URIError', 'EvalError']
                  .map(name => {
                    const C = globalThis[name];
                    return [Object.hasOwn(new C(), 'message'), Object.hasOwn(new C(undefined, { cause: 1 }), 'message'),
                      Object.hasOwn(new C('', { cause: 2 }), 'message'), new C(undefined, { cause: 3 }).cause].join();
                  })
                  .concat([[Object.hasOwn(new AggregateError([]), 'message'),
                    Object.hasOwn(new AggregateError([], undefined), 'message'),
                    Object.hasOwn(new AggregateError([], undefined, { cause: 4 }), 'message'),
                    new AggregateError([], undefined, { cause: 4 }).cause].join()])
                  .join(' | ')""");
    }

    @Test
    public void testJoinOfAnArrayLikeWhoseElementThrows() {
        assertStringWithJavet("""
                try {
                  Array.prototype.join.call({ length: 2, get 1() { throw new Error('element getter') } });
                  'no'
                } catch (e) { e.message }""");
    }

    @Test
    public void testStringMethodsWithoutArguments() {
        assertStringWithJavet("""
                JSON.stringify(['abc'.at(), 'abc'.at(undefined), 'abc'.at(-1), ''.at(), 'undefined'.includes(),
                  'xundefined'.indexOf(), 'undefinedundefined'.lastIndexOf(), 'undefinedx'.startsWith(),
                  'xundefined'.endsWith(), 'abc'.includes(), 'abc'.indexOf(), 'abc'.startsWith(), 'abc'.endsWith(),
                  'abc'.lastIndexOf()])""");
    }
}
