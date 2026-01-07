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

import java.util.Objects;

/**
 * Represents a property key (can be string, symbol, or integer index).
 * Based on ECMAScript property keys and QuickJS atom system.
 * <p>
 * In JavaScript, property keys can be:
 * - Strings (most common)
 * - Symbols (for unique properties)
 * - Integer indices (for array-like objects)
 */
public final class PropertyKey {
    private final int atomIndex; // -1 if not interned
    private final Object value; // String, Integer, or JSSymbol

    private PropertyKey(Object value, int atomIndex) {
        this.value = value;
        this.atomIndex = atomIndex;
    }

    /**
     * Create a property key from an interned string (atom).
     */
    public static PropertyKey fromAtom(String str, int atomIndex) {
        return new PropertyKey(str, atomIndex);
    }

    /**
     * Create a property key from an integer index.
     */
    public static PropertyKey fromIndex(int index) {
        return new PropertyKey(index, -1);
    }

    /**
     * Create a property key from a string.
     */
    public static PropertyKey fromString(String str) {
        return new PropertyKey(str, -1);
    }

    /**
     * Create a property key from a symbol.
     */
    public static PropertyKey fromSymbol(JSSymbol symbol) {
        return new PropertyKey(symbol, -1);
    }

    /**
     * Create a property key from a JSValue.
     * Converts the value to an appropriate key.
     */
    public static PropertyKey fromValue(JSContext context, JSValue value) {
        

        PropertyKey result = null;
        if (value instanceof JSString s) {
            // Treat numeric strings as array indices as well (e.g. "0" -> index 0)
            String str = s.value();
            if (str != null && !str.isEmpty()) {
                boolean isDigits = true;
                for (int i = 0; i < str.length(); ++i) {
                    char ch = str.charAt(i);
                    if (ch < '0' || ch > '9') {
                        isDigits = false;
                        break;
                    }
                }
                if (isDigits) {
                    try {
                        long v = Long.parseLong(str);
                        if (v >= 0 && v <= 0xFFFFFFFFL) {
                            result = fromIndex((int) v);
                        }
                    } catch (NumberFormatException ignored) {
                        // fall back to string
                    }
                }
            }
            if (result == null) result = fromString(s.value());
        } else if (value instanceof JSSymbol s) {
            result = fromSymbol(s);
        } else if (value instanceof JSNumber n) {
            // Check if it's an array index
            double d = n.value();
            if (d >= 0 && d < 0xFFFFFFFFL && d == Math.floor(d)) {
                result = fromIndex((int) d);
            }
        }

        if (result == null) {
            // Convert to string for other types
            JSString str = JSTypeConversions.toString(context, value);
            result = fromString(str.value());
        }

        

        return result;
    }

    /**
     * Get the integer value (if this is an index key).
     */
    public int asIndex() {
        return value instanceof Integer i ? i : -1;
    }

    /**
     * Get the string value (if this is a string key).
     */
    public String asString() {
        return value instanceof String s ? s : null;
    }

    /**
     * Get the symbol value (if this is a symbol key).
     */
    public JSSymbol asSymbol() {
        return value instanceof JSSymbol s ? s : null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PropertyKey other)) return false;

        // Fast path: if both have atom indices, compare them
        if (atomIndex >= 0 && other.atomIndex >= 0) {
            return atomIndex == other.atomIndex;
        }

        // Compare values
        return Objects.equals(value, other.value);
    }

    public int getAtomIndex() {
        return atomIndex;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        // Use atom index for faster hashing if available
        if (atomIndex >= 0) {
            return atomIndex;
        }
        return Objects.hashCode(value);
    }

    /**
     * Check if this key is an integer index.
     */
    public boolean isIndex() {
        return value instanceof Integer;
    }

    /**
     * Check if this key is interned.
     */
    public boolean isInterned() {
        return atomIndex >= 0;
    }

    /**
     * Check if this key is a string.
     */
    public boolean isString() {
        return value instanceof String;
    }

    /**
     * Check if this key is a symbol.
     */
    public boolean isSymbol() {
        return value instanceof JSSymbol;
    }

    /**
     * Convert to a string representation.
     */
    public String toPropertyString() {
        if (value instanceof String s) {
            return s;
        }
        if (value instanceof Integer i) {
            return i.toString();
        }
        if (value instanceof JSSymbol s) {
            return s.toJavaObject();
        }
        return value.toString();
    }

    @Override
    public String toString() {
        return "PropertyKey{" + toPropertyString() +
                (atomIndex >= 0 ? ", atom=" + atomIndex : "") +
                "}";
    }
}
