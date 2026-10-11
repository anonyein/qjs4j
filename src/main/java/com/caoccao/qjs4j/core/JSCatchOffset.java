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

package com.caoccao.qjs4j.core;

/**
 * Special marker value pushed on stack by CATCH opcode. Contains the PC offset to the catch/finally handler. When an
 * exception occurs, the VM unwinds the stack looking for this marker, then jumps to the handler.
 * <p>
 * The {@code isFinally} flag distinguishes try-catch from try-finally handlers. During generator return unwinding,
 * catch handlers are skipped while finally handlers are entered (to execute finally blocks before completing the
 * return).
 * <p>
 * This is an internal VM type, not a JavaScript value.
 */
public record JSCatchOffset(int offset, boolean isFinally, boolean isIteratorCloseMarker) implements JSStackValue {
    /**
     * The marker of a {@code for await} loop. {@code ITERATOR_CLOSE} only removes such a loop's iterator: the compiler
     * calls its {@code return()} and awaits the result itself, as AsyncIteratorClose requires.
     */
    public static final JSCatchOffset ASYNC_ITERATOR_CLOSE_MARKER = new JSCatchOffset(1, false, true);
    public static final JSCatchOffset ITERATOR_CLOSE_MARKER = new JSCatchOffset(0, false, true);

    public JSCatchOffset(int offset, boolean isFinally) {
        this(offset, isFinally, false);
    }

    /**
     * Whether this marks a {@code for await} loop's iterator.
     *
     * @return true for {@link #ASYNC_ITERATOR_CLOSE_MARKER}
     */
    public boolean isAsyncIteratorCloseMarker() {
        return equals(ASYNC_ITERATOR_CLOSE_MARKER);
    }

    @Override
    public String toString() {
        return "[CatchOffset:" + offset + (isFinally ? ",finally" : "")
                + (isIteratorCloseMarker ? ",iteratorClose" : "") + "]";
    }
}
