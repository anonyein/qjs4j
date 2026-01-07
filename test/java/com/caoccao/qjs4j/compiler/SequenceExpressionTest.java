package com.caoccao.qjs4j.compiler;

import com.caoccao.qjs4j.core.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SequenceExpressionTest {

    @Test
    public void testSequenceExpressionInAssignment() {
        try (JSContext context = new JSContext(new JSRuntime())) {
            JSValue v = context.eval("let x = (1 + 1, 2 + 2, 3 + 3); x");
            assertThat(v).isInstanceOf(JSNumber.class);
            assertThat(((JSNumber) v).getValue()).isEqualTo(6.0);
        }
    }

    @Test
    public void testCommaInForHead() {
        try (JSContext context = new JSContext(new JSRuntime())) {
            JSValue v = context.eval(
                    "var res = 0; for (var i = 1, j = 1; j < 10; i++, j++) { res += i + j; } res;"
            );
            assertThat(v).isInstanceOf(JSNumber.class);
            // Compute expected: sum over j=1..9 of (i+j) where i starts at1 and increments with j -> pairs (1+1)+(2+2)+...+(9+9) = 2*(1+2+...+9)
            double expected = 2 * (9.0 * 10.0 / 2.0); // 2 * 45 = 90
            assertThat(((JSNumber) v).getValue()).isEqualTo(expected);
        }
    }
}
