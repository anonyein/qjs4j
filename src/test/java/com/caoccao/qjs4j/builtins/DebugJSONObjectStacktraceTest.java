package com.caoccao.qjs4j.builtins;

import com.caoccao.qjs4j.BaseTest;
import org.junit.jupiter.api.Test;

public class DebugJSONObjectStacktraceTest extends BaseTest {
    @Test
    public void dumpStackOnStringifyCircularArray() {
        String code = "var arr = [1,2]; arr.push(arr); JSON.stringify(arr)";
        try {
            resetContext().eval(code);
            // If no exception thrown, fail the test
            org.junit.jupiter.api.Assertions.fail("Expected JSON.stringify to throw for circular structure");
        } catch (Throwable t) {
            // Expected: JSON.stringify on circular structures throws. Print stack for debug.
            t.printStackTrace(System.out);
        }
    }
}
