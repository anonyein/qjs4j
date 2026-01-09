package com.caoccao.qjs4j.examples;

import com.caoccao.qjs4j.BaseTest;
import com.caoccao.qjs4j.core.JSArray;
import com.caoccao.qjs4j.core.JSNumber;
import com.caoccao.qjs4j.core.JSValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DebugAssignTest extends BaseTest {
    @Test
    void testSimpleComputedAssignment() {
        String code = """
            var BI_RC = new Array();
            var rr = "0".charCodeAt(0);
            for (var vv = 0; vv <= 2; ++vv) { var idx = rr++; BI_RC[idx] = vv; console.log('DBG_ASSIGN', idx, vv, typeof BI_RC[idx]); }
            BI_RC
            """;

        JSValue value = context.eval(code);
        assertThat(value).isInstanceOf(JSArray.class);
        JSArray arr = (JSArray) value;
        assertThat(arr.get(48)).isInstanceOfSatisfying(JSNumber.class, n -> assertThat(n.value()).isEqualTo(0.0));
        assertThat(arr.get(49)).isInstanceOfSatisfying(JSNumber.class, n -> assertThat(n.value()).isEqualTo(1.0));
        assertThat(arr.get(50)).isInstanceOfSatisfying(JSNumber.class, n -> assertThat(n.value()).isEqualTo(2.0));
    }
}
