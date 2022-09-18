package sbpm_solution.bpms.utils

class EvalContext(val localContext: MutableMap<String, Any?>, val parentContext: Map<String, Any?>?) : MutableMap<String, Any?> {

    override fun toString(): String {
        return getWorkCtx().toString()
    }

    fun getWorkCtx(): LinkedHashMap<String, Any?> {
        val retMap: LinkedHashMap<String, Any?> = LinkedHashMap()
        if (parentContext != null) {
            retMap.putAll(parentContext)
        }
        retMap.putAll(localContext)
        return retMap
    }

    override val size: Int
        get() = getWorkCtx().size

    override fun containsKey(key: String): Boolean {
        return getWorkCtx().containsKey(key)
    }

    override fun containsValue(value: Any?): Boolean {
        return getWorkCtx().containsValue(value)
    }

    override fun get(key: String): Any? {
        val ret = getWorkCtx().get(key)
        return ret
    }

    override fun isEmpty(): Boolean {
        return getWorkCtx().isEmpty()
    }

    override val entries: MutableSet<MutableMap.MutableEntry<String, Any?>>
        get() = getWorkCtx().entries

    override val keys: MutableSet<String>
        get() = getWorkCtx().keys

    override val values: MutableCollection<Any?>
        get() = getWorkCtx().values

    override fun clear() {
        localContext.clear()
    }

    override fun put(key: String, value: Any?): Any? {
        val ret = localContext.put(key, value)
        return ret
    }

    override fun putAll(from: Map<out String, Any?>) {
        localContext.putAll(from)
    }

    override fun remove(key: String): Any? {
        val ret = localContext.remove(key)
        return ret
    }
}
