package com.caoccao.qjs4j.core;

import com.caoccao.qjs4j.BaseJavetTest;
import org.junit.jupiter.api.Test;

class Review02LanguageTest extends BaseJavetTest {
    @Test
    void asyncFromSyncClosesOnlyWhenAwaitingAnUnfinishedValueRejects() {
        for (String value : new String[]{"Promise.reject('value')", "{get then(){throw 'then'}}",
                "Object.defineProperty(Promise.resolve(1),'constructor',{get(){throw 'constructor'}})"}) {
            for (boolean done : new boolean[]{false, true}) {
                for (String close : new String[]{"log.push('return');return {done:true}",
                        "log.push('return');throw 'close'"}) {
                    assertStringWithJavet("""
                            (async()=>{const log=[],it={ [Symbol.iterator](){return this},
                            next(){return {value:%s,done:%s}},return(){%s}};
                            try{for await(const x of it){log.push('body')}}catch(e){log.push(String(e))}
                            return JSON.stringify(log)})()
                            """.formatted(value, done, close));
                }
            }
        }
    }

    @Test
    void asyncIteratorClosePreservesOriginalThrowForSynchronousFailures() {
        for (String close : new String[]{"get return(){throw 'getter'}", "return(){throw 'method'}", "return:1",
                "return:undefined", "return:new Proxy(function(){return Promise.resolve({done:true})},{})"}) {
            assertStringWithJavet("""
                    (async()=>{const log=[],it={ [Symbol.asyncIterator](){return this},
                    next(){return Promise.resolve({value:1,done:false})},%s};
                    try{for await(const x of it){throw 'body'}}catch(e){log.push(String(e))}
                    return JSON.stringify(log)})()
                    """.formatted(close));
        }
    }

    @Test
    void asyncYieldDoesNotAddAnotherQueueTurnAfterAwait() {
        assertStringWithJavet("""
                (()=>{const log=[];async function* f(){log.push('start');yield 123}
                const done=Promise.resolve().then(()=>log.push('tick1')).then(()=>log.push('tick2'))
                  .then(()=>JSON.stringify(log));const it=f();it.next();
                it.return({get then(){log.push('then')}});return done})()
                """, """
                (async()=>{const log=[];async function* f(){await using x={
                  [Symbol.asyncDispose](){log.push('disposed')}};yield x}
                const g=f();await g.next();g.next();log.push('after');
                await Promise.resolve();return JSON.stringify(log)})()
                """);
    }

    @Test
    void classDeclarationAndExpressionShareInitializationSemantics() {
        for (String declaration : new String[]{"class C", "const C=class C"}) {
            assertStringWithJavet("""
                    (()=>{const log=[];class B{m(){return 'base'}};%s extends B {
                      #x=3;static x=log.push('static');static {log.push(this.name)}
                      ['m'](){return super.m()+this.#x} [log.push('key')]=4;
                    };const c=new C();return JSON.stringify([log,c.m(),c[1],C.name]);})()
                    """.formatted(declaration));
        }
    }

    @Test
    void dollarNamesRemainVisibleToDirectEval() {
        assertIntegerWithJavet("(()=>{let $x=7;return (()=>eval('$x'))()})()",
                "(()=>{let $x=7;return (()=>(()=>eval('$x')))()()})()");
    }

    @Test
    void forAwaitDoesNotCloseWhenNextOrItsResultFails() {
        for (String next : new String[]{"throw 'next'", "return Promise.reject('next')",
                "return Promise.resolve({get done(){throw 'done'}})",
                "return Promise.resolve({done:false,get value(){throw 'value'}})"}) {
            assertStringWithJavet("""
                    (async()=>{const log=[],it={ [Symbol.asyncIterator](){return this},next(){%s},
                    return(){log.push('return');return {done:true}}};
                    try{for await(const x of it){log.push('body')}}catch(e){log.push(String(e))}
                    return JSON.stringify(log)})()
                    """.formatted(next));
        }
        assertStringWithJavet("""
                (()=>{const log=[];async function f(){const p=Promise.resolve(0);
                Object.defineProperty(p,'constructor',{get(){throw 'constructor'}});
                log.push('start');for await(const x of [p]);}
                const done=Promise.resolve().then(()=>log.push('tick1')).then(()=>log.push('tick2'))
                  .then(()=>JSON.stringify(log));f().catch(()=>log.push('catch'));return done})()
                """);
    }

    @Test
    void nestedEvalDoesNotDrainMicrotasks() {
        assertStringWithJavet("""
                (()=>{const log=[];Promise.resolve().then(()=>log.push('then'));eval('1');log.push('after');
                return JSON.stringify(log)})()
                """, """
                (()=>{const log=[];Promise.resolve().then(()=>log.push('then'));(0,eval)('1');log.push('after');
                return JSON.stringify(log)})()
                """);
    }

    @Test
    void nestedTryCompletionsPreserveHandlerStackAndEmptyValues() {
        assertIntegerWithJavet("try{try{42}finally{7}}catch(e){99}", "try{try{throw 1}catch(e){42}}catch(e){99}",
                "try{42;var x;}catch(e){99}", "try{if(true)42;}catch(e){99}",
                "try{throw 1}catch(e){try{throw 2}catch(e){42}}",
                "let n=0;try{try{throw 1}finally{try{n=7;throw 2}catch(e){n++}}}catch(e){n+=e}n");
    }

    @Test
    void privateClosuresShareTheInstancesPrivateBrand() {
        for (String form : new String[]{"class C", "const C=class"}) {
            assertStringWithJavet("""
                    (()=>{function make(n){%s {#$_=n; #m(){return this.#$_}
                    get #v(){return this.#m()} set #v(v){this.#$_=v}
                    read(){return this.#v} write(v){this.#v=v} method(){return this.#m}}
                    return new C()};const a=make(1),b=make(2);a.write(7);
                    const out=[a.read(),b.read()];try{a.method().call(b)}catch(e){out.push(e.name)}
                    return JSON.stringify(out)})()
                    """.formatted(form));
        }
    }

    @Test
    void privateMethodsCaptureTheirClassEnvironment() {
        for (String declaration : new String[]{"class C", "const C=class C"}) {
            assertStringWithJavet(
                    """
                            (()=>{function make(x){%s {
                              #m(){return [x,typeof C]}
                              get #v(){return x} set #v(v){x=v}
                              read(){return this.#m()} get(){return this.#v} set(v){this.#v=v}
                              method(){return this.#m}
                            };return [new C(),new C()]}
                            const [a,b]=make(1),[c,d]=make(2);a.set(7);
                            return JSON.stringify([a.read(),b.get(),c.read(),d.get(),a.method()===b.method(),a.method()===c.method()]);})()
                            """
                            .formatted(declaration));
        }
    }

    @Test
    void rejectedAsyncYieldThrowsInsideGenerator() {
        for (String value : new String[]{"Promise.reject('x')", "{then(r,j){j('x')}}", "{get then(){throw 'x'}}"}) {
            assertStringWithJavet(
                    """
                            (async()=>{const log=[];async function* f(){try{yield %s}catch(e){log.push(e);yield 'caught'}finally{log.push('finally')}}
                            const g=f();const a=await g.next(),b=await g.next();return JSON.stringify([a,b,log])})()
                            """
                            .formatted(value));
        }
    }

    @Test
    void scriptCompletionMatchesEval() {
        assertIntegerWithJavet("if(true)42;", "{42;}", "42;var x;", "for(let i=0;i<3;i++){i;}", "try{42;}finally{7;}",
                "switch(1){case 1:42;break;}");
        assertUndefinedWithJavet("42;if(false)7;", "42;while(false)7;");
    }

    @Test
    void sharedIteratorMethods() {
        assertStringWithJavet("""
                JSON.stringify([[][Symbol.iterator](),new Map().entries(),new Set().values(),''[Symbol.iterator]()]
                .map(i=>[Object.hasOwn(i,Symbol.iterator),i[Symbol.iterator]()===i,typeof i[Symbol.iterator].call]))
                """, """
                (async()=>{async function* f(){yield 1;yield 2}const a=f(),b=f();
                const result=[Object.keys(a),typeof a.next.call,Object.hasOwn(a,Symbol.asyncIterator),
                    await Reflect.apply(a.next,b,[]),await a.next()];
                try{await a.next.call({})}catch(e){result.push(e.name)}
                return JSON.stringify(result)})()
                """);
    }

    @Test
    void throwingForAwaitBodyAwaitsCleanup() {
        for (String result : new String[]{"{then(resolve){log.push('closed');resolve({done:true})}}",
                "Promise.resolve().then(()=>{log.push('closed');return {done:true}})", "Promise.reject('cleanup')",
                "{then(resolve,reject){log.push('closed');reject('cleanup')}}", "1"}) {
            assertStringWithJavet("""
                    (async()=>{const log=[];const it={
                      [Symbol.asyncIterator](){return this},next(){return Promise.resolve({value:1,done:false})},
                      return(){log.push('return');return %s}
                    };try{for await(const x of it){throw 'body'}}catch(e){log.push('catch:'+e)}
                    return JSON.stringify(log)})()
                    """.formatted(result));
        }
        assertStringWithJavet("""
                (async()=>{const log=[];function it(n){return {
                [Symbol.asyncIterator](){return this},next(){return Promise.resolve({value:1,done:false})},
                return(){return {then(r){log.push(n);r({done:true})}}}}}
                try{for await(const x of it('outer'))for await(const y of it('inner'))throw 'body'}
                catch(e){log.push(e)}return JSON.stringify(log)})()
                """);
    }

    @Test
    void usingSwitchValidationDoesNotDependOnTestMarker() {
        for (String marker : new String[]{"", "// $DONOTEVALUATE\n"}) {
            for (String statement : new String[]{"using x=null;", "await using x=null;"}) {
                String source = marker + "switch(1){case 1:" + statement + "}";
                assertStringWithJavet("(()=>{try{Object.getPrototypeOf(async function(){}).constructor(" + "'"
                        + source.replace("\n", "\\n") + "');return 'accepted'}catch(e){return e.name}})()");
            }
        }
        assertStringWithJavet("(()=>{switch(1){case 1:{using x=null;return 'ok'}}})()");
    }
}
