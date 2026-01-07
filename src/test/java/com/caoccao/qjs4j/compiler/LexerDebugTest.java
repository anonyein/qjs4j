package com.caoccao.qjs4j.compiler;

import org.junit.jupiter.api.Test;

public class LexerDebugTest {
    @Test
    public void dumpTokens() {
        String src = "Array.prototype.join.call({ 0: 'a', 0.1: 'b', length: 5 }, '-')";
        Lexer lexer = new Lexer(src);
        Token token;
        while ((token = lexer.nextToken()).type() != TokenType.EOF) {
            System.out.println(token.type() + " -> '" + token.value() + "' at " + token.line() + ":" + token.column());
        }
    }
}
