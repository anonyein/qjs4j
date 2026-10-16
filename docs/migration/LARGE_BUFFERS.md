# Large buffers and the Java embedding API

The storage migration removes the single-`byte[]` limit from ArrayBuffer, SharedArrayBuffer,
TypedArray and DataView. Byte lengths, offsets, element lengths and indices now use `long`.
JavaScript values remain Numbers; all supported buffer offsets are exact integers in that format.

## Java source and binary compatibility

Recompile embedding applications. Methods returning lengths or offsets now return `long`, and
constructors/factory methods accept `long`. Existing `int` arguments widen automatically, but
assignments of returned lengths to `int` require a checked conversion when a Java API needs one.
`IJSArrayBuffer.getBuffer()` now returns `JSByteBuffer`, replacing `java.nio.ByteBuffer`.
This is a binary-incompatible API change.

```java
long offset = 1L << 31;
try (JSRuntime runtime = new JSRuntime(new JSRuntimeOptions().setMaxDataBlockBytes(0));
     JSContext context = runtime.createContext()) {
    JSArrayBuffer buffer = context.createJSArrayBuffer(offset + 16);
    JSByteBuffer bytes = buffer.getBuffer();
    bytes.putInt(offset, 42);
    int value = bytes.getInt(offset);
}
```

`JSByteBuffer` supplies absolute primitive accessors, byte order, position/limit,
duplicate/slice, bulk byte-array access and `copyTo`. Its duplicate and sliced views share storage.
Use bulk `get(byte[])` / `put(byte[])` to interoperate with Java libraries in bounded chunks.
`array()` works only for contiguous stores. Large stores do not have one backing array and throw
`UnsupportedOperationException` for that operation. `arrayAt` and `arrayOffsetAt` expose a stable
page for internal atomic access; callers must not assume the returned page spans a multi-page view.
The position and limit of a view are not thread-safe. Shared byte access should use the engine's
Atomics operations when synchronization is required.

## Storage and resource policy

Stores of at most one MiB use contiguous byte arrays. Larger stores use lazy one-MiB pages.
Reads from absent pages return zero; writes and atomic access materialize the relevant page.
Sparse copying visits allocated pages and clears existing destination bytes, preserving zero-filled
holes. Overlapping copies use a temporary sparse store, with memmove semantics.

The configurable default data-block budget remains **64 MiB**. It measures logical reserved
capacity, including alignment padding, rather than committed physical pages or total Java heap.
Raising the budget, or setting it to zero to disable that policy, is required for multi-gigabyte buffers.
ArrayBuffer growth reserves additional capacity before allocation and rolls reservations back on
failure. Shrinking zeroes truncated bytes before later growth can expose them. Transfer detaches the
old buffer and releases its reservation. Growable SharedArrayBuffer reserves its maximum capacity;
its atomic pages retain identity while its published length grows monotonically. Aligned segmented
primitive reads and writes use the same atomic helpers as Atomics, including their fallback locks;
ordinary aligned TypedArray accesses therefore cannot tear against an atomic write.

The fixed-buffer ceiling is **32 GiB - 1**, matching sandboxed 64-bit V8. V8 checks resizable
reservations after rounding to allocation pages, so their maximum is the largest allocation-page multiple
below that ceiling. The compatibility quantum is 64 KiB on Windows, 16 KiB on macOS arm64, and 4 KiB on the other
standard Javet hosts. Windows uses its reservation granularity, not its smaller commit-page size.
Hosts built with a different V8 sandbox or OS page configuration can have a
different limit; these constants target the supported standard configuration, not every V8 build.
The one-MiB Java storage page is independent of this compatibility alignment.
See V8's [buffer limit](https://github.com/v8/v8/blob/main/src/objects/js-array-buffer.h) and
[resizable page validation](https://github.com/v8/v8/blob/main/src/objects/js-array-buffer.cc), and
[Windows allocation alignment](https://github.com/v8/v8/blob/main/src/base/platform/platform-win32.cc).

Large buffers do not imply unlimited working memory. General TypedArray sorting still needs Java
array workspace and checks its length before allocation; algorithms that inspect every element
remain proportional to element count. Dense writes can consume the host heap even if the sparse
logical representation fits. This budget is not a general heap quota.

Strings now use V8's **536,870,888 UTF-16 code-unit** ceiling. Repeat, concat, join and replacement
preserve ropes and check length before growing the result. Flattening a string still needs enough
Java heap for its characters.

## Regression evidence

`JSLargeBufferTest` compares long-offset ArrayBuffer, DataView, all TypedArray element types,
resizing, transfer, SharedArrayBuffer and Atomics with Javet/V8. It also checks argument conversion
that grows, shrinks or detaches a buffer. Java-only assertions cover sparse copies of entire
multi-gigabyte ranges and reservation release without making V8 physically copy gigabytes.
`JSByteBufferTest` compares primitive encoding with `java.nio.ByteBuffer`, checks page crossings,
views, overlapping copies, zero-filled holes, positions and invalid ranges.
