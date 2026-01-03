package com.caoccao.qjs4j.compiler.ast;

import com.caoccao.qjs4j.compiler.Lexer;
import com.caoccao.qjs4j.compiler.Parser;
import com.caoccao.qjs4j.compiler.Token;
import com.caoccao.qjs4j.compiler.TokenType;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class LiteralTest {
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

    @Test
    public void testParseBigIntLiteral() {
        Lexer lexer = new Lexer("123n");
        Parser parser = new Parser(lexer);
        Program program = parser.parse();

        assertThat(program.body()).hasSize(1);
        assertThat(program.body().get(0)).isInstanceOfSatisfying(ExpressionStatement.class, exprStmt -> {
            assertThat(exprStmt.expression()).isInstanceOfSatisfying(Literal.class, literal -> {
                assertThat(literal.value()).isInstanceOf(BigInteger.class);
                assertThat(literal.value()).isEqualTo(BigInteger.valueOf(123));
            });
        });
    }

    @Test
    public void testParseHexBigIntLiteral() {
        Lexer lexer = new Lexer("0xFFn");
        Parser parser = new Parser(lexer);
        Program program = parser.parse();

        assertThat(program.body()).hasSize(1);
        assertThat(program.body().get(0)).isInstanceOfSatisfying(ExpressionStatement.class, exprStmt -> {
            assertThat(exprStmt.expression()).isInstanceOfSatisfying(Literal.class, literal -> {
                assertThat(literal.value()).isEqualTo(BigInteger.valueOf(255));
            });
        });
    }
}
