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

package com.caoccao.qjs4j.vm;

import com.caoccao.qjs4j.core.JSFunction;
import com.caoccao.qjs4j.core.JSValue;

/**
 * Represents a call frame (activation record) on the call stack.
 */
public final class StackFrame {
    private final StackFrame caller;
    private final JSValue[] closureVars;
    private final JSFunction function;
    private final JSValue[] locals;
    private final JSValue thisArg;
    private int programCounter;

    public StackFrame(JSFunction function, JSValue thisArg, JSValue[] args, StackFrame caller) {
        this.function = function;
        this.thisArg = thisArg;
        this.locals = args;
        this.closureVars = new JSValue[0];
        this.programCounter = 0;
        this.caller = caller;
    }

    public StackFrame getCaller() {
        return caller;
    }

    public JSFunction getFunction() {
        return function;
    }

    public JSValue[] getLocals() {
        return locals;
    }

    public int getProgramCounter() {
        return programCounter;
    }

    public JSValue getThisArg() {
        return thisArg;
    }

    public void setProgramCounter(int pc) {
        this.programCounter = pc;
    }
}
