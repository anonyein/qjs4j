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

package com.caoccao.qjs4j.cli;

import com.caoccao.qjs4j.core.JSRuntime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The CLI is the project's {@code mainClass}, so its behaviour is part of the shipped surface.
 * <p>
 * It used to close neither the runtime nor the context despite both being {@code AutoCloseable}, drop the filename so
 * stack traces had no source name and module detection never triggered, offer no {@code --module}, {@code --eval} or
 * {@code scriptArgs}, and exit 0 after printing a Java stack trace for an uncaught error.
 */
public class QuickJSInterpreterTest {
    private ByteArrayOutputStream capturedErr;

    private ByteArrayOutputStream capturedOut;
    private PrintStream originalErr;
    private PrintStream originalOut;
    @TempDir
    Path workingDirectory;

    private String err() {
        return capturedErr.toString(StandardCharsets.UTF_8);
    }

    private String out() {
        return capturedOut.toString(StandardCharsets.UTF_8);
    }

    @BeforeEach
    public void setUp() {
        originalOut = System.out;
        originalErr = System.err;
        capturedOut = new ByteArrayOutputStream();
        capturedErr = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capturedOut, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(capturedErr, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    public void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    public void testAHandledRejectionIsNotReported() {
        assertThat(QuickJSInterpreter.run(new String[]{"-e", """
                const late = Promise.reject(new Error('late'));
                (async () => { try { await Promise.reject(new Error('caught')) } catch (e) {} })();
                Promise.resolve().then(() => late.catch(() => {}));
                console.log('done');"""})).isZero();
        assertThat(out()).contains("done");
        assertThat(err()).doesNotContain("Uncaught");
    }

    @Test
    public void testAnUnhandledRejectionIsReportedAndExitsNonZero() {
        // Used to print nothing and exit 0; node reports it and fails.
        assertThat(QuickJSInterpreter.run(new String[]{"-e", """
                Promise.reject(new Error('truly-unhandled'));
                (async () => { throw new TypeError('async-unhandled') })();
                console.log('still runs');"""})).isEqualTo(1);
        assertThat(out()).contains("still runs");
        assertThat(err()).contains("qjs4j: Uncaught (in promise) Error: truly-unhandled")
                .contains("qjs4j: Uncaught (in promise) TypeError: async-unhandled");
    }

    @Test
    public void testBlankStackFallsBackToTheHeadline() {
        assertThat(QuickJSInterpreter.run(new String[]{"-e", "throw { message: 'blank stack', stack: '   ' }"}))
                .isEqualTo(1);
        assertThat(err()).contains("blank stack");
    }

    @Test
    public void testDeeplyNestedSourceIsReportedAsARangeError() {
        // Used to end the process with a JVM stack dump: the parser's StackOverflowError escaped
        // every engine catch and the interpreter's own last resort caught RuntimeException only.
        String deep = "(".repeat(100_000) + "1" + ")".repeat(100_000);
        assertThat(QuickJSInterpreter.run(new String[]{"-e", deep})).isEqualTo(1);
        assertThat(err()).contains("RangeError: Maximum call stack size exceeded")
                .doesNotContain("java.lang.StackOverflowError");
    }

    @Test
    public void testDeepRecursionReachesTheDefaultCallDepth() throws InterruptedException {
        // On the JVM's default thread stack this ran out at a few hundred frames.
        assertThat(QuickJSInterpreter.runWithLargeStack(
                new String[]{"-e", "function f(n) { return n === 0 ? 'bottom' : f(n - 1) } console.log(f(9900))"}))
                .isZero();
        assertThat(out()).contains("bottom");
    }

    @Test
    public void testErrorEscapingTheEngineIsReportedAndExitsNonZero() {
        // A termination signal is a java.lang.Error by design, so that a script cannot catch it.
        // The interpreter must still report it as a failure rather than die with a stack dump.
        assertThat(QuickJSInterpreter.run(new String[]{"-e", "while (true) {}"}, JSRuntime::requestInterrupt))
                .isEqualTo(1);
        assertThat(err()).contains("qjs4j: ").contains("execution interrupted");
    }

    @Test
    public void testEvalOptionEvaluatesInlineSource() {
        assertThat(QuickJSInterpreter.run(new String[]{"-e", "console.log(1 + 1)"})).isZero();
        assertThat(out()).contains("2");
    }

    @Test
    public void testHelpExitsSuccessfully() {
        assertThat(QuickJSInterpreter.run(new String[]{"--help"})).isZero();
        assertThat(err()).contains("Usage: qjs4j");
    }

    @Test
    public void testMicrotasksAreDrainedBeforeExit() throws IOException {
        Path script = writeFile("micro.js",
                "Promise.resolve('settled').then(v => console.log('promise', v));\nconsole.log('sync');\n");
        assertThat(QuickJSInterpreter.run(new String[]{script.toString()})).isZero();
        assertThat(out()).contains("sync").contains("promise settled");
    }

    @Test
    public void testMissingEvalArgumentIsAUsageError() {
        assertThat(QuickJSInterpreter.run(new String[]{"-e"})).isEqualTo(2);
        assertThat(err()).contains("requires an argument");
    }

    @Test
    public void testModuleModeResolvesImports() throws IOException {
        writeFile("dep.mjs", "export const value = 5;\n");
        Path main = writeFile("main.mjs", "import { value } from './dep.mjs';\nconsole.log('value', value);\n");
        assertThat(QuickJSInterpreter.run(new String[]{"-m", main.toString()})).isZero();
        assertThat(out()).contains("value 5");
    }

    @Test
    public void testNonStringStackFallsBackToTheHeadline() {
        assertThat(QuickJSInterpreter.run(new String[]{"-e", "throw { message: 'no stack here', stack: 42 }"}))
                .isEqualTo(1);
        assertThat(err()).contains("no stack here").doesNotContain("42");
    }

    @Test
    public void testOrdinaryErrorStillPrintsItsStack() {
        // The complement: an ordinary Error keeps stack as a data property, so the trace is still
        // reported in full.
        assertThat(QuickJSInterpreter.run(new String[]{"-e", "function boom() { throw new Error('deep') }\nboom();"}))
                .isEqualTo(1);
        assertThat(err()).contains("Error: deep").contains("    at ");
    }

    @Test
    public void testScriptArgsAreExposed() throws IOException {
        Path script = writeFile("args.js", "console.log(JSON.stringify(scriptArgs.slice(1)));\n");
        assertThat(QuickJSInterpreter.run(new String[]{script.toString(), "alpha", "beta"})).isZero();
        assertThat(out()).contains("[\"alpha\",\"beta\"]");
    }

    @Test
    public void testStackGetterOnAThrownObjectIsNotInvoked() {
        // Reporting the crash used to perform Get(error, "stack"), so the failing script chose
        // what the interpreter printed about its own failure: it could spoof the trace, work
        // indefinitely, or throw a second error out of the reporting path.
        assertThat(QuickJSInterpreter.run(new String[]{"-e", """
                throw Object.defineProperty({ message: 'real failure' }, 'stack', {
                  get() { console.log('STACK GETTER RAN'); return 'spoofed stack' },
                });"""})).isEqualTo(1);
        assertThat(out()).doesNotContain("STACK GETTER RAN");
        assertThat(err()).doesNotContain("spoofed stack");
        assertThat(err()).contains("real failure");
    }

    @Test
    public void testStackTraceCarriesTheSourceName() throws IOException {
        Path script = writeFile("named.js", "function boom() { throw new Error('inside') }\nboom();\n");
        assertThat(QuickJSInterpreter.run(new String[]{script.toString()})).isEqualTo(1);
        assertThat(err()).as("the filename must reach eval so stack traces can name the source").contains("named.js");
    }

    @Test
    public void testThrownPrimitiveIsReported() {
        assertThat(QuickJSInterpreter.run(new String[]{"-e", "throw 'a bare string'"})).isEqualTo(1);
        assertThat(err()).contains("a bare string");
    }

    @Test
    public void testThrownProxyDoesNotRunItsTrapsDuringReporting() {
        assertThat(QuickJSInterpreter.run(new String[]{"-e", """
                throw new Proxy({}, {
                  get() { console.log('GET TRAP RAN'); return 'trapped' },
                  getOwnPropertyDescriptor() { console.log('DESCRIPTOR TRAP RAN'); return undefined },
                });"""})).isEqualTo(1);
        assertThat(out()).doesNotContain("GET TRAP RAN").doesNotContain("DESCRIPTOR TRAP RAN");
    }

    @Test
    public void testTimerAndMicrotaskArgumentsAreValidated() {
        assertThat(QuickJSInterpreter.run(new String[]{"-e", """
                const names = [];
                for (const call of [() => setTimeout(1), () => setInterval(), () => queueMicrotask('x')]) {
                  try { call() } catch (e) { names.push(e.name + ': ' + e.message) }
                }
                try {
                  setTimeout(() => {}, { valueOf() { throw new Error('delay') } });
                } catch (e) { names.push(e.message) }
                // A delay that is not a number from 1 to 2^31 - 1 is 1, so these run in creation order.
                setTimeout(() => console.log('nan'), NaN);
                setTimeout(() => console.log('negative'), -5);
                setTimeout(() => console.log('huge'), 2 ** 40);
                setTimeout(() => console.log('string'), '1');
                setTimeout(() => console.log('default'));
                clearTimeout();
                clearTimeout(undefined);
                clearTimeout(12345);
                console.log(names.join(' | '));"""})).isZero();
        assertThat(out()).isEqualTo("""
                TypeError: The "callback" argument must be of type function | \
                TypeError: The "callback" argument must be of type function | \
                TypeError: The "callback" argument must be of type function | delay
                nan
                negative
                huge
                string
                default
                """);
    }

    @Test
    public void testTimersAndMicrotasksRunInNodeOrder() {
        // The CLI adds queueMicrotask and the timer functions, which scripts written for Node or browsers rely on.
        // This order is node's. The three 1 ms interval ticks have 100 ms before the next timer is due, so the order
        // holds on a loaded machine too; with 10 ms the last tick could slip behind the later timers.
        assertThat(QuickJSInterpreter.run(new String[]{"-e", """
                setTimeout(() => console.log('timeout 200'), 200);
                setTimeout((a, b) => console.log('timeout 0 args', a, b), 0, 'x', 'y');
                const cleared = setTimeout(() => console.log('cleared'), 50);
                clearTimeout(cleared);
                let count = 0;
                const interval = setInterval(() => {
                  console.log('interval', ++count);
                  if (count === 3) clearInterval(interval);
                }, 1);
                queueMicrotask(() => console.log('microtask'));
                Promise.resolve().then(() => console.log('promise'));
                setTimeout(() => {
                  Promise.resolve().then(() => console.log('promise in timer'));
                  queueMicrotask(() => console.log('micro in timer'));
                }, 100);
                console.log('sync');"""})).isZero();
        assertThat(out()).isEqualTo("""
                sync
                microtask
                promise
                timeout 0 args x y
                interval 1
                interval 2
                interval 3
                promise in timer
                micro in timer
                timeout 200
                """);
    }

    @Test
    public void testUncaughtErrorExitsNonZeroWithADiagnosticMessage() {
        assertThat(QuickJSInterpreter.run(new String[]{"-e", "throw new TypeError('boom')"})).isEqualTo(1);
        assertThat(err()).contains("TypeError: boom");
    }

    @Test
    public void testUncaughtErrorInACallbackEndsTheRun() {
        for (String code : new String[]{
                "setTimeout(() => { throw new Error('in timer') }, 1); setTimeout(() => console.log('never'), 50)", """
                        queueMicrotask(() => { throw new Error('in micro') });
                        queueMicrotask(() => { throw new Error('second') });
                        setTimeout(() => console.log('never'), 5)""",
                "setTimeout(queueMicrotask, 1); setTimeout(() => console.log('never'), 50)",
                "setTimeout(() => Promise.reject(new Error('late')), 1); setTimeout(() => console.log('never'), 30)"}) {
            capturedOut.reset();
            capturedErr.reset();
            assertThat(QuickJSInterpreter.run(new String[]{"-e", code})).as(code).isEqualTo(1);
            assertThat(out()).as(code).doesNotContain("never");
        }
        assertThat(err()).contains("qjs4j: Uncaught (in promise) Error: late");
    }

    @Test
    public void testUncaughtMicrotaskErrorStopsEveryJobBehindIt() throws IOException {
        // Node exits as soon as a queueMicrotask callback throws: no microtask, promise job, timer or module code
        // queued behind it runs. The error used to be recorded and reported after the drain, so everything behind it
        // ran first, and a job that kept requeueing itself kept the error from ever being reported.
        Path module = writeFile("failing.mjs", """
                queueMicrotask(() => { throw new Error('boom') });
                await null;
                console.log('module body after await');""");
        String[][] runs = {{"-e", """
                queueMicrotask(() => { throw new Error('boom') });
                queueMicrotask(() => console.log('microtask'));
                Promise.resolve().then(() => console.log('promise job'));
                setTimeout(() => console.log('timer'));"""}, {"-e", """
                queueMicrotask(() => { throw new Error('boom') });
                const spin = () => queueMicrotask(spin);
                queueMicrotask(spin);"""}, {"-e", """
                setTimeout(() => {
                  queueMicrotask(() => { throw new Error('boom') });
                  queueMicrotask(() => console.log('microtask in timer'));
                });
                setTimeout(() => console.log('second timer'));"""},
                // Evaluation drains the microtask queue, so here the error is raised inside the timer callback.
                {"-e", """
                        setTimeout(() => eval("queueMicrotask(() => { throw new Error('boom') })"));
                        setTimeout(() => console.log('second timer'));"""}, {"-m", module.toString()}};
        // Should the drain not stop, the spinning job is interrupted rather than left to hang the build.
        ScheduledExecutorService watchdog = Executors.newSingleThreadScheduledExecutor();
        try {
            for (String[] run : runs) {
                capturedOut.reset();
                capturedErr.reset();
                assertThat(QuickJSInterpreter.run(run,
                        runtime -> watchdog.schedule(runtime::requestInterrupt, 60, TimeUnit.SECONDS))).as(run[1])
                        .isEqualTo(1);
                assertThat(out()).as(run[1]).isEmpty();
                // The callback's own error, with its JavaScript stack, rather than the termination that carried it.
                assertThat(err()).as(run[1]).startsWith("qjs4j: Error: boom\n    at ");
            }
        } finally {
            watchdog.shutdownNow();
        }
    }

    @Test
    public void testUnknownOptionIsAUsageError() {
        assertThat(QuickJSInterpreter.run(new String[]{"--bogus"})).isEqualTo(2);
        assertThat(err()).contains("unknown option --bogus");
    }

    @Test
    public void testUnreadableFileExitsNonZero() {
        assertThat(QuickJSInterpreter.run(new String[]{workingDirectory.resolve("does-not-exist.js").toString()}))
                .isEqualTo(1);
        assertThat(err()).contains("cannot read");
    }

    private Path writeFile(String name, String content) throws IOException {
        Path path = workingDirectory.resolve(name);
        Files.writeString(path, content);
        return path;
    }
}
