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

/** The ECMAScript Call operation, including callable proxies. */
public final class JSCall {
    private JSCall() {
    }

    public static JSValue call(JSContext context, JSValue callable, JSValue thisArg, JSValue[] args) {
        if (callable instanceof JSProxy proxy) {
            return proxy.apply(context, thisArg, args);
        }
        if (callable instanceof JSFunction function) {
            return function.call(context, thisArg, args);
        }
        return context.throwTypeError("Value is not callable");
    }
}
