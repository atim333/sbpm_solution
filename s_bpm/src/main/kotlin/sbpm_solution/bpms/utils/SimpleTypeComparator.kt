package sbpm_solution.bpms.utils

import sbpm_solution.bpms.utils.converter.Convertor

class SimpleTypeComparator : Comparator<Any?> {
    companion object {
        val comparator = SimpleTypeComparator()

        fun compareObjects(o1: Any?, o2: Any?): Int {
            return comparator.compare(o1, o2)
        }

        fun equalsObjects(o1: Any?, o2: Any?): Boolean {
            return compareObjects(o1, o2) == 0
        }
    }

    override fun compare(o1: Any?, o2: Any?): Int {

        return if (o1 == null && o2 == null) {
            0
        } else if (o1 == null && o2 != null) {
            -1
        } else if (o1 != null && o2 == null) {
            1
        } else {
            val ot2 = Convertor.convert(o2, o1!!.javaClass)!!

            if (o1 is Comparable<*>) {
                @Suppress("UNCHECKED_CAST")
                if (o1 as Comparable<Any> < ot2) {
                    return -1
                }

                if (o1 > ot2) {
                    return 1
                }
                return 0
            }
            throw ClassCastException()
        }
    }
}
