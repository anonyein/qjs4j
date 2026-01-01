package com.caoccao.qjs4j.core;

import com.caoccao.qjs4j.BaseJavetTest;
import org.junit.jupiter.api.Test;

public class JSErrorTest extends BaseJavetTest {
    @Test
    public void testInstanceof() {
        assertBooleanWithJavet(
                "new Error('Error') instanceof Error",
                "new EvalError('EvalError') instanceof EvalError",
                "new Error('Error') instanceof Error",
                "new RangeError('RangeError') instanceof RangeError",
                "new ReferenceError('ReferenceError') instanceof ReferenceError",
                "new SyntaxError('SyntaxError') instanceof SyntaxError",
                "new TypeError('TypeError') instanceof TypeError",
                "new URIError('URIError') instanceof URIError",
                "new Error('Error') instanceof Error",
                "new AggregateError('AggregateError') instanceof AggregateError");
    }

    @Test
    public void testTryCatchTypeError() {
        assertStringWithJavet("try { throw new TypeError('I am a TypeError'); } catch (e) { e.name + ': ' + e.message; }");
    }
}
