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

package com.caoccao.qjs4j.vm;

import com.caoccao.qjs4j.core.*;

/**
 * The JavaScript virtual machine bytecode interpreter.
 * Executes compiled bytecode using a stack-based architecture.
 */
public final class VirtualMachine {
    private final CallStack valueStack;
    private final JSContext context;
    private StackFrame currentFrame;
    private JSValue pendingException;

    public VirtualMachine(JSContext context) {
        this.valueStack = new CallStack();
        this.context = context;
        this.currentFrame = null;
        this.pendingException = null;
    }

    /**
     * Execute a bytecode function.
     */
    public JSValue execute(JSBytecodeFunction function, JSValue thisArg, JSValue[] args) {
        // Create new stack frame
        StackFrame frame = new StackFrame(function, thisArg, args, currentFrame);
        StackFrame previousFrame = currentFrame;
        currentFrame = frame;

        try {
            Bytecode bytecode = function.getBytecode();
            int pc = 0;

            // Main execution loop
            while (true) {
                if (pendingException != null) {
                    // Exception handling - simplified for now
                    JSValue exception = pendingException;
                    pendingException = null;
                    currentFrame = previousFrame;
                    throw new VMException("Unhandled exception: " + exception);
                }

                int opcode = bytecode.readOpcode(pc);
                Opcode op = Opcode.fromInt(opcode);

                switch (op) {
                    // ==================== Constants and Literals ====================
                    case INVALID:
                        throw new VMException("Invalid opcode at PC " + pc);

                    case PUSH_I32:
                        valueStack.push(new JSNumber(bytecode.readI32(pc + 1)));
                        pc += 5;
                        break;

                    case PUSH_CONST:
                        int constIndex = bytecode.readU32(pc + 1);
                        valueStack.push(bytecode.getConstants()[constIndex]);
                        pc += 5;
                        break;

                    case UNDEFINED:
                        valueStack.push(JSUndefined.INSTANCE);
                        pc += 1;
                        break;

                    case NULL:
                        valueStack.push(JSNull.INSTANCE);
                        pc += 1;
                        break;

                    case PUSH_THIS:
                        valueStack.push(currentFrame.getThisArg());
                        pc += 1;
                        break;

                    case PUSH_FALSE:
                        valueStack.push(JSBoolean.FALSE);
                        pc += 1;
                        break;

                    case PUSH_TRUE:
                        valueStack.push(JSBoolean.TRUE);
                        pc += 1;
                        break;

                    // ==================== Stack Manipulation ====================
                    case DROP:
                        valueStack.pop();
                        pc += 1;
                        break;

                    case NIP:
                        JSValue top = valueStack.pop();
                        valueStack.pop();
                        valueStack.push(top);
                        pc += 1;
                        break;

                    case DUP:
                        valueStack.push(valueStack.peek(0));
                        pc += 1;
                        break;

                    case DUP2:
                        valueStack.push(valueStack.peek(1));
                        valueStack.push(valueStack.peek(1));
                        pc += 1;
                        break;

                    case SWAP:
                        JSValue v1 = valueStack.pop();
                        JSValue v2 = valueStack.pop();
                        valueStack.push(v1);
                        valueStack.push(v2);
                        pc += 1;
                        break;

                    case ROT3L:
                        JSValue a = valueStack.pop();
                        JSValue b = valueStack.pop();
                        JSValue c = valueStack.pop();
                        valueStack.push(b);
                        valueStack.push(a);
                        valueStack.push(c);
                        pc += 1;
                        break;

                    // ==================== Arithmetic Operations ====================
                    case ADD:
                        handleAdd();
                        pc += 1;
                        break;

                    case SUB:
                        handleSub();
                        pc += 1;
                        break;

                    case MUL:
                        handleMul();
                        pc += 1;
                        break;

                    case DIV:
                        handleDiv();
                        pc += 1;
                        break;

                    case MOD:
                        handleMod();
                        pc += 1;
                        break;

                    case EXP:
                        handleExp();
                        pc += 1;
                        break;

                    case PLUS:
                        handlePlus();
                        pc += 1;
                        break;

                    case NEG:
                        handleNeg();
                        pc += 1;
                        break;

                    case INC:
                        handleInc();
                        pc += 1;
                        break;

                    case DEC:
                        handleDec();
                        pc += 1;
                        break;

                    // ==================== Bitwise Operations ====================
                    case SHL:
                        handleShl();
                        pc += 1;
                        break;

                    case SAR:
                        handleSar();
                        pc += 1;
                        break;

                    case SHR:
                        handleShr();
                        pc += 1;
                        break;

                    case AND:
                        handleAnd();
                        pc += 1;
                        break;

                    case OR:
                        handleOr();
                        pc += 1;
                        break;

                    case XOR:
                        handleXor();
                        pc += 1;
                        break;

                    case NOT:
                        handleNot();
                        pc += 1;
                        break;

                    // ==================== Comparison Operations ====================
                    case EQ:
                        handleEq();
                        pc += 1;
                        break;

                    case NEQ:
                        handleNeq();
                        pc += 1;
                        break;

                    case STRICT_EQ:
                        handleStrictEq();
                        pc += 1;
                        break;

                    case STRICT_NEQ:
                        handleStrictNeq();
                        pc += 1;
                        break;

                    case LT:
                        handleLt();
                        pc += 1;
                        break;

                    case LTE:
                        handleLte();
                        pc += 1;
                        break;

                    case GT:
                        handleGt();
                        pc += 1;
                        break;

                    case GTE:
                        handleGte();
                        pc += 1;
                        break;

                    case INSTANCEOF:
                        handleInstanceof();
                        pc += 1;
                        break;

                    case IN:
                        handleIn();
                        pc += 1;
                        break;

                    // ==================== Logical Operations ====================
                    case LOGICAL_NOT:
                        handleLogicalNot();
                        pc += 1;
                        break;

                    case LOGICAL_AND:
                        handleLogicalAnd();
                        pc += 1;
                        break;

                    case LOGICAL_OR:
                        handleLogicalOr();
                        pc += 1;
                        break;

                    case NULLISH_COALESCE:
                        handleNullishCoalesce();
                        pc += 1;
                        break;

                    // ==================== Variable Access ====================
                    case GET_VAR:
                        int getVarAtom = bytecode.readU32(pc + 1);
                        String getVarName = bytecode.getAtoms()[getVarAtom];
                        JSValue varValue = context.getGlobalObject().get(PropertyKey.fromString(getVarName));
                        valueStack.push(varValue);
                        pc += 5;
                        break;

                    case PUT_VAR:
                        int putVarAtom = bytecode.readU32(pc + 1);
                        String putVarName = bytecode.getAtoms()[putVarAtom];
                        JSValue putValue = valueStack.pop();
                        context.getGlobalObject().set(PropertyKey.fromString(putVarName), putValue);
                        pc += 5;
                        break;

                    case SET_VAR:
                        int setVarAtom = bytecode.readU32(pc + 1);
                        String setVarName = bytecode.getAtoms()[setVarAtom];
                        JSValue setValue = valueStack.peek(0);
                        context.getGlobalObject().set(PropertyKey.fromString(setVarName), setValue);
                        pc += 5;
                        break;

                    case GET_LOCAL:
                        int getLocalIndex = bytecode.readU16(pc + 1);
                        JSValue localValue = currentFrame.getLocals()[getLocalIndex];
                        valueStack.push(localValue);
                        pc += 3;
                        break;

                    case PUT_LOCAL:
                        int putLocalIndex = bytecode.readU16(pc + 1);
                        currentFrame.getLocals()[putLocalIndex] = valueStack.pop();
                        pc += 3;
                        break;

                    case SET_LOCAL:
                        int setLocalIndex = bytecode.readU16(pc + 1);
                        currentFrame.getLocals()[setLocalIndex] = valueStack.peek(0);
                        pc += 3;
                        break;

                    // ==================== Property Access ====================
                    case GET_FIELD:
                        int getFieldAtom = bytecode.readU32(pc + 1);
                        String fieldName = bytecode.getAtoms()[getFieldAtom];
                        JSValue obj = valueStack.pop();
                        if (obj instanceof JSObject jsObj) {
                            valueStack.push(jsObj.get(PropertyKey.fromString(fieldName)));
                        } else {
                            valueStack.push(JSUndefined.INSTANCE);
                        }
                        pc += 5;
                        break;

                    case PUT_FIELD:
                        int putFieldAtom = bytecode.readU32(pc + 1);
                        String putFieldName = bytecode.getAtoms()[putFieldAtom];
                        JSValue putFieldValue = valueStack.pop();
                        JSValue putFieldObj = valueStack.pop();
                        if (putFieldObj instanceof JSObject jsObj) {
                            jsObj.set(PropertyKey.fromString(putFieldName), putFieldValue);
                        }
                        pc += 5;
                        break;

                    case GET_ARRAY_EL:
                        JSValue index = valueStack.pop();
                        JSValue arrayObj = valueStack.pop();
                        if (arrayObj instanceof JSObject jsObj) {
                            PropertyKey key = PropertyKey.fromValue(index);
                            valueStack.push(jsObj.get(key));
                        } else {
                            valueStack.push(JSUndefined.INSTANCE);
                        }
                        pc += 1;
                        break;

                    case PUT_ARRAY_EL:
                        JSValue putElValue = valueStack.pop();
                        JSValue putElIndex = valueStack.pop();
                        JSValue putElObj = valueStack.pop();
                        if (putElObj instanceof JSObject jsObj) {
                            PropertyKey key = PropertyKey.fromValue(putElIndex);
                            jsObj.set(key, putElValue);
                        }
                        pc += 1;
                        break;

                    // ==================== Control Flow ====================
                    case IF_FALSE:
                        JSValue condition = valueStack.pop();
                        boolean isFalsy = JSTypeConversions.toBoolean(condition) == JSBoolean.FALSE;
                        if (isFalsy) {
                            int offset = bytecode.readI32(pc + 1);
                            pc = pc + 5 + offset;
                        } else {
                            pc += 5;
                        }
                        break;

                    case IF_TRUE:
                        JSValue trueCondition = valueStack.pop();
                        boolean isTruthy = JSTypeConversions.toBoolean(trueCondition) == JSBoolean.TRUE;
                        if (isTruthy) {
                            int offset = bytecode.readI32(pc + 1);
                            pc = pc + 5 + offset;
                        } else {
                            pc += 5;
                        }
                        break;

                    case GOTO:
                        int gotoOffset = bytecode.readI32(pc + 1);
                        pc = pc + 5 + gotoOffset;
                        break;

                    case RETURN:
                        JSValue returnValue = valueStack.pop();
                        currentFrame = previousFrame;
                        return returnValue;

                    case RETURN_UNDEF:
                        currentFrame = previousFrame;
                        return JSUndefined.INSTANCE;

                    // ==================== Function Calls ====================
                    case CALL:
                        int argCount = bytecode.readU16(pc + 1);
                        handleCall(argCount);
                        pc += 3;
                        break;

                    case CALL_CONSTRUCTOR:
                        int ctorArgCount = bytecode.readU16(pc + 1);
                        handleCallConstructor(ctorArgCount);
                        pc += 3;
                        break;

                    // ==================== Object/Array Creation ====================
                    case OBJECT:
                    case OBJECT_NEW:
                        valueStack.push(new JSObject());
                        pc += 1;
                        break;

                    case ARRAY_NEW:
                        valueStack.push(new JSArray());
                        pc += 1;
                        break;

                    case PUSH_ARRAY:
                        JSValue element = valueStack.pop();
                        JSValue array = valueStack.peek(0);
                        if (array instanceof JSArray jsArray) {
                            jsArray.push(element);
                        }
                        pc += 1;
                        break;

                    case DEFINE_PROP:
                        JSValue propValue = valueStack.pop();
                        JSValue propKey = valueStack.pop();
                        JSValue propObj = valueStack.peek(0);
                        if (propObj instanceof JSObject jsObj) {
                            PropertyKey key = PropertyKey.fromValue(propKey);
                            jsObj.set(key, propValue);
                        }
                        pc += 1;
                        break;

                    // ==================== Exception Handling ====================
                    case THROW:
                        pendingException = valueStack.pop();
                        throw new VMException("Exception thrown: " + pendingException);

                    case CATCH:
                        // Set up exception handler - simplified
                        int catchOffset = bytecode.readI32(pc + 1);
                        pc += 5;
                        break;

                    // ==================== Type Operations ====================
                    case TYPEOF:
                        handleTypeof();
                        pc += 1;
                        break;

                    case DELETE:
                        handleDelete();
                        pc += 1;
                        break;

                    // ==================== Other Operations ====================
                    default:
                        throw new VMException("Unimplemented opcode: " + op + " at PC " + pc);
                }
            }
        } catch (VMException e) {
            currentFrame = previousFrame;
            throw e;
        } catch (Exception e) {
            currentFrame = previousFrame;
            throw new VMException("VM error: " + e.getMessage(), e);
        }
    }

    // ==================== Arithmetic Operation Handlers ====================

    private void handleAdd() {
        JSValue right = valueStack.pop();
        JSValue left = valueStack.pop();

        // String concatenation or numeric addition
        if (left instanceof JSString || right instanceof JSString) {
            String leftStr = JSTypeConversions.toString(left).getValue();
            String rightStr = JSTypeConversions.toString(right).getValue();
            valueStack.push(new JSString(leftStr + rightStr));
        } else {
            double leftNum = JSTypeConversions.toNumber(left).value();
            double rightNum = JSTypeConversions.toNumber(right).value();
            valueStack.push(new JSNumber(leftNum + rightNum));
        }
    }

    private void handleSub() {
        JSValue right = valueStack.pop();
        JSValue left = valueStack.pop();
        double result = JSTypeConversions.toNumber(left).value() - JSTypeConversions.toNumber(right).value();
        valueStack.push(new JSNumber(result));
    }

    private void handleMul() {
        JSValue right = valueStack.pop();
        JSValue left = valueStack.pop();
        double result = JSTypeConversions.toNumber(left).value() * JSTypeConversions.toNumber(right).value();
        valueStack.push(new JSNumber(result));
    }

    private void handleDiv() {
        JSValue right = valueStack.pop();
        JSValue left = valueStack.pop();
        double result = JSTypeConversions.toNumber(left).value() / JSTypeConversions.toNumber(right).value();
        valueStack.push(new JSNumber(result));
    }

    private void handleMod() {
        JSValue right = valueStack.pop();
        JSValue left = valueStack.pop();
        double result = JSTypeConversions.toNumber(left).value() % JSTypeConversions.toNumber(right).value();
        valueStack.push(new JSNumber(result));
    }

    private void handleExp() {
        JSValue right = valueStack.pop();
        JSValue left = valueStack.pop();
        double result = Math.pow(JSTypeConversions.toNumber(left).value(), JSTypeConversions.toNumber(right).value());
        valueStack.push(new JSNumber(result));
    }

    private void handlePlus() {
        JSValue operand = valueStack.pop();
        double result = JSTypeConversions.toNumber(operand).value();
        valueStack.push(new JSNumber(result));
    }

    private void handleNeg() {
        JSValue operand = valueStack.pop();
        double result = -JSTypeConversions.toNumber(operand).value();
        valueStack.push(new JSNumber(result));
    }

    private void handleInc() {
        JSValue operand = valueStack.pop();
        double result = JSTypeConversions.toNumber(operand).value() + 1;
        valueStack.push(new JSNumber(result));
    }

    private void handleDec() {
        JSValue operand = valueStack.pop();
        double result = JSTypeConversions.toNumber(operand).value() - 1;
        valueStack.push(new JSNumber(result));
    }

    // ==================== Bitwise Operation Handlers ====================

    private void handleShl() {
        JSValue right = valueStack.pop();
        JSValue left = valueStack.pop();
        int leftInt = JSTypeConversions.toInt32(left);
        int rightInt = JSTypeConversions.toInt32(right);
        valueStack.push(new JSNumber(leftInt << (rightInt & 0x1F)));
    }

    private void handleSar() {
        JSValue right = valueStack.pop();
        JSValue left = valueStack.pop();
        int leftInt = JSTypeConversions.toInt32(left);
        int rightInt = JSTypeConversions.toInt32(right);
        valueStack.push(new JSNumber(leftInt >> (rightInt & 0x1F)));
    }

    private void handleShr() {
        JSValue right = valueStack.pop();
        JSValue left = valueStack.pop();
        int leftInt = JSTypeConversions.toInt32(left);
        int rightInt = JSTypeConversions.toInt32(right);
        valueStack.push(new JSNumber((leftInt >>> (rightInt & 0x1F)) & 0xFFFFFFFFL));
    }

    private void handleAnd() {
        JSValue right = valueStack.pop();
        JSValue left = valueStack.pop();
        int result = JSTypeConversions.toInt32(left) & JSTypeConversions.toInt32(right);
        valueStack.push(new JSNumber(result));
    }

    private void handleOr() {
        JSValue right = valueStack.pop();
        JSValue left = valueStack.pop();
        int result = JSTypeConversions.toInt32(left) | JSTypeConversions.toInt32(right);
        valueStack.push(new JSNumber(result));
    }

    private void handleXor() {
        JSValue right = valueStack.pop();
        JSValue left = valueStack.pop();
        int result = JSTypeConversions.toInt32(left) ^ JSTypeConversions.toInt32(right);
        valueStack.push(new JSNumber(result));
    }

    private void handleNot() {
        JSValue operand = valueStack.pop();
        int result = ~JSTypeConversions.toInt32(operand);
        valueStack.push(new JSNumber(result));
    }

    // ==================== Comparison Operation Handlers ====================

    private void handleEq() {
        JSValue right = valueStack.pop();
        JSValue left = valueStack.pop();
        boolean result = JSTypeConversions.abstractEquals(left, right);
        valueStack.push(JSBoolean.valueOf(result));
    }

    private void handleNeq() {
        JSValue right = valueStack.pop();
        JSValue left = valueStack.pop();
        boolean result = !JSTypeConversions.abstractEquals(left, right);
        valueStack.push(JSBoolean.valueOf(result));
    }

    private void handleStrictEq() {
        JSValue right = valueStack.pop();
        JSValue left = valueStack.pop();
        boolean result = JSTypeConversions.strictEquals(left, right);
        valueStack.push(JSBoolean.valueOf(result));
    }

    private void handleStrictNeq() {
        JSValue right = valueStack.pop();
        JSValue left = valueStack.pop();
        boolean result = !JSTypeConversions.strictEquals(left, right);
        valueStack.push(JSBoolean.valueOf(result));
    }

    private void handleLt() {
        JSValue right = valueStack.pop();
        JSValue left = valueStack.pop();
        boolean result = JSTypeConversions.lessThan(left, right);
        valueStack.push(JSBoolean.valueOf(result));
    }

    private void handleLte() {
        JSValue right = valueStack.pop();
        JSValue left = valueStack.pop();
        boolean result = JSTypeConversions.lessThan(left, right) ||
                JSTypeConversions.abstractEquals(left, right);
        valueStack.push(JSBoolean.valueOf(result));
    }

    private void handleGt() {
        JSValue right = valueStack.pop();
        JSValue left = valueStack.pop();
        boolean result = JSTypeConversions.lessThan(right, left);
        valueStack.push(JSBoolean.valueOf(result));
    }

    private void handleGte() {
        JSValue right = valueStack.pop();
        JSValue left = valueStack.pop();
        boolean result = JSTypeConversions.lessThan(right, left) ||
                JSTypeConversions.abstractEquals(left, right);
        valueStack.push(JSBoolean.valueOf(result));
    }

    private void handleInstanceof() {
        JSValue right = valueStack.pop();
        JSValue left = valueStack.pop();
        // Simplified instanceof check
        boolean result = false;
        if (right instanceof JSFunction && left instanceof JSObject) {
            result = true; // Simplified
        }
        valueStack.push(JSBoolean.valueOf(result));
    }

    private void handleIn() {
        JSValue right = valueStack.pop();
        JSValue left = valueStack.pop();
        boolean result = false;
        if (right instanceof JSObject jsObj) {
            PropertyKey key = PropertyKey.fromValue(left);
            result = jsObj.has(key);
        }
        valueStack.push(JSBoolean.valueOf(result));
    }

    // ==================== Logical Operation Handlers ====================

    private void handleLogicalNot() {
        JSValue operand = valueStack.pop();
        boolean result = JSTypeConversions.toBoolean(operand) == JSBoolean.FALSE;
        valueStack.push(JSBoolean.valueOf(result));
    }

    private void handleLogicalAnd() {
        JSValue right = valueStack.pop();
        JSValue left = valueStack.pop();
        // Short-circuit: return left if falsy, otherwise right
        if (JSTypeConversions.toBoolean(left) == JSBoolean.FALSE) {
            valueStack.push(left);
        } else {
            valueStack.push(right);
        }
    }

    private void handleLogicalOr() {
        JSValue right = valueStack.pop();
        JSValue left = valueStack.pop();
        // Short-circuit: return left if truthy, otherwise right
        if (JSTypeConversions.toBoolean(left) == JSBoolean.TRUE) {
            valueStack.push(left);
        } else {
            valueStack.push(right);
        }
    }

    private void handleNullishCoalesce() {
        JSValue right = valueStack.pop();
        JSValue left = valueStack.pop();
        // Return right if left is null or undefined
        if (left instanceof JSNull || left instanceof JSUndefined) {
            valueStack.push(right);
        } else {
            valueStack.push(left);
        }
    }

    // ==================== Type Operation Handlers ====================

    private void handleTypeof() {
        JSValue operand = valueStack.pop();
        String type = JSTypeChecking.typeof(operand);
        valueStack.push(new JSString(type));
    }

    private void handleDelete() {
        JSValue property = valueStack.pop();
        JSValue object = valueStack.pop();
        boolean result = false;
        if (object instanceof JSObject jsObj) {
            PropertyKey key = PropertyKey.fromValue(property);
            result = jsObj.delete(key);
        }
        valueStack.push(JSBoolean.valueOf(result));
    }

    // ==================== Function Call Handlers ====================

    private void handleCall(int argCount) {
        // Pop arguments from stack
        JSValue[] args = new JSValue[argCount];
        for (int i = argCount - 1; i >= 0; i--) {
            args[i] = valueStack.pop();
        }

        // Pop callee
        JSValue callee = valueStack.pop();

        if (callee instanceof JSFunction function) {
            if (function instanceof JSNativeFunction nativeFunc) {
                // Call native function
                JSValue result = nativeFunc.call(context, JSUndefined.INSTANCE, args);
                valueStack.push(result);
            } else if (function instanceof JSBytecodeFunction bytecodeFunc) {
                // Recursive call
                JSValue result = execute(bytecodeFunc, JSUndefined.INSTANCE, args);
                valueStack.push(result);
            } else {
                valueStack.push(JSUndefined.INSTANCE);
            }
        } else {
            throw new VMException("Cannot call non-function value");
        }
    }

    private void handleCallConstructor(int argCount) {
        // Pop arguments
        JSValue[] args = new JSValue[argCount];
        for (int i = argCount - 1; i >= 0; i--) {
            args[i] = valueStack.pop();
        }

        // Pop constructor
        JSValue constructor = valueStack.pop();

        if (constructor instanceof JSFunction) {
            // Create new object
            JSObject newObj = new JSObject();

            // Call constructor with new object as this
            if (constructor instanceof JSNativeFunction nativeFunc) {
                nativeFunc.call(context, newObj, args);
            } else if (constructor instanceof JSBytecodeFunction bytecodeFunc) {
                execute(bytecodeFunc, newObj, args);
            }

            valueStack.push(newObj);
        } else {
            throw new VMException("Cannot construct non-function value");
        }
    }

    /**
     * VM exception for runtime errors.
     */
    public static class VMException extends RuntimeException {
        public VMException(String message) {
            super(message);
        }

        public VMException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
