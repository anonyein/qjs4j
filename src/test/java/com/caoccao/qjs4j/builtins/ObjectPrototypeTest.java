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
import com.caoccao.qjs4j.core.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ObjectPrototype methods.
 */
public class ObjectPrototypeTest extends BaseJavetTest {

    @Test
    public void testAssign() {
        JSObject target = new JSObject();
        target.set("a", new JSNumber(1));

        JSObject source = new JSObject();
        source.set("b", new JSNumber(2));
        source.set("c", new JSString("hello"));

        // Normal case: assign sources to target
        JSValue result = ObjectPrototype.assign(context, JSUndefined.INSTANCE, new JSValue[]{target, source});
        assertThat(result).isEqualTo(target);
        assertThat(target.get("a").asNumber().map(JSNumber::value).orElseThrow()).isEqualTo(1.0);
        assertThat(target.get("b").asNumber().map(JSNumber::value).orElseThrow()).isEqualTo(2.0);
        assertThat(target.get("c").asString().map(JSString::value).orElseThrow()).isEqualTo("hello");

        // Normal case: multiple sources
        JSObject source2 = new JSObject();
        source2.set("d", JSBoolean.TRUE);
        result = ObjectPrototype.assign(context, JSUndefined.INSTANCE, new JSValue[]{target, source2});
        assertThat(result).isEqualTo(target);
        assertThat(target.get("d")).isEqualTo(JSBoolean.TRUE);

        // Normal case: skip null/undefined sources
        result = ObjectPrototype.assign(context, JSUndefined.INSTANCE, new JSValue[]{target, JSNull.INSTANCE, source});
        assertThat(result).isEqualTo(target);

        // Edge case: no arguments
        result = ObjectPrototype.assign(context, JSUndefined.INSTANCE, new JSValue[]{});
        assertTypeError(result);
        assertPendingException(context);

        // Edge case: null target
        result = ObjectPrototype.assign(context, JSUndefined.INSTANCE, new JSValue[]{JSNull.INSTANCE});
        assertTypeError(result);
        assertPendingException(context);
    }

    @Test
    public void testCreate() {
        // Normal case: create object with null prototype
        JSValue result = ObjectPrototype.create(context, JSUndefined.INSTANCE, new JSValue[]{JSNull.INSTANCE});
        assertThat(result).isInstanceOfSatisfying(JSObject.class, obj -> assertThat(obj.getPrototype()).isNull());

        // Normal case: create object with object prototype
        JSObject proto = new JSObject();
        proto.set("testProp", new JSString("testValue"));
        result = ObjectPrototype.create(context, JSUndefined.INSTANCE, new JSValue[]{proto});
        assertThat(result).isInstanceOfSatisfying(JSObject.class, obj2 -> {
            assertThat(obj2.getPrototype()).isEqualTo(proto);
            // Should inherit property
            assertThat(obj2.get("testProp").asString().map(JSString::value).orElseThrow()).isEqualTo("testValue");
        });

        // Edge case: invalid prototype
        result = ObjectPrototype.create(context, JSUndefined.INSTANCE, new JSValue[]{new JSString("not object")});
        assertTypeError(result);
        assertPendingException(context);

        // Edge case: no arguments
        result = ObjectPrototype.create(context, JSUndefined.INSTANCE, new JSValue[]{});
        assertTypeError(result);
        assertPendingException(context);
    }

    @Test
    public void testDefineProperty() {
        JSObject obj = new JSObject();

        // Normal case: define data property
        JSObject descriptor = new JSObject();
        descriptor.set("value", new JSString("test"));
        descriptor.set("writable", JSBoolean.TRUE);
        descriptor.set("enumerable", JSBoolean.TRUE);
        descriptor.set("configurable", JSBoolean.TRUE);

        JSValue result = ObjectPrototype.defineProperty(context, JSUndefined.INSTANCE, new JSValue[]{
                obj, new JSString("testProp"), descriptor
        });
        assertThat(result).isEqualTo(obj);
        assertThat(obj.get("testProp").asString().map(JSString::value).orElseThrow()).isEqualTo("test");

        // Edge case: not enough arguments
        result = ObjectPrototype.defineProperty(context, JSUndefined.INSTANCE, new JSValue[]{obj, new JSString("prop")});
        assertTypeError(result);
        assertPendingException(context);

        // Edge case: first argument not object
        result = ObjectPrototype.defineProperty(context, JSUndefined.INSTANCE, new JSValue[]{
                new JSString("not object"), new JSString("prop"), descriptor
        });
        assertTypeError(result);
        assertPendingException(context);
    }

    @Test
    public void testEntries() {
        JSObject obj = new JSObject();
        obj.set("a", new JSNumber(1));
        obj.set("b", new JSString("hello"));

        // Normal case: object with properties
        JSValue result = ObjectPrototype.entries(context, JSUndefined.INSTANCE, new JSValue[]{obj});
        assertThat(result).isInstanceOfSatisfying(JSArray.class, entries -> assertThat(entries.getLength()).isGreaterThanOrEqualTo(2));

        // Normal case: empty object
        JSObject emptyObj = new JSObject();
        result = ObjectPrototype.entries(context, JSUndefined.INSTANCE, new JSValue[]{emptyObj});
        assertThat(result).isInstanceOfSatisfying(JSArray.class, emptyEntries -> assertThat(emptyEntries.getLength()).isEqualTo(0));

        // Edge case: null
        result = ObjectPrototype.entries(context, JSUndefined.INSTANCE, new JSValue[]{JSNull.INSTANCE});
        assertTypeError(result);
        assertPendingException(context);

        // Edge case: primitive
        result = ObjectPrototype.entries(context, JSUndefined.INSTANCE, new JSValue[]{JSBoolean.FALSE});
        assertThat(result).isInstanceOfSatisfying(JSArray.class, primitiveEntries -> assertThat(primitiveEntries.getLength()).isEqualTo(0));
    }

    @Test
    public void testFreeze() {
        JSObject obj = new JSObject();
        obj.set("a", new JSNumber(1));

        // Normal case: freeze object
        JSValue result = ObjectPrototype.freeze(context, JSUndefined.INSTANCE, new JSValue[]{obj});
        assertThat(result).isEqualTo(obj);

        // Normal case: freeze primitive (returns primitive)
        result = ObjectPrototype.freeze(context, JSUndefined.INSTANCE, new JSValue[]{new JSString("string")});
        assertThat(result).isInstanceOfSatisfying(JSString.class, jsStr -> assertThat(jsStr.value()).isEqualTo("string"));
    }

    @Test
    public void testHasOwnProperty() {
        JSObject obj = new JSObject();
        obj.set("a", new JSNumber(1));
        obj.set("b", new JSNumber(2));

        // Normal case: has property
        JSValue result = ObjectPrototype.hasOwnProperty(context, obj, new JSValue[]{new JSString("a")});
        assertThat(result).isEqualTo(JSBoolean.TRUE);

        // Normal case: doesn't have property
        result = ObjectPrototype.hasOwnProperty(context, obj, new JSValue[]{new JSString("c")});
        assertThat(result).isEqualTo(JSBoolean.FALSE);

        // Normal case: numeric property
        result = ObjectPrototype.hasOwnProperty(context, obj, new JSValue[]{new JSNumber(1)});
        assertThat(result).isEqualTo(JSBoolean.FALSE); // "1" != "a"

        // Edge case: no arguments
        result = ObjectPrototype.hasOwnProperty(context, obj, new JSValue[]{});
        assertThat(result).isEqualTo(JSBoolean.FALSE);

        // Edge case: called on non-object
        result = ObjectPrototype.hasOwnProperty(context, new JSString("string"), new JSValue[]{new JSString("length")});
        assertThat(result).isEqualTo(JSBoolean.FALSE);
    }

    @Test
    public void testKeys() {
        JSObject obj = new JSObject();
        obj.set("a", new JSNumber(1));
        obj.set("b", new JSNumber(2));
        obj.set("c", new JSNumber(3));

        // Normal case: object with properties
        JSValue result = ObjectPrototype.keys(context, JSUndefined.INSTANCE, new JSValue[]{obj});
        assertThat(result).isInstanceOfSatisfying(JSArray.class, keys -> assertThat(keys.getLength()).isGreaterThanOrEqualTo(3)); // May include prototype properties

        // Normal case: empty object
        JSObject emptyObj = new JSObject();
        result = ObjectPrototype.keys(context, JSUndefined.INSTANCE, new JSValue[]{emptyObj});
        assertThat(result).isInstanceOfSatisfying(JSArray.class, emptyKeys -> assertThat(emptyKeys.getLength()).isEqualTo(0));

        // Edge case: null
        result = ObjectPrototype.keys(context, JSUndefined.INSTANCE, new JSValue[]{JSNull.INSTANCE});
        assertTypeError(result);
        assertPendingException(context);

        // Edge case: undefined
        result = ObjectPrototype.keys(context, JSUndefined.INSTANCE, new JSValue[]{JSUndefined.INSTANCE});
        assertTypeError(result);
        assertPendingException(context);

        // Edge case: primitive
        result = ObjectPrototype.keys(context, JSUndefined.INSTANCE, new JSValue[]{new JSString("string")});
        assertThat(result).isInstanceOfSatisfying(JSArray.class, primitiveKeys -> assertThat(primitiveKeys.getLength()).isEqualTo(0));
    }

    @Test
    public void testSet() {
        String code = """
                const obj = {};
                obj.a = 1;
                JSON.stringify(obj);""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeString(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testToString() {
        // Normal case: undefined
        JSValue result = ObjectPrototype.toString(context, JSUndefined.INSTANCE, new JSValue[]{});
        assertThat(result).isInstanceOfSatisfying(JSString.class, jsStr -> assertThat(jsStr.value()).isEqualTo("[object Undefined]"));

        // Normal case: null
        result = ObjectPrototype.toString(context, JSNull.INSTANCE, new JSValue[]{});
        assertThat(result).isInstanceOfSatisfying(JSString.class, jsStr -> assertThat(jsStr.value()).isEqualTo("[object Null]"));

        // Normal case: object
        JSObject obj = new JSObject();
        result = ObjectPrototype.toString(context, obj, new JSValue[]{});
        assertThat(result).isInstanceOfSatisfying(JSString.class, jsStr -> assertThat(jsStr.value()).isEqualTo("[object Object]"));

        // Normal case: array
        JSArray arr = new JSArray();
        result = ObjectPrototype.toString(context, arr, new JSValue[]{});
        assertThat(result).isInstanceOfSatisfying(JSString.class, jsStr -> assertThat(jsStr.value()).isEqualTo("[object Array]"));

        // Normal case: function
        JSFunction func = new JSNativeFunction("test", 0, (ctx, thisArg, args) -> JSUndefined.INSTANCE);
        result = ObjectPrototype.toString(context, func, new JSValue[]{});
        assertThat(result).isInstanceOfSatisfying(JSString.class, jsStr -> assertThat(jsStr.value()).isEqualTo("[object Function]"));

        // Normal case: string
        result = ObjectPrototype.toString(context, new JSString("test"), new JSValue[]{});
        assertThat(result).isInstanceOfSatisfying(JSString.class, jsStr -> assertThat(jsStr.value()).isEqualTo("[object String]"));

        // Normal case: number
        result = ObjectPrototype.toString(context, new JSNumber(42), new JSValue[]{});
        assertThat(result).isInstanceOfSatisfying(JSString.class, jsStr -> assertThat(jsStr.value()).isEqualTo("[object Number]"));

        // Normal case: boolean
        result = ObjectPrototype.toString(context, JSBoolean.TRUE, new JSValue[]{});
        assertThat(result).isInstanceOfSatisfying(JSString.class, jsStr -> assertThat(jsStr.value()).isEqualTo("[object Boolean]"));
    }

    @Test
    public void testValueOf() {
        JSObject obj = new JSObject();
        obj.set("a", new JSNumber(1));

        // Normal case: object
        JSValue result = ObjectPrototype.valueOf(context, obj, new JSValue[]{});
        assertThat(result).isEqualTo(obj);

        // Normal case: primitive
        JSString str = new JSString("hello");
        result = ObjectPrototype.valueOf(context, str, new JSValue[]{});
        assertThat(result).isEqualTo(str);

        // Normal case: null
        result = ObjectPrototype.valueOf(context, JSNull.INSTANCE, new JSValue[]{});
        assertThat(result).isEqualTo(JSNull.INSTANCE);
    }

    @Test
    public void testValues() {
        JSObject obj = new JSObject();
        obj.set("a", new JSNumber(1));
        obj.set("b", new JSString("hello"));
        obj.set("c", JSBoolean.TRUE);

        // Normal case: object with properties
        JSValue result = ObjectPrototype.values(context, JSUndefined.INSTANCE, new JSValue[]{obj});
        assertThat(result).isInstanceOfSatisfying(JSArray.class, values -> assertThat(values.getLength()).isGreaterThanOrEqualTo(3));

        // Normal case: empty object
        JSObject emptyObj = new JSObject();
        result = ObjectPrototype.values(context, JSUndefined.INSTANCE, new JSValue[]{emptyObj});
        assertThat(result).isInstanceOfSatisfying(JSArray.class, emptyValues -> assertThat(emptyValues.getLength()).isEqualTo(0));

        // Edge case: null
        result = ObjectPrototype.values(context, JSUndefined.INSTANCE, new JSValue[]{JSNull.INSTANCE});
        assertTypeError(result);
        assertPendingException(context);

        // Edge case: primitive
        result = ObjectPrototype.values(context, JSUndefined.INSTANCE, new JSValue[]{new JSNumber(42)});
        assertThat(result).isInstanceOfSatisfying(JSArray.class, primitiveValues -> assertThat(primitiveValues.getLength()).isEqualTo(0));
    }
}
