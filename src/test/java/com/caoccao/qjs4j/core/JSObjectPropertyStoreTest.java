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

package com.caoccao.qjs4j.core;

import com.caoccao.qjs4j.BaseJavetTest;
import org.junit.jupiter.api.Test;

/**
 * Storing a property the object does not have yet: [[Set]] walks the prototype chain for a setter or a read-only
 * property, then creates a data property on the receiver.
 * <p>
 * The walk allocated a {@code HashSet} of visited prototypes on every such store — the most common operation in
 * building an object — although prototype chains cannot be cyclic. It now walks the chain directly, and an ordinary
 * object adding a named property to itself skips the descriptor lookup that can only find nothing. The cases pin the
 * semantics the faster path must keep.
 */
public class JSObjectPropertyStoreTest extends BaseJavetTest {
    @Test
    public void testFailureMessages() {
        // An accessor without a setter used to be reported as a read-only property.
        assertErrorWithJavet("'use strict'; var o = { get x() { return 1 } }; o.x = 2",
                "'use strict'; var o = Object.create({ get x() { return 1 } }); o.x = 2",
                "'use strict'; var a = []; Object.defineProperty(a, 'x', { get() {} }); a.x = 1",
                "'use strict'; class K { get y() { return 1 } } new K().y = 2",
                "'use strict'; var o = Object.create(null); Object.defineProperty(o, 'z', { get() {} }); o.z = 1",
                "'use strict'; var f = function () {}; Object.defineProperty(f, 'q', { get() {} }); f.q = 1",
                "'use strict'; var o = { get [Symbol.iterator]() {} }; o[Symbol.iterator] = 1",
                "'use strict'; var o = { get 0() {} }; o[0] = 1", "'use strict'; function f(){} delete f.prototype",
                "'use strict'; function g(){} Object.defineProperty(g, 'r', { value: 1 }); g.r = 2");
        assertBooleanWithJavet("Reflect.set({ get x() {} }, 'x', 1)");
    }

    @Test
    public void testNewPropertiesOnNonExtensibleObjects() {
        assertStringWithJavet("""
                const o = Object.preventExtensions({});
                const results = [Reflect.set(o, 'x', 1)];
                o.y = 2;
                results.push(Object.keys(o).length);
                results.join()""");
        assertErrorWithJavet("'use strict'; const o = Object.preventExtensions({}); o.x = 1");
    }

    @Test
    public void testNewPropertiesOnOrdinaryObjects() {
        assertStringWithJavet("""
                var o = {};
                o.a = 1; o.b = 2; o[0] = 'index'; o['1'] = 'string index'; o[Symbol.for('s')] = 3;
                JSON.stringify([Object.keys(o), o.a, o.b, o[0], o[1], o[Symbol.for('s')]])""", """
                var o = Object.preventExtensions({ a: 1 });
                o.a = 2; o.b = 3;
                JSON.stringify([o.a, o.b, Object.hasOwn(o, 'b')])""", """
                'use strict';
                var o = Object.preventExtensions({});
                try { o.b = 3; 'no' } catch (e) { e.constructor.name + ': ' + e.message }""", """
                var o = Object.create(null);
                o.x = 1;
                JSON.stringify([Object.getOwnPropertyDescriptor(o, 'x')])""", """
                class C { constructor() { this.a = 1; this.b = 2 } }
                var c = new C();
                c.d = 4;
                JSON.stringify([Object.keys(c), Object.getOwnPropertyDescriptor(c, 'd')])""");
    }

    @Test
    public void testPrimitiveReceivers() {
        assertStringWithJavet(
                """
                        var log = [];
                        Object.defineProperty(Number.prototype, 'tag', { set(v) { log.push(typeof this + ' ' + v) }, configurable: true });
                        (5).tag = 'x';
                        'str'.missing = 1;
                        JSON.stringify([log, 'str'.missing])""",
                """
                        'use strict';
                        try { 'str'.missing = 1; 'no' } catch (e) { e.constructor.name }""",
                """
                        'use strict';
                        Object.defineProperty(String.prototype, 'ro', { value: 1, writable: false, configurable: true });
                        try { 'str'.ro = 2; 'no' } catch (e) { e.constructor.name }""");
    }

    @Test
    public void testStringObjectCharactersCannotBeDeleted() {
        assertStringWithJavet("""
                const s = new String('abc');
                [delete s[1], delete s[5], delete s.length, s[1]].join()""");
        assertErrorWithJavet("'use strict'; delete new String('abc')[1]");
    }

    @Test
    public void testThePrototypeChainDecides() {
        assertStringWithJavet("""
                var log = [];
                var proto = { set x(v) { log.push('setter ' + v + ' ' + (this === child)) } };
                var child = Object.create(Object.create(proto));
                child.x = 5;
                JSON.stringify([log, Object.hasOwn(child, 'x')])""", """
                var proto = Object.defineProperty({}, 'x', { value: 1, writable: false });
                var child = Object.create(proto);
                child.x = 2;
                JSON.stringify([child.x, Object.hasOwn(child, 'x')])""", """
                'use strict';
                var child = Object.create(Object.defineProperty({}, 'x', { value: 1, writable: false }));
                try { child.x = 2; 'no' } catch (e) { e.constructor.name }""", """
                var child = Object.create(Object.defineProperty({}, 'x', { get() { return 1 } }));
                child.x = 2;
                child.x + ''""", """
                var log = [];
                var proxy = new Proxy({}, { set(t, k, v, r) { log.push(k + '=' + v); return true } });
                var child = Object.create(proxy);
                child.y = 1;
                JSON.stringify([log, Object.hasOwn(child, 'y')])""", """
                var child = Object.create(new Uint8Array(2));
                child[1] = 7; child[5] = 9; child.named = 1;
                JSON.stringify([Object.hasOwn(child, '1'), Object.hasOwn(child, '5'), child.named])""", """
                var o = {};
                for (var i = 0; i < 2000; i++) o = Object.create(o);
                o.deep = 1;
                Object.hasOwn(o, 'deep') + ''""");
    }
}
