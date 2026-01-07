package com.caoccao.qjs4j.debug;

import com.caoccao.qjs4j.compiler.Compiler;
import com.caoccao.qjs4j.core.JSBytecodeFunction;
import com.caoccao.qjs4j.core.JSValue;
import com.caoccao.qjs4j.vm.Bytecode;
import com.caoccao.qjs4j.vm.Opcode;
import org.junit.jupiter.api.Test;

public class DumpClassBytecodeTest {
    @Test
    public void dumpClassWithMultipleStaticBlocks() throws Exception {
        String code = "class Test {\n"
                + "    static x = 0;\n"
                + "    static {\n"
                + "        this.x = 10;\n"
                + "    }\n"
                + "    static {\n"
                + "        this.x = this.x + 5;\n"
                + "    }\n"
                + "}\n"
                + "Test.x";

        JSBytecodeFunction func = Compiler.compile(code, "<dump>");
        Bytecode bytecode = func.getBytecode();

        System.out.println("---- Atom Pool ----");
        String[] atoms = bytecode.getAtoms();
        if (atoms != null) {
            for (int i = 0; i < atoms.length; i++) {
                System.out.printf("[%d] %s\n", i, atoms[i]);
            }
        }

        System.out.println("---- Constant Pool ----");
        JSValue[] consts = bytecode.getConstants();
        if (consts != null) {
            for (int i = 0; i < consts.length; i++) {
                JSValue v = consts[i];
                System.out.printf("[%d] %s (%s)\n", i, v == null ? "null" : v.toString(), v == null ? "null" : v.getClass().getSimpleName());
            }
        }

        System.out.println("---- Disassembly ----");
        for (int pc = 0; pc < bytecode.getLength(); ) {
            int opc = bytecode.readOpcode(pc);
            Opcode op = Opcode.fromInt(opc);
            System.out.printf("%04d: %s", pc, op.name());
            int size = op.getSize();
            if (size > 1) {
                System.out.print("   ");
                for (int j = 1; j < size; j++) {
                    int b = bytecode.readU8(pc + j);
                    System.out.printf(" %02X", b);
                }
            }
            System.out.println();
            pc += size;
        }
    }
}
