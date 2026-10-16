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
 * The identity of one direct eval's lexical environment. The eval code carries it, every function created inside the
 * eval inherits it, and a direct eval nested in that code gets an environment chained to it. The overlay a direct eval
 * installs on the global object is only visible to code whose environment is the eval's own or nested within it: a
 * function the eval merely calls, or one an earlier eval created, resolves its free names on its own.
 */
public final class JSEvalEnvironment {
    private final JSEvalEnvironment parent;

    public JSEvalEnvironment(JSEvalEnvironment parent) {
        this.parent = parent;
    }

    /** Whether code in this environment sees the bindings of {@code other}: it is that environment or nested in it. */
    public boolean isWithin(JSEvalEnvironment other) {
        for (JSEvalEnvironment environment = this; environment != null; environment = environment.parent) {
            if (environment == other) {
                return true;
            }
        }
        return false;
    }
}
