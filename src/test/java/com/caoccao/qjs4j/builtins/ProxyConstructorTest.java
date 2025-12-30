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

import com.caoccao.qjs4j.BaseJavetTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Proxy.revocable with working revoke function.
 */
public class ProxyConstructorTest extends BaseJavetTest {

    @Test
    public void testProxyApplyBasic() {
        String code = """
                var target = function(a, b) { return a + b; };
                var handler = {
                  apply: function(target, thisArg, args) {
                    return target.apply(thisArg, args) * 2;
                  }
                };
                var proxy = new Proxy(target, handler);
                proxy(1, 2)""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeInteger().doubleValue(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxyApplyForward() {
        String code = """
                var target = function(a, b) { return a + b; };
                var proxy = new Proxy(target, {});
                proxy(1, 2)""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeInteger().doubleValue(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxyApplyNonFunction() {
        // Test that apply trap on non-function throws error
        assertErrorWithJavet("""
                var target = {};
                var handler = {
                  apply: function(target, thisArg, args) {
                    return 42;
                  }
                };
                var proxy = new Proxy(target, handler);
                proxy()""");
    }

    @Test
    public void testProxyApplyWithThisBinding() {
        // Test that apply trap can modify this binding
        String code = """
                var target = function() { return this.value; };
                var handler = {
                  apply: function(target, thisArg, args) {
                    return thisArg ? thisArg.value * 2 : 0;
                  }
                };
                var proxy = new Proxy(target, handler);
                var obj = {value: 5};
                proxy.call(obj)""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeInteger().doubleValue(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxyChainWithMultipleLevels() {
        // Test proxy chain with 3 levels
        String code = """
                var target = {x: 1};
                var proxy1 = new Proxy(target, {
                  get: function(t, p) { return t[p] + 1; }
                });
                var proxy2 = new Proxy(proxy1, {
                  get: function(t, p) { return t[p] + 1; }
                });
                var proxy3 = new Proxy(proxy2, {
                  get: function(t, p) { return t[p] + 1; }
                });
                proxy3.x""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeInteger().doubleValue(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxyChaining() {
        // Test proxy of proxy
        String code = """
                var target = {x: 1};
                var handler1 = {
                  get: function(target, prop) {
                    return target[prop] + 1;
                  }
                };
                var proxy1 = new Proxy(target, handler1);
                var handler2 = {
                  get: function(target, prop) {
                    return target[prop] + 1;
                  }
                };
                var proxy2 = new Proxy(proxy1, handler2);
                proxy2.x""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeInteger().doubleValue(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxyConstructBasic() {
        String code = """
                var target = function(x) { this.value = x; };
                var handler = {
                  construct: function(target, args, newTarget) {
                    var obj = Object.create(target.prototype);
                    obj.value = args[0] * 2;
                    return obj;
                  }
                };
                var proxy = new Proxy(target, handler);
                var instance = new proxy(5);
                instance.value""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeInteger().doubleValue(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxyConstructForward() {
        // Test that construct without trap forwards to target
        String code = """
                var target = function(x) { this.value = x; };
                var proxy = new Proxy(target, {});
                var instance = new proxy(42);
                instance.value""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeInteger().doubleValue(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxyConstructNonConstructor() {
        // Test that construct on non-constructor throws
        assertErrorWithJavet("""
                var target = {}; // Not a constructor
                var handler = {
                  construct: function(target, args) {
                    return {};
                  }
                };
                var proxy = new Proxy(target, handler);
                new proxy()""");
    }

    @Test
    public void testProxyConstructNonObject() {
        // Test that construct trap must return an object
        assertErrorWithJavet("""
                var target = function() {};
                var handler = {
                  construct: function(target, args, newTarget) {
                    return 42; // Return non-object
                  }
                };
                var proxy = new Proxy(target, handler);
                new proxy()""");
    }

    @Test
    public void testProxyDefinePropertyBasic() {
        String code = """
                var target = {};
                var handler = {
                  defineProperty: function(target, prop, descriptor) {
                    Object.defineProperty(target, prop, descriptor);
                    return true;
                  }
                };
                var proxy = new Proxy(target, handler);
                Object.defineProperty(proxy, 'x', {value: 42});
                proxy.x""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeInteger().doubleValue(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxyDefinePropertyForward() {
        // Test that defineProperty without trap forwards to target
        String code = """
                var target = {};
                var proxy = new Proxy(target, {});
                Object.defineProperty(proxy, 'x', {value: 42, writable: true});
                proxy.x""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeInteger().doubleValue(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxyDefinePropertyInvariantNonConfigurableChange() {
        // Test invariant: can't change non-configurable property descriptor
        assertErrorWithJavet("""
                var target = {};
                Object.defineProperty(target, 'x', {
                  value: 1,
                  writable: false,
                  configurable: false
                });
                var handler = {
                  defineProperty: function(target, prop, descriptor) {
                    return true; // Claim success without matching descriptor
                  }
                };
                var proxy = new Proxy(target, handler);
                Object.defineProperty(proxy, 'x', {value: 2})""");
    }

    @Test
    public void testProxyDefinePropertyInvariantNonExtensible() {
        // Test invariant: can't add property to non-extensible target
        assertErrorWithJavet("""
                var target = {};
                Object.preventExtensions(target);
                var handler = {
                  defineProperty: function(target, prop, descriptor) {
                    return true; // Claim success
                  }
                };
                var proxy = new Proxy(target, handler);
                Object.defineProperty(proxy, 'x', {value: 42})""");
    }

    @Test
    public void testProxyDefinePropertyWithGetterSetter() {
        // Test defineProperty with getter/setter
        String code = """
                var target = {};
                var value = 0;
                var handler = {
                  defineProperty: function(target, prop, desc) {
                    Object.defineProperty(target, prop, desc);
                    return true;
                  }
                };
                var proxy = new Proxy(target, handler);
                Object.defineProperty(proxy, 'x', {
                  get: function() { return value; },
                  set: function(v) { value = v; }
                });
                proxy.x = 42;
                proxy.x""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeInteger().doubleValue(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxyDeletePropertyBasic() {
        String code = """
                var target = {x: 1};
                var handler = {
                  deleteProperty: function(target, prop) {
                    delete target[prop];
                    return true;
                  }
                };
                var proxy = new Proxy(target, handler);
                delete proxy.x;
                'x' in proxy""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeBoolean(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxyDeletePropertyForward() {
        String code = """
                var target = {x: 1};
                var proxy = new Proxy(target, {});
                delete proxy.x;
                'x' in proxy""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeBoolean(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxyDeletePropertyNonConfigurable() {
        // Test invariant: can't delete non-configurable property
        assertErrorWithJavet("""
                var target = {};
                Object.defineProperty(target, 'x', {
                  value: 1,
                  configurable: false
                });
                var handler = {
                  deleteProperty: function(target, prop) {
                    return true; // Claim success
                  }
                };
                var proxy = new Proxy(target, handler);
                delete proxy.x""");
    }

    @Test
    public void testProxyDeletePropertyReturningFalse() {
        // Test that deleteProperty trap returning false throws in strict mode
        assertErrorWithJavet("""
                'use strict';
                var target = {x: 1};
                var handler = {
                  deleteProperty: function(target, prop) {
                    return false; // Reject the deletion
                  }
                };
                var proxy = new Proxy(target, handler);
                delete proxy.x""");
    }

    @Test
    public void testProxyGetBasic() {
        String code = """
                var target = {x: 1};
                var handler = {
                  get: function(target, prop, receiver) {
                    return target[prop] * 2;
                  }
                };
                var proxy = new Proxy(target, handler);
                proxy.x""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeInteger().doubleValue(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxyGetForward() {
        String code = """
                var target = {x: 1};
                var proxy = new Proxy(target, {});
                proxy.x""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeInteger().doubleValue(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxyGetInvariantNonConfigurableAccessor() {
        // Test invariant: get must return undefined for non-configurable accessor without getter
        assertErrorWithJavet("""
                var target = {};
                Object.defineProperty(target, 'x', {
                  set: function(v) {},
                  configurable: false
                });
                var handler = {
                  get: function(target, prop) {
                    return 42; // Return value for accessor without getter
                  }
                };
                var proxy = new Proxy(target, handler);
                proxy.x""");
    }

    @Test
    public void testProxyGetInvariantNonWritableNonConfigurable() {
        // Test invariant: must return same value for non-writable, non-configurable property
        assertErrorWithJavet("""
                var target = {};
                Object.defineProperty(target, 'x', {
                  value: 1,
                  writable: false,
                  configurable: false
                });
                var handler = {
                  get: function(target, prop) {
                    return 2; // Return different value
                  }
                };
                var proxy = new Proxy(target, handler);
                proxy.x""");
    }

    @Test
    public void testProxyGetOwnPropertyDescriptorBasic() {
        String code = """
                var target = {x: 1};
                var handler = {
                  getOwnPropertyDescriptor: function(target, prop) {
                    return Object.getOwnPropertyDescriptor(target, prop);
                  }
                };
                var proxy = new Proxy(target, handler);
                var desc = Object.getOwnPropertyDescriptor(proxy, 'x');
                desc.value""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeInteger().doubleValue(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxyGetOwnPropertyDescriptorForward() {
        // Test that getOwnPropertyDescriptor without trap forwards to target
        String code = """
                var target = {x: 42};
                var proxy = new Proxy(target, {});
                var desc = Object.getOwnPropertyDescriptor(proxy, 'x');
                desc.value""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeInteger().doubleValue(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxyGetOwnPropertyDescriptorInvariantNonConfigurable() {
        // Test invariant: can't return undefined for non-configurable property
        assertErrorWithJavet(
                "var target = {}; " +
                        "Object.defineProperty(target, 'x', { " +
                        "  value: 1, " +
                        "  configurable: false " +
                        "}); " +
                        "var handler = { " +
                        "  getOwnPropertyDescriptor: function(target, prop) { " +
                        "    return undefined; " +
                        "  } " +
                        "}; " +
                        "var proxy = new Proxy(target, handler); " +
                        "Object.getOwnPropertyDescriptor(proxy, 'x')");
    }

    @Test
    public void testProxyGetOwnPropertyDescriptorUndefined() {
        String code = """
                var target = {x: 1};
                var handler = {
                  getOwnPropertyDescriptor: function(target, prop) {
                    return undefined;
                  }
                };
                var proxy = new Proxy(target, handler);
                Object.getOwnPropertyDescriptor(proxy, 'x')""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).execute().isUndefined(),
                () -> context.eval(code).isUndefined());
    }

    @Test
    public void testProxyGetPrototypeOfBasic() {
        String code = "var proto = {x: 1}; " +
                "var target = Object.create(proto); " +
                "var handler = { " +
                "  getPrototypeOf: function(target) { " +
                "    return proto; " +
                "  } " +
                "}; " +
                "var proxy = new Proxy(target, handler); " +
                "Object.getPrototypeOf(proxy).x";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeInteger().doubleValue(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxyGetPrototypeOfForward() {
        // Test that missing trap forwards to target
        String code = "var proto = {x: 1}; " +
                "var target = Object.create(proto); " +
                "var proxy = new Proxy(target, {}); " +
                "Object.getPrototypeOf(proxy).x";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeInteger().doubleValue(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxyGetPrototypeOfInvariant() {
        // Test invariant: if target is non-extensible, trap must return target's prototype
        assertErrorWithJavet("""
                var proto1 = {x: 1};
                var proto2 = {x: 2};
                var target = Object.create(proto1);
                Object.preventExtensions(target);
                var handler = {
                  getPrototypeOf: function(target) {
                    return proto2; // Return different prototype
                  }
                };
                var proxy = new Proxy(target, handler);
                Object.getPrototypeOf(proxy)""");
    }

    @Test
    public void testProxyGetPrototypeOfNull() {
        // Test that getPrototypeOf can return null
        String code = "var target = Object.create(null); " +
                "var handler = { " +
                "  getPrototypeOf: function(target) { " +
                "    return null; " +
                "  } " +
                "}; " +
                "var proxy = new Proxy(target, handler); " +
                "Object.getPrototypeOf(proxy)";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeObject(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxyHasBasic() {
        String code = """
                var target = {x: 1};
                var handler = {
                  has: function(target, prop) {
                    return prop in target;
                  }
                };
                var proxy = new Proxy(target, handler);
                'x' in proxy""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeBoolean(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxyHasForward() {
        String code = """
                var target = {x: 1};
                var proxy = new Proxy(target, {});
                'x' in proxy""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeBoolean(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxyHasInvariantNonConfigurable() {
        // Test invariant: must report non-configurable property as present
        assertErrorWithJavet("""
                var target = {};
                Object.defineProperty(target, 'x', {
                  value: 1,
                  configurable: false
                });
                var handler = {
                  has: function(target, prop) {
                    return false; // Hide non-configurable property
                  }
                };
                var proxy = new Proxy(target, handler);
                'x' in proxy""");
    }

    @Test
    public void testProxyHasInvariantNonExtensible() {
        // Test invariant: must report all properties on non-extensible target
        assertErrorWithJavet("""
                var target = {x: 1};
                Object.preventExtensions(target);
                var handler = {
                  has: function(target, prop) {
                    return false; // Hide existing property
                  }
                };
                var proxy = new Proxy(target, handler);
                'x' in proxy""");
    }

    @Test
    public void testProxyInPrototypeChain() {
        // Test proxy used in prototype chain
        String code = """
                var proto = {x: 1};
                var handler = {
                  get: function(target, prop) {
                    return target[prop] * 2;
                  }
                };
                var proxy = new Proxy(proto, handler);
                var obj = Object.create(proxy);
                obj.x""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeInteger().doubleValue(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxyIsExtensibleBasic() {
        String code = """
                var target = {};
                var handler = {
                  isExtensible: function(target) {
                    return Object.isExtensible(target);
                  }
                };
                var proxy = new Proxy(target, handler);
                Object.isExtensible(proxy)""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeBoolean(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxyIsExtensibleForward() {
        // Test that isExtensible without trap forwards to target
        String code = """
                var target = {};
                var proxy = new Proxy(target, {});
                Object.isExtensible(proxy)""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeBoolean(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxyIsExtensibleInvariant() {
        // Test invariant: trap result must match target's extensibility
        assertErrorWithJavet("""
                var target = {};
                var handler = {
                  isExtensible: function(target) {
                    return false; // Lie about extensibility
                  }
                };
                var proxy = new Proxy(target, handler);
                Object.isExtensible(proxy)""");
    }

    @Test
    public void testProxyMultipleTraps() {
        String code = """
                var target = {x: 1};
                var getCalled = false;
                var setCalled = false;
                var handler = {
                  get: function(target, prop) {
                    getCalled = true;
                    return target[prop];
                  },
                  set: function(target, prop, value) {
                    setCalled = true;
                    target[prop] = value;
                    return true;
                  }
                };
                var proxy = new Proxy(target, handler);
                var val = proxy.x;
                proxy.y = 2;
                getCalled && setCalled""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeBoolean(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxyNestedRevocation() {
        // Test that revoking outer proxy doesn't affect inner proxy
        String code = """
                var target = {x: 1};
                var {proxy: inner, revoke: revokeInner} = Proxy.revocable(target, {});
                var {proxy: outer, revoke: revokeOuter} = Proxy.revocable(inner, {});
                revokeOuter();
                inner.x""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeInteger().doubleValue(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxyOwnKeysBasic() {
        String code = """
                var target = {x: 1, y: 2};
                var handler = {
                  ownKeys: function(target) {
                    return ['x', 'y', 'z'];
                  }
                };
                var proxy = new Proxy(target, handler);
                JSON.stringify(Object.keys(proxy))""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeString(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxyOwnKeysForward() {
        String code = """
                var target = {x: 1, y: 2};
                var proxy = new Proxy(target, {});
                Object.keys(proxy).length""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeInteger().doubleValue(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxyOwnKeysInvariantDuplicates() {
        // Test invariant: ownKeys result can't have duplicates
        try {
            context.eval("""
                    var target = {x: 1};
                    var handler = {
                      ownKeys: function(target) {
                        return ['x', 'x']; // Duplicate property
                      }
                    };
                    var proxy = new Proxy(target, handler);
                    Object.keys(proxy)""");
            fail("Should have thrown TypeError");
        } catch (Exception e) {
            // TypeError: 'ownKeys' on proxy: trap returned duplicate entries
            assertTrue(e.getMessage().contains("duplicate") ||
                    e.getMessage().contains("TypeError"));
        }
    }

    @Test
    public void testProxyOwnKeysInvariantNonExtensible() {
        // Test invariant: ownKeys must include all non-configurable properties
        try {
            context.eval("""
                    var target = {};
                    Object.defineProperty(target, 'x', {
                      value: 1,
                      configurable: false
                    });
                    var handler = {
                      ownKeys: function(target) {
                        return []; // Omit non-configurable property
                      }
                    };
                    var proxy = new Proxy(target, handler);
                    Object.keys(proxy)""");
            fail("Should have thrown TypeError");
        } catch (Exception e) {
            assertEquals("TypeError: 'ownKeys' on proxy: trap result did not include 'x'", e.getMessage());
        }
    }

    @Test
    public void testProxyOwnKeysWithSymbols() {
        // Test that ownKeys can return symbols
        String code = """
                var sym1 = Symbol('a');
                var sym2 = Symbol('b');
                var target = {x: 1};
                target[sym1] = 2;
                var handler = {
                  ownKeys: function(target) {
                    return ['x', sym1, sym2];
                  }
                };
                var proxy = new Proxy(target, handler);
                Object.getOwnPropertySymbols(proxy).length""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeInteger().doubleValue(),
                () -> context.eval(code).toJavaObject());
    }

    // ============================================================
    // Additional invariant and edge case tests
    // ============================================================

    @Test
    public void testProxyPreventExtensionsBasic() {
        String code = """
                var target = {};
                var handler = {
                  preventExtensions: function(target) {
                    Object.preventExtensions(target);
                    return true;
                  }
                };
                var proxy = new Proxy(target, handler);
                Object.preventExtensions(proxy);
                Object.isExtensible(proxy)""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeBoolean(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxyPreventExtensionsForward() {
        // Test that preventExtensions without trap forwards to target
        String code = """
                var target = {};
                var proxy = new Proxy(target, {});
                Object.preventExtensions(proxy);
                Object.isExtensible(proxy)""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeBoolean(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxyPreventExtensionsInvariant() {
        // Test invariant: if trap returns true, target must be non-extensible
        String code = """
                var target = {};
                var handler = {
                  preventExtensions: function(target) {
                    return true;
                  }
                };
                var proxy = new Proxy(target, handler);
                Object.preventExtensions(proxy)""";
        assertErrorWithJavet(code, "trap returned truish but the proxy target is extensible");
    }

    @Test
    public void testProxyPreventExtensionsReturningFalse() {
        // Test that preventExtensions trap can return false
        String code = """
                var target = {};
                var handler = {
                  preventExtensions: function(target) {
                    return false;
                  }
                };
                var proxy = new Proxy(target, handler);
                Object.preventExtensions(proxy)""";
        assertErrorWithJavet(code, "trap returned falsish");
    }

    @Test
    public void testProxyReceiverInGet() {
        // Test that get trap receives correct receiver
        String code = """
                var target = {x: 1};
                var handler = {
                  get: function(target, prop, receiver) {
                    return receiver === proxy ? 'correct' : 'wrong';
                  }
                };
                var proxy = new Proxy(target, handler);
                proxy.x""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeString(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxyReceiverInSet() {
        // Test that set trap receives correct receiver
        String code = """
                var target = {};
                var handler = {
                  set: function(target, prop, value, receiver) {
                    if (receiver === proxy) {
                      target.result = 'correct';
                    }
                    return true;
                  }
                };
                var proxy = new Proxy(target, handler);
                proxy.x = 1;
                target.result""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeString(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxyRevocableAccessAfterRevoke() {
        // Test that accessing revoked proxy throws TypeError
        assertErrorWithJavet("""
                var target = {x: 1};
                var handler = {};
                var {proxy, revoke} = Proxy.revocable(target, handler);
                proxy.x; // Works before revoke
                revoke();
                proxy.x""");
    }

    @Test
    public void testProxyRevocableAccessBeforeRevoke() {
        // Test that proxy works normally before revocation
        String code = """
                var target = {x: 1};
                var handler = {};
                var {proxy, revoke} = Proxy.revocable(target, handler);
                proxy.x""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeInteger().doubleValue(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxyRevocableBasic() {
        // Test that Proxy.revocable returns an object with proxy and revoke
        String code1 = """
                var target = {x: 1};
                var handler = {};
                var revocable = Proxy.revocable(target, handler);
                typeof revocable""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code1).executeString(),
                () -> context.eval(code1).toJavaObject());

        String code2 = """
                var target = {x: 1};
                var handler = {};
                var revocable = Proxy.revocable(target, handler);
                typeof revocable.proxy""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code2).executeString(),
                () -> context.eval(code2).toJavaObject());

        String code3 = """
                var target = {x: 1};
                var handler = {};
                var revocable = Proxy.revocable(target, handler);
                typeof revocable.revoke""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code3).executeString(),
                () -> context.eval(code3).toJavaObject());
    }

    @Test
    public void testProxyRevocableRevokeMultipleTimes() {
        // Test that calling revoke multiple times doesn't cause issues
        String code = """
                var target = {x: 1};
                var handler = {};
                var {proxy, revoke} = Proxy.revocable(target, handler);
                revoke();
                revoke(); // Call revoke again
                'ok'""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeString(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxyRevocableSetAfterRevoke() {
        // Test that setting on revoked proxy throws TypeError
        assertErrorWithJavet("""
                var target = {x: 1};
                var handler = {};
                var {proxy, revoke} = Proxy.revocable(target, handler);
                revoke();
                proxy.y = 2""");
    }

    @Test
    public void testProxySetBasic() {
        String code = """
                var target = {};
                var handler = {
                  set: function(target, prop, value, receiver) {
                    target[prop] = value * 2;
                    return true;
                  }
                };
                var proxy = new Proxy(target, handler);
                proxy.x = 5;
                proxy.x""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeInteger().doubleValue(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxySetForward() {
        String code = """
                var target = {};
                var proxy = new Proxy(target, {});
                proxy.x = 42;
                proxy.x""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeInteger().doubleValue(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxySetInvariantNonWritable() {
        // Test invariant: can't change non-writable property
        String code = """
                var target = {};
                Object.defineProperty(target, 'x', {
                  value: 1,
                  writable: false,
                  configurable: true
                });
                var handler = {
                  set: function(target, prop, value) {
                    return true;
                  }
                };
                var proxy = new Proxy(target, handler);
                proxy.x = 2;
                proxy.x""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeInteger().doubleValue(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxySetPrototypeOfBasic() {
        String code = """
                var newProto = {x: 2};
                var target = {y: 1};
                var handler = {
                  setPrototypeOf: function(target, proto) {
                    Object.setPrototypeOf(target, proto);
                    return true;
                  }
                };
                var proxy = new Proxy(target, handler);
                Object.setPrototypeOf(proxy, newProto);
                Object.getPrototypeOf(proxy).x""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeInteger().doubleValue(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxySetPrototypeOfForward() {
        // Test that setPrototypeOf without trap forwards to target
        String code = """
                var newProto = {x: 42};
                var target = {};
                var proxy = new Proxy(target, {});
                Object.setPrototypeOf(proxy, newProto);
                Object.getPrototypeOf(proxy).x""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeInteger().doubleValue(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxySetPrototypeOfInvariant() {
        // Test invariant: if target is non-extensible, can't change prototype
        assertErrorWithJavet("""
                var proto1 = {x: 1};
                var proto2 = {x: 2};
                var target = Object.create(proto1);
                Object.preventExtensions(target);
                var handler = {
                  setPrototypeOf: function(target, proto) {
                    return true; // Claim success without changing
                  }
                };
                var proxy = new Proxy(target, handler);
                Object.setPrototypeOf(proxy, proto2)""");
    }

    @Test
    public void testProxySetPrototypeOfReturningFalse() {
        // Test that setPrototypeOf trap can return false
        try {
            context.eval("""
                    'use strict';
                    var target = {};
                    var handler = {
                      setPrototypeOf: function(target, proto) {
                        return false; // Refuse to set prototype
                      }
                    };
                    var proxy = new Proxy(target, handler);
                    Object.setPrototypeOf(proxy, {})""");
            fail("Should have thrown TypeError in strict mode");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("TypeError") ||
                    e.getMessage().contains("returned false"));
        }
    }

    @Test
    public void testProxySetReturningFalse() {
        // Test that set trap returning false throws in strict mode
        try {
            // Reject the assignment
            context.eval("""
                    'use strict';
                    var target = {};
                    var handler = {
                      set: function(target, prop, value) {
                        return false;
                      }
                    };
                    var proxy = new Proxy(target, handler);
                    proxy.x = 1""");
            fail("Should have thrown TypeError in strict mode");
        } catch (Exception e) {
            assertEquals("TypeError: 'set' on proxy: trap returned falsish for property 'x'", e.getMessage());
        }
    }

    @Test
    public void testProxyThrowingTrap() {
        // Test that trap can throw custom error
        try {
            context.eval("""
                    var target = {x: 1};
                    var handler = {
                      get: function(target, prop) {
                        throw new Error('custom error');
                      }
                    };
                    var proxy = new Proxy(target, handler);
                    proxy.x""");
            fail("Should have thrown custom error");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("custom error"));
        }
    }

    @Test
    public void testProxyTrapWithNonCallableHandler() {
        // Test that non-callable trap throws TypeError
        try {
            // Not a function
            context.eval("""
                    var target = {x: 1};
                    var handler = {
                      get: 42
                    };
                    var proxy = new Proxy(target, handler);
                    proxy.x""");
            fail("Should have thrown TypeError");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("not a function") ||
                    e.getMessage().contains("TypeError") ||
                    e.getMessage().contains("callable"));
        }
    }

    @Test
    public void testProxyWithArrayLikeObject() {
        // Test proxy with array-like object (has length property)
        String code = """
                var target = {0: 'a', 1: 'b', 2: 'c', length: 3};
                var handler = {
                  get: function(target, prop) {
                    return target[prop];
                  }
                };
                var proxy = new Proxy(target, handler);
                Array.prototype.join.call(proxy, ',')""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeString(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxyWithBigIntObjectArithmetic() {
        // Test that proxied BigInt object valueOf works
        assertErrorWithJavet("""
                var target = Object(BigInt(10));
                var handler = {};
                var proxy = new Proxy(target, handler);
                proxy.valueOf()""");
    }

    @Test
    public void testProxyWithBigIntObjectAsTarget() {
        // Test that BigInt object (Object(BigInt(42))) can be a proxy target
        // BigInt objects are needed as proxy targets since primitive BigInts cannot be proxied
        String code = """
                var target = Object(BigInt(42));
                var handler = {
                  get: function(target, prop) {
                    if (prop === 'test') {
                      return 'intercepted';
                    }
                    return target[prop];
                  }
                };
                var proxy = new Proxy(target, handler);
                proxy.test""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeString(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxyWithBigIntObjectHasTrap() {
        // Test has trap on BigInt object proxy
        String code = """
                var target = Object(BigInt(42));
                target.customProp = 'exists';
                var handler = {
                  has: function(target, prop) {
                    if (prop === 'fakeProperty') {
                      return true;
                    }
                    return prop in target;
                  }
                };
                var proxy = new Proxy(target, handler);
                'fakeProperty' in proxy""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeBoolean(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxyWithBigIntObjectSetTrap() {
        // Test set trap on BigInt object proxy
        String code = """
                var target = Object(BigInt(42));
                var handler = {
                  set: function(target, prop, value) {
                    target[prop] = value * 2;
                    return true;
                  }
                };
                var proxy = new Proxy(target, handler);
                proxy.test = 21;
                proxy.test""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeInteger().doubleValue(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxyWithBigIntObjectToString() {
        // Test that proxied BigInt object toString works correctly
        assertErrorWithJavet("""
                var target = Object(BigInt(255));
                var handler = {};
                var proxy = new Proxy(target, handler);
                proxy.toString()""");
    }

    @Test
    public void testProxyWithBigIntObjectValueOf() {
        // Test that proxied BigInt object valueOf works correctly
        assertErrorWithJavet("""
                var target = Object(BigInt(100));
                var handler = {
                  get: function(target, prop) {
                    return target[prop];
                  }
                };
                var proxy = new Proxy(target, handler);
                proxy.valueOf()""");
    }

    @Test
    public void testProxyWithBooleanObjectAsTarget() {
        // Test that Boolean object (new Boolean(true)) can be a proxy target
        // Boolean objects are needed as proxy targets since primitive booleans cannot be proxied
        String code = """
                var target = new Boolean(true);
                var handler = {
                  get: function(target, prop) {
                    if (prop === 'test') {
                      return 'intercepted';
                    }
                    return target[prop];
                  }
                };
                var proxy = new Proxy(target, handler);
                proxy.test""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeString(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxyWithBooleanObjectToString() {
        // Test that proxied Boolean object still works with toString
        assertErrorWithJavet("""
                var target = new Boolean(true);
                var handler = {};
                var proxy = new Proxy(target, handler);
                proxy.toString()""");
    }

    @Test
    public void testProxyWithBooleanObjectTrapGet() {
        // Test that get trap intercepts valueOf on Boolean object
        String code = """
                var target = new Boolean(true);
                var handler = {
                  get: function(target, prop) {
                    if (prop === 'valueOf') {
                      return function() { return false; };
                    }
                    return target[prop];
                  }
                };
                var proxy = new Proxy(target, handler);
                proxy.valueOf()""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeBoolean(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxyWithBooleanObjectValueOf() {
        // Test that proxied Boolean object still works with valueOf
        assertErrorWithJavet("""
                var target = new Boolean(true);
                var handler = {};
                var proxy = new Proxy(target, handler);
                proxy.valueOf()""");
    }

    @Test
    public void testProxyWithNullPrototype() {
        // Test proxy with null prototype target
        String code = """
                var target = Object.create(null);
                target.x = 42;
                var handler = {
                  get: function(target, prop) {
                    return target[prop];
                  }
                };
                var proxy = new Proxy(target, handler);
                proxy.x""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeInteger().doubleValue(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxyWithNumberObjectAsTarget() {
        // Test that Number object (new Number(42)) can be a proxy target
        // Number objects are needed as proxy targets since primitive numbers cannot be proxied
        String code = """
                var target = new Number(42);
                var handler = {
                  get: function(target, prop) {
                    if (prop === 'test') {
                      return 'intercepted';
                    }
                    return target[prop];
                  }
                };
                var proxy = new Proxy(target, handler);
                proxy.test""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeString(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxyWithNumberObjectToString() {
        // Test that proxied Number object still works with toString
        assertErrorWithJavet("""
                var target = new Number(42);
                var handler = {};
                var proxy = new Proxy(target, handler);
                proxy.toString()""");
    }

    @Test
    public void testProxyWithNumberObjectTrapGet() {
        // Test that get trap intercepts valueOf on Number object
        String code = """
                var target = new Number(100);
                var handler = {
                  get: function(target, prop) {
                    if (prop === 'valueOf') {
                      return function() { return 999; };
                    }
                    return target[prop];
                  }
                };
                var proxy = new Proxy(target, handler);
                proxy.valueOf()""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeInteger().doubleValue(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxyWithNumberObjectValueOf() {
        // Test that proxied Number object still works with valueOf
        assertErrorWithJavet("""
                var target = new Number(3.14);
                var handler = {};
                var proxy = new Proxy(target, handler);
                proxy.valueOf()""");
    }

    @Test
    public void testProxyWithNumericProperties() {
        // Test that proxy works with object having numeric property names
        String code = """
                var target = {};
                target['0'] = 1;
                target['1'] = 2;
                var handler = {
                  get: function(target, prop, receiver) {
                    var val = target[prop];
                    return val !== undefined ? val * 2 : undefined;
                  }
                };
                var proxy = new Proxy(target, handler);
                proxy['0']""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeInteger().doubleValue(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxyWithStringObjectAsTarget() {
        // Test that String object (new String("hello")) can be a proxy target
        // String objects are needed as proxy targets since primitive strings cannot be proxied
        String code = """
                var target = new String('hello');
                var handler = {
                  get: function(target, prop) {
                    if (prop === 'test') {
                      return 'intercepted';
                    }
                    return target[prop];
                  }
                };
                var proxy = new Proxy(target, handler);
                proxy.test""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeString(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxyWithStringObjectCharAccess() {
        // Test that proxied String object supports character access
        assertErrorWithJavet("""
                var target = new String('hello');
                var handler = {
                  get: function(target, prop) {
                    if (prop === '1') {
                      return 'X';
                    }
                    return target[prop];
                  }
                };
                var proxy = new Proxy(target, handler);
                proxy[1]""");
    }

    @Test
    public void testProxyWithStringObjectLength() {
        // Test that proxied String object has length property
        String code = """
                var target = new String('hello');
                var handler = {};
                var proxy = new Proxy(target, handler);
                proxy.length""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeInteger().doubleValue(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxyWithStringObjectToString() {
        // Test that proxied String object still works with toString
        assertErrorWithJavet("""
                var target = new String('hello');
                var handler = {};
                var proxy = new Proxy(target, handler);
                proxy.toString()""");
    }

    @Test
    public void testProxyWithStringObjectTrapGet() {
        // Test that get trap intercepts methods on String object
        String code = """
                var target = new String('hello');
                var handler = {
                  get: function(target, prop) {
                    if (prop === 'toUpperCase') {
                      return function() { return 'INTERCEPTED'; };
                    }
                    return target[prop];
                  }
                };
                var proxy = new Proxy(target, handler);
                proxy.toUpperCase()""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeString(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxyWithStringObjectValueOf() {
        // Test that proxied String object still works with valueOf
        assertErrorWithJavet("""
                var target = new String('world');
                var handler = {};
                var proxy = new Proxy(target, handler);
                proxy.valueOf()""");
    }

    @Test
    public void testProxyWithSymbolObjectAsPropertyKey() {
        // Test that symbol object is created and can be used with proxy
        String code = """
                var symObj = Object(Symbol('key'));
                var sym = symObj.valueOf();
                var target = {};
                var handler = {
                  set: function(t, p, v) {
                    t[p] = v;
                    return true;
                  },
                  get: function(t, p) {
                    return t[p];
                  }
                };
                var proxy = new Proxy(target, handler);
                proxy[sym] = 'value';
                proxy[sym]""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeString(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxyWithSymbolObjectAsTarget() {
        // Test that Symbol object (Object(Symbol('foo'))) can be a proxy target
        // Symbol objects are needed as proxy targets since primitive symbols cannot be proxied
        String code = """
                var target = Object(Symbol('foo'));
                var handler = {
                  get: function(target, prop) {
                    if (prop === 'test') {
                      return 'intercepted';
                    }
                    return target[prop];
                  }
                };
                var proxy = new Proxy(target, handler);
                proxy.test""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeString(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxyWithSymbolObjectDescription() {
        // Test accessing description through proxy
        String code = """
                var target = Object(Symbol('myDescription'));
                var handler = {
                  get: function(target, prop) {
                    if (prop === 'description') {
                      return 'modified';
                    }
                    return target[prop];
                  }
                };
                var proxy = new Proxy(target, handler);
                proxy.description""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeString(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxyWithSymbolObjectGetPrimitiveValue() {
        // Test accessing [[PrimitiveValue]] through proxy
        String code = """
                var target = Object(Symbol('test'));
                var handler = {};
                var proxy = new Proxy(target, handler);
                var primitiveValue = proxy['[[PrimitiveValue]]'];
                typeof primitiveValue""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeString(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxyWithSymbolObjectToString() {
        // Test that proxied Symbol object still works with toString
        assertErrorWithJavet("""
                var target = Object(Symbol('test'));
                var handler = {};
                var proxy = new Proxy(target, handler);
                proxy.toString()""");
    }

    @Test
    public void testProxyWithSymbolObjectTrapGet() {
        // Test that get trap intercepts methods on Symbol object
        String code = """
                var target = Object(Symbol('test'));
                var handler = {
                  get: function(target, prop) {
                    if (prop === 'toString') {
                      return function() { return 'INTERCEPTED'; };
                    }
                    return target[prop];
                  }
                };
                var proxy = new Proxy(target, handler);
                proxy.toString()""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeString(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxyWithSymbolObjectValueOf() {
        // Test that proxied Symbol object still works with valueOf
        assertErrorWithJavet("""
                var target = Object(Symbol('mySymbol'));
                var handler = {};
                var proxy = new Proxy(target, handler);
                var primitiveValue = proxy.valueOf();
                typeof primitiveValue""");
    }

    @Test
    public void testProxyWithSymbolObjectWellKnown() {
        // Test proxying an object wrapping a well-known symbol
        String code = """
                var target = Object(Symbol.iterator);
                var handler = {
                  get: function(target, prop) {
                    if (prop === 'test') {
                      return 'well-known';
                    }
                    return target[prop];
                  }
                };
                var proxy = new Proxy(target, handler);
                proxy.test""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeString(),
                () -> context.eval(code).toJavaObject());
    }

    @Test
    public void testProxyWithSymbolProperty() {
        // Test that proxy works with symbol properties
        String code = """
                var sym = Symbol('test');
                var target = {};
                target[sym] = 42;
                var handler = {
                  get: function(target, prop) {
                    return target[prop];
                  }
                };
                var proxy = new Proxy(target, handler);
                proxy[sym]""";
        assertWithJavet(
                () -> v8Runtime.getExecutor(code).executeInteger().doubleValue(),
                () -> context.eval(code).toJavaObject());
    }
}

