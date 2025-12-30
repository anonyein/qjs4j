package com.caoccao.qjs4j;

import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.interfaces.IJavetSupplier;
import com.caoccao.javet.interop.V8Host;
import com.caoccao.javet.interop.V8Runtime;
import com.caoccao.qjs4j.exceptions.JSException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.*;

public class BaseJavetTest extends BaseTest {
    protected V8Runtime v8Runtime;

    protected void assertErrorWithJavet(String code, String expectedMessage) {
        assertThatThrownBy(() -> v8Runtime.getExecutor(code).executeVoid())
                .isInstanceOf(JavetException.class)
                .hasMessageContaining(expectedMessage);
        assertThatThrownBy(() -> context.eval(code))
                .isInstanceOf(JSException.class)
                .hasMessageContaining(expectedMessage);
    }

    protected <E extends Exception, T> void assertWithJavet(
            IJavetSupplier<T, E> javetSupplier,
            Supplier<T> qjs4jSupplier) {
        try {
            T expectedResult = javetSupplier.get();
            T result = qjs4jSupplier.get();
            assertThat(result).isEqualTo(expectedResult);
        } catch (Throwable t) {
            fail(t);
        }
    }

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        v8Runtime = V8Host.getV8Instance().createV8Runtime();
    }

    @AfterEach
    @Override
    public void tearDown() throws Exception {
        assertThat(v8Runtime.getReferenceCount()).as("V8 runtime reference count").isEqualTo(0);
        v8Runtime.close();
        super.tearDown();
    }
}
