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
 * Javet-based tests for JSON.stringify() and JSON.parse() methods.
 * Tests cover replacer/reviver functionality, edge cases, and error conditions.
 */
public class JSONObjectJavetTest extends BaseJavetTest {

    @Test
    public void testParseArrayElement() {
        assertIntegerWithJavet("JSON.parse('[1,2,3]')[1]");
    }

    @Test
    public void testParseArrayLength() {
        assertIntegerWithJavet("JSON.parse('[1,\"two\",true,null]').length");
    }

    @Test
    public void testParseBoolean() {
        assertBooleanWithJavet("JSON.parse('true')");
    }

    @Test
    public void testParseComplexNested() {
        assertStringWithJavet("""
                var json = '{"users":[{"name":"Alice","age":30},{"name":"Bob","age":25}],"metadata":{"version":"1.0"}}';
                var obj = JSON.parse(json);
                obj.users[0].name""");
    }

    @Test
    public void testParseEmptyString() {
        assertErrorWithJavet("JSON.parse('')");
    }

    @Test
    public void testParseEscapedNewline() {
        assertStringWithJavet("JSON.parse('\"hello\\\\nworld\"')");
    }

    @Test
    public void testParseEscapedTab() {
        assertStringWithJavet("JSON.parse('\"tab\\\\there\"')");
    }

    @Test
    public void testParseInvalidJSON() {
        assertErrorWithJavet("JSON.parse('{invalid}')");
    }

    @Test
    public void testParseInvalidNumber() {
        assertErrorWithJavet("JSON.parse('01')"); // Leading zeros not allowed
    }

    @Test
    public void testParseNestedArray() {
        assertIntegerWithJavet("JSON.parse('{\"items\":[1,2,3]}').items[1]");
    }

    @Test
    public void testParseNestedObject() {
        assertStringWithJavet("JSON.parse('{\"user\":{\"name\":\"Alice\",\"age\":30}}').user.name");
    }

    @Test
    public void testParseNoArguments() {
        assertErrorWithJavet("JSON.parse()");
    }

    @Test
    public void testParseNull() {
        assertStringWithJavet("JSON.parse('null')");
    }

    @Test
    public void testParseNumber() {
        assertIntegerWithJavet("JSON.parse('42')");
    }

    @Test
    public void testParseObjectProperty() {
        assertIntegerWithJavet("JSON.parse('{\"a\":1,\"b\":2}').a");
    }

    @Test
    public void testParseObjectStringProperty() {
        assertStringWithJavet("JSON.parse('{\"name\":\"test\",\"value\":42}').name");
    }

    @Test
    public void testParseString() {
        assertStringWithJavet("JSON.parse('\"hello\"')");
    }

    @Test
    public void testParseTrailingComma() {
        assertErrorWithJavet("JSON.parse('{\"a\":1,}')");
    }

    @Test
    public void testParseUnterminatedArray() {
        assertErrorWithJavet("JSON.parse('[1,2,3')");
    }

    @Test
    public void testParseUnterminatedObject() {
        assertErrorWithJavet("JSON.parse('{\"a\":1')");
    }

    @Test
    public void testParseUnterminatedString() {
        assertErrorWithJavet("JSON.parse('\"unterminated')");
    }

    @Test
    public void testParseWithReviver() {
        assertIntegerWithJavet("""
                JSON.parse('{"a":1,"b":2,"c":3}', function(key, value) {
                  if (typeof value === 'number') {
                    return value * 2;
                  }
                  return value;
                }).b""");
    }

    @Test
    public void testParseWithReviverAndComplexStructure() {
        assertIntegerWithJavet("""
                var json = '{"numbers":[1,2,3],"data":{"x":10,"y":20}}';
                var result = JSON.parse(json, function(key, value) {
                  if (typeof value === 'number') {
                    return value + 1;
                  }
                  return value;
                });
                result.data.x""");
    }

    @Test
    public void testParseWithReviverArray() {
        assertIntegerWithJavet("""
                JSON.parse('[1,2,3,4,5]', function(key, value) {
                  if (typeof value === 'number') {
                    return value * 2;
                  }
                  return value;
                })[2]""");
    }

    @Test
    public void testParseWithReviverFilter() {
        assertBooleanWithJavet("""
                var result = JSON.parse('{"a":1,"b":2,"c":3}', function(key, value) {
                  if (key === 'b') {
                    return undefined;
                  }
                  return value;
                });
                result.b === undefined""");
    }

    @Test
    public void testParseWithReviverNested() {
        assertIntegerWithJavet("""
                JSON.parse('{"user":{"name":"Bob","age":25}}', function(key, value) {
                  if (key === 'age' && typeof value === 'number') {
                    return value + 10;
                  }
                  return value;
                }).user.age""");
    }

    @Test
    public void testParseWithReviverTransform() {
        assertStringWithJavet("""
                JSON.parse('{"date":"2023-01-01"}', function(key, value) {
                  if (key === 'date') {
                    return 'Transformed: ' + value;
                  }
                  return value;
                }).date""");
    }

    @Test
    public void testParseWithWhitespace() {
        assertIntegerWithJavet("JSON.parse('  {  \"a\"  :  1  }  ').a");
    }

    @Test
    public void testRoundTrip() {
        assertIntegerWithJavet("""
                var original = {name: 'test', value: 42, items: [1, 2, 3]};
                var json = JSON.stringify(original);
                var parsed = JSON.parse(json);
                parsed.value""");
    }

    @Test
    public void testRoundTripWithReplacerReviverForBigInt() {
        assertBigIntegerWithJavet("""
                var original = {a: 1n, b: 2n, c: 3};
                var json = JSON.stringify(original, function(key, value) {
                  if (typeof value === "bigint") {
                    return value.toString() + "n";
                  }
                  return value;
                });
                var parsed = JSON.parse(json, function(key, value) {
                  if (typeof value === 'string' && /^\\d+n$/.test(value)) {
                    return BigInt(value.slice(0, -1));
                  }
                  return value;
                });
                parsed.b""");
    }

    @Test
    public void testRoundTripWithReplacerReviverForNumber() {
        assertIntegerWithJavet("""
                var original = {a: 1, b: 2, c: 3};
                var json = JSON.stringify(original, function(key, value) {
                  if (typeof value === 'number') {
                    return value * 2;
                  }
                  return value;
                });
                var parsed = JSON.parse(json, function(key, value) {
                  if (typeof value === 'number') {
                    return value / 2;
                  }
                  return value;
                });
                parsed.b""");
    }

    @Test
    public void testRoundTripWithSpace() {
        assertIntegerWithJavet("""
                var original = {a: 1, b: 2};
                var json = JSON.stringify(original, null, 2);
                var parsed = JSON.parse(json);
                parsed.b""");
    }

    @Test
    public void testStringifyArrayMixed() {
        assertStringWithJavet("JSON.stringify([1, 'two', true, null])");
    }

    @Test
    public void testStringifyArrayNested() {
        assertStringWithJavet("JSON.stringify([[1, 2], [3, 4]])");
    }

    @Test
    public void testStringifyArraySimple() {
        assertStringWithJavet("JSON.stringify([1, 2, 3])");
    }

    @Test
    public void testStringifyArrayWithMixedTypes() {
        assertStringWithJavet("JSON.stringify([1, 'two', true, null, {a: 5}, [6, 7]])");
    }

    @Test
    public void testStringifyArrayWithNumberSpace() {
        assertStringWithJavet("JSON.stringify([1, 2, 3], null, 4)");
    }

    @Test
    public void testStringifyArrayWithUndefined() {
        assertStringWithJavet("JSON.stringify([1, undefined, 3])");
    }

    @Test
    public void testStringifyBoolean() {
        assertStringWithJavet("JSON.stringify(true)");
    }

    @Test
    public void testStringifyCircularReference() {
        assertErrorWithJavet("""
                var obj = {a: 1};
                obj.self = obj;
                JSON.stringify(obj)""");
    }

    @Test
    public void testStringifyCircularReferenceInArray() {
        assertErrorWithJavet("""
                var arr = [1, 2];
                arr.push(arr);
                JSON.stringify(arr)""");
    }

    @Test
    public void testStringifyCircularReferenceNested() {
        assertErrorWithJavet("""
                var a = {name: 'a'};
                var b = {name: 'b', ref: a};
                a.ref = b;
                JSON.stringify(a)""");
    }

    @Test
    public void testStringifyComplexNested() {
        String code = """
                JSON.stringify({
                  users: [
                    {name: 'Alice', age: 30, tags: ['dev', 'admin']},
                    {name: 'Bob', age: 25, tags: ['user']}
                  ],
                  metadata: {version: '1.0', count: 2}
                })""";
        assertStringWithJavet(code);
    }

    @Test
    public void testStringifyEmptyArray() {
        assertStringWithJavet("JSON.stringify([])");
    }

    @Test
    public void testStringifyEmptyObject() {
        assertStringWithJavet("JSON.stringify({})");
    }

    @Test
    public void testStringifyEscapeNewline() {
        assertStringWithJavet("JSON.stringify('hello\\nworld')");
    }

    @Test
    public void testStringifyEscapeQuote() {
        assertStringWithJavet("JSON.stringify('quote\\\"test')");
    }

    @Test
    public void testStringifyEscapeTab() {
        assertStringWithJavet("JSON.stringify('tab\\there')");
    }

    @Test
    public void testStringifyFloatingPoint() {
        assertStringWithJavet("JSON.stringify(0.1 + 0.2)");
    }

    @Test
    public void testStringifyFunction() {
        assertStringWithJavet("JSON.stringify(function() {})");
    }

    @Test
    public void testStringifyInfinity() {
        assertStringWithJavet("JSON.stringify(Infinity)");
    }

    @Test
    public void testStringifyNaN() {
        assertStringWithJavet("JSON.stringify(NaN)");
    }

    @Test
    public void testStringifyNegativeInfinity() {
        assertStringWithJavet("JSON.stringify(-Infinity)");
    }

    @Test
    public void testStringifyNestedStructure() {
        assertStringWithJavet("""
                JSON.stringify({
                  user: {
                    name: 'Alice',
                    age: 30
                  },
                  items: [1, 2, 3]
                })""");
    }

    @Test
    public void testStringifyNull() {
        assertStringWithJavet("JSON.stringify(null)");
    }

    @Test
    public void testStringifyNumber() {
        assertStringWithJavet("JSON.stringify(42)");
    }

    @Test
    public void testStringifyObjectComplex() {
        assertStringWithJavet("JSON.stringify({name: 'test', value: 42, active: true})");
    }

    @Test
    public void testStringifyObjectSimple() {
        assertStringWithJavet("JSON.stringify({a: 1, b: 2})");
    }

    @Test
    public void testStringifyObjectWithFunction() {
        assertStringWithJavet("JSON.stringify({a: 1, b: function() {}, c: 2})");
    }

    @Test
    public void testStringifyObjectWithUndefined() {
        assertStringWithJavet("JSON.stringify({a: 1, b: undefined, c: 3})");
    }

    @Test
    public void testStringifyScientificNotationLarge() {
        assertStringWithJavet("JSON.stringify(1e10)");
    }

    @Test
    public void testStringifyScientificNotationSmall() {
        assertStringWithJavet("JSON.stringify(1e-10)");
    }

    @Test
    public void testStringifyString() {
        assertStringWithJavet("JSON.stringify('hello')");
    }

    @Test
    public void testStringifyUndefined() {
        assertBooleanWithJavet("JSON.stringify(undefined) === undefined");
    }

    @Test
    public void testStringifyWithNumberSpace() {
        assertStringWithJavet("JSON.stringify({a: 1, b: 2}, null, 2)");
    }

    @Test
    public void testStringifyWithReplacerAndSpace() {
        assertStringWithJavet("""
                JSON.stringify(
                  {a: 1, b: 2, c: 3, d: 4},
                  ['a', 'c'],
                  2
                )""");
    }

    @Test
    public void testStringifyWithReplacerArray() {
        assertStringWithJavet("JSON.stringify({a: 1, b: 2, c: 3, d: 4}, ['a', 'c'])");
    }

    @Test
    public void testStringifyWithReplacerArrayNested() {
        assertStringWithJavet("""
                JSON.stringify({
                  name: 'test',
                  data: {x: 1, y: 2, z: 3},
                  extra: 'value'
                }, ['name', 'data', 'x', 'y'])""");
    }

    @Test
    public void testStringifyWithReplacerArrayNumbers() {
        assertStringWithJavet("JSON.stringify({0: 'a', 1: 'b', 2: 'c'}, [0, 2])");
    }

    @Test
    public void testStringifyWithReplacerFilteringOut() {
        assertStringWithJavet("""
                JSON.stringify({a: 1, b: 2, c: 3}, function(key, value) {
                  if (key === 'b') {
                    return undefined;
                  }
                  return value;
                })""");
    }

    @Test
    public void testStringifyWithReplacerFunction() {
        assertStringWithJavet("""
                JSON.stringify({a: 1, b: 2, c: 3}, function(key, value) {
                  if (typeof value === 'number') {
                    return value * 2;
                  }
                  return value;
                })""");
    }

    @Test
    public void testStringifyWithSpaceLimitNumber() {
        // Space is limited to 10 characters
        assertStringWithJavet("JSON.stringify({a: 1}, null, 20)");
    }

    @Test
    public void testStringifyWithSpaceLimitString() {
        assertStringWithJavet("JSON.stringify({a: 1}, null, 'abcdefghijklmnop')");
    }

    @Test
    public void testStringifyWithStringSpace() {
        assertStringWithJavet("JSON.stringify({a: 1, b: 2}, null, '  ')");
    }

    @Test
    public void testStringifyWithTabSpace() {
        assertStringWithJavet("JSON.stringify({a: 1}, null, '\\t')");
    }

    @Test
    public void testStringifyWithToJSON() {
        assertStringWithJavet("""
                var obj = {
                  value: 42,
                  toJSON: function() {
                    return this.value * 2;
                  }
                };
                JSON.stringify(obj)""");
    }

    @Test
    public void testStringifyWithToJSONAndReplacer() {
        assertStringWithJavet("""
                var obj = {
                  value: 10,
                  toJSON: function() {
                    return this.value * 2;
                  }
                };
                JSON.stringify(obj, function(key, value) {
                  if (typeof value === 'number') {
                    return value + 10;
                  }
                  return value;
                })""");
    }
}
