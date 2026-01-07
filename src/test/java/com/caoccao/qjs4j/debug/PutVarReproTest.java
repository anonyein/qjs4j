package com.caoccao.qjs4j.debug;

import com.caoccao.qjs4j.core.JSContext;
import com.caoccao.qjs4j.core.JSRuntime;
import com.caoccao.qjs4j.compiler.Compiler;
import com.caoccao.qjs4j.core.JSBytecodeFunction;
import com.caoccao.qjs4j.vm.Bytecode;
import com.caoccao.qjs4j.vm.Opcode;
import org.junit.jupiter.api.Test;

public class PutVarReproTest {
    @Test
    public void reproPutVarUnderflow() {
        try (JSRuntime runtime = new JSRuntime(); JSContext context = runtime.createContext()) {
            String code = "var BI_RC = new Array();\n"
                    + "var rr, vv;\n"
                    + "rr = \"0\".charCodeAt(0);\n"
                    + "for (vv = 0; vv <= 9; ++vv) BI_RC[rr++] = vv;\n"
                    + "rr = \"a\".charCodeAt(0);\n"
                    + "for (vv = 10; vv < 36; ++vv) BI_RC[rr++] = vv;\n"
                    + "rr = \"A\".charCodeAt(0);\n"
                    + "for (vv = 10; vv < 36; ++vv) BI_RC[rr++] = vv;\n\n"
                    + "function intAt(s, i) {\n"
                    + "    var c = BI_RC[s.charCodeAt(i)];\n"
                    + "    return (c == null) ? -1 : c;\n"
                    + "}\n"
                    + "console.log(BI_RC);\n";

            try {
                // Compile and dump bytecode for inspection
                JSBytecodeFunction func = Compiler.compile(code, "<repro>");
                Bytecode bytecode = func.getBytecode();
                System.out.println("Disassembly:");
                for (int i = 0; i < bytecode.getLength(); ) {
                    int opc = bytecode.readOpcode(i);
                    Opcode op = Opcode.fromInt(opc);
                    System.out.printf("%04d: %s\n", i, op.name());
                    i += op.getSize();
                }

                context.eval(code);
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }
    }
}
