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

package com.caoccao.qjs4j.regexp;

import com.caoccao.qjs4j.util.DynamicBuffer;

import java.util.ArrayList;
import java.util.List;

/**
 * Compiles regex patterns to bytecode.
 * Based on QuickJS libregexp.c compiler.
 * <p>
 * This is a simplified initial implementation that handles basic patterns.
 * Full ES2020 regex syntax support will be added incrementally.
 */
public final class RegExpCompiler {

    private int captureCount;
    private List<String> groupNames;

    /**
     * Compile a regex pattern to bytecode.
     *
     * @param pattern The regex pattern string
     * @param flags   The regex flags (g, i, m, s, u, y, d, v)
     * @return Compiled bytecode
     * @throws RegExpSyntaxException if the pattern is invalid
     */
    public RegExpBytecode compile(String pattern, String flags) {
        if (pattern == null) {
            throw new RegExpSyntaxException("Pattern cannot be null");
        }

        int flagBits = RegExpBytecode.parseFlags(flags);
        captureCount = 1; // Capture group 0 (the entire match)
        groupNames = new ArrayList<>();

        DynamicBuffer buffer = new DynamicBuffer(256);
        CompileContext context = new CompileContext(pattern, flagBits, buffer);

        try {
            compilePattern(context);
            // End with MATCH opcode
            buffer.appendU8(RegExpOpcode.MATCH.getCode());

            return new RegExpBytecode(
                    buffer.toByteArray(),
                    flagBits,
                    captureCount,
                    groupNames.isEmpty() ? null : groupNames.toArray(new String[0])
            );
        } catch (Exception e) {
            throw new RegExpSyntaxException("Failed to compile pattern: " + e.getMessage(), e);
        }
    }

    private void compileAlternative(CompileContext context) {
        // An alternative is a sequence of terms
        while (context.pos < context.codePoints.length) {
            int ch = context.codePoints[context.pos];

            switch (ch) {
                case '^' -> {
                    context.buffer.appendU8(context.isMultiline() ?
                            RegExpOpcode.LINE_START_M.getCode() :
                            RegExpOpcode.LINE_START.getCode());
                    context.pos++;
                }

                case '$' -> {
                    context.buffer.appendU8(context.isMultiline() ?
                            RegExpOpcode.LINE_END_M.getCode() :
                            RegExpOpcode.LINE_END.getCode());
                    context.pos++;
                }

                case '.' -> {
                    context.buffer.appendU8(context.isDotAll() ?
                            RegExpOpcode.ANY.getCode() :
                            RegExpOpcode.DOT.getCode());
                    context.pos++;
                }

                case '\\' -> {
                    context.pos++;
                    compileEscape(context);
                }

                case '[' -> {
                    compileCharacterClass(context);
                }

                case '(' -> {
                    compileGroup(context);
                }

                case ')' -> {
                    // End of group
                    return;
                }

                case '*', '+', '?', '{' -> {
                    // Quantifiers - not implemented yet
                    throw new RegExpSyntaxException("Quantifiers not yet implemented");
                }

                case '|' -> {
                    // Alternation - not implemented yet
                    throw new RegExpSyntaxException("Alternation not yet implemented");
                }

                default -> {
                    // Literal character
                    compileLiteralChar(context, ch);
                    context.pos++;
                }
            }
        }
    }

    private void compileCharacterClass(CompileContext context) {
        // Character classes [abc], [^abc], [a-z]
        // Not fully implemented yet
        throw new RegExpSyntaxException("Character classes not yet fully implemented");
    }

    private void compileDisjunction(CompileContext context) {
        // A disjunction is a sequence of alternatives separated by |
        // For now, we just compile a sequence (no | support yet)
        compileAlternative(context);
    }

    private void compileEscape(CompileContext context) {
        if (context.pos >= context.codePoints.length) {
            throw new RegExpSyntaxException("Incomplete escape sequence");
        }

        int ch = context.codePoints[context.pos++];
        switch (ch) {
            case 'n' -> compileLiteralChar(context, '\n');
            case 'r' -> compileLiteralChar(context, '\r');
            case 't' -> compileLiteralChar(context, '\t');
            case 'f' -> compileLiteralChar(context, '\f');
            case 'v' -> compileLiteralChar(context, '\u000B');
            case 'd', 'D', 'w', 'W', 's', 'S' -> {
                // Character class escapes - not fully implemented
                throw new RegExpSyntaxException("Character class escapes not yet implemented");
            }
            case 'b', 'B' -> {
                // Word boundary - not implemented
                throw new RegExpSyntaxException("Word boundary not yet implemented");
            }
            default -> {
                // Literal escaped character
                compileLiteralChar(context, ch);
            }
        }
    }

    private void compileGroup(CompileContext context) {
        // Capture groups (...)
        context.pos++; // Skip '('

        int groupIndex = captureCount++;

        // Save start
        context.buffer.appendU8(RegExpOpcode.SAVE_START.getCode());
        context.buffer.appendU8(groupIndex);

        // Compile group contents
        compileDisjunction(context);

        // Save end
        context.buffer.appendU8(RegExpOpcode.SAVE_END.getCode());
        context.buffer.appendU8(groupIndex);

        if (context.pos >= context.codePoints.length || context.codePoints[context.pos] != ')') {
            throw new RegExpSyntaxException("Unclosed group");
        }
        context.pos++; // Skip ')'
    }

    private void compileLiteralChar(CompileContext context, int ch) {
        if (ch <= 0xFFFF) {
            context.buffer.appendU8(context.isIgnoreCase() ?
                    RegExpOpcode.CHAR_I.getCode() :
                    RegExpOpcode.CHAR.getCode());
            context.buffer.appendU16(ch);
        } else {
            context.buffer.appendU8(context.isIgnoreCase() ?
                    RegExpOpcode.CHAR32_I.getCode() :
                    RegExpOpcode.CHAR32.getCode());
            context.buffer.appendU32(ch);
        }
    }

    private void compilePattern(CompileContext context) {
        // Save start of capture group 0
        context.buffer.appendU8(RegExpOpcode.SAVE_START.getCode());
        context.buffer.appendU8(0); // Capture group 0

        compileDisjunction(context);

        // Save end of capture group 0
        context.buffer.appendU8(RegExpOpcode.SAVE_END.getCode());
        context.buffer.appendU8(0); // Capture group 0
    }

    private static class CompileContext {
        final DynamicBuffer buffer;
        final int[] codePoints;
        final int flags;
        final String pattern;
        int pos;

        CompileContext(String pattern, int flags, DynamicBuffer buffer) {
            this.pattern = pattern;
            this.codePoints = pattern.codePoints().toArray();
            this.flags = flags;
            this.buffer = buffer;
            this.pos = 0;
        }

        boolean isDotAll() {
            return (flags & RegExpBytecode.FLAG_DOTALL) != 0;
        }

        boolean isIgnoreCase() {
            return (flags & RegExpBytecode.FLAG_IGNORECASE) != 0;
        }

        boolean isMultiline() {
            return (flags & RegExpBytecode.FLAG_MULTILINE) != 0;
        }

        boolean isUnicode() {
            return (flags & RegExpBytecode.FLAG_UNICODE) != 0;
        }
    }

    /**
     * Exception thrown when regex pattern has syntax errors.
     */
    public static class RegExpSyntaxException extends RuntimeException {
        public RegExpSyntaxException(String message) {
            super(message);
        }

        public RegExpSyntaxException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
