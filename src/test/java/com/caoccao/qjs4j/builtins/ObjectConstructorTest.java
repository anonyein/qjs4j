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

import com.caoccao.qjs4j.BaseTest;
import com.caoccao.qjs4j.core.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Object constructor static methods.
 */
public class ObjectConstructorTest extends BaseTest {
    @Test
    public void testAssign() {
        JSObject target = new JSObject();
        target.set("a", new JSNumber(1));

        JSObject source1 = new JSObject();
        source1.set("b", new JSNumber(2));

        JSObject source2 = new JSObject();
        source2.set("c", new JSNumber(3));
        source2.set("a", new JSNumber(10)); // Override target.a

        // Normal case
        JSValue result = ObjectConstructor.assign(ctx, JSUndefined.INSTANCE, new JSValue[]{target, source1, source2});
        assertSame(target, result);
        assertEquals(10.0, target.get("a").asNumber().map(JSNumber::value).orElse(0.0));
        assertEquals(2.0, target.get("b").asNumber().map(JSNumber::value).orElse(0.0));
        assertEquals(3.0, target.get("c").asNumber().map(JSNumber::value).orElse(0.0));

        // Edge case: null/undefined sources (should be ignored)
        JSObject target2 = new JSObject();
        target2.set("x", new JSNumber(1));
        result = ObjectConstructor.assign(ctx, JSUndefined.INSTANCE, new JSValue[]{target2, JSNull.INSTANCE, JSUndefined.INSTANCE});
        assertSame(target2, result);
        assertEquals(1.0, target2.get("x").asNumber().map(JSNumber::value).orElse(0.0));

        // Edge case: no arguments
        assertTypeError(ObjectConstructor.assign(ctx, JSUndefined.INSTANCE, new JSValue[]{}));
        assertPendingException(ctx);

        // Edge case: non-object target
        assertTypeError(ObjectConstructor.assign(ctx, JSUndefined.INSTANCE, new JSValue[]{JSNull.INSTANCE}));
        assertPendingException(ctx);

        result = ctx.eval("var target = {a: 1}; Object.assign(target, {b: 2}, {c: 3}); JSON.stringify(target)");
        assertNotNull(result);
        assertEquals("{\"a\":1,\"b\":2,\"c\":3}", result.toJavaObject());
    }

    @Test
    public void testCreate() {
        JSObject proto = new JSObject();
        proto.set("x", new JSNumber(100));

        // Normal case: create object with prototype
        JSValue result = ObjectConstructor.create(ctx, JSUndefined.INSTANCE, new JSValue[]{proto});
        JSObject obj = result.asObject().orElse(null);
        assertNotNull(obj);
        assertSame(proto, obj.getPrototype());

        // Edge case: create with null prototype
        result = ObjectConstructor.create(ctx, JSUndefined.INSTANCE, new JSValue[]{JSNull.INSTANCE});
        obj = result.asObject().orElse(null);
        assertNotNull(obj);
        assertNull(obj.getPrototype());

        // Edge case: no arguments
        assertTypeError(ObjectConstructor.create(ctx, JSUndefined.INSTANCE, new JSValue[]{}));
        assertPendingException(ctx);

        // Edge case: invalid prototype
        assertTypeError(ObjectConstructor.create(ctx, JSUndefined.INSTANCE, new JSValue[]{new JSString("not valid")}));
        assertPendingException(ctx);

        ctx.eval("var proto = {x: 10}");
        result = ctx.eval("var newObj = Object.create(proto); newObj.x");
        assertNotNull(result);
        assertEquals(10.0, (Double) result.toJavaObject());
    }

    @Test
    public void testDefineProperties() {
        JSValue result = ctx.eval(
                "var obj = {}; " +
                        "Object.defineProperties(obj, {" +
                        "  x: {value: 1, writable: true, enumerable: true}," +
                        "  y: {value: 2, writable: false, enumerable: true}" +
                        "}); " +
                        "JSON.stringify({x: obj.x, y: obj.y})"
        );
        assertEquals("{\"x\":1,\"y\":2}", result.toJavaObject());
    }

    @Test
    public void testDefineProperty() {
        JSObject obj = new JSObject();

        // Create a data descriptor
        JSObject descriptor = new JSObject();
        descriptor.set("value", new JSNumber(42));
        descriptor.set("writable", JSBoolean.TRUE);
        descriptor.set("enumerable", JSBoolean.TRUE);
        descriptor.set("configurable", JSBoolean.TRUE);

        JSValue result = ctx.eval("var obj = {}; Object.defineProperty(obj, 'x', {value: 42, writable: true}); obj.x");
        assertEquals(42.0, (Double) result.toJavaObject());

        // Test with eval to verify it's registered
        result = ctx.eval("var obj2 = {}; Object.defineProperty(obj2, 'y', {value: 100}); obj2.y");
        assertEquals(100.0, (Double) result.toJavaObject());
    }

    @Test
    public void testEntries() {
        JSObject obj = new JSObject();
        obj.set("a", new JSNumber(1));
        obj.set("b", new JSNumber(2));

        // Normal case
        JSValue result = ObjectConstructor.entries(ctx, JSUndefined.INSTANCE, new JSValue[]{obj});
        JSArray entries = result.asArray().orElse(null);
        assertNotNull(entries);
        assertEquals(2, entries.getLength());

        // Check first entry
        JSValue firstEntry = entries.get(0);
        JSArray firstPair = firstEntry.asArray().orElse(null);
        assertNotNull(firstPair);
        assertEquals(2, firstPair.getLength());
        assertTrue(firstPair.get(0).asString().isPresent());
        assertTrue(firstPair.get(1).asNumber().isPresent());

        // Edge case: empty object
        JSObject emptyObj = new JSObject();
        result = ObjectConstructor.entries(ctx, JSUndefined.INSTANCE, new JSValue[]{emptyObj});
        entries = result.asArray().orElse(null);
        assertNotNull(entries);
        assertEquals(0, entries.getLength());

        // Edge case: no arguments
        assertTypeError(ObjectConstructor.entries(ctx, JSUndefined.INSTANCE, new JSValue[]{}));
        assertPendingException(ctx);

        ctx.eval("var obj = {a: 1, b: 2, c: 3}");
        result = ctx.eval("JSON.stringify(Object.entries(obj))");
        assertNotNull(result);
        assertEquals("[[\"a\",1],[\"b\",2],[\"c\",3]]", result.toJavaObject());
    }

    @Test
    public void testFreeze() {
        JSObject obj = new JSObject();
        obj.set("a", new JSNumber(1));

        // Normal case
        JSValue result = ObjectConstructor.freeze(ctx, JSUndefined.INSTANCE, new JSValue[]{obj});
        assertSame(obj, result);
        assertTrue(obj.isFrozen());

        // Edge case: no arguments
        assertTypeError(ObjectConstructor.freeze(ctx, JSUndefined.INSTANCE, new JSValue[]{}));
        assertPendingException(ctx);
    }

    @Test
    public void testFromEntries() {
        // Test using eval to create proper arrays with prototypes
        ctx.eval("var entries = [['a', 1], ['b', 2]]");
        JSValue entries = ctx.getGlobalObject().get("entries");

        // For now, just test that fromEntries doesn't crash
        JSValue result = ObjectConstructor.fromEntries(ctx, JSUndefined.INSTANCE, new JSValue[]{entries});
        JSObject obj = result.asObject().orElse(null);
        assertNotNull(obj);
        assertEquals(1.0, obj.get("a").asNumber().map(JSNumber::value).orElse(0.0));
        assertEquals(2.0, obj.get("b").asNumber().map(JSNumber::value).orElse(0.0));

        // Edge case: empty array
        ctx.eval("var emptyEntries = []");
        JSValue emptyEntries = ctx.getGlobalObject().get("emptyEntries");
        result = ObjectConstructor.fromEntries(ctx, JSUndefined.INSTANCE, new JSValue[]{emptyEntries});
        obj = result.asObject().orElse(null);
        assertNotNull(obj);
        assertEquals(0, obj.getOwnPropertyKeys().size());

        // Edge case: no arguments
        assertTypeError(ObjectConstructor.fromEntries(ctx, JSUndefined.INSTANCE, new JSValue[]{}));
        assertPendingException(ctx);

        // Edge case: non-iterable argument
        assertTypeError(ObjectConstructor.fromEntries(ctx, JSUndefined.INSTANCE, new JSValue[]{new JSNumber(42)}));
        assertPendingException(ctx);

        // Test with string keys and various value types - use manual creation
        JSArray mixedEntries = new JSArray();
        JSArray mixedEntry1 = new JSArray();
        mixedEntry1.push(new JSString("key1"));
        mixedEntry1.push(new JSString("value1"));
        mixedEntries.push(mixedEntry1);

        JSArray mixedEntry2 = new JSArray();
        mixedEntry2.push(new JSString("key2"));
        mixedEntry2.push(JSBoolean.TRUE);
        mixedEntries.push(mixedEntry2);

        // Set prototype for the arrays
        JSObject arrayProto = ctx.getGlobalObject().get("Array").asObject().orElse(null).get("prototype").asObject().orElse(null);
        mixedEntries.setPrototype(arrayProto);
        mixedEntry1.setPrototype(arrayProto);
        mixedEntry2.setPrototype(arrayProto);

        result = ObjectConstructor.fromEntries(ctx, JSUndefined.INSTANCE, new JSValue[]{mixedEntries});
        obj = result.asObject().orElse(null);
        assertNotNull(obj);
        assertEquals("value1", obj.get("key1").asString().map(JSString::value).orElse(""));
        assertEquals(JSBoolean.TRUE, obj.get("key2"));
    }

    @Test
    public void testGetOwnPropertyDescriptor() {
        JSObject obj = new JSObject();
        obj.set("testProp", new JSString("testValue"));

        // Normal case: existing property
        JSValue result = ObjectConstructor.getOwnPropertyDescriptor(ctx, JSUndefined.INSTANCE, new JSValue[]{obj, new JSString("testProp")});
        assertInstanceOf(JSObject.class, result);
        JSObject desc = (JSObject) result;
        assertEquals("testValue", desc.get("value").asString().map(JSString::value).orElse(""));
        assertTrue(desc.get("writable").asBoolean().map(JSBoolean::isBooleanTrue).orElse(false));
        assertTrue(desc.get("enumerable").asBoolean().map(JSBoolean::isBooleanTrue).orElse(false));
        assertTrue(desc.get("configurable").asBoolean().map(JSBoolean::isBooleanTrue).orElse(false));

        // Normal case: non-existing property
        result = ObjectConstructor.getOwnPropertyDescriptor(ctx, JSUndefined.INSTANCE, new JSValue[]{obj, new JSString("nonexistent")});
        assertTrue(result.isUndefined());

        // Edge case: insufficient arguments
        result = ObjectConstructor.getOwnPropertyDescriptor(ctx, JSUndefined.INSTANCE, new JSValue[]{obj});
        assertTrue(result.isUndefined());

        // Edge case: non-object
        assertTypeError(ObjectConstructor.getOwnPropertyDescriptor(ctx, JSUndefined.INSTANCE, new JSValue[]{new JSString("not object"), new JSString("prop")}));
        assertPendingException(ctx);
    }

    @Test
    public void testGetOwnPropertyDescriptors() {
        JSValue result = ctx.eval(
                "var obj = {a: 1, b: 2}; " +
                        "var descs = Object.getOwnPropertyDescriptors(obj); " +
                        "descs.a.value + descs.b.value"
        );
        assertEquals(3.0, (Double) result.toJavaObject());
    }

    @Test
    public void testGetOwnPropertyNames() {
        JSObject obj = new JSObject();
        obj.set("prop1", new JSString("value1"));
        obj.set("prop2", new JSString("value2"));

        // Normal case: object with properties
        JSValue result = ObjectConstructor.getOwnPropertyNames(ctx, JSUndefined.INSTANCE, new JSValue[]{obj});
        assertInstanceOf(JSArray.class, result);
        JSArray names = (JSArray) result;
        assertEquals(2, names.getLength());
        // Note: order may vary, so check both are present
        String name0 = names.get(0).asString().map(JSString::value).orElse("");
        String name1 = names.get(1).asString().map(JSString::value).orElse("");
        assertTrue((name0.equals("prop1") && name1.equals("prop2")) || (name0.equals("prop2") && name1.equals("prop1")));

        // Normal case: empty object
        JSObject emptyObj = new JSObject();
        result = ObjectConstructor.getOwnPropertyNames(ctx, JSUndefined.INSTANCE, new JSValue[]{emptyObj});
        assertInstanceOf(JSArray.class, result);
        names = (JSArray) result;
        assertEquals(0, names.getLength());

        // Edge case: no arguments
        assertTypeError(ObjectConstructor.getOwnPropertyNames(ctx, JSUndefined.INSTANCE, new JSValue[]{}));
        assertPendingException(ctx);

        // Edge case: non-object
        assertTypeError(ObjectConstructor.getOwnPropertyNames(ctx, JSUndefined.INSTANCE, new JSValue[]{new JSString("not object")}));
        assertPendingException(ctx);
    }

    @Test
    public void testGetOwnPropertySymbols() {
        JSObject obj = new JSObject();
        JSSymbol symbol1 = new JSSymbol("sym1");
        JSSymbol symbol2 = new JSSymbol("sym2");
        obj.set(PropertyKey.fromSymbol(symbol1), new JSString("value1"));
        obj.set(PropertyKey.fromSymbol(symbol2), new JSString("value2"));

        // Normal case: object with symbol properties
        JSValue result = ObjectConstructor.getOwnPropertySymbols(ctx, JSUndefined.INSTANCE, new JSValue[]{obj});
        assertInstanceOf(JSArray.class, result);
        JSArray symbols = (JSArray) result;
        assertEquals(2, symbols.getLength());

        // Normal case: object with no symbol properties
        JSObject regularObj = new JSObject();
        regularObj.set("prop", new JSString("value"));
        result = ObjectConstructor.getOwnPropertySymbols(ctx, JSUndefined.INSTANCE, new JSValue[]{regularObj});
        assertInstanceOf(JSArray.class, result);
        symbols = (JSArray) result;
        assertEquals(0, symbols.getLength());

        // Edge case: no arguments
        result = ObjectConstructor.getOwnPropertySymbols(ctx, JSUndefined.INSTANCE, new JSValue[]{});
        assertInstanceOf(JSArray.class, result);
        symbols = (JSArray) result;
        assertEquals(0, symbols.getLength());

        // Edge case: non-object (should return empty array)
        result = ObjectConstructor.getOwnPropertySymbols(ctx, JSUndefined.INSTANCE, new JSValue[]{new JSString("not object")});
        assertInstanceOf(JSArray.class, result);
        symbols = (JSArray) result;
        assertEquals(0, symbols.getLength());
    }

    @Test
    public void testGetPrototypeOf() {
        JSObject proto = new JSObject();
        JSObject obj = new JSObject();
        obj.setPrototype(proto);

        // Normal case
        JSValue result = ObjectConstructor.getPrototypeOf(ctx, JSUndefined.INSTANCE, new JSValue[]{obj});
        assertSame(proto, result);

        // Edge case: null prototype
        JSObject objWithNullProto = new JSObject();
        objWithNullProto.setPrototype(null);
        result = ObjectConstructor.getPrototypeOf(ctx, JSUndefined.INSTANCE, new JSValue[]{objWithNullProto});
        assertTrue(result.isNull());

        // Edge case: no arguments
        assertTypeError(ObjectConstructor.getPrototypeOf(ctx, JSUndefined.INSTANCE, new JSValue[]{}));
        assertPendingException(ctx);

        // Edge case: non-object
        assertTypeError(ObjectConstructor.getPrototypeOf(ctx, JSUndefined.INSTANCE, new JSValue[]{new JSString("not object")}));
        assertPendingException(ctx);

        ctx.eval("var proto = {x: 10}; var newObj = Object.create(proto)");
        result = ctx.eval("Object.getPrototypeOf(newObj) === proto");
        assertNotNull(result);
        assertTrue((boolean) result.toJavaObject());
    }

    @Test
    public void testGroupBy() {
        JSArray items = new JSArray();
        items.push(new JSNumber(1));
        items.push(new JSNumber(2));
        items.push(new JSNumber(3));
        items.push(new JSNumber(4));

        // Callback function: group by even/odd
        JSFunction callback = new JSNativeFunction("testCallback", 3, (ctx, thisArg, args) -> {
            double num = args[0].asNumber().map(JSNumber::value).orElse(0.0);
            return (num % 2 == 0) ? new JSString("even") : new JSString("odd");
        });

        // Normal case: group by even/odd
        JSValue result = ObjectConstructor.groupBy(ctx, JSUndefined.INSTANCE, new JSValue[]{items, callback});
        assertInstanceOf(JSObject.class, result);
        JSObject groups = (JSObject) result;

        // Check even group
        JSValue evenGroup = groups.get("even");
        assertInstanceOf(JSArray.class, evenGroup);
        JSArray evenArray = (JSArray) evenGroup;
        assertEquals(2, evenArray.getLength());
        assertEquals(2.0, evenArray.get(0).asNumber().map(JSNumber::value).orElse(0.0));
        assertEquals(4.0, evenArray.get(1).asNumber().map(JSNumber::value).orElse(0.0));

        // Check odd group
        JSValue oddGroup = groups.get("odd");
        assertInstanceOf(JSArray.class, oddGroup);
        JSArray oddArray = (JSArray) oddGroup;
        assertEquals(2, oddArray.getLength());
        assertEquals(1.0, oddArray.get(0).asNumber().map(JSNumber::value).orElse(0.0));
        assertEquals(3.0, oddArray.get(1).asNumber().map(JSNumber::value).orElse(0.0));

        // Edge case: empty array
        JSArray emptyItems = new JSArray();
        result = ObjectConstructor.groupBy(ctx, JSUndefined.INSTANCE, new JSValue[]{emptyItems, callback});
        assertInstanceOf(JSObject.class, result);
        groups = (JSObject) result;
        // Should have no properties
        assertTrue(groups.get("even").isUndefined());
        assertTrue(groups.get("odd").isUndefined());

        // Edge case: insufficient arguments
        assertTypeError(ObjectConstructor.groupBy(ctx, JSUndefined.INSTANCE, new JSValue[]{items}));
        assertPendingException(ctx);

        // Edge case: non-array items
        assertTypeError(ObjectConstructor.groupBy(ctx, JSUndefined.INSTANCE, new JSValue[]{new JSString("not array"), callback}));
        assertPendingException(ctx);

        // Edge case: non-function callback
        assertTypeError(ObjectConstructor.groupBy(ctx, JSUndefined.INSTANCE, new JSValue[]{items, new JSString("not function")}));
        assertPendingException(ctx);
    }

    @Test
    public void testHasOwn() {
        JSObject obj = new JSObject();
        obj.set("existingProp", new JSString("value"));

        // Normal case: existing property
        JSValue result = ObjectConstructor.hasOwn(ctx, JSUndefined.INSTANCE, new JSValue[]{obj, new JSString("existingProp")});
        assertTrue(result.isBooleanTrue());

        // Normal case: non-existing property
        result = ObjectConstructor.hasOwn(ctx, JSUndefined.INSTANCE, new JSValue[]{obj, new JSString("nonexistent")});
        assertTrue(result.isBooleanFalse());

        // Edge case: insufficient arguments (should return false, not throw)
        result = ObjectConstructor.hasOwn(ctx, JSUndefined.INSTANCE, new JSValue[]{obj});
        assertTrue(result.isBooleanFalse());

        // Edge case: no arguments
        assertTypeError(ObjectConstructor.hasOwn(ctx, JSUndefined.INSTANCE, new JSValue[]{}));
        assertPendingException(ctx);

        // Edge case: non-object
        assertTypeError(ObjectConstructor.hasOwn(ctx, JSUndefined.INSTANCE, new JSValue[]{new JSString("not object"), new JSString("prop")}));
        assertPendingException(ctx);
    }

    @Test
    public void testHasOwnProperty() {
        JSObject obj = new JSObject();
        obj.set("a", new JSNumber(1));
        obj.set("b", new JSNumber(2));

        // Normal case: property exists
        JSValue result = ObjectConstructor.hasOwnProperty(ctx, obj, new JSValue[]{new JSString("a")});
        assertTrue(result.isBooleanTrue());

        // Property doesn't exist
        result = ObjectConstructor.hasOwnProperty(ctx, obj, new JSValue[]{new JSString("z")});
        assertTrue(result.isBooleanFalse());

        // Edge case: no arguments
        result = ObjectConstructor.hasOwnProperty(ctx, obj, new JSValue[]{});
        assertTrue(result.isBooleanFalse());

        // Edge case: called on non-object
        assertTypeError(ObjectConstructor.hasOwnProperty(ctx, new JSString("not object"), new JSValue[]{new JSString("a")}));
        assertPendingException(ctx);
    }

    @Test
    public void testIs() {
        // Test SameValue algorithm
        JSValue result = ctx.eval("Object.is(1, 1)");
        assertTrue((Boolean) result.toJavaObject());

        result = ctx.eval("Object.is(1, 2)");
        assertFalse((Boolean) result.toJavaObject());

        // NaN equals NaN in SameValue
        result = ctx.eval("Object.is(NaN, NaN)");
        assertTrue((Boolean) result.toJavaObject());

        // +0 differs from -0 in SameValue
        result = ctx.eval("Object.is(0, -0)");
        assertFalse((Boolean) result.toJavaObject());

        result = ctx.eval("Object.is(0, 0)");
        assertTrue((Boolean) result.toJavaObject());

        // Objects
        result = ctx.eval("var obj = {}; Object.is(obj, obj)");
        assertTrue((Boolean) result.toJavaObject());

        result = ctx.eval("Object.is({}, {})");
        assertFalse((Boolean) result.toJavaObject());
    }

    // NOTE: Additional tests for Object extensibility methods (preventExtensions, getOwnPropertyDescriptor,
    // getOwnPropertySymbols, etc.) from Phase 34-35 have been commented out due to issues with JavaScript
    // evaluation returning incorrect types (returning "[object Object]" instead of expected string/number
    // values). The implementations exist but proper testing requires investigation into the
    // JSValue.toJavaObject() behavior with JavaScript-evaluated results.

    @Test
    public void testIsExtensible() {
        // Test normal extensible object
        JSValue result = ctx.eval("var obj = {}; Object.isExtensible(obj)");
        assertTrue((Boolean) result.toJavaObject());

        // Test after preventExtensions
        result = ctx.eval(
                "var obj2 = {}; " +
                        "Object.preventExtensions(obj2); " +
                        "Object.isExtensible(obj2)"
        );
        assertFalse((Boolean) result.toJavaObject());

        // Test sealed object is not extensible
        result = ctx.eval(
                "var obj3 = {}; " +
                        "Object.seal(obj3); " +
                        "Object.isExtensible(obj3)"
        );
        assertFalse((Boolean) result.toJavaObject());

        // Test frozen object is not extensible
        result = ctx.eval(
                "var obj4 = {}; " +
                        "Object.freeze(obj4); " +
                        "Object.isExtensible(obj4)"
        );
        assertFalse((Boolean) result.toJavaObject());
    }

    @Test
    public void testIsFrozen() {
        JSObject obj = new JSObject();
        obj.set("a", new JSNumber(1));

        // Normal case: not frozen
        JSValue result = ObjectConstructor.isFrozen(ctx, JSUndefined.INSTANCE, new JSValue[]{obj});
        assertTrue(result.isBooleanFalse());

        // After freezing
        obj.freeze();
        result = ObjectConstructor.isFrozen(ctx, JSUndefined.INSTANCE, new JSValue[]{obj});
        assertTrue(result.isBooleanTrue());

        // Edge case: no arguments
        assertTypeError(ObjectConstructor.isFrozen(ctx, JSUndefined.INSTANCE, new JSValue[]{}));
        assertPendingException(ctx);
    }

    @Test
    public void testIsSealed() {
        JSObject obj = new JSObject();
        obj.set("a", new JSNumber(1));

        // Normal case: not sealed
        JSValue result = ObjectConstructor.isSealed(ctx, JSUndefined.INSTANCE, new JSValue[]{obj});
        assertTrue(result.isBooleanFalse());

        // After sealing
        obj.seal();
        result = ObjectConstructor.isSealed(ctx, JSUndefined.INSTANCE, new JSValue[]{obj});
        assertTrue(result.isBooleanTrue());

        // Edge case: no arguments
        assertTypeError(ObjectConstructor.isSealed(ctx, JSUndefined.INSTANCE, new JSValue[]{}));
        assertPendingException(ctx);
    }

    @Test
    public void testKeys() {
        JSObject obj = new JSObject();
        obj.set("a", new JSNumber(1));
        obj.set("b", new JSNumber(2));
        obj.set("c", new JSNumber(3));

        // Normal case
        JSValue result = ObjectConstructor.keys(ctx, JSUndefined.INSTANCE, new JSValue[]{obj});
        JSArray keys = result.asArray().orElse(null);
        assertNotNull(keys);
        assertEquals(3, keys.getLength());

        // Edge case: empty object
        JSObject emptyObj = new JSObject();
        result = ObjectConstructor.keys(ctx, JSUndefined.INSTANCE, new JSValue[]{emptyObj});
        keys = result.asArray().orElse(null);
        assertNotNull(keys);
        assertEquals(0, keys.getLength());

        // Edge case: no arguments
        assertTypeError(ObjectConstructor.keys(ctx, JSUndefined.INSTANCE, new JSValue[]{}));
        assertPendingException(ctx);

        // Edge case: non-object
        assertTypeError(ObjectConstructor.keys(ctx, JSUndefined.INSTANCE, new JSValue[]{JSNull.INSTANCE}));
        assertPendingException(ctx);

        result = ctx.eval("var obj = {a: 1, b: 2, c: 3}; Object.keys(obj)");
        assertNotNull(result);
        assertEquals("[\"a\", \"b\", \"c\"]", result.toString());
    }

    @Test
    public void testPreventExtensions() {
        JSObject obj = new JSObject();

        // Normal case: prevent extensions on object
        JSValue result = ObjectConstructor.preventExtensions(ctx, JSUndefined.INSTANCE, new JSValue[]{obj});
        assertSame(obj, result);

        // Edge case: no arguments
        assertTypeError(ObjectConstructor.preventExtensions(ctx, JSUndefined.INSTANCE, new JSValue[]{}));
        assertPendingException(ctx);

        // Edge case: non-object (should return as-is)
        JSValue primitive = new JSString("string");
        result = ObjectConstructor.preventExtensions(ctx, JSUndefined.INSTANCE, new JSValue[]{primitive});
        assertSame(primitive, result);
    }

    @Test
    public void testSeal() {
        JSObject obj = new JSObject();
        obj.set("a", new JSNumber(1));

        // Normal case
        JSValue result = ObjectConstructor.seal(ctx, JSUndefined.INSTANCE, new JSValue[]{obj});
        assertSame(obj, result);
        assertTrue(obj.isSealed());

        // Edge case: no arguments
        assertTypeError(ObjectConstructor.seal(ctx, JSUndefined.INSTANCE, new JSValue[]{}));
        assertPendingException(ctx);
    }

    @Test
    public void testSetPrototypeOf() {
        JSObject obj = new JSObject();
        JSObject newProto = new JSObject();
        newProto.set("y", new JSNumber(200));

        // Normal case
        JSValue result = ObjectConstructor.setPrototypeOf(ctx, JSUndefined.INSTANCE, new JSValue[]{obj, newProto});
        assertSame(obj, result);
        assertSame(newProto, obj.getPrototype());

        // Edge case: set to null
        result = ObjectConstructor.setPrototypeOf(ctx, JSUndefined.INSTANCE, new JSValue[]{obj, JSNull.INSTANCE});
        assertSame(obj, result);
        assertNull(obj.getPrototype());

        // Edge case: missing arguments
        assertTypeError(ObjectConstructor.setPrototypeOf(ctx, JSUndefined.INSTANCE, new JSValue[]{obj}));
        assertPendingException(ctx);

        // Edge case: non-object target
        assertTypeError(ObjectConstructor.setPrototypeOf(ctx, JSUndefined.INSTANCE, new JSValue[]{new JSString("not object"), newProto}));
        assertPendingException(ctx);

        // Edge case: invalid prototype
        assertTypeError(ObjectConstructor.setPrototypeOf(ctx, JSUndefined.INSTANCE, new JSValue[]{obj, new JSString("invalid")}));
        assertPendingException(ctx);
    }

    @Test
    public void testValues() {
        JSObject obj = new JSObject();
        obj.set("a", new JSNumber(1));
        obj.set("b", new JSNumber(2));
        obj.set("c", new JSNumber(3));

        // Normal case
        JSValue result = ObjectConstructor.values(ctx, JSUndefined.INSTANCE, new JSValue[]{obj});
        JSArray values = result.asArray().orElse(null);
        assertNotNull(values);
        assertEquals(3, values.getLength());

        // Edge case: empty object
        JSObject emptyObj = new JSObject();
        result = ObjectConstructor.values(ctx, JSUndefined.INSTANCE, new JSValue[]{emptyObj});
        values = result.asArray().orElse(null);
        assertNotNull(values);
        assertEquals(0, values.getLength());

        // Edge case: no arguments
        assertTypeError(ObjectConstructor.values(ctx, JSUndefined.INSTANCE, new JSValue[]{}));
        assertPendingException(ctx);

        ctx.eval("var obj = {a: 1, b: 2, c: 3}");
        result = ctx.eval("JSON.stringify(Object.values(obj))");
        assertNotNull(result);
        assertEquals("[1,2,3]", result.toJavaObject());
    }
}
