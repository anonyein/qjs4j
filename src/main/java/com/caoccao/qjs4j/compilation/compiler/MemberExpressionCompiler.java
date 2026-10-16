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

package com.caoccao.qjs4j.compilation.compiler;

import com.caoccao.qjs4j.compilation.ast.Identifier;
import com.caoccao.qjs4j.compilation.ast.MemberExpression;
import com.caoccao.qjs4j.compilation.ast.PrivateIdentifier;
import com.caoccao.qjs4j.core.JSSymbol;
import com.caoccao.qjs4j.exceptions.JSSyntaxErrorException;
import com.caoccao.qjs4j.vm.Opcode;

final class MemberExpressionCompiler extends AstNodeCompiler<MemberExpression> {
    MemberExpressionCompiler(CompilerContext compilerContext) {
        super(compilerContext);
    }

    @Override
    void compile(MemberExpression memberExpr) {
        boolean joinsOptionalChain = compilerContext.emitHelpers.consumeOptionalChainLink(memberExpr);
        if (memberExpr.getObject().isSuperIdentifier()) {
            compilerContext.emitHelpers.emitGetSuperValue(memberExpr, false);
            return;
        }
        if (!joinsOptionalChain && memberExpr.isPartOfOptionalChain()) {
            // The last link of an optional chain: every `?.` in it exits here.
            compilerContext.emitHelpers.compileOptionalChain(() -> compileLink(memberExpr), null);
        } else {
            compileLink(memberExpr);
        }
    }

    private void compileLink(MemberExpression memberExpr) {
        // Stack: -> value
        compilerContext.emitHelpers.compileOptionalChainLinkTarget(memberExpr, memberExpr.getObject());
        if (memberExpr.isOptional()) {
            compilerContext.emitHelpers.emitOptionalChainTest(1);
        }
        emitPropertyAccess(memberExpr);
    }

    private void emitPropertyAccess(MemberExpression memberExpr) {
        if (memberExpr.isComputed()) {
            compilerContext.expressionCompiler.compile(memberExpr.getProperty());
            compilerContext.emitHelpers.emitMemberSourcePosition(memberExpr);
            compilerContext.emitter.emitOpcode(Opcode.GET_ARRAY_EL);
        } else if (memberExpr.getProperty() instanceof PrivateIdentifier privateId) {
            String fieldName = privateId.getName();
            JSSymbol symbol = compilerContext.privateSymbols != null
                    ? compilerContext.privateSymbols.get(fieldName)
                    : null;
            if (symbol != null) {
                compilerContext.emitter.emitOpcodeConstant(Opcode.PUSH_CONST, symbol);
                compilerContext.emitHelpers.emitMemberSourcePosition(memberExpr);
                compilerContext.emitter.emitOpcode(Opcode.GET_PRIVATE_FIELD);
            } else {
                throw new JSSyntaxErrorException("Unexpected private field", privateId.getLocation());
            }
        } else if (memberExpr.getProperty() instanceof Identifier propId) {
            // A failing property read reports the position of the property name, as V8 does.
            compilerContext.emitHelpers.emitMemberSourcePosition(memberExpr);
            compilerContext.emitter.emitOpcodeAtom(Opcode.GET_FIELD, propId.getName());
        }
    }
}
