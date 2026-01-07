package com.caoccao.qjs4j.builtins;

import com.caoccao.qjs4j.core.JSContext;
import com.caoccao.qjs4j.core.JSObject;
import com.caoccao.qjs4j.core.JSRuntime;
import com.caoccao.qjs4j.core.JSValue;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PrototypeChainJavaTest {
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
    public void checkFunctionPrototypeLinks() {
        JSObject global = context.getGlobalObject();
        JSValue objCtor = global.get("Object");
        JSValue funcCtor = global.get("Function");

        assertNotNull(objCtor, "Object constructor must exist");
        assertNotNull(funcCtor, "Function constructor must exist");

        JSValue objProto = ((JSObject) objCtor).get("prototype");
        JSValue funcProto = ((JSObject) funcCtor).get("prototype");

        assertTrue(objProto instanceof JSObject, "Object.prototype should be JSObject");
        assertTrue(funcProto instanceof JSObject, "Function.prototype should be JSObject");

        JSObject objProtoObj = (JSObject) objProto;
        JSObject funcProtoObj = (JSObject) funcProto;

        // Diagnostic output to understand prototype links
        System.out.println("objProtoObj = " + objProtoObj);
        System.out.println("funcProtoObj = " + funcProtoObj);
        System.out.println("funcProtoObj.getPrototype() = " + funcProtoObj.getPrototype());

        // Check that Function.prototype's internal prototype points to Object.prototype
        assertSame(objProtoObj, funcProtoObj.getPrototype(), "Function.prototype.__proto__ should be Object.prototype");
    }
}
