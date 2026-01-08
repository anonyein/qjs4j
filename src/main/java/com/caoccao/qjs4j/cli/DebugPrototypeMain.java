package com.caoccao.qjs4j.cli;

import com.caoccao.qjs4j.core.JSContext;
import com.caoccao.qjs4j.core.JSObject;
import com.caoccao.qjs4j.core.JSRuntime;
import com.caoccao.qjs4j.core.JSValue;

public class DebugPrototypeMain {
    public static void main(String[] args) {
        JSRuntime runtime = new JSRuntime();
        JSContext context = runtime.createContext();

        if (args.length == 0) {
            // Run a small snippet to validate WeakMap chaining behavior
            String code = "const wm = new WeakMap();\nconst k1 = {}, k2 = {};\nconst result = wm.set(k1,1).set(k2,2);\n[ result === wm, typeof result ];";
            JSValue value1 = context.eval(code);
            JSValue value2 = context.eval("'use strict';\n" + code);
            System.out.println("Eval (non-strict): " + value1);
            System.out.println("Eval (strict): " + value2);
        } else {
            JSObject global = context.getGlobalObject();
            JSValue objCtor = global.get("Object");
            JSValue funcCtor = global.get("Function");

            System.out.println("Object constructor class: " + (objCtor == null ? "null" : objCtor.getClass()));
            System.out.println("Function constructor class: " + (funcCtor == null ? "null" : funcCtor.getClass()));

            JSValue objProto = ((JSObject) objCtor).get("prototype");
            JSValue funcProto = ((JSObject) funcCtor).get("prototype");

            System.out.println("Object.prototype: " + objProto);
            System.out.println("Function.prototype: " + funcProto);
            System.out.println("Function.prototype.__proto__: " + ((JSObject) funcProto).getPrototype());
        }

        context.close();
        runtime.close();
    }
}
