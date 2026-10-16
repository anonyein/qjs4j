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

package com.caoccao.qjs4j.builtins;

import com.caoccao.qjs4j.core.*;
import java.util.Comparator;

/** Stable sorting that accepts arbitrary JavaScript comparators and stops on abrupt completion. */
final class SortOperations {
    private SortOperations() {
    }

    static int compareArrayElements(JSContext context, JSValue comparator, JSValue left, JSValue right) {
        if (left instanceof JSUndefined) {
            return right instanceof JSUndefined ? 0 : 1;
        }
        if (right instanceof JSUndefined) {
            return -1;
        }
        if (!(comparator instanceof JSUndefined)) {
            return compareWithCallback(context, comparator, left, right);
        }
        String leftString = JSTypeConversions.toString(context, left).value();
        if (context.hasPendingException()) {
            return 0;
        }
        String rightString = JSTypeConversions.toString(context, right).value();
        return leftString.compareTo(rightString);
    }

    static int compareWithCallback(JSContext context, JSValue comparator, JSValue left, JSValue right) {
        JSValue result = JSCall.call(context, comparator, JSUndefined.INSTANCE, new JSValue[]{left, right});
        if (context.hasPendingException()) {
            return 0;
        }
        double number = JSTypeConversions.toNumber(context, result).value();
        return Double.isNaN(number) || number == 0 ? 0 : number < 0 ? -1 : 1;
    }

    /** Bottom-up merge sort: bounded progress even when the comparison relation is inconsistent. */
    static boolean stableSort(JSContext context, JSValue[] values, Comparator<JSValue> comparator) {
        int length = values.length;
        if (length < 2) {
            return true;
        }
        JSValue[] source = values;
        JSValue[] destination = new JSValue[length];
        for (long width = 1; width < length; width *= 2) {
            for (long start = 0; start < length; start += width * 2) {
                int left = (int) start;
                int middle = (int) Math.min(start + width, length);
                int right = middle;
                int end = (int) Math.min(start + width * 2, length);
                for (int output = left; output < end; output++) {
                    context.getVirtualMachine().checkExecutionInterruptForNative();
                    boolean takeLeft = right == end;
                    if (!takeLeft && left < middle) {
                        takeLeft = comparator.compare(source[left], source[right]) <= 0;
                        if (context.hasPendingException()) {
                            return false;
                        }
                    }
                    destination[output] = takeLeft ? source[left++] : source[right++];
                }
            }
            JSValue[] temporary = source;
            source = destination;
            destination = temporary;
        }
        if (source != values) {
            System.arraycopy(source, 0, values, 0, length);
        }
        return true;
    }
}
