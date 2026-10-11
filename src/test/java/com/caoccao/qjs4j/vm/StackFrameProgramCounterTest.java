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

import com.caoccao.qjs4j.BaseTest;
import com.caoccao.qjs4j.core.JSBytecodeFunction;
import com.caoccao.qjs4j.core.JSUndefined;
import com.caoccao.qjs4j.core.JSValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A frame reports the program counter of the instruction it is executing, which is what a stack trace maps back to a
 * source position.
 */
public class StackFrameProgramCounterTest extends BaseTest {
    @Test
    public void testAFrameNotYetRunningReportsItsOwnCounter() {
        JSBytecodeFunction function = (JSBytecodeFunction) context.eval("(function f() {})");
        StackFrame frame = new StackFrame(function, JSUndefined.INSTANCE, JSValue.NO_ARGS, 0, null,
                JSUndefined.INSTANCE, 0);
        assertThat(frame.getProgramCounter()).isZero();
        frame.setProgramCounter(7);
        assertThat(frame.getProgramCounter()).isEqualTo(7);
    }

    @Test
    public void testARunningFrameReportsItsActivationsCounter() {
        JSBytecodeFunction function = (JSBytecodeFunction) context.eval("(function f() {})");
        StackFrame frame = new StackFrame(function, JSUndefined.INSTANCE, JSValue.NO_ARGS, 0, null,
                JSUndefined.INSTANCE, 0);
        ExecutionContext executionContext = new ExecutionContext(context.getVirtualMachine(), function.getBytecode(),
                frame, null, 0, 0, false);
        executionContext.pc = 3;
        assertThat(frame.getProgramCounter()).isEqualTo(3);
    }
}
