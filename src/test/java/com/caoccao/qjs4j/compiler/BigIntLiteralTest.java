package com.caoccao.qjs4j.compiler;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BigIntLiteralTest {
    @Test
    public void testLexerBigInt() {
        Lexer lexer = new Lexer("123n");
        Token token = lexer.nextToken();
        assertThat(token.type()).isEqualTo(TokenType.BIGINT);
        assertThat(token.value()).isEqualTo("123");
    }

    @Test
    public void testLexerBinaryBigInt() {
        Lexer lexer = new Lexer("0b1111n");
        Token token = lexer.nextToken();
        assertThat(token.type()).isEqualTo(TokenType.BIGINT);
        assertThat(token.value()).isEqualTo("0b1111");
    }

    @Test
    public void testLexerHexBigInt() {
        Lexer lexer = new Lexer("0xFFn");
        Token token = lexer.nextToken();
        assertThat(token.type()).isEqualTo(TokenType.BIGINT);
        assertThat(token.value()).isEqualTo("0xFF");
    }

    @Test
    public void testLexerOctalBigInt() {
        Lexer lexer = new Lexer("0o77n");
        Token token = lexer.nextToken();
        assertThat(token.type()).isEqualTo(TokenType.BIGINT);
        assertThat(token.value()).isEqualTo("0o77");
    }
}
