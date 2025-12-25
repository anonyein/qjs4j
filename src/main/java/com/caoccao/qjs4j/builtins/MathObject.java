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

/**
 * Implementation of JavaScript Math object.
 * Based on ES2020 Math object specification.
 */
public final class MathObject {

    // Math constants
    public static final double E = Math.E;           // 2.718281828459045
    public static final double LN10 = Math.log(10);  // 2.302585092994046
    public static final double LN2 = Math.log(2);    // 0.6931471805599453
    public static final double LOG10E = Math.log10(Math.E);  // 0.4342944819032518
    public static final double LOG2E = 1.0 / Math.log(2);    // 1.4426950408889634
    public static final double PI = Math.PI;          // 3.141592653589793
    public static final double SQRT1_2 = Math.sqrt(0.5);     // 0.7071067811865476
    public static final double SQRT2 = Math.sqrt(2); // 1.4142135623730951

    /**
     * Math.abs(x)
     * ES2020 20.2.2.1
     */
    public static JSValue abs(JSContext ctx, JSValue thisArg, JSValue[] args) {
        if (args.length == 0) {
            return new JSNumber(Double.NaN);
        }
        double x = JSTypeConversions.toNumber(args[0]).value();
        return new JSNumber(Math.abs(x));
    }

    /**
     * Math.acos(x)
     * ES2020 20.2.2.2
     */
    public static JSValue acos(JSContext ctx, JSValue thisArg, JSValue[] args) {
        if (args.length == 0) {
            return new JSNumber(Double.NaN);
        }
        double x = JSTypeConversions.toNumber(args[0]).value();
        return new JSNumber(Math.acos(x));
    }

    /**
     * Math.acosh(x)
     * ES2020 20.2.2.3
     */
    public static JSValue acosh(JSContext ctx, JSValue thisArg, JSValue[] args) {
        if (args.length == 0) {
            return new JSNumber(Double.NaN);
        }
        double x = JSTypeConversions.toNumber(args[0]).value();
        // acosh(x) = ln(x + sqrt(x*x - 1))
        return new JSNumber(Math.log(x + Math.sqrt(x * x - 1)));
    }

    /**
     * Math.asin(x)
     * ES2020 20.2.2.4
     */
    public static JSValue asin(JSContext ctx, JSValue thisArg, JSValue[] args) {
        if (args.length == 0) {
            return new JSNumber(Double.NaN);
        }
        double x = JSTypeConversions.toNumber(args[0]).value();
        return new JSNumber(Math.asin(x));
    }

    /**
     * Math.asinh(x)
     * ES2020 20.2.2.5
     */
    public static JSValue asinh(JSContext ctx, JSValue thisArg, JSValue[] args) {
        if (args.length == 0) {
            return new JSNumber(Double.NaN);
        }
        double x = JSTypeConversions.toNumber(args[0]).value();
        // asinh(x) = ln(x + sqrt(x*x + 1))
        return new JSNumber(Math.log(x + Math.sqrt(x * x + 1)));
    }

    /**
     * Math.atan(x)
     * ES2020 20.2.2.6
     */
    public static JSValue atan(JSContext ctx, JSValue thisArg, JSValue[] args) {
        if (args.length == 0) {
            return new JSNumber(Double.NaN);
        }
        double x = JSTypeConversions.toNumber(args[0]).value();
        return new JSNumber(Math.atan(x));
    }

    /**
     * Math.atanh(x)
     * ES2020 20.2.2.7
     */
    public static JSValue atanh(JSContext ctx, JSValue thisArg, JSValue[] args) {
        if (args.length == 0) {
            return new JSNumber(Double.NaN);
        }
        double x = JSTypeConversions.toNumber(args[0]).value();
        // atanh(x) = 0.5 * ln((1+x)/(1-x))
        return new JSNumber(0.5 * Math.log((1 + x) / (1 - x)));
    }

    /**
     * Math.atan2(y, x)
     * ES2020 20.2.2.8
     */
    public static JSValue atan2(JSContext ctx, JSValue thisArg, JSValue[] args) {
        double y = args.length > 0 ? JSTypeConversions.toNumber(args[0]).value() : Double.NaN;
        double x = args.length > 1 ? JSTypeConversions.toNumber(args[1]).value() : Double.NaN;
        return new JSNumber(Math.atan2(y, x));
    }

    /**
     * Math.cbrt(x)
     * ES2020 20.2.2.9
     */
    public static JSValue cbrt(JSContext ctx, JSValue thisArg, JSValue[] args) {
        if (args.length == 0) {
            return new JSNumber(Double.NaN);
        }
        double x = JSTypeConversions.toNumber(args[0]).value();
        return new JSNumber(Math.cbrt(x));
    }

    /**
     * Math.ceil(x)
     * ES2020 20.2.2.10
     */
    public static JSValue ceil(JSContext ctx, JSValue thisArg, JSValue[] args) {
        if (args.length == 0) {
            return new JSNumber(Double.NaN);
        }
        double x = JSTypeConversions.toNumber(args[0]).value();
        return new JSNumber(Math.ceil(x));
    }

    /**
     * Math.clz32(x)
     * ES2020 20.2.2.11
     * Count leading zeros in 32-bit representation
     */
    public static JSValue clz32(JSContext ctx, JSValue thisArg, JSValue[] args) {
        if (args.length == 0) {
            return new JSNumber(32);
        }
        int n = JSTypeConversions.toInt32(args[0]);
        return new JSNumber(Integer.numberOfLeadingZeros(n));
    }

    /**
     * Math.cos(x)
     * ES2020 20.2.2.12
     */
    public static JSValue cos(JSContext ctx, JSValue thisArg, JSValue[] args) {
        if (args.length == 0) {
            return new JSNumber(Double.NaN);
        }
        double x = JSTypeConversions.toNumber(args[0]).value();
        return new JSNumber(Math.cos(x));
    }

    /**
     * Math.cosh(x)
     * ES2020 20.2.2.13
     */
    public static JSValue cosh(JSContext ctx, JSValue thisArg, JSValue[] args) {
        if (args.length == 0) {
            return new JSNumber(Double.NaN);
        }
        double x = JSTypeConversions.toNumber(args[0]).value();
        return new JSNumber(Math.cosh(x));
    }

    /**
     * Math.exp(x)
     * ES2020 20.2.2.14
     */
    public static JSValue exp(JSContext ctx, JSValue thisArg, JSValue[] args) {
        if (args.length == 0) {
            return new JSNumber(Double.NaN);
        }
        double x = JSTypeConversions.toNumber(args[0]).value();
        return new JSNumber(Math.exp(x));
    }

    /**
     * Math.expm1(x)
     * ES2020 20.2.2.15
     * Returns e^x - 1
     */
    public static JSValue expm1(JSContext ctx, JSValue thisArg, JSValue[] args) {
        if (args.length == 0) {
            return new JSNumber(Double.NaN);
        }
        double x = JSTypeConversions.toNumber(args[0]).value();
        return new JSNumber(Math.expm1(x));
    }

    /**
     * Math.floor(x)
     * ES2020 20.2.2.16
     */
    public static JSValue floor(JSContext ctx, JSValue thisArg, JSValue[] args) {
        if (args.length == 0) {
            return new JSNumber(Double.NaN);
        }
        double x = JSTypeConversions.toNumber(args[0]).value();
        return new JSNumber(Math.floor(x));
    }

    /**
     * Math.fround(x)
     * ES2020 20.2.2.17
     * Round to nearest 32-bit float
     */
    public static JSValue fround(JSContext ctx, JSValue thisArg, JSValue[] args) {
        if (args.length == 0) {
            return new JSNumber(Double.NaN);
        }
        double x = JSTypeConversions.toNumber(args[0]).value();
        return new JSNumber((double) (float) x);
    }

    /**
     * Math.hypot(...values)
     * ES2020 20.2.2.18
     * Returns sqrt(sum of squares)
     */
    public static JSValue hypot(JSContext ctx, JSValue thisArg, JSValue[] args) {
        double sum = 0.0;
        for (JSValue arg : args) {
            double x = JSTypeConversions.toNumber(arg).value();
            if (Double.isInfinite(x)) {
                return new JSNumber(Double.POSITIVE_INFINITY);
            }
            if (Double.isNaN(x)) {
                return new JSNumber(Double.NaN);
            }
            sum += x * x;
        }
        return new JSNumber(Math.sqrt(sum));
    }

    /**
     * Math.imul(x, y)
     * ES2020 20.2.2.19
     * 32-bit integer multiplication
     */
    public static JSValue imul(JSContext ctx, JSValue thisArg, JSValue[] args) {
        int a = args.length > 0 ? JSTypeConversions.toInt32(args[0]) : 0;
        int b = args.length > 1 ? JSTypeConversions.toInt32(args[1]) : 0;
        return new JSNumber(a * b);
    }

    /**
     * Math.log(x)
     * ES2020 20.2.2.20
     */
    public static JSValue log(JSContext ctx, JSValue thisArg, JSValue[] args) {
        if (args.length == 0) {
            return new JSNumber(Double.NaN);
        }
        double x = JSTypeConversions.toNumber(args[0]).value();
        return new JSNumber(Math.log(x));
    }

    /**
     * Math.log1p(x)
     * ES2020 20.2.2.21
     * Returns ln(1 + x)
     */
    public static JSValue log1p(JSContext ctx, JSValue thisArg, JSValue[] args) {
        if (args.length == 0) {
            return new JSNumber(Double.NaN);
        }
        double x = JSTypeConversions.toNumber(args[0]).value();
        return new JSNumber(Math.log1p(x));
    }

    /**
     * Math.log10(x)
     * ES2020 20.2.2.22
     */
    public static JSValue log10(JSContext ctx, JSValue thisArg, JSValue[] args) {
        if (args.length == 0) {
            return new JSNumber(Double.NaN);
        }
        double x = JSTypeConversions.toNumber(args[0]).value();
        return new JSNumber(Math.log10(x));
    }

    /**
     * Math.log2(x)
     * ES2020 20.2.2.23
     */
    public static JSValue log2(JSContext ctx, JSValue thisArg, JSValue[] args) {
        if (args.length == 0) {
            return new JSNumber(Double.NaN);
        }
        double x = JSTypeConversions.toNumber(args[0]).value();
        return new JSNumber(Math.log(x) / Math.log(2));
    }

    /**
     * Math.max(...values)
     * ES2020 20.2.2.24
     */
    public static JSValue max(JSContext ctx, JSValue thisArg, JSValue[] args) {
        if (args.length == 0) {
            return new JSNumber(Double.NEGATIVE_INFINITY);
        }

        double max = Double.NEGATIVE_INFINITY;
        for (JSValue arg : args) {
            double x = JSTypeConversions.toNumber(arg).value();
            if (Double.isNaN(x)) {
                return new JSNumber(Double.NaN);
            }
            max = Math.max(max, x);
        }
        return new JSNumber(max);
    }

    /**
     * Math.min(...values)
     * ES2020 20.2.2.25
     */
    public static JSValue min(JSContext ctx, JSValue thisArg, JSValue[] args) {
        if (args.length == 0) {
            return new JSNumber(Double.POSITIVE_INFINITY);
        }

        double min = Double.POSITIVE_INFINITY;
        for (JSValue arg : args) {
            double x = JSTypeConversions.toNumber(arg).value();
            if (Double.isNaN(x)) {
                return new JSNumber(Double.NaN);
            }
            min = Math.min(min, x);
        }
        return new JSNumber(min);
    }

    /**
     * Math.pow(base, exponent)
     * ES2020 20.2.2.26
     */
    public static JSValue pow(JSContext ctx, JSValue thisArg, JSValue[] args) {
        double base = args.length > 0 ? JSTypeConversions.toNumber(args[0]).value() : Double.NaN;
        double exp = args.length > 1 ? JSTypeConversions.toNumber(args[1]).value() : Double.NaN;
        return new JSNumber(Math.pow(base, exp));
    }

    /**
     * Math.random()
     * ES2020 20.2.2.27
     */
    public static JSValue random(JSContext ctx, JSValue thisArg, JSValue[] args) {
        return new JSNumber(Math.random());
    }

    /**
     * Math.round(x)
     * ES2020 20.2.2.28
     */
    public static JSValue round(JSContext ctx, JSValue thisArg, JSValue[] args) {
        if (args.length == 0) {
            return new JSNumber(Double.NaN);
        }
        double x = JSTypeConversions.toNumber(args[0]).value();
        return new JSNumber(Math.round(x));
    }

    /**
     * Math.sign(x)
     * ES2020 20.2.2.29
     */
    public static JSValue sign(JSContext ctx, JSValue thisArg, JSValue[] args) {
        if (args.length == 0) {
            return new JSNumber(Double.NaN);
        }
        double x = JSTypeConversions.toNumber(args[0]).value();
        return new JSNumber(Math.signum(x));
    }

    /**
     * Math.sin(x)
     * ES2020 20.2.2.30
     */
    public static JSValue sin(JSContext ctx, JSValue thisArg, JSValue[] args) {
        if (args.length == 0) {
            return new JSNumber(Double.NaN);
        }
        double x = JSTypeConversions.toNumber(args[0]).value();
        return new JSNumber(Math.sin(x));
    }

    /**
     * Math.sinh(x)
     * ES2020 20.2.2.31
     */
    public static JSValue sinh(JSContext ctx, JSValue thisArg, JSValue[] args) {
        if (args.length == 0) {
            return new JSNumber(Double.NaN);
        }
        double x = JSTypeConversions.toNumber(args[0]).value();
        return new JSNumber(Math.sinh(x));
    }

    /**
     * Math.sqrt(x)
     * ES2020 20.2.2.32
     */
    public static JSValue sqrt(JSContext ctx, JSValue thisArg, JSValue[] args) {
        if (args.length == 0) {
            return new JSNumber(Double.NaN);
        }
        double x = JSTypeConversions.toNumber(args[0]).value();
        return new JSNumber(Math.sqrt(x));
    }

    /**
     * Math.tan(x)
     * ES2020 20.2.2.33
     */
    public static JSValue tan(JSContext ctx, JSValue thisArg, JSValue[] args) {
        if (args.length == 0) {
            return new JSNumber(Double.NaN);
        }
        double x = JSTypeConversions.toNumber(args[0]).value();
        return new JSNumber(Math.tan(x));
    }

    /**
     * Math.tanh(x)
     * ES2020 20.2.2.34
     */
    public static JSValue tanh(JSContext ctx, JSValue thisArg, JSValue[] args) {
        if (args.length == 0) {
            return new JSNumber(Double.NaN);
        }
        double x = JSTypeConversions.toNumber(args[0]).value();
        return new JSNumber(Math.tanh(x));
    }

    /**
     * Math.trunc(x)
     * ES2020 20.2.2.35
     */
    public static JSValue trunc(JSContext ctx, JSValue thisArg, JSValue[] args) {
        if (args.length == 0) {
            return new JSNumber(Double.NaN);
        }
        double x = JSTypeConversions.toNumber(args[0]).value();
        if (Double.isNaN(x) || Double.isInfinite(x) || x == 0.0) {
            return new JSNumber(x);
        }
        return new JSNumber(x > 0 ? Math.floor(x) : Math.ceil(x));
    }
}
