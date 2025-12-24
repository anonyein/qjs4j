# qjs4j

[![Build and Test](https://github.com/caoccao/qjs4j/workflows/Build/badge.svg)](https://github.com/caoccao/qjs4j/actions) [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

qjs4j is a native Java implementation of QuickJS.

## Project Summary

qjs4j is a complete reimplementation of the QuickJS JavaScript engine in pure Java, targeting JDK 17 with no external dependencies. It aims to provide full ES2020 compliance, including modern features like modules, BigInt, Symbol, and a comprehensive regular expression engine.

### Key Features

- Full JavaScript ES2020 specification support
- Bytecode compilation and execution with 244+ opcodes
- Module system (import/export)
- Built-in regular expression engine
- Unicode support and normalization
- Command-line tools (interpreter, compiler)

### Architecture

The implementation is organized into several key packages:

- **core**: Core runtime components (JSRuntime, JSContext, JSValue, etc.)
- **compiler**: Parser, lexer, bytecode compiler, and AST
- **vm**: Virtual machine, bytecode execution, and stack management
- **memory**: Garbage collection and memory management
- **types**: Advanced types (BigInt, Symbol, Promise, etc.)
- **builtins**: JavaScript built-in objects and prototypes
- **regexp**: Regular expression engine
- **unicode**: Unicode support
- **util**: Utility classes
- **cli**: Command-line tools

This project represents a significant undertaking to bring the efficiency and features of QuickJS to the Java ecosystem.

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.
