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

import java.util.WeakHashMap;

/**
 * Represents a JavaScript WeakMap object.
 * Keys must be objects and are weakly referenced.
 * WeakMaps are not enumerable.
 */
public final class JSWeakMap extends JSObject {
    // Use WeakHashMap for automatic garbage collection of keys
    // Keys are compared by identity (reference equality)
    private final WeakHashMap<JSObject, JSValue> data;

    /**
     * Create an empty WeakMap.
     */
    public JSWeakMap() {
        super();
        this.data = new WeakHashMap<>();
    }

    /**
     * Set a key-value pair in the WeakMap.
     * Key must be an object.
     */
    public void weakMapSet(JSObject key, JSValue value) {
        data.put(key, value);
    }

    /**
     * Get a value from the WeakMap by key.
     */
    public JSValue weakMapGet(JSObject key) {
        JSValue value = data.get(key);
        return value != null ? value : JSUndefined.INSTANCE;
    }

    /**
     * Check if the WeakMap has a key.
     */
    public boolean weakMapHas(JSObject key) {
        return data.containsKey(key);
    }

    /**
     * Delete a key from the WeakMap.
     */
    public boolean weakMapDelete(JSObject key) {
        return data.remove(key) != null;
    }

    @Override
    public String toString() {
        return "[object WeakMap]";
    }
}
