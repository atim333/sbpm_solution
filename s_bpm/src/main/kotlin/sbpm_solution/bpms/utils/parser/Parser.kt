package sbpm_solution.bpms.utils.parser
import org.apache.commons.lang3.StringUtils
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.PushbackReader
import java.io.Reader
import java.io.UnsupportedEncodingException
import java.text.NumberFormat
import java.util.Locale
import java.util.Stack


class ParserException : RuntimeException {
    private var line = 0
    private var pos = 0
    private var lastString = ""
    private var token: Token? = null

    fun getLine(): Int {
        return line
    }

    fun getPos(): Int {
        return pos
    }

    fun getToken(): Token? {
        return token
    }

    fun getLastString(): String {
        return lastString
    }

    constructor (scaner: Scaner?, message: String?) : super(message) {
        requireNotNull(scaner) { "Scanner can not be null!" }
        line = scaner.line
        pos = scaner.pos
        lastString = scaner.getLastString()
    }

    constructor (scaner: Scaner?, message: String?, token: Token?) : super(message) {
        requireNotNull(scaner) { "Scanner can not be null!" }
        line = scaner.line
        pos = scaner.pos
        lastString = scaner.getLastString()
        this.token = token
    }
}

enum class TokenType {
    EOF, IDENTIFIER, STRING, INTEGER, NUMBER, EXTERNAL, DELIMETER
}

class Token(val tokenType: TokenType, val content: String) {

    fun isTokenType(test: TokenType): Boolean {
        return if (test === TokenType.NUMBER && (tokenType === TokenType.INTEGER || tokenType === TokenType.NUMBER)) {
            true
        } else test === tokenType
    }

    fun getTokenTypeName(): String {
        return when (tokenType) {
            TokenType.EOF -> "конец потока обработки"
            TokenType.IDENTIFIER -> "идентификатор"
            TokenType.STRING -> "строка"
            TokenType.INTEGER -> "целое число"
            TokenType.NUMBER -> "число"
            TokenType.EXTERNAL -> "вставка"
            TokenType.DELIMETER -> "разделитель"
        }
    }

    fun isDelimetr(del: String?): Boolean {
        if (tokenType === TokenType.DELIMETER) {
            if (content.equals(del)) {
                return true
            }
        }
        return false
    }

    fun getNumber(): Number {
        return NumberFormat.getNumberInstance(Locale.US).parse(content)
    }

    override fun toString(): String {
        return String.format(" %s [%s] ", getTokenTypeName(), content)
    }
}

class Scaner {
    private var input: PushbackReader? = null
    private val stack = Stack<Token>()
    var pos = 0
        private set
    var line = 1
        private set
    private var delimetrs = StringUtils.EMPTY
    private val lineBreak = System.getProperty("line.separator")
    private var lastString = StringBuffer()
    fun init(`in`: InputStream, encoding: String) {
        input = try {
            val r: Reader = InputStreamReader(`in`, encoding)
            PushbackReader(r)
        } catch (ex: UnsupportedEncodingException) {
            throw RuntimeException(ex)
        }
    }

    constructor(`in`: InputStream) {
        init(`in`, "UTF-8")
    }

    constructor(code: String) {
        init(ByteArrayInputStream(code.toByteArray(charset("UTF-8"))), "UTF-8")
    }

    fun setDelimetrs(delimetrs: String) {
        this.delimetrs = delimetrs
    }

    fun getLastString(): String {
        return lastString.toString()
    }

    private fun getChar(): Int {
        val ret = input!!.read()
        lastString.append(ret.toChar())
        pos++
        return ret
    }

    @Throws(IOException::class)
    private fun ungetChar(chr: Int) {
        input!!.unread(chr)
        lastString = lastString.deleteCharAt(lastString.length - 1)
        pos--
    }

    @Throws(IOException::class)
    private fun skipWhite() {
        var chr: Int
        while (true) {
            chr = getChar()
            if (lineBreak.indexOf(chr.toChar()) > -1) {
                lastString = StringBuffer()
                pos = 1
                line++
                continue
            } else if (Character.isWhitespace(chr)) {
                continue
            } else {
                break
            }
        }
        if (chr != -1) {
            ungetChar(chr)
        }
    }

    @Throws(IOException::class)
    private fun skipComment() {
        var chr: Int
        while (true) {
            chr = getChar()
            if (lineBreak.indexOf(chr.toChar()) > -1) {
                lastString = StringBuffer()
                pos = 1
                line++
                continue
            }
            if (chr == '*'.code) {
                val nextChar = getChar()
                if (nextChar == -1) {
                    throw IOException("Неожиданный конец файла")
                }
                if (nextChar == '/'.code) {
                    break
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun readExrernal(): Token {
        val buffer = StringBuffer()
        var chr: Int
        while (true) {
            chr = getChar()
            if (lineBreak.indexOf(chr.toChar()) > -1) {
                pos = 1
                line++
            }
            if (chr == ']'.code) {
                val nextChar = getChar()
                if (nextChar == -1) {
                    throw IOException("Неожиданный конец файла")
                }
                if (nextChar == '#'.code) {
                    break
                }
                ungetChar(nextChar)
            }
            buffer.append(chr.toChar())
        }
        val ret = buffer.toString()
        return Token(TokenType.EXTERNAL, ret)
    }

    @Throws(IOException::class)
    private fun readIdentificator(): Token {
        val buffer = StringBuffer()
        var chr = getChar()

        buffer.append(chr.toChar())
        while (true) {
            chr = getChar()
            if (!Character.isUnicodeIdentifierPart(chr)) {
                break
            }

            buffer.append(chr.toChar())
        }
        if (chr != -1) {
            ungetChar(chr)
        }
        val ret = buffer.toString()
        return Token(TokenType.IDENTIFIER, ret)
    }

    @Throws(IOException::class)
    private fun readNumber(): Token {
        var isPoint = false
        var chr: Int
        val buffer = StringBuffer()
        while (true) {
            chr = getChar()
            if (Character.isDigit(chr)) {
                buffer.append(chr.toChar())
            } else if (chr == '.'.code) {
                if (isPoint) {
                    break
                }
                isPoint = true
                buffer.append(chr.toChar())
            } else {
                break
            }
        }
        if (chr != -1) {
            ungetChar(chr)
        }
        val ret = buffer.toString()
        return Token(if (isPoint) TokenType.NUMBER else TokenType.INTEGER, ret)
    }

    @Throws(IOException::class)
    private fun readString(Q: Char): Token {
        var chr: Int
        val buffer = StringBuffer()
        while (true) {
            chr = getChar()
            if (chr == Q.code) {
                break
            }
            if (chr == -1) {
                throw IOException("Неожиданный конец файла")
            }
            buffer.append(chr.toChar())
            if (lineBreak.indexOf(chr.toChar()) > -1) {
                pos = 1
                line++
            }
        }
        val ret = buffer.toString()
        return Token(TokenType.STRING, ret)
    }

    fun getDelimetr(chr: Int): String? {
        val idx = delimetrs.indexOf(chr.toChar())
        if (idx == -1) {
            return null
        }
        val c = delimetrs[idx]
        return "" + c
    }

    @Throws(ParserException::class)
    fun nextToken(): Token {
        try {
            if (!stack.empty()) {
                return stack.pop()
            }
            skipWhite()
            val chr = getChar()
            if (chr == -1) {
                return Token(TokenType.EOF, "end of file")
            }
            if (chr == '/'.code) {
                val nextchar = getChar()
                if (nextchar == '*'.code) {
                    skipComment()
                    return nextToken()
                } else {
                    ungetChar(nextchar)
                }
            }
            if (chr == '#'.code) {
                val nextchar = getChar()
                if (nextchar == '['.code) {
                    return readExrernal()
                } else {
                    ungetChar(nextchar)
                }
            }
            if (Character.isJavaIdentifierStart(chr) && chr != '$'.code) {
                ungetChar(chr)
                return readIdentificator()
            }
            if (Character.isDigit(chr)) {
                ungetChar(chr)
                return readNumber()
            }
            if (chr == '\''.code || chr == '"'.code) {
                return readString(chr.toChar())
            }
            val delim = getDelimetr(chr)
            if (delim != null) {
                return Token(TokenType.DELIMETER, delim)
            }
            val schr = "" + chr.toChar()
            throw ParserException(this, "Недопустимый символ $schr", null)
        } catch (ex: IOException) {
            throw ParserException(this, "Системная ошибка $ex")
        }
    }

    fun putBack(token: Token) {
        stack.push(token)
    }
}
