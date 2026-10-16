package com.caoccao.qjs4j.core;

import com.caoccao.qjs4j.BaseJavetTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Regressions for the module environment defects originally documented in review 02. */
class JSModuleEnvironmentTest extends BaseJavetTest {
    @TempDir
    Path moduleDirectory;

    private void assertModuleWithJavet(String source, String probe) throws IOException {
        Path entry = moduleDirectory.resolve("main.mjs");
        Files.writeString(entry, source);
        assertWithJavet(() -> {
            v8Runtime.resetContext();
            v8Runtime.setV8ModuleResolver((runtime, name, referrer) -> {
                Path path = moduleDirectory.resolve(name).normalize();
                try {
                    return runtime.getExecutor(Files.readString(path)).setResourceName(path.toString())
                            .compileV8Module();
                } catch (IOException error) {
                    throw new UncheckedIOException(error);
                }
            });
            v8Runtime.getExecutor(source).setResourceName(entry.toString()).setModule(true).executeVoid();
            v8Runtime.await();
            try {
                return v8Runtime.getExecutor(probe).executeString();
            } finally {
                v8Runtime.removeV8Modules(true);
            }
        }, () -> {
            resetContext().eval(source, entry.toString(), true);
            context.processMicrotasks();
            return context.eval(probe).toString();
        });
    }

    @Test
    void asyncModuleClosuresReadLiveImportsAfterSuspension() throws IOException {
        Files.writeString(moduleDirectory.resolve("dep.mjs"), "export let x=1;export function inc(){x++}");
        assertModuleWithJavet("""
                import {x,inc} from './dep.mjs';
                await Promise.resolve();
                inc();globalThis.read=()=>x;globalThis.bump=inc;export const after=x;
                """, "(()=>{const first=read();bump();return JSON.stringify([first,read()])})()");
    }

    @Test
    void deferredCycleEvaluatesAsyncDependencyOnce() throws IOException {
        Files.writeString(moduleDirectory.resolve("dep.mjs"), """
                import './main.mjs';
                globalThis.runs=(globalThis.runs||0)+1;
                await Promise.resolve();
                export const x=42;
                """);
        // Javet's V8 build disables import-defer syntax, so this proposal uses an explicit semantic assertion.
        String source = "import defer * as dep from './dep.mjs'; globalThis.read=()=>dep.x;";
        Files.writeString(moduleDirectory.resolve("main.mjs"), source);
        context.eval(source, moduleDirectory.resolve("main.mjs").toString(), true);
        context.processMicrotasks();
        assertThat(context.eval("JSON.stringify([runs,read()])").toString()).isEqualTo("[1,42]");
    }

    @Test
    void generatedBindingsAreNotVisibleToEval() throws IOException {
        assertModuleWithJavet("""
                export const x=1;
                globalThis.probe=eval('typeof __q'+'js4jModuleExports');
                globalThis.read=()=>eval('typeof __q'+'js4jModuleExports');
                """, "JSON.stringify([probe,read(),Object.keys(globalThis).filter(k=>k.startsWith('__qjs4j'))])");
    }

    @Test
    void namespaceImportsOutliveModuleEvaluation() throws IOException {
        Files.writeString(moduleDirectory.resolve("dep.mjs"), "export let x=1;export function inc(){x++}");
        assertModuleWithJavet("""
                import * as ns from './dep.mjs';
                globalThis.read=()=>ns.x;globalThis.bump=()=>ns.inc();
                """, "(()=>{const first=read();bump();return JSON.stringify([first,read()])})()");
    }

    @Test
    void retainedImportsAreLiveAndImmutable() throws IOException {
        Files.writeString(moduleDirectory.resolve("dep.mjs"), "export let x=1;export function inc(){x++}");
        assertModuleWithJavet("""
                import {x,inc} from './dep.mjs';
                globalThis.readX=()=>x;globalThis.bumpX=inc;
                globalThis.writeX=()=>{try{x=10}catch(e){return e.name}};
                globalThis.updateX=()=>{try{x++}catch(e){return e.name}};
                globalThis.evalWriteX=()=>{try{eval('x=10')}catch(e){return e.name}};
                globalThis.evalX=()=>eval('x');
                globalThis.indirectX=()=> (0,eval)('typeof x');
                globalThis.shadowX=()=>{let x=9;return eval('x')};
                """,
                """
                        (()=>{const first=readX();bumpX();return JSON.stringify([first,readX(),evalX(),writeX(),updateX(),evalWriteX(),indirectX(),shadowX(),Object.hasOwn(globalThis,'x')])})()
                        """);
    }

    @Test
    void unsupportedImportTypeFailsBeforeBody() throws IOException {
        Files.writeString(moduleDirectory.resolve("dep.mjs"), "globalThis.depRan=true;export const x=1;");
        Path entry = moduleDirectory.resolve("main.mjs");
        String source = "import {x} from './dep.mjs' with {type:'javascript'};globalThis.ran=true;";
        // Import attributes are a host contract: the native V8 resolver leaves validation to its embedder.
        assertThatThrownBy(() -> context.eval(source, entry.toString(), true)).hasMessageContaining("Import attribute")
                .hasMessageContaining("not supported");
        assertThat(context.eval("typeof ran+','+typeof depRan").toString()).isEqualTo("undefined,undefined");
    }
}
