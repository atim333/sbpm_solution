package sbpm_solution.bpms.sbpm.decisiontable.el

import sbpm_solution.bpms.utils.SimpleTypeComparator
import sbpm_solution.bpms.utils.converter.Convertor

interface ElNode {
    fun evaluate(value: Any?): Boolean

}

enum class UnaryType {
    LT, //   <
    LE, //   <=
    GT, //   >
    GE, //   >=
    EQ, //    =
    NE //   !=
}


class UnaryTest(val operator: UnaryType, val value: Any) : ElNode {
    override fun toString(): String {
        val v=Convertor.convert(value,String::class.java)
        val op= when (operator) {
            UnaryType.LT -> "<"
            UnaryType.LE -> "<="
            UnaryType.GT -> ">"
            UnaryType.GE -> ">="
            UnaryType.EQ -> "=="
            UnaryType.NE -> "!="
        }
        return " "+op+" "+v
    }

    override fun evaluate(value: Any?): Boolean {
        if (value == null) {
            return false
        }
        val ret: Int = SimpleTypeComparator.compareObjects(value, this.value)
        return when (operator){
            UnaryType.LT -> ret<0
            UnaryType.LE -> ret <= 0
            UnaryType.GT -> ret > 0
            UnaryType.GE -> ret >= 0
            UnaryType.EQ -> ret == 0
            UnaryType.NE -> ret !=0
        }
    }
}

class SetTest(val isNICHT: Boolean = false) : ElNode {
    private var testSet = HashSet<Any>()

    fun setSet(objects: List<*>): SetTest {
        testSet.clear()
        for (o in objects) {
            val s = Convertor.convert(o,String::class.java)
            testSet.add(s!!)
        }
        return this
    }

    override fun evaluate(value: Any?): Boolean {
        val svalue = Convertor.convert(value,String::class.java)
        if (svalue==null){
            return false
        }
        var ret = testSet.contains(svalue)
        if (isNICHT) {
            ret = !ret
        }
        return ret
    }
}

class NullTest(val isNull: Boolean) : ElNode {

    override fun evaluate(value: Any?): Boolean {
        return if (isNull) {
            value == null
        } else {
            value != null
        }
    }
}

class LogicalTest(val logical: Boolean) : ElNode {

    override fun evaluate(value: Any?): Boolean {
        val v = Convertor.convert(value, Boolean::class.java)
        if (!logical && value == null) {
            return true
        }
        return v == logical
    }
}

enum class IntervalType {
    OPEN, CLOSE
}

class Interval(
    val start: IntervalType,
    val end: IntervalType,
    val n1: Any,
    val n2: Any,
    val isNICHT: Boolean = false
) : ElNode {

    override fun evaluate(value: Any?): Boolean {
        if (value == null) {
            return false
        }

        var ret: Boolean
        ret = if (start === IntervalType.OPEN && end === IntervalType.OPEN) { // ret=(numValue > N1) && (numValue<N2);
            SimpleTypeComparator.compareObjects(value, n1) > 0 && SimpleTypeComparator.compareObjects(
                value,
                n2
            ) < 0
        } else if (start === IntervalType.CLOSE && end === IntervalType.OPEN) { // ret=(numValue >= N1) && (numValue<N2);
            SimpleTypeComparator.compareObjects(value, n1) >= 0 && SimpleTypeComparator.compareObjects(
                value,
                n2
            ) < 0
        } else if (start === IntervalType.CLOSE && end === IntervalType.CLOSE) { // ret=(numValue >= N1) && (numValue<=N2);
            SimpleTypeComparator.compareObjects(value, n1) >= 0 && SimpleTypeComparator.compareObjects(
                value,
                n2
            ) <= 0
        } else { // start==IntervalType.OPEN &&end==IntervalType.CLOSE  (numValue > N1) && (numValue<=N2);
            SimpleTypeComparator.compareObjects(value, n1) > 0 && SimpleTypeComparator.compareObjects(
                value,
                n2
            ) <= 0
        }
        if (isNICHT) {
            ret = !ret
        }
        return ret
    }
}

class AnyTest : ElNode {
    override fun evaluate(value: Any?): Boolean {
        return true
    }
}


