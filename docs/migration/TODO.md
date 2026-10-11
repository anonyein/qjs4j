# qjs4j TODO

This document tracks remaining features, known bugs, and planned enhancements for qjs4j.

**Last Updated**: 2026-10-10

## Not Yet Implemented

No pending medium-priority built-in object gaps are currently tracked.

---

## Opcode Migration Status

**Summary**: High, medium, and low-priority opcode migration is complete.

### High Priority Opcodes (Core Functionality)
- Completed on 2026-02-10.

### Medium Priority Opcodes (Enhanced Functionality)
- Completed on 2026-02-10.

### Low Priority Opcodes (Optimizations)
- Completed on 2026-02-10 (includes SHORT_OPCODES 179-244, `DEC_LOC`/`INC_LOC`/`ADD_LOC`, `NOP`, and `GET_ARRAY_EL3`).

**Reference**: [OPCODE_IMPLEMENTATION_STATUS.md](OPCODE_IMPLEMENTATION_STATUS.md)

---

## Test Coverage Gaps

### Tests Needing Implementation
From [TEST262_PLAN.md](TEST262_PLAN.md) (the Test262 runner itself is integrated: `test262*` Gradle
tasks, a pinned suite revision and time zone, and the CI `test262` job):
- Async iteration integration tests (for-await-of with various iterables)
- Break/continue in nested for-await-of loops
- Error handling in async iteration

From [ASYNC_AWAIT_ENHANCEMENTS.md](ASYNC_AWAIT_ENHANCEMENTS.md):
- Destructuring patterns in for-of loops (`for (const {x, y} of items)`)
- Full promise awaiting in async loops

---

## Documentation TODOs

- [ ] Document JavaScript-Java value conversion patterns
- [ ] Add troubleshooting guide for common issues
- [ ] Create performance benchmarking guide

---

## Known Divergences from V8

Open after the 2026-10 code review; each was checked against V8 and is either deferred or a
deliberate limit.

- In an `import defer` cycle whose dependency has top-level `await`, the dependency's body runs
  twice (textual module rewriting; see the README's known limitations).
- An async generator's `return()` at a `yield` runs its `finally` one microtask early; a `throw`
  completion inside a `for await` body closes the iterator without awaiting `return()`.
- `Intl`: unit names are translated for English only; component date patterns follow CLDR
  skeletons on JDK 19+ and the date styles on JDK 17/18; the JDK's CLDR version differs from V8's
  ICU data.
- `Function.prototype.caller`/`arguments` keep the specification's `%ThrowTypeError%` accessors
  (test262 checks their identity), where V8 15 uses its own accessor functions.
- Strings are capped at 2^27 − 1 code units and data blocks below 2 GiB (see the README).
- `JSON.parse` error messages and `console` string quoting/layout differ from V8's wording.
- An instance private method or accessor cannot see its class's own binding
  (`class C { #m() { return typeof C } }` gives `undefined`): private methods are installed from
  constant templates rather than created as closures.
- In an async generator, `yield` of a rejected promise rejects the pending `next()` instead of
  throwing at the `yield`, where the generator could catch it.
- `JSContext.eval` returns `undefined` as the completion value of `if`, block, loop, `switch` and
  labelled statements (`eval()` called from JavaScript is correct).
- `super()` with a non-constructor parent and `Reflect.construct` with a non-constructor target
  report QuickJS-style messages, not V8's `Super constructor … is not a constructor`.
- `eval()` called from JavaScript, direct or indirect, drains the microtask queue before it
  returns, so promise jobs run in the middle of the calling script; V8 runs them only once the
  script has finished. `EvalRunner` drains at the end of every evaluation unless suppressed.

---

## Implementation Recommendations

### Phase 4: Advanced Features
1. Advanced Intl APIs (Segmenter, DisplayNames, full option coverage)
- AsyncDisposableStack and explicit resource management are implemented (including `using` / `await using` scope disposal semantics).

### Phase 5: Optimizations
1. Performance profiling and optimization
2. Test262 conformance improvements

---

## References

- [FEATURES.md](FEATURES.md) - Complete feature implementation status
- [MIGRATION_STATUS.md](MIGRATION_STATUS.md) - Migration progress from QuickJS C
- [OPCODE_IMPLEMENTATION_STATUS.md](OPCODE_IMPLEMENTATION_STATUS.md) - Bytecode instruction coverage
- [TESTING_NOTES.md](TESTING_NOTES.md) - Known test issues
- [TEST262_PLAN.md](TEST262_PLAN.md) - ECMAScript conformance test runner plan
- [ASYNC_AWAIT_ENHANCEMENTS.md](ASYNC_AWAIT_ENHANCEMENTS.md) - Async/await implementation details
- [PRIVATE_FIELDS_IMPLEMENTATION.md](PRIVATE_FIELDS_IMPLEMENTATION.md) - Class features status
- [ASYNC_DISPOSABLE_STACK_PLAN.md](ASYNC_DISPOSABLE_STACK_PLAN.md) - Planned ES2024 feature
