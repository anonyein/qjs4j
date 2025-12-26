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

import java.util.LinkedHashSet;

/**
 * Represents a JavaScript Set object.
 * Sets maintain insertion order and use SameValueZero equality for values.
 */
public final class JSSet extends JSObject {
    // Use LinkedHashSet to maintain insertion order
    // Use KeyWrapper from JSMap for consistent SameValueZero equality
    private final LinkedHashSet<JSMap.KeyWrapper> data;

    /**
     * Create an empty Set.
     */
    public JSSet() {
        super();
        this.data = new LinkedHashSet<>();
    }

    /**
     * Get the number of values in the Set.
     */
    public int size() {
        return data.size();
    }

    /**
     * Add a value to the Set.
     */
    public void setAdd(JSValue value) {
        data.add(new JSMap.KeyWrapper(value));
    }

    /**
     * Check if the Set has a value.
     */
    public boolean setHas(JSValue value) {
        return data.contains(new JSMap.KeyWrapper(value));
    }

    /**
     * Delete a value from the Set.
     */
    public boolean setDelete(JSValue value) {
        return data.remove(new JSMap.KeyWrapper(value));
    }

    /**
     * Clear all values from the Set.
     */
    public void setClear() {
        data.clear();
    }

    /**
     * Get all values as an iterable.
     */
    public Iterable<JSMap.KeyWrapper> values() {
        return data;
    }

    @Override
    public String toString() {
        return "[object Set]";
    }
}
