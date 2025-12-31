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
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for DataView prototype methods.
 */
public class DataViewPrototypeTest extends BaseTest {

    @Test
    public void testGetBuffer() {
        JSArrayBuffer buffer = new JSArrayBuffer(16);
        JSDataView dataView = new JSDataView(buffer);

        JSValue result = DataViewPrototype.getBuffer(context, dataView, new JSValue[]{});
        assertThat(result).isEqualTo(buffer);

        // Edge case: called on non-DataView
        result = DataViewPrototype.getBuffer(context, new JSString("not a dataview"), new JSValue[]{});
        assertTypeError(result);
        assertPendingException(context);
    }

    @Test
    public void testGetByteLength() {
        JSArrayBuffer buffer = new JSArrayBuffer(16);
        JSDataView dataView = new JSDataView(buffer);

        JSValue result = DataViewPrototype.getByteLength(context, dataView, new JSValue[]{});
        assertThat(result).isInstanceOfSatisfying(JSNumber.class, jsNum -> assertThat(jsNum.value()).isEqualTo(16.0));

        // With offset and length
        JSDataView dataView2 = new JSDataView(buffer, 4, 8);
        result = DataViewPrototype.getByteLength(context, dataView2, new JSValue[]{});
        assertThat(result).isInstanceOfSatisfying(JSNumber.class, jsNum -> assertThat(jsNum.value()).isEqualTo(8.0));

        // Edge case: called on non-DataView
        result = DataViewPrototype.getByteLength(context, new JSNumber(123), new JSValue[]{});
        assertTypeError(result);
        assertPendingException(context);
    }

    @Test
    public void testGetByteOffset() {
        JSArrayBuffer buffer = new JSArrayBuffer(16);
        JSDataView dataView = new JSDataView(buffer);

        JSValue result = DataViewPrototype.getByteOffset(context, dataView, new JSValue[]{});
        assertThat(result).isInstanceOfSatisfying(JSNumber.class, jsNum -> assertThat(jsNum.value()).isEqualTo(0.0));

        // With offset
        JSDataView dataView2 = new JSDataView(buffer, 4, 8);
        result = DataViewPrototype.getByteOffset(context, dataView2, new JSValue[]{});
        assertThat(result).isInstanceOfSatisfying(JSNumber.class, jsNum -> assertThat(jsNum.value()).isEqualTo(4.0));

        // Edge case: called on non-DataView
        result = DataViewPrototype.getByteOffset(context, new JSObject(), new JSValue[]{});
        assertTypeError(result);
        assertPendingException(context);
    }

    @Test
    public void testGetFloat32() {
        JSArrayBuffer buffer = new JSArrayBuffer(16);
        JSDataView dataView = new JSDataView(buffer);

        // Set some test data using DataView
        DataViewPrototype.setFloat32(context, dataView, new JSValue[]{new JSNumber(0), new JSNumber(3.14159f)});

        JSValue result = DataViewPrototype.getFloat32(context, dataView, new JSValue[]{new JSNumber(0)});
        assertThat(result).isInstanceOfSatisfying(JSNumber.class, jsNum -> assertThat(jsNum.value()).isCloseTo(3.14159, Offset.offset(0.00001)));

        // With little-endian flag
        DataViewPrototype.setFloat32(context, dataView, new JSValue[]{new JSNumber(4), new JSNumber(2.718f), JSBoolean.TRUE});
        result = DataViewPrototype.getFloat32(context, dataView, new JSValue[]{new JSNumber(4), JSBoolean.TRUE});
        assertThat(result).isInstanceOfSatisfying(JSNumber.class, jsNum -> assertThat(jsNum.value()).isCloseTo(2.718, Offset.offset(0.00001)));

        // Edge case: called on non-DataView
        result = DataViewPrototype.getFloat32(context, new JSString("not a dataview"), new JSValue[]{new JSNumber(0)});
        assertTypeError(result);
        assertPendingException(context);

        // Edge case: out of bounds
        result = DataViewPrototype.getFloat32(context, dataView, new JSValue[]{new JSNumber(20)});
        assertRangeError(result);
        assertPendingException(context);
    }

    @Test
    public void testGetFloat64() {
        JSArrayBuffer buffer = new JSArrayBuffer(16);
        JSDataView dataView = new JSDataView(buffer);

        // Set some test data using DataView
        DataViewPrototype.setFloat64(context, dataView, new JSValue[]{new JSNumber(0), new JSNumber(2.71828)});

        JSValue result = DataViewPrototype.getFloat64(context, dataView, new JSValue[]{new JSNumber(0)});
        assertThat(result).isInstanceOfSatisfying(JSNumber.class, jsNum -> assertThat(jsNum.value()).isCloseTo(2.71828, Offset.offset(0.00001)));

        // With little-endian flag
        DataViewPrototype.setFloat64(context, dataView, new JSValue[]{new JSNumber(0), new JSNumber(3.14159), JSBoolean.TRUE});
        result = DataViewPrototype.getFloat64(context, dataView, new JSValue[]{new JSNumber(0), JSBoolean.TRUE});
        assertThat(result).isInstanceOfSatisfying(JSNumber.class, jsNum -> assertThat(jsNum.value()).isCloseTo(3.14159, Offset.offset(0.00001)));

        // Edge case: called on non-DataView
        result = DataViewPrototype.getFloat64(context, new JSNumber(123), new JSValue[]{new JSNumber(0)});
        assertTypeError(result);
        assertPendingException(context);
    }

    @Test
    public void testGetInt16() {
        JSArrayBuffer buffer = new JSArrayBuffer(16);
        JSDataView dataView = new JSDataView(buffer);

        // Set some test data using DataView
        DataViewPrototype.setInt16(context, dataView, new JSValue[]{new JSNumber(0), new JSNumber(12345)});

        JSValue result = DataViewPrototype.getInt16(context, dataView, new JSValue[]{new JSNumber(0)});
        assertThat(result).isInstanceOfSatisfying(JSNumber.class, jsNum -> assertThat(jsNum.value()).isEqualTo(12345.0));

        // With little-endian flag
        DataViewPrototype.setInt16(context, dataView, new JSValue[]{new JSNumber(2), new JSNumber(-12345), JSBoolean.TRUE});
        result = DataViewPrototype.getInt16(context, dataView, new JSValue[]{new JSNumber(2), JSBoolean.TRUE});
        assertThat(result).isInstanceOfSatisfying(JSNumber.class, jsNum -> assertThat(jsNum.value()).isEqualTo(-12345.0));

        // Edge case: called on non-DataView
        result = DataViewPrototype.getInt16(context, new JSObject(), new JSValue[]{new JSNumber(0)});
        assertTypeError(result);
        assertPendingException(context);
    }

    @Test
    public void testGetInt32() {
        JSArrayBuffer buffer = new JSArrayBuffer(16);
        JSDataView dataView = new JSDataView(buffer);

        // Set some test data using DataView
        DataViewPrototype.setInt32(context, dataView, new JSValue[]{new JSNumber(0), new JSNumber(123456789)});

        JSValue result = DataViewPrototype.getInt32(context, dataView, new JSValue[]{new JSNumber(0)});
        assertThat(result).isInstanceOfSatisfying(JSNumber.class, jsNum -> assertThat(jsNum.value()).isEqualTo(123456789.0));

        // With little-endian flag
        DataViewPrototype.setInt32(context, dataView, new JSValue[]{new JSNumber(4), new JSNumber(-987654321), JSBoolean.TRUE});
        result = DataViewPrototype.getInt32(context, dataView, new JSValue[]{new JSNumber(4), JSBoolean.TRUE});
        assertThat(result).isInstanceOfSatisfying(JSNumber.class, jsNum -> assertThat(jsNum.value()).isEqualTo(-987654321.0));

        // Edge case: called on non-DataView
        result = DataViewPrototype.getInt32(context, new JSString("not a dataview"), new JSValue[]{new JSNumber(0)});
        assertTypeError(result);
        assertPendingException(context);
    }

    @Test
    public void testGetInt8() {
        JSArrayBuffer buffer = new JSArrayBuffer(16);
        JSDataView dataView = new JSDataView(buffer);

        // Set some test data
        buffer.getBuffer().put(0, (byte) -123);

        JSValue result = DataViewPrototype.getInt8(context, dataView, new JSValue[]{new JSNumber(0)});
        assertThat(result).isInstanceOfSatisfying(JSNumber.class, jsNum -> assertThat(jsNum.value()).isEqualTo(-123.0));

        // Edge case: called on non-DataView
        result = DataViewPrototype.getInt8(context, new JSNumber(456), new JSValue[]{new JSNumber(0)});
        assertTypeError(result);
        assertPendingException(context);
    }

    @Test
    public void testGetUint8() {
        JSArrayBuffer buffer = new JSArrayBuffer(16);
        JSDataView dataView = new JSDataView(buffer);

        // Set some test data
        buffer.getBuffer().put(0, (byte) 200);

        JSValue result = DataViewPrototype.getUint8(context, dataView, new JSValue[]{new JSNumber(0)});
        assertThat(result).isInstanceOfSatisfying(JSNumber.class, jsNum -> assertThat(jsNum.value()).isEqualTo(200.0));

        // Edge case: called on non-DataView
        result = DataViewPrototype.getUint8(context, new JSObject(), new JSValue[]{new JSNumber(0)});
        assertTypeError(result);
        assertPendingException(context);
    }

    @Test
    public void testSetFloat32() {
        JSArrayBuffer buffer = new JSArrayBuffer(16);
        JSDataView dataView = new JSDataView(buffer);

        JSValue result = DataViewPrototype.setFloat32(context, dataView, new JSValue[]{new JSNumber(0), new JSNumber(3.14159f)});
        assertThat(result).isEqualTo(JSUndefined.INSTANCE);

        // Check the value was set by reading it back
        JSValue readResult = DataViewPrototype.getFloat32(context, dataView, new JSValue[]{new JSNumber(0)});
        assertThat(readResult).isInstanceOfSatisfying(JSNumber.class, jsNum -> assertThat(jsNum.value()).isCloseTo(3.14159, Offset.offset(0.00001)));

        // With little-endian flag
        result = DataViewPrototype.setFloat32(context, dataView, new JSValue[]{new JSNumber(4), new JSNumber(2.718f), JSBoolean.TRUE});
        assertThat(result).isEqualTo(JSUndefined.INSTANCE);
        readResult = DataViewPrototype.getFloat32(context, dataView, new JSValue[]{new JSNumber(4), JSBoolean.TRUE});
        assertThat(readResult).isInstanceOfSatisfying(JSNumber.class, jsNum -> assertThat(jsNum.value()).isCloseTo(2.718, Offset.offset(0.00001)));

        // Edge case: called on non-DataView
        result = DataViewPrototype.setFloat32(context, new JSString("not a dataview"), new JSValue[]{new JSNumber(0), new JSNumber(1.0f)});
        assertTypeError(result);
        assertPendingException(context);

        // Edge case: out of bounds
        result = DataViewPrototype.setFloat32(context, dataView, new JSValue[]{new JSNumber(20), new JSNumber(1.0f)});
        assertRangeError(result);
        assertPendingException(context);
    }

    @Test
    public void testSetFloat64() {
        JSArrayBuffer buffer = new JSArrayBuffer(16);
        JSDataView dataView = new JSDataView(buffer);

        JSValue result = DataViewPrototype.setFloat64(context, dataView, new JSValue[]{new JSNumber(0), new JSNumber(2.71828)});
        assertThat(result).isEqualTo(JSUndefined.INSTANCE);

        // Check the value was set by reading it back
        JSValue readResult = DataViewPrototype.getFloat64(context, dataView, new JSValue[]{new JSNumber(0)});
        assertThat(readResult).isInstanceOfSatisfying(JSNumber.class, jsNum -> assertThat(jsNum.value()).isCloseTo(2.71828, Offset.offset(0.00001)));

        // With little-endian flag
        result = DataViewPrototype.setFloat64(context, dataView, new JSValue[]{new JSNumber(0), new JSNumber(3.14159), JSBoolean.TRUE});
        assertThat(result).isEqualTo(JSUndefined.INSTANCE);
        readResult = DataViewPrototype.getFloat64(context, dataView, new JSValue[]{new JSNumber(0), JSBoolean.TRUE});
        assertThat(readResult).isInstanceOfSatisfying(JSNumber.class, jsNum -> assertThat(jsNum.value()).isCloseTo(3.14159, Offset.offset(0.00001)));

        // Edge case: called on non-DataView
        result = DataViewPrototype.setFloat64(context, new JSNumber(123), new JSValue[]{new JSNumber(0), new JSNumber(1.0)});
        assertTypeError(result);
        assertPendingException(context);
    }

    @Test
    public void testSetInt16() {
        JSArrayBuffer buffer = new JSArrayBuffer(16);
        JSDataView dataView = new JSDataView(buffer);

        JSValue result = DataViewPrototype.setInt16(context, dataView, new JSValue[]{new JSNumber(0), new JSNumber(12345)});
        assertThat(result).isEqualTo(JSUndefined.INSTANCE);

        // Check the value was set by reading it back
        JSValue readResult = DataViewPrototype.getInt16(context, dataView, new JSValue[]{new JSNumber(0)});
        assertThat(readResult).isInstanceOfSatisfying(JSNumber.class, jsNum -> assertThat(jsNum.value()).isEqualTo(12345.0));

        // With little-endian flag
        result = DataViewPrototype.setInt16(context, dataView, new JSValue[]{new JSNumber(2), new JSNumber(-12345), JSBoolean.TRUE});
        assertThat(result).isEqualTo(JSUndefined.INSTANCE);
        readResult = DataViewPrototype.getInt16(context, dataView, new JSValue[]{new JSNumber(2), JSBoolean.TRUE});
        assertThat(readResult).isInstanceOfSatisfying(JSNumber.class, jsNum -> assertThat(jsNum.value()).isEqualTo(-12345.0));

        // Edge case: called on non-DataView
        result = DataViewPrototype.setInt16(context, new JSObject(), new JSValue[]{new JSNumber(0), new JSNumber(123)});
        assertTypeError(result);
        assertPendingException(context);
    }

    @Test
    public void testSetInt32() {
        JSArrayBuffer buffer = new JSArrayBuffer(16);
        JSDataView dataView = new JSDataView(buffer);

        JSValue result = DataViewPrototype.setInt32(context, dataView, new JSValue[]{new JSNumber(0), new JSNumber(123456789)});
        assertThat(result).isEqualTo(JSUndefined.INSTANCE);

        // Check the value was set by reading it back
        JSValue readResult = DataViewPrototype.getInt32(context, dataView, new JSValue[]{new JSNumber(0)});
        assertThat(readResult).isInstanceOfSatisfying(JSNumber.class, jsNum -> assertThat(jsNum.value()).isEqualTo(123456789.0));

        // With little-endian flag
        result = DataViewPrototype.setInt32(context, dataView, new JSValue[]{new JSNumber(4), new JSNumber(-987654321), JSBoolean.TRUE});
        assertThat(result).isEqualTo(JSUndefined.INSTANCE);
        readResult = DataViewPrototype.getInt32(context, dataView, new JSValue[]{new JSNumber(4), JSBoolean.TRUE});
        assertThat(readResult).isInstanceOfSatisfying(JSNumber.class, jsNum -> assertThat(jsNum.value()).isEqualTo(-987654321.0));

        // Edge case: called on non-DataView
        result = DataViewPrototype.setInt32(context, new JSString("not a dataview"), new JSValue[]{new JSNumber(0), new JSNumber(123)});
        assertTypeError(result);
        assertPendingException(context);
    }

    @Test
    public void testSetInt8() {
        JSArrayBuffer buffer = new JSArrayBuffer(16);
        JSDataView dataView = new JSDataView(buffer);

        JSValue result = DataViewPrototype.setInt8(context, dataView, new JSValue[]{new JSNumber(0), new JSNumber(-123)});
        assertThat(result).isEqualTo(JSUndefined.INSTANCE);

        // Check the value was set
        byte value = buffer.getBuffer().get(0);
        assertThat(value).isEqualTo((byte) -123);

        // Edge case: called on non-DataView
        result = DataViewPrototype.setInt8(context, new JSNumber(456), new JSValue[]{new JSNumber(0), new JSNumber(123)});
        assertTypeError(result);
        assertPendingException(context);
    }

    @Test
    public void testSetUint8() {
        JSArrayBuffer buffer = new JSArrayBuffer(16);
        JSDataView dataView = new JSDataView(buffer);

        JSValue result = DataViewPrototype.setUint8(context, dataView, new JSValue[]{new JSNumber(0), new JSNumber(200)});
        assertThat(result).isEqualTo(JSUndefined.INSTANCE);

        // Check the value was set
        byte value = buffer.getBuffer().get(0);
        assertThat(value).isEqualTo((byte) 200);

        // Edge case: called on non-DataView
        result = DataViewPrototype.setUint8(context, new JSObject(), new JSValue[]{new JSNumber(0), new JSNumber(123)});
        assertTypeError(result);
        assertPendingException(context);
    }
}