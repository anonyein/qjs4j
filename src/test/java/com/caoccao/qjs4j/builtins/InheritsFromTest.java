package com.caoccao.qjs4j.builtins;

import com.caoccao.qjs4j.core.JSContext;
import com.caoccao.qjs4j.core.JSRuntime;
import com.caoccao.qjs4j.exceptions.JSException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InheritsFromTest {
    private static JSRuntime runtime;
    private static JSContext context;

    @BeforeAll
    public static void setup() {
        runtime = new JSRuntime();
        context = runtime.createContext();
    }

    @AfterAll
    public static void teardown() {
        if (context != null) context.close();
        if (runtime != null) runtime.close();
    }

    @Test
    public void testInheritsFrom() {
        String code = "Object.prototype.inheritsFrom = function (shuper) {\n"
                + "  try {\n"
                + "    function Inheriter() { }\n"
                + "    Inheriter.prototype = shuper.prototype;\n"
                + "    this.prototype = new Inheriter();\n"
                + "    this.superConstructor = shuper;\n"
                + "  } catch (error) {\n"
                + "    console.error('Inheritance error: ' + error + error.stack);\n"
                + "  }\n"
                + "}\n"
                + "function UnaryConstraint(v, strength) {\n"
                + "  try {\n"
                + "    UnaryConstraint.superConstructor.call(this, strength);\n"
                + "    this.myOutput = v;\n"
                + "    this.satisfied = false;\n"
                + "  } catch (error) {\n"
                + "    console.error('Inheritance error: ' + error + error.stack);\n"
                + "  }\n"
                + "}\n"
                + "function Constraint(strength) {\n"
                + "  this.strength = strength;\n"
                + "}\n"
                + "UnaryConstraint.inheritsFrom(Constraint);\n"
                + "var result = typeof UnaryConstraint.inheritsFrom === 'function' && UnaryConstraint.superConstructor === Constraint;\n"
                + "result;";

        try {
            Object evalResult = context.eval(code).toJavaObject();
            assertTrue(Boolean.TRUE.equals(evalResult) || "true".equals(String.valueOf(evalResult)), "Inheritance should be established");
        } catch (JSException e) {
            fail("JSException thrown: " + e.getMessage());
        }
    }
}
