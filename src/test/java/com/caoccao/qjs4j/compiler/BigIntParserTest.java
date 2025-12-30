package com.caoccao.qjs4j.compiler;

import com.caoccao.qjs4j.compiler.ast.ExpressionStatement;
import com.caoccao.qjs4j.compiler.ast.Literal;
import com.caoccao.qjs4j.compiler.ast.Program;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class BigIntParserTest {
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
