/*
 * Simple unit test to run a given JavaScript snippet and assert the result.
 */
package com.caoccao.qjs4j.examples;

import com.caoccao.qjs4j.BaseTest;
import com.caoccao.qjs4j.core.JSArray;
import com.caoccao.qjs4j.core.JSFunction;
import com.caoccao.qjs4j.core.JSNumber;
import com.caoccao.qjs4j.core.JSValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RunProvidedJsTest extends BaseTest {

    @Test
    void testProvidedScriptProducesExpectedResult() {
        String code = """
                var BI_RC = new Array();
                BI_RC._dbgId = 'BI_RC_main';
                var rr, vv;
                rr = "0".charCodeAt(0);
                console.log('DBG rr initial type=', typeof rr, rr);
                for (vv = 0; vv <= 9; ++vv) { var _idx = rr++; BI_RC[_idx] = vv; console.log('ASSIGN_LOOP0', _idx, vv, typeof BI_RC[_idx]); }
                rr = "a".charCodeAt(0);
                for (vv = 10; vv < 36; ++vv) { var _idx = rr++; BI_RC[_idx] = vv; console.log('ASSIGN_LOOP1', _idx, vv, typeof BI_RC[_idx]); }
                rr = "A".charCodeAt(0);
                for (vv = 10; vv < 36; ++vv) { var _idx = rr++; BI_RC[_idx] = vv; console.log('ASSIGN_LOOP2', _idx, vv, typeof BI_RC[_idx]); }

                console.log(JSON.stringify(BI_RC)); // Example usage
                console.log("DBG BI_RC[0]=", BI_RC["0"], "type", typeof BI_RC["0"]);
                console.log("DBG BI_RC[48]=", BI_RC[48], "type", typeof BI_RC[48]);
                // Direct assignment sanity checks
                var __A = new Array();
                __A[1] = 5;
                console.log('DIRECT_ASSIGN', JSON.stringify(__A), typeof __A[1]);
                __A['2'] = 6;
                console.log('DIRECT_ASSIGN_STR', JSON.stringify(__A), typeof __A[2]);
                Object.prototype.inheritsFrom = function (shuper) {
                  try {
                    function Inheriter() { }
                    Inheriter.prototype = shuper.prototype;
                    this.prototype = new Inheriter();
                    this.superConstructor = shuper;
                  } catch (error) {
                    console.error("Inheritance error: " + error + error.stack);
                  }
                }

                function UnaryConstraint(v, strength) {
                  try {
                    UnaryConstraint.superConstructor.call(this, strength);
                    this.myOutput = v;
                    this.satisfied = false;
                  } catch (error) {
                    console.error("Inheritance error: " + error + error.stack);
                  }
                }

                function Constraint(strength) {
                  this.strength = strength;
                }

                UnaryConstraint.inheritsFrom(Constraint)

                function plus(i, j) {
                  return i + j;
                }
                var res = 0;
                for (var i = 1, j = 1; j < 10; i++, j++) {
                  res += i + j;
                }
                console.log("测试" + plus(i, j));
                res
                """;

        JSValue value = context.eval(code);

        // Assert final returned value (res)
        assertThat(value).isInstanceOfSatisfying(JSNumber.class, jsNumber -> {
          assertThat(jsNumber.value()).isEqualTo(90.0);
        });

        // Validate BI_RC contents (char codes)
        JSValue biRcVal = context.getGlobalObject().get("BI_RC");
        assertThat(biRcVal).isInstanceOf(JSArray.class);
        JSArray biRc = (JSArray) biRcVal;

        // Print identity and debug id for correlation with VM diagnostics
        System.out.println("DEBUG biRc.identityHash=" + System.identityHashCode(biRc));
        System.out.println("DEBUG biRc._dbgId=" + biRc.get("_dbgId"));

        int code0 = '0';
        int codea = 'a';
        int codeA = 'A';

        // Debug: print array length and sample entries before assertions
        System.out.println("DEBUG biRc.length=" + biRc.getLength());
        System.out.println("DEBUG biRc["+code0+"]=" + biRc.get(code0) + ", class=" + (biRc.get(code0) == null ? "null" : biRc.get(code0).getClass().getSimpleName()));
        System.out.println("DEBUG biRc["+codea+"]=" + biRc.get(codea) + ", class=" + (biRc.get(codea) == null ? "null" : biRc.get(codea).getClass().getSimpleName()));
        System.out.println("DEBUG biRc["+codeA+"]=" + biRc.get(codeA) + ", class=" + (biRc.get(codeA) == null ? "null" : biRc.get(codeA).getClass().getSimpleName()));

        assertThat(biRc.get(code0)).isInstanceOfSatisfying(JSNumber.class, num -> assertThat(num.value()).isEqualTo(0.0));
        assertThat(biRc.get(codea)).isInstanceOfSatisfying(JSNumber.class, num -> assertThat(num.value()).isEqualTo(10.0));
        assertThat(biRc.get(codeA)).isInstanceOfSatisfying(JSNumber.class, num -> assertThat(num.value()).isEqualTo(10.0));

        // Validate inheritance wiring: UnaryConstraint.superConstructor should be Constraint function
        JSValue unaryVal = context.getGlobalObject().get("UnaryConstraint");
        assertThat(unaryVal).isInstanceOf(JSFunction.class);
        JSFunction unaryFunc = (JSFunction) unaryVal;
        JSValue superCtor = unaryFunc.get("superConstructor");
        assertThat(superCtor).isInstanceOf(JSFunction.class);

        // Validate plus(i,j) after loop: should be 20 (i=10,j=10)
        JSValue plusResult = context.eval("plus(i,j)");
        assertThat(plusResult).isInstanceOfSatisfying(JSNumber.class, num -> assertThat(num.value()).isEqualTo(20.0));
    }
}
