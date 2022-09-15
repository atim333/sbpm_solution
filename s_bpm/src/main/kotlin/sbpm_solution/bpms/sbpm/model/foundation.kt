package sbpm_solution.bpms.sbpm.model


interface Reference<V> {
    val id: String
    fun resolvedReference(): V?
}

interface BaseElement {
    val id: String?
    val processDefinition: ProcessDefinition

    fun <T> getReference(): Reference<T> {
        return processDefinition.createReference(id!!)
    }
}

interface RootElement : BaseElement
