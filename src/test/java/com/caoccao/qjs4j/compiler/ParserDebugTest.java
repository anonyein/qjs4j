package com.caoccao.qjs4j.compiler;

import com.caoccao.qjs4j.compiler.ast.Expression;
import org.junit.jupiter.api.Test;

public class ParserDebugTest {
    @Test
    public void parseObject() {
        String src = "({ 0: 'a', 0.1: 'b', length: 5 })";
        Parser parser = new Parser(new Lexer(src));
        var program = parser.parse();
        System.out.println(program);
    }
}
