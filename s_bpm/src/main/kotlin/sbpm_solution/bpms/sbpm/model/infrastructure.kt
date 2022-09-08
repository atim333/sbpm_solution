package sbpm_solution.bpms.sbpm.model

interface ProcessDefinition : BaseElement {
    var name: String
    var version: String

    fun <T> creareReference(id: String): Reference<T>

}