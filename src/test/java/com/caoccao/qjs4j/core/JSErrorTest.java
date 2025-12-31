package com.caoccao.qjs4j.core;

import com.caoccao.qjs4j.BaseJavetTest;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class JSErrorTest extends BaseJavetTest {
    @Test
    public void testInstanceof() {
        Stream.of(
                "new Error('Error') instanceof Error",
                "new EvalError('EvalError') instanceof EvalError",
                "new Error('Error') instanceof Error",
                "new RangeError('RangeError') instanceof RangeError",
                "new ReferenceError('ReferenceError') instanceof ReferenceError",
                "new SyntaxError('SyntaxError') instanceof SyntaxError",
                "new TypeError('TypeError') instanceof TypeError",
                "new URIError('URIError') instanceof URIError",
                "new Error('Error') instanceof Error",
                "new AggregateError('AggregateError') instanceof AggregateError").forEach(code -> assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeBoolean(),
                () -> context.eval(code).toJavaObject()));
    }

    @Test
    public void testTryCatchTypeError() {
        JSValue result = context.eval("try { throw new TypeError('I am a TypeError'); } catch (e) { e.name + ': ' + e.message; }");
        assertThat(result).isInstanceOfSatisfying(JSString.class, jsString -> {
            assertThat(jsString.value()).isEqualTo("TypeError: I am a TypeError");
        });
    }
}
