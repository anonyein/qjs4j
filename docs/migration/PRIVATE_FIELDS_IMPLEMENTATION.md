# Private Fields and Static Blocks Implementation

## Status: Partial Implementation ⏳

This document tracks the implementation of ES2022 private class fields (#field) and static blocks (static { }) in qjs4j, following the QuickJS implementation.

## Completed ✅

### 1. Lexer Support
**File**: `src/main/java/com/caoccao/qjs4j/compiler/Lexer.java`

- Added `HASH` token type for `#` character
- Added `PRIVATE_NAME` token type for `#identifier` patterns
- Implemented scanning logic to recognize private identifiers
- Private names are scanned as a single token including the `#` prefix

**Example**:
```javascript
#privateField  // Tokenized as PRIVATE_NAME with value "#privateField"
```

### 2. AST Nodes
**Files**:
- `src/main/java/com/caoccao/qjs4j/compiler/ast/PrivateIdentifier.java`
- `src/main/java/com/caoccao/qjs4j/compiler/ast/ClassDeclaration.java`
- `src/main/java/com/caoccao/qjs4j/compiler/ast/Expression.java`

**Changes**:
- Created `PrivateIdentifier` record to represent `#name` expressions
- Updated `ClassDeclaration` to support `ClassElement` instead of just `MethodDefinition`
- Created sealed `ClassElement` interface with three implementations:
  - `MethodDefinition`: Class methods (added `isPrivate` flag)
  - `PropertyDefinition`: Class fields (public or private)
  - `StaticBlock`: Static initialization blocks
- Added `PrivateIdentifier` to Expression's permitted types

**Example AST**:
```javascript
class Example {
  #private = 42;
  static { console.log("init"); }
}
```
→ ClassDeclaration with body containing:
  - PropertyDefinition(key: PrivateIdentifier("#private"), value: Literal(42), isStatic: false, isPrivate: true)
  - StaticBlock(body: [ExpressionStatement(...)])

### 3. Opcodes
**File**: `src/main/java/com/caoccao/qjs4j/vm/Opcode.java`

Added the following opcodes matching QuickJS specification:

| Opcode | Number | Stack Effect | Description |
|--------|--------|--------------|-------------|
| GET_PRIVATE_FIELD | 131 | obj prop → value | Get value of private field |
| PUT_PRIVATE_FIELD | 132 | obj value prop → | Set value of private field |
| DEFINE_PRIVATE_FIELD | 133 | obj prop value → obj | Define new private field on object |
| DEFINE_FIELD | 134 | obj value → obj | Define regular (public) field (takes atom parameter) |
| PRIVATE_IN | 135 | obj prop → boolean | Check if object has private field (for `#field in obj`) |

**Note**: Opcodes `PRIVATE_SYMBOL(5)`, `CHECK_BRAND(43)`, and `ADD_BRAND(44)` already existed in qjs4j.

### 4. Token Types
**File**: `src/main/java/com/caoccao/qjs4j/compiler/TokenType.java`

- Added `HASH` token type
- Added `PRIVATE_NAME` token type

### 5. Parser Implementation ✅ COMPLETE
**File**: `src/main/java/com/caoccao/qjs4j/compiler/Parser.java`

**Implemented**:

1. **Parse Class Declarations** (Lines 1592-1631):
   - ✅ Implemented `parseClassDeclaration()` method
   - ✅ Handles `class Name extends Super { }` syntax
   - ✅ Properly parses optional class name (for class expressions)
   - ✅ Parses extends clause with `parseMemberExpression()`

2. **Parse Class Body Elements** (Lines 1636-1742):
   - ✅ Parse methods: `methodName() { }`, `static methodName() { }`, `#privateMethod() { }`
   - ✅ Parse fields: `field = value;`, `static field = value;`, `#privateField = value;`
   - ✅ Parse static blocks: `static { statements }`
   - ✅ Handle computed property names: `[expression]`
   - ✅ Handle getters/setters: `get name()`, `set name(value)`
   - ✅ Handle edge case: `static` as property name (e.g., `static = 42;`)

3. **Parse Private Identifiers** (Lines 1667-1676):
   - ✅ When `TokenType.PRIVATE_NAME` is encountered, create `PrivateIdentifier` node
   - ✅ Remove `#` prefix from the name for the AST node
   - ✅ Set `isPrivate` flag on ClassElement
   - ✅ Support private fields, methods, and getters/setters

4. **Parse Static Blocks** (Lines 1798-1812):
   - ✅ When `static {` is encountered, parse block statement
   - ✅ Create `StaticBlock` element with the parsed statements
   - ✅ Properly handle nested statements within the block

**Implementation Notes**:
- Class elements are parsed using `parseClassElement()` which dispatches to appropriate handlers
- Method parsing uses `parseMethod()` which parses parameters and block body
- Field parsing uses `parseMethodOrField()` which detects `=` or `;` to distinguish from methods
- Private names are properly tokenized by the lexer as `PRIVATE_NAME` tokens
- All parser functionality is tested in `ClassParserTest.java` with 8 comprehensive test cases

### 6. Compiler Implementation ✅ PARTIAL (Basic classes complete)
**File**: `src/main/java/com/caoccao/qjs4j/compiler/BytecodeCompiler.java`

**Completed** (Lines 861-1091):

1. **Compile Class Declarations** (`compileClassDeclaration`):
   - ✅ Removed "not yet implemented" exception (line 1211)
   - ✅ Compile superclass expression or emit undefined
   - ✅ Separate class elements by type (methods, fields, static blocks, constructor)
   - ✅ Compile constructor function or create default constructor
   - ✅ Emit `DEFINE_CLASS` opcode with class name
   - ✅ Compile and emit instance methods with `DEFINE_METHOD`
   - ✅ Handle class name storage in local or global scope
   - ⏳ Static methods (throws exception - not yet implemented)
   - ⏳ Fields compilation (TODO comment added)
   - ⏳ Static blocks (TODO comment added)

2. **Helper Methods**:
   - ✅ `compileMethodAsFunction` - Compiles method definitions into JSBytecodeFunction
   - ✅ `createDefaultConstructor` - Creates default constructor for classes without explicit constructor
   - ✅ `getMethodName` - Extracts method name from various key types (Identifier, Literal, PrivateIdentifier)

2. **Private Field Compilation**:
   - For each private field, emit `PRIVATE_SYMBOL` to create unique symbol
   - Store private symbol in class scope
   - Emit `DEFINE_PRIVATE_FIELD` when defining private fields on instances
   - Emit `ADD_BRAND` to mark instances with class brand
   - Emit `CHECK_BRAND` before private field access

3. **Field Initializers**:
   - Create a special "fields init" function for instance fields
   - Compile field initializer expressions in the context of that function
   - Call fields init function in constructor

4. **Static Block Compilation**:
   - Create a special class static init function for each `static { }` block
   - Compile the block body as a function
   - Emit call to the static init function immediately after class definition
   - Use class as `this` binding

5. **Private Field Access**:
   - Compile `obj.#field` as `GET_PRIVATE_FIELD` opcode
   - Compile `obj.#field = value` as `PUT_PRIVATE_FIELD` opcode
   - Compile `#field in obj` as `PRIVATE_IN` opcode

**QuickJS Reference** (quickjs.c:24795-24850):
```c
// Static block compilation
if (is_static && s->token.val == '{') {
    if (js_parse_function_decl2(s, JS_PARSE_FUNC_CLASS_STATIC_INIT,
                                JS_FUNC_NORMAL, JS_ATOM_NULL,
                                s->token.ptr,
                                JS_PARSE_EXPORT_NONE, &init) < 0) {
        goto fail;
    }
    emit_op(s, OP_scope_get_var);  // Get 'this' (the class)
    emit_atom(s, JS_ATOM_this);
    emit_op(s, OP_swap);
    emit_op(s, OP_call_method);     // Call static init
    emit_u16(s, 0);                 // 0 arguments
    emit_op(s, OP_drop);            // Drop return value
}
```

### 7. Runtime Support ✅ PARTIAL (Basic opcodes complete)
**Files**:
- `src/main/java/com/caoccao/qjs4j/core/JSObject.java`
- `src/main/java/com/caoccao/qjs4j/vm/VirtualMachine.java`

**Completed** (VirtualMachine.java lines 669-734):

1. **DEFINE_CLASS Opcode Handler**:
   - ✅ Pops constructor and superClass from stack
   - ✅ Creates prototype object
   - ✅ Sets up prototype chain for inheritance
   - ✅ Sets constructor.prototype = prototype
   - ✅ Sets prototype.constructor = constructor
   - ✅ Pushes proto and constructor back to stack

2. **DEFINE_METHOD Opcode Handler**:
   - ✅ Reads method name from atom table
   - ✅ Pops method and object from stack
   - ✅ Adds method to object as property
   - ✅ Pushes object back to stack

**Remaining Work**:

1. **JSObject Private Field Storage**:
   - Add private field storage (Map<JSValue, JSValue> for symbol → value mapping)
   - Private symbols must be unique per class
   - Brand checking to ensure field access is only on correct instances

2. **VM Opcode Handlers**:
   - Implement `handleGetPrivateField()`: Look up private symbol, get value from obj
   - Implement `handlePutPrivateField()`: Look up private symbol, set value on obj
   - Implement `handleDefinePrivateField()`: Create property with private symbol
   - Implement `handleDefineField()`: Create public field (regular property)
   - Implement `handlePrivateIn()`: Check if object has private field
   - Implement `handleCheckBrand()`: Verify object has correct brand for class
   - Implement `handleAddBrand()`: Add brand to object instance

3. **Private Symbol Creation**:
   - `PRIVATE_SYMBOL` opcode creates a new unique Symbol
   - Each private field name gets a unique symbol
   - Symbols are stored in class scope for reuse

**QuickJS Reference** (quickjs.c:18838-18888):
```c
CASE(OP_get_private_field):
{
    JSValue val;
    val = JS_GetPrivateField(ctx, sp[-2], sp[-1]);  // obj, prop
    JS_FreeValue(ctx, sp[-1]);
    JS_FreeValue(ctx, sp[-2]);
    sp[-2] = val;
    sp--;
    if (unlikely(JS_IsException(val)))
        goto exception;
}
BREAK;

CASE(OP_define_private_field):
{
    int ret;
    ret = JS_DefinePrivateField(ctx, sp[-3], sp[-2], sp[-1]);  // obj, prop, value
    JS_FreeValue(ctx, sp[-2]);
    sp -= 2;
    if (unlikely(ret < 0))
        goto exception;
}
BREAK;
```

### 8. Testing ✅ PARTIAL (Basic class tests complete)
**Files**:
- `src/test/java/com/caoccao/qjs4j/compiler/ClassParserTest.java` (8 tests - ✅ all passing)
- `src/test/java/com/caoccao/qjs4j/compiler/ClassCompilerTest.java` (3 tests - ✅ all passing)
- `src/test/java/com/caoccao/qjs4j/compiler/ClassDebugTest.java` (2 debug tests)

**Completed Test Coverage**:

1. **Basic Class Tests** (ClassCompilerTest.java):
   - ✅ Simple empty class declaration
   - ✅ Class with instance method
   - ✅ Class with constructor setting properties

**Test Coverage Needed**:

1. **Private Fields Tests**:
   ```javascript
   class Counter {
     #count = 0;
     increment() { this.#count++; }
     getCount() { return this.#count; }
   }
   ```

2. **Static Private Fields**:
   ```javascript
   class Example {
     static #instance;
     static getInstance() {
       if (!this.#instance) {
         this.#instance = new Example();
       }
       return this.#instance;
     }
   }
   ```

3. **Static Blocks**:
   ```javascript
   class Config {
     static apiUrl;
     static {
       this.apiUrl = process.env.API_URL || 'localhost';
     }
   }
   ```

4. **Private Methods**:
   ```javascript
   class Calculator {
     #validate(x) { return typeof x === 'number'; }
     add(a, b) {
       if (!this.#validate(a) || !this.#validate(b)) {
         throw new Error('Invalid input');
       }
       return a + b;
     }
   }
   ```

5. **Private In Operator**:
   ```javascript
   class MyClass {
     #private;
     hasPrivateField(obj) {
       return #private in obj;
     }
   }
   ```

6. **Brand Checking** (should throw TypeError):
   ```javascript
   class A {
     #x = 1;
   }
   class B {
     static test(obj) {
       return obj.#x;  // TypeError if obj is not instance of A
     }
   }
   B.test(new B());  // Should throw TypeError
   ```

## Implementation Priority

1. **High Priority** (Core functionality):
   - Parser: Class declaration and class body parsing
   - Compiler: Basic class compilation with public methods
   - Runtime: Basic class creation and method calls

2. **Medium Priority** (Essential features):
   - Parser: Private field syntax
   - Compiler: Private field opcodes
   - Runtime: Private field storage and access

3. **Lower Priority** (Advanced features):
   - Parser: Static blocks
   - Compiler: Static block compilation
   - Runtime: Static initialization

## References

- **QuickJS Source**: `../quickjs/quickjs.c` (lines 24700-25000 for class parsing)
- **QuickJS Opcodes**: `../quickjs/quickjs-opcode.h` (lines 137-146 for private field opcodes)
- **ECMAScript Spec**: Private fields (ES2022), Static blocks (ES2022)
- **Test262**: Class features test suite in QuickJS

## Design Decisions

Following QuickJS implementation:

1. **Private Symbols**: Each private field name creates a unique Symbol, stored in class scope
2. **Brand Checking**: Classes add a "brand" to instances; private field access checks the brand
3. **Initialization Order**:
   - Static fields and blocks execute in order during class definition
   - Instance fields execute in constructor before constructor body
4. **Scope**: Private field names are lexically scoped to the class body
5. **Inheritance**: Private fields are not inherited; subclasses cannot access parent's private fields

## Build Status

✅ **Current Build**: Compiles successfully with lexer, AST, parser, compiler, and VM changes
✅ **Parser Tests**: All ClassParserTest cases pass (8/8)
✅ **Compiler Tests**: All ClassCompilerTest cases pass (3/3)
✅ **Basic Class Support**: Classes with constructors and instance methods working
⏳ **Next Step**: Implement public field compilation, then private fields and static blocks

## Remaining Work ⏳
