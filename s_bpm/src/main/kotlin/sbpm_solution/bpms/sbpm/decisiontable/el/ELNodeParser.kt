package sbpm_solution.bpms.sbpm.decisiontable.el

import sbpm_solution.bpms.utils.parser.ParserException
import sbpm_solution.bpms.utils.parser.Scaner
import sbpm_solution.bpms.utils.parser.Token
import sbpm_solution.bpms.utils.parser.TokenType

class ELNodeParser(`in`: String) {
    private val scaner: Scaner = Scaner(`in`)

    init {
        scaner.setDelimetrs(delim)
    }

    companion object {
        private const val delim = "[]()=<>!-+,*:"

        private const val ANY = "ANY"
        private const val NULL = "NULL"
        private const val NULLNICHT = "NULLNICHT"
        private const val TRUE = "TRUE"
        private const val FALSE = "FALSE"
        private const val NE = "NE"
        private const val EQ = "EQ"
        private const val LT = "LT"
        private const val LE = "LE"
        private const val GT = "GT"
        private const val GE = "GE"
        private const val INTERVALOO = "INTERVALOO"
        private const val INTERVALOC = "INTERVALOC"
        private const val INTERVALOONICHT = "INTERVALOONICHT"
        private const val INTERVALOCNICHT = "INTERVALOCNICHT"
        private const val INTERVALCO = "INTERVALCO"
        private const val INTERVALCC = "INTERVALCC"
        private const val INTERVALCONICHT = "INTERVALCONICHT"
        private const val INTERVALCCNICHT = "INTERVALCCNICHT"
        private const val SET = "SET"
        private const val SETNICHT = "SETNICHT"
        private const val NICHT = "NICHT"
        private const val ИСТИНА = "ИСТИНА"
        private const val ФАЛЬШ = "ФАЛЬШ"
        private const val ПУСТО = "ПУСТО"
    }

    fun parse(): ElNode {
        val arguments = ArrayList<Any>()
        val op = parseOperation(arguments)
        var ret: ElNode? = null
        when (op) {
            ANY -> {
                ret = AnyTest()
            }
            NULL -> {
                ret = NullTest(true)
            }
            NULLNICHT -> {
                ret = NullTest(false)
            }
            TRUE -> {
                ret = LogicalTest(true)
            }
            FALSE -> {
                ret = LogicalTest(false)
            }
            NE -> {
                ret = UnaryTest(UnaryType.NE, arguments[0])
            }
            EQ -> {
                ret = UnaryTest(UnaryType.EQ, arguments[0])
            }
            LT -> {
                ret = UnaryTest(UnaryType.LT, arguments[0])
            }
            LE -> {
                ret = UnaryTest(UnaryType.LE, arguments[0])
            }
            GT -> {
                ret = UnaryTest(UnaryType.GT, arguments[0])
            }
            GE -> {
                ret = UnaryTest(UnaryType.GE, arguments[0])
            }
            INTERVALOO -> {
                ret = Interval(
                    IntervalType.OPEN,
                    IntervalType.OPEN,
                    arguments[0],
                    arguments[1]
                )
            }
            INTERVALOC -> {
                ret = Interval(
                    IntervalType.OPEN,
                    IntervalType.CLOSE,
                    arguments[0],
                    arguments[1]
                )
            }
            INTERVALOONICHT -> {
                ret = Interval(
                    IntervalType.OPEN,
                    IntervalType.OPEN,
                    arguments[0],
                    arguments[1],
                    true
                )
            }
            INTERVALOCNICHT -> {
                ret = Interval(
                    IntervalType.OPEN,
                    IntervalType.CLOSE,
                    arguments[0],
                    arguments[1],
                    true
                )
            }
            INTERVALCO -> {
                ret = Interval(
                    IntervalType.CLOSE,
                    IntervalType.OPEN,
                    arguments[0],
                    arguments[1]
                )
            }
            INTERVALCC -> {
                ret = Interval(
                    IntervalType.CLOSE,
                    IntervalType.CLOSE,
                    arguments[0],
                    arguments[1]
                )
            }
            INTERVALCONICHT -> {
                ret = Interval(
                    IntervalType.CLOSE,
                    IntervalType.OPEN,
                    arguments[0],
                    arguments[1],
                    true
                )
            }
            INTERVALCCNICHT -> {
                ret = Interval(
                    IntervalType.CLOSE,
                    IntervalType.CLOSE,
                    arguments[0],
                    arguments[1],
                    true

                )
            }
            SET -> {
                ret = SetTest().setSet(arguments)
            }
            SETNICHT -> {
                ret = SetTest(true).setSet(arguments)
            }
        }
        return ret!!
    }

    private fun parseOperation(arguments: MutableList<Any>): String {
        var nicht = ""
        var token: Token = scaner.nextToken()
        if (token.isDelimetr("!")) {
            nicht = NICHT
            token = scaner.nextToken()
            if (token.isDelimetr("=") || token.isDelimetr("[") || token.isDelimetr("(")) {
                scaner.putBack(token)
            } else if (token.isTokenType(TokenType.IDENTIFIER)) {
                val test: String = token.content
                if (test.equals(NULL, ignoreCase = true) ||
                    test.equals(ПУСТО, ignoreCase = true) ||
                    test.equals(TRUE, ignoreCase = true) ||
                    test.equals(ИСТИНА, ignoreCase = true) ||
                    test.equals(FALSE, ignoreCase = true) ||
                    test.equals(ФАЛЬШ, ignoreCase = true)
                ) {
                    scaner.putBack(token)
                } else {
                    throw ParserException(scaner, "Встретился недопустимый символ", token)
                }
            } else {
                throw ParserException(scaner, "Встретился недопустимый символ", token)
            }
        } else {
            scaner.putBack(token)
        }
        token = scaner.nextToken()
        if (token.isTokenType(TokenType.IDENTIFIER)) {
            val test: String = token.content
            return if (test.equals(NULL, ignoreCase = true) || test.equals(ПУСТО, ignoreCase = true)) {
                val retOP = "NULL$nicht"
                parseStop()
                retOP
            } else if (test.equals(TRUE, ignoreCase = true) || test.equals(ИСТИНА, ignoreCase = true)) {
                val retOP = if (nicht.isEmpty()) TRUE else FALSE
                parseStop()
                retOP
            } else if (test.equals(FALSE, ignoreCase = true) || test.equals(ФАЛЬШ, ignoreCase = true)) {
                val retOP = if (nicht.isEmpty()) FALSE else TRUE
                parseStop()
                retOP
            } else {
                throw ParserException(scaner, "Встретился недопустимый символ", token)
            }
        }
        if (token.isDelimetr("*")) {
            parseStop()
            return ANY
        }
        if (token.isDelimetr("=")) {
            val arg = parseConst()
            parseStop()
            arguments.add(arg)
            return if (nicht.isNotEmpty()) {
                NE
            } else EQ
        }
        if (token.isDelimetr("<")) {
            var retOP = LT
            token = scaner.nextToken()
            if (token.isDelimetr("=")) {
                retOP = LE
            } else {
                scaner.putBack(token)
            }
            val arg = parseConst()
            parseStop()
            arguments.add(arg)
            return retOP
        }
        if (token.isDelimetr(">")) {
            var retOP = GT
            token = scaner.nextToken()
            if (token.isDelimetr("=")) {
                retOP = GE
            } else {
                scaner.putBack(token)
            }
            val arg = parseConst()
            parseStop()
            arguments.add(arg)
            return retOP
        }
        if (token.isDelimetr("(")) {
            var retOP = "INTERVALO"
            val first = parseConst()
            token = scaner.nextToken()
            if (!token.isDelimetr(":")) {
                throw ParserException(scaner, "Встретился недопустимый символ", token)
            }
            //
            val second = parseConst()
            token = scaner.nextToken()
            retOP = when {
                token.isDelimetr(")") -> {
                    retOP + "O"
                }
                token.isDelimetr("]") -> {
                    retOP + "C"
                }
                else -> {
                    throw ParserException(scaner, "Встретился недопустимый символ", token)
                }
            }
            parseStop()
            arguments.add(first)
            arguments.add(second)
            retOP += nicht
            return retOP
        }
        if (token.isDelimetr("[")) {
            var retOp = SET
            token = scaner.nextToken()
            if (token.isDelimetr("]")) {
                parseStop()
                return retOp + nicht
            } else {
                scaner.putBack(token)
            }
            val first = parseConst()
            token = scaner.nextToken()
            if (token.isDelimetr(":")) {
                retOp = "INTERVALC"
                val second = parseConst()
                token = scaner.nextToken()
                retOp = when {
                    token.isDelimetr(")") -> {
                        retOp + "O"
                    }
                    token.isDelimetr("]") -> {
                        retOp + "C"
                    }
                    else -> {
                        throw ParserException(scaner, "Встретился недопустимый символ", token)
                    }
                }
                parseStop()
                arguments.add(first)
                arguments.add(second)
                retOp += nicht
                return retOp
            } else if (token.isDelimetr("]")) {
                parseStop()
                arguments.add(first)
                retOp += nicht
                return retOp
            } else if (token.isDelimetr(",")) {
                arguments.add(first)
                while (true) {
                    val next = parseConst()
                    arguments.add(next)
                    token = scaner.nextToken()
                    if (token.isDelimetr(",")) {
                        continue
                    } else if (token.isDelimetr("]")) {
                        break
                    } else {
                        throw ParserException(scaner, "Встретился недопустимый символ", token)
                    }
                }
                parseStop()
                retOp += nicht
                return retOp
            }
        }
        throw ParserException(scaner, "Встретился недопустимый символ", token)
    }

    // (
    /*
  *  LT, //   <
  *  LE, //   <=
  *  GT, //   >
  *  GE, //   >=
  *  EQ, //    =
  *  NE  //   !=
     */
    fun parseStop() {
        val token: Token = scaner.nextToken()
        if (!token.isTokenType(TokenType.EOF)) {
            throw ParserException(scaner, "Встретился недопустимый символ", token)
        }
    }

    fun parseConst(): Any {
        var token: Token = scaner.nextToken()
        if (token.isTokenType(TokenType.STRING) || token.isTokenType(TokenType.IDENTIFIER)) {
            return token.content
        }
        if (token.isTokenType(TokenType.INTEGER) || token.isTokenType(TokenType.NUMBER)
        ) {
            if (token.isTokenType(TokenType.INTEGER)) {
                return token.getNumber().toLong()
            }
            return token.getNumber().toDouble()
        }

        if (token.isDelimetr("+")) {
            token = scaner.nextToken()
            if (token.isTokenType(TokenType.INTEGER) || token.isTokenType(TokenType.NUMBER)
            ) {
                if (token.isTokenType(TokenType.INTEGER)) {
                    return token.getNumber().toLong()
                }
                return token.getNumber().toDouble()
            } else {
                throw ParserException(scaner, "Встретился недопустимый символ", token)
            }
        } else if (token.isDelimetr("-")) {
            token = scaner.nextToken()
            if (token.isTokenType(TokenType.INTEGER) || token.isTokenType(TokenType.NUMBER)) {
                if (token.isTokenType(TokenType.INTEGER)) {
                    var ret = token.getNumber().toLong()
                    return -ret
                }
                var ret = token.getNumber().toDouble() // !!!
                return -ret
            } else {
                throw ParserException(scaner, "Встретился недопустимый символ", token)
            }
        } else {
            throw ParserException(scaner, "Встретился недопустимый символ", token)
        }
    }
}

/*

OPERATION
   LT, //   <
    LE, //   <=
    GT, //   >
    GE, //   >=
    EQ, //    =
    NE  //   !=
    NOTNULL  ! NULL
    NULL     NULL
    NOTSET   !( ![      | INTERVALSET
    SET      [  || (    |  SET
    ELSE

OP [list]



    setNode
      isInterval
      R  OPEN CLOSE
      L
      RV
      List set + проверка после парсинга interval


    - или пусто   Что угодно  AnyTest результат всегда истина
Интервал
    [число число]
    (число число] проверка числа в интервале to do распространить на дату [строка строка]
    [число число)
    ()
Unary
    = Число
    < Число
    > Число
    != Число

Logical
    true
    false
NULL
    NULL
    ANY   (not null)

Simple

    список строк или чисел

policy U F

     context.x                 |  context.x/context.x
======================================================================================================================
*                                  *

                               |
     >=8                        |   "OK2"

     ! 8
     !=
                               |
     true истина                      |   "OK3"
                               |
     NULL пусто                     |   ""
                               |
     !NULL !пусто                     |   "OK5"    context.x+67+
                               |
     ---------------------------------------------------------------------
    !( -5 : -10)                    |   "OK1"

      SetNode

      1 interaval set[2] + 2 flag

      2 set


    [ "A'mmm", 'F"kkk' ,12 ]             |   "OK6"

    ![ 'A", "F" ,12]

     ELSE  иначе                      |   ";;;;"


JJJJJ
   k  ->
   k1  ->

X>8
-------------------->

*/
