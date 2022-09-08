package sbpm_solution.bpms.utils.converter
import java.math.BigDecimal
import java.math.BigInteger
import java.sql.Time
import java.sql.Timestamp
import java.text.DateFormat
import java.text.NumberFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Date
import java.util.Locale
import java.util.UUID

class ConvertorException(message: String?) : RuntimeException(message)

object Convertor {

    fun <T> convert(value: Any?, type: Class<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return coerceToType(value, type) as T?
    }

    private fun coerceToUUID(value: Any): UUID {
        if (value is String) {
            return UUID.fromString(value)
        } else if (value is UUID) {
            return value
        }
        throw ConvertorException("Can't convert " + value + " to " + UUID::class.java)
    }

    private fun coerceToString(value: Any): String {
        if (value is String) {
            return value
        }
        if (value is Enum<*>) {
            return value.name
        }

        if (value is Double) {
            val bd = BigDecimal.valueOf(value)
            val bds = bd.toPlainString()
            val ss = bds.split("\\.").toTypedArray()
            return if (ss.size == 1 || ss[1].toInt() == 0) {
                ss[0]
            } else {
                bds
            }
        }
        if (value is Float) {
            val bd = value.toBigDecimal()
            val bds = bd.toPlainString()
            val ss = bds.split("\\.").toTypedArray()
            return if (ss.size == 1 || ss[1].toInt() == 0) {
                ss[0]
            } else {
                bds
            }
        }

        if (value is Date) {
            if (value is Time) {
                return value.toString()
            }
            val l: Long = value.time
            val ret = Timestamp(l).toLocalDateTime().toString()
            return ret
        }
        if (value is LocalDateTime) {
            return value.toString()
        }
        return value.toString()
    }

    private fun coerceToLong(value: Any): Long {
        if (value is Long) {
            return value
        }
        if (value is Number) {
            return java.lang.Long.valueOf(value.toLong())
        } else if (value is String) {
            return try {
                java.lang.Long.valueOf(value)
            } catch (e: NumberFormatException) {
                throw ConvertorException("Can't convert " + value + " to " + Long::class.java)
            }
        } else if (value is Boolean) {
            if (value) {
                return 1
            } else {
                return 0
            }
        }

        throw ConvertorException("Can't convert " + value + " to " + Long::class.java)
    }

    private fun coerceToDouble(value: Any): Double? {
        if (value is Double) {
            return value
        }
        if (value is Number) {
            return java.lang.Double.valueOf(value.toDouble())
        }
        if (value is String) {
            return try {
                val str = value
                if (str.isEmpty()) {
                    return null
                }
                NumberFormat.getNumberInstance(Locale.US).parse(str).toDouble()
            } catch (e: NumberFormatException) {
                throw ConvertorException("Can't convert " + value + " to " + Double::class.java)
            } catch (e: ParseException) {
                throw ConvertorException("Can't convert " + value + " to " + Double::class.java)
            }
        }

        throw ConvertorException("Can't convert " + value + " to " + Double::class.java)
    }

    private fun coerceToBigDecimal(value: Any): BigDecimal {
        if (value is BigDecimal) {
            return value
        }
        if (value is BigInteger) {
            return BigDecimal(value)
        }
        if (value is Number) {
            return BigDecimal(value.toDouble())
        }
        if (value is String) {
            return try {
                BigDecimal(value)
            } catch (e: NumberFormatException) {
                throw ConvertorException("Can't convert " + value + " to " + BigDecimal::class.java)
            }
        }

        throw ConvertorException("Can't convert " + value + " to " + BigDecimal::class.java)
    }

    private fun coerceToBigInteger(value: Any?): BigInteger {
        if (value == null || "" == value) {
            return BigInteger.valueOf(0L)
        }
        if (value is BigInteger) {
            return value
        }
        if (value is BigDecimal) {
            return value.toBigInteger()
        }
        if (value is Number) {
            return BigInteger.valueOf(value.toLong())
        }
        if (value is String) {
            return try {
                BigInteger(value)
            } catch (e: NumberFormatException) {
                throw ConvertorException("Can't convert " + value + " to " + BigInteger::class.java)
            }
        }

        throw ConvertorException("Can't convert " + value + " to " + BigInteger::class.java)
    }

    private fun coerceToFloat(value: Any): Float {
        if (value is Float) {
            return value
        }
        if (value is Number) {
            return java.lang.Float.valueOf(value.toFloat())
        }
        if (value is String) {
            return try {
                NumberFormat.getNumberInstance(Locale.US).parse(value).toFloat()
            } catch (e: NumberFormatException) {
                throw ConvertorException("Can't convert " + value + " to " + Float::class.java)
            } catch (e: ParseException) {
                throw ConvertorException("Can't convert " + value + " to " + Float::class.java)
            }
        }

        throw ConvertorException("Can't convert " + value + " to " + Float::class.java)
    }

    private fun coerceToBoolean(value: Any): Boolean {
        if (value is Boolean) {
            return value
        }
        if (value is String) {
            return java.lang.Boolean.valueOf(value.trim { it <= ' ' })
        }
        if (value is Number) {
            val i = value.toInt()
            return i > 0
        }
        throw ConvertorException("Can't convert " + value + " to " + Boolean::class.java)
    }

    private fun coerceToInteger(value: Any): Int? {
        if (value is Int) {
            return value
        }
        if (value is Number) {
            return Integer.valueOf(value.toInt())
        }
        if (value is String) {
            if (value.isNullOrEmpty()) {
                return null
            }
            return try {
                Integer.valueOf(value)
            } catch (e: NumberFormatException) {
                throw ConvertorException("Can't convert " + value + " to " + Int::class.java)
            }
        }

        throw ConvertorException("Can't convert " + value + " to " + Int::class.java)
    }

    private fun coerceToShort(value: Any?): Short {
        if (value == null || "" == value) {
            return 0
        }
        if (value is Short) {
            return value
        }
        if (value is Number) {
            return value.toShort()
        }
        if (value is String) {
            return try {
                value.toShort()
            } catch (e: NumberFormatException) {
                throw ConvertorException("Can't convert " + value + " to " + Short::class.java)
            }
        }
        if (value is Char) {
            return value.code.toShort()
        }
        throw ConvertorException("Can't convert " + value + " to " + Short::class.java)
    }

    private fun coerceToByte(value: Any): Byte {
        if (value is Byte) {
            return value
        }
        if (value is Number) {
            return java.lang.Byte.valueOf(value.toByte())
        }
        if (value is String) {
            return try {
                java.lang.Byte.valueOf(value)
            } catch (e: NumberFormatException) {
                throw ConvertorException("Can't convert " + value + " to " + Byte::class.java)
            }
        }
        if (value is Char) {
            return value.code.toByte()
        }
        throw ConvertorException("Can't convert " + value + " to " + Byte::class.java)
    }

    private fun coerceToCharacter(value: Any): Char {
        if (value is Char) {
            return value
        }
        if (value is Number) {
            return Character.valueOf(value.toShort().toInt().toChar())
        }
        if (value is String) {
            return Character.valueOf(value[0])
        }
        throw ConvertorException("Can't convert " + value + " to " + Char::class.java)
    }

    private fun <T : Enum<T>?> coerceToEnum(value: Any?, type: Class<T>): T? {
        if (value == null || "" == value) {
            return null
        }
        if (type.isInstance(value)) {
            @Suppress("UNCHECKED_CAST")
            return value as T
        }
        if (value is String) {
            return try {
                java.lang.Enum.valueOf(type, value as String?)
            } catch (e: IllegalArgumentException) {
                throw ConvertorException("Can't convert $value to $type")
            }
        }
        throw ConvertorException("Can't convert $value to $type")
    }

    private fun coerceToDate(value: Any): Date {
        if (value is Date) {
            return value
        }
        if (value is String) {
            val s = value
            return try {
                LocalDateTime.parse(value)
                val ret = Timestamp.valueOf(LocalDateTime.parse(value))
                return ret
            } catch (ex: UnsupportedOperationException) {
                val df: DateFormat = SimpleDateFormat("yyyy-MM-dd")
                try {
                    df.parse(s)
                } catch (e: ParseException) {
                    throw ConvertorException("Can't convert " + value + " to " + Date::class.java)
                }
            } catch (ex: IllegalArgumentException) {
                val df: DateFormat = SimpleDateFormat("yyyy-MM-dd")
                try {
                    df.parse(s)
                } catch (e: ParseException) {
                    throw ConvertorException("Can't convert " + value + " to " + Date::class.java)
                }
            }
        }
        throw ConvertorException("Can't convert " + value + " to " + Date::class.java)
    }

    private fun coerceToLocalDateTime(value: Any): LocalDateTime? {
        if (value is LocalDateTime) {
            return value
        }
        try {
            if (value is String) {
                if (value.isEmpty()) {
                    return null
                }
                try {
                    return LocalDateTime.parse(value)
                } catch (ex: DateTimeParseException) {
                    return LocalDateTime.parse(value, DateTimeFormatter.ISO_INSTANT)
                }
            }
        } catch (ex: DateTimeParseException) {
            throw ConvertorException("Can't convert " + value + " to " + LocalDateTime::class.java)
        }
        throw ConvertorException("Can't convert " + value + " to " + LocalDateTime::class.java)
    }
    // 2021-09-16T10:50:00.000Z
    private fun coerceToType(value: Any?, type: Class<*>): Any? {
        if (value == null) {
            return null
        }
        if (type == String::class.java) {
            return coerceToString(value)
        }

        if (type == Long::class.java || type == Long::class.javaObjectType || type == Long::class.javaPrimitiveType) {
            return coerceToLong(value)
        }
        if (type == Double::class.java || type == Double::class.javaObjectType || type == Double::class.javaPrimitiveType) {
            return coerceToDouble(value)
        }
        if (type == Boolean::class.java || type == Boolean::class.javaPrimitiveType) {
            return coerceToBoolean(value)
        }
        if (type == Int::class.java || type == Int::class.javaObjectType || type == Int::class.javaPrimitiveType) {
            return coerceToInteger(value)
        }
        if (type == Float::class.java || type == Float::class.javaPrimitiveType) {
            return coerceToFloat(value)
        }
        if (type == Short::class.java || type == Short::class.javaPrimitiveType) {
            return coerceToShort(value)
        }
        if (type == Byte::class.java || type == Byte::class.javaPrimitiveType) {
            return coerceToByte(value)
        }
        if (type == Char::class.java || type == Char::class.javaPrimitiveType) {
            return coerceToCharacter(value)
        }
        if (type == BigDecimal::class.java) {
            return coerceToBigDecimal(value)
        }
        if (type == BigInteger::class.java) {
            return coerceToBigInteger(value)
        }
        if (type.superclass == Enum::class.java) {
            @Suppress("UNCHECKED_CAST")
            return coerceToEnum(value, type as Class<out Enum<*>>)
        }
        if (value.javaClass == type || type.isInstance(value)) {
            return value
        }
        if (type == Date::class.java) {
            return coerceToDate(value)
        }
        if (type == LocalDateTime::class.java) {
            return coerceToLocalDateTime(value)
        }
        if (type == UUID::class.java) {
            return coerceToUUID(value)
        }
        throw ConvertorException("Can't convert $value to $type")
    }
}



