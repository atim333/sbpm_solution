package sbpm_solution.bpms.sbpm.decisiontable.el

import kotlin.test.Test
import kotlin.test.assertEquals


class ELParserTest {
    @Test
    fun unaryTestLT() {
        //   <
        val code = """ < 15 """
        val parser = ELNodeParser(code)
        val test = parser.parse()

        var ret = test.evaluate(16.1)
        assertEquals(false, ret)

        ret = test.evaluate("15")
        assertEquals(false, ret)

        ret = test.evaluate("13")
        assertEquals(true, ret)

        ret = test.evaluate(null)
        assertEquals(false, ret)

        System.out.println("OK")
    }

    @Test
    fun unaryTestLE() {
        //   <=
        val code = """ <= 15 """
        val parser = ELNodeParser(code)
        val test = parser.parse()

        var ret = test.evaluate(16.1)
        assertEquals(false, ret)

        ret = test.evaluate("15")
        assertEquals(true, ret)

        ret = test.evaluate("13")
        assertEquals(true, ret)

        ret = test.evaluate(null)
        assertEquals(false, ret)

        // System.out.println("OK")
    }

    @Test
    fun unaryTestGT() {
        val code = """ > 15"""
        val parser = ELNodeParser(code)
        val test = parser.parse()

        var ret = test.evaluate(16.1)
        assertEquals(true, ret)

        ret = test.evaluate("15")
        assertEquals(false, ret)

        ret = test.evaluate("13")
        assertEquals(false, ret)

        ret = test.evaluate(null)
        assertEquals(false, ret)

        System.out.println("OK")
    }

    @Test
    fun unaryTestGE() {
        //   >=
        val code = """ >= 15 """
        val parser = ELNodeParser(code)
        val test = parser.parse()

        var ret = test.evaluate(16.1)
        assertEquals(true, ret)

        ret = test.evaluate("15")
        assertEquals(true, ret)

        ret = test.evaluate("13")
        assertEquals(false, ret)

        ret = test.evaluate(null)
        assertEquals(false, ret)

        // System.out.println("OK")
    }

    @Test
    fun unaryTestEQ() {
        //   =
        val code = """ = 15 """
        val parser = ELNodeParser(code)
        val test = parser.parse()

        var ret = test.evaluate(16.1)
        assertEquals(false, ret)

        ret = test.evaluate("15")
        assertEquals(true, ret)

        ret = test.evaluate("13")
        assertEquals(false, ret)

        ret = test.evaluate(null)
        assertEquals(false, ret)

        //  System.out.println("OK")
    }

    @Test
    fun unaryTestEQ11() {
        //   =
        val code = """ = CREATE """
        val parser = ELNodeParser(code)
        val test = parser.parse()

        var ret = test.evaluate("16.1")
        assertEquals(false, ret)

        ret = test.evaluate("CREATE")
        assertEquals(true, ret)

        ret = test.evaluate(null)
        assertEquals(false, ret)

        // System.out.println("OK")
    }

    @Test
    fun unaryTestEQ12() {
        //   =
        val code = """ = "CREATE" """
        val parser = ELNodeParser(code)
        val test = parser.parse()

        var ret = test.evaluate("16.1")
        assertEquals(false, ret)

        ret = test.evaluate("CREATE")
        assertEquals(true, ret)

        ret = test.evaluate(null)
        assertEquals(false, ret)

        // System.out.println("OK")
    }

    @Test
    fun unaryTestNE() {
        // !=
        val code = """ != 15 """
        val parser = ELNodeParser(code)
        val test = parser.parse()

        var ret = test.evaluate(16.1)
        assertEquals(true, ret)

        ret = test.evaluate("15")
        assertEquals(false, ret)

        ret = test.evaluate("13")
        assertEquals(true, ret)

        ret = test.evaluate(null)
        assertEquals(false, ret)

        // System.out.println("OK")
    }

    @Test
    fun testInSet() {
        val code = """ [ "A'mmm", 'F"kkk' ,12, "1",2,"3" ] """
        val parser = ELNodeParser(code)
        val test = parser.parse()

        var ret = test.evaluate("8")
        assertEquals(false, ret)

        ret = test.evaluate(1)
        assertEquals(true, ret)

        // System.out.println("OK")
    }

    @Test
    fun testNotInSet() {
        val code = """ ![ "A'mmm", 'F"kkk' ,12, "1",2,"3" ] """
        val parser = ELNodeParser(code)
        val test = parser.parse()

        var ret = test.evaluate("8")
        assertEquals(true, ret)

        ret = test.evaluate(1)
        assertEquals(false, ret)

        System.out.println("OK")
    }

    @Test
    fun testIsNull() {
        val code = """ NULL """
        val parser = ELNodeParser(code)
        val test = parser.parse()

        var ret = test.evaluate(null)
        assertEquals(true, ret)

        ret = test.evaluate(1)
        assertEquals(false, ret)

        // System.out.println("OK")
    }

    @Test
    fun testIsNotNull() {

        val code = """ !NULL """
        val parser = ELNodeParser(code)
        val test = parser.parse()

        var ret = test.evaluate(null)
        assertEquals(false, ret)

        ret = test.evaluate(1)
        assertEquals(true, ret)
        // System.out.println("OK")
    }

    @Test
    fun testLogicalTrue() {
        val code = """  true """
        val parser = ELNodeParser(code)
        val test = parser.parse()

        var ret = test.evaluate(true)
        assertEquals(true, ret)

        ret = test.evaluate(" true ")
        assertEquals(true, ret)

        ret = test.evaluate(false)
        assertEquals(false, ret)

        ret = test.evaluate("false")
        assertEquals(false, ret)

        ret = test.evaluate(null)
        assertEquals(false, ret)
//        System.out.println("OK")
    }

    @Test
    fun testLogicalFalse() {

        val code = """  false """
        val parser = ELNodeParser(code)
        val test = parser.parse()

        var ret = test.evaluate(true)
        assertEquals(false, ret)

        ret = test.evaluate(" true ")
        assertEquals(false, ret)

        ret = test.evaluate(false)
        assertEquals(true, ret)

        ret = test.evaluate("false")
        assertEquals(true, ret)

        ret = test.evaluate(null)
        assertEquals(true, ret)
        // System.out.println("OK")
    }

    @Test
    fun testIntervalCC() {
        val code = """ [3 : 5] """
        val parser = ELNodeParser(code)
        val test = parser.parse()

        var ret = test.evaluate("4")
        assertEquals(true, ret)

        ret = test.evaluate(3)
        assertEquals(true, ret)

        ret = test.evaluate("5")
        assertEquals(true, ret)

        ret = test.evaluate(2)
        assertEquals(false, ret)

        ret = test.evaluate("6")
        assertEquals(false, ret)

        ret = test.evaluate(null)
        assertEquals(false, ret)
        // System.out.println("OK")
    }

    @Test
    fun testIntervalOC() {

        val code = """ (3 : 5] """
        val parser = ELNodeParser(code)
        val test = parser.parse()

        var ret = test.evaluate("4")
        assertEquals(true, ret)

        ret = test.evaluate(3)
        assertEquals(false, ret)

        ret = test.evaluate("5")
        assertEquals(true, ret)

        ret = test.evaluate(2)
        assertEquals(false, ret)

        ret = test.evaluate("6")
        assertEquals(false, ret)

        ret = test.evaluate(null)
        assertEquals(false, ret)
        //  System.out.println("OK")
    }

    @Test
    fun testIntervalCO() {

        val code = """ [3 : 5) """
        val parser = ELNodeParser(code)
        val test = parser.parse()

        var ret = test.evaluate("4")
        assertEquals(true, ret)

        ret = test.evaluate(3)
        assertEquals(true, ret)

        ret = test.evaluate("5")
        assertEquals(false, ret)

        ret = test.evaluate(2)
        assertEquals(false, ret)

        ret = test.evaluate("6")
        assertEquals(false, ret)

        ret = test.evaluate(null)
        assertEquals(false, ret)
        //  System.out.println("OK")
    }

    @Test
    fun testIntervalOO() {
        val code = """ (3 : 5) """
        val parser = ELNodeParser(code)
        val test = parser.parse()

        var ret = test.evaluate("4")
        assertEquals(true, ret)

        ret = test.evaluate(3)
        assertEquals(false, ret)

        ret = test.evaluate("5")
        assertEquals(false, ret)

        ret = test.evaluate(2)
        assertEquals(false, ret)

        ret = test.evaluate("6")
        assertEquals(false, ret)

        ret = test.evaluate(null)
        assertEquals(false, ret)
        // System.out.println("OK")
    }

    @Test
    fun testIntervalNCC() {

        val code = """ ![3 : 5] """
        val parser = ELNodeParser(code)
        val test = parser.parse()

        var ret = test.evaluate("4")
        assertEquals(false, ret)

        ret = test.evaluate(3)
        assertEquals(false, ret)

        ret = test.evaluate("5")
        assertEquals(false, ret)

        ret = test.evaluate(2)
        assertEquals(true, ret)

        ret = test.evaluate("6")
        assertEquals(true, ret)

        ret = test.evaluate(null)
        assertEquals(false, ret)
        // System.out.println("OK")
    }

    @Test
    fun testIntervalNOC() {

        val code = """ !(3 : 5] """
        val parser = ELNodeParser(code)
        val test = parser.parse()

        var ret = test.evaluate("4")
        assertEquals(false, ret)

        ret = test.evaluate(3)
        assertEquals(true, ret)

        ret = test.evaluate("5")
        assertEquals(false, ret)

        ret = test.evaluate(2)
        assertEquals(true, ret)

        ret = test.evaluate("6")
        assertEquals(true, ret)

        ret = test.evaluate(null)
        assertEquals(false, ret)
        // System.out.println("OK")
    }

    @Test
    fun testIntervalNCO() {

        val code = """ ![3 : 5) """
        val parser = ELNodeParser(code)
        val test = parser.parse()

        var ret = test.evaluate("4")
        assertEquals(false, ret)

        ret = test.evaluate(3)
        assertEquals(false, ret)

        ret = test.evaluate("5")
        assertEquals(true, ret)

        ret = test.evaluate(2)
        assertEquals(true, ret)

        ret = test.evaluate("6")
        assertEquals(true, ret)

        ret = test.evaluate(null)
        assertEquals(false, ret)
        // System.out.println("OK")
    }

    @Test
    fun testIntervalNOO() {
        val code = """ !(3 : 5) """
        val parser = ELNodeParser(code)
        val test = parser.parse()

        var ret = test.evaluate("4")
        assertEquals(false, ret)

        ret = test.evaluate(3)
        assertEquals(true, ret)

        ret = test.evaluate("5")
        assertEquals(true, ret)

        ret = test.evaluate(2)
        assertEquals(true, ret)

        ret = test.evaluate("6")
        assertEquals(true, ret)

        ret = test.evaluate(null)
        assertEquals(false, ret)
        // System.out.println("OK")
    }

    @Test
    fun testAny() {
        val code = """ * """
        val parser = ELNodeParser(code)
        val node = parser.parse()
        val ret = node.evaluate(7)
        assertEquals(true, ret)
        // System.out.println("OK")
    }
}