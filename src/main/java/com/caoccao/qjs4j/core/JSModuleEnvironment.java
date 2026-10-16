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

import java.util.HashMap;
import java.util.Map;

/** Persistent indirect import bindings, inherited by closures without changing the global object. */
public final class JSModuleEnvironment {
    private final JSObject imports;
    private final Map<String, JSValue> privateBindings;

    JSModuleEnvironment(JSContext context) {
        this(new JSObject(context), new HashMap<>());
    }

    private JSModuleEnvironment(JSObject imports, Map<String, JSValue> privateBindings) {
        this.imports = imports;
        this.privateBindings = privateBindings;
    }

    JSModuleEnvironment forDirectEval() {
        return new JSModuleEnvironment(imports, Map.of());
    }

    public JSValue get(String name) {
        JSValue privateValue = privateBindings.get(name);
        return privateValue == null ? imports.get(PropertyKey.fromString(name)) : privateValue;
    }

    public boolean has(String name) {
        return privateBindings.containsKey(name) || imports.hasOwnProperty(PropertyKey.fromString(name));
    }

    public JSObject imports() {
        return imports;
    }

    void putPrivate(String name, JSValue value) {
        privateBindings.put(name, value);
    }
}
