package sbpm_solution.bpms.sbpm.modelimpl

import sbpm_solution.bpms.sbpm.model.BaseElement
import sbpm_solution.bpms.sbpm.model.ProcessDefinition
import sbpm_solution.bpms.sbpm.model.Reference
import sbpm_solution.bpms.sbpm.model.RootElement


class ProcessDefinitionImpl(
    override val name: String,
    override val version: String,
    override val rootElements: List<RootElement>
): BaseElement, ProcessDefinition {
    override val id: String?
        get() = name+":"+version
    override val processDefinition = this

    private val localRef = hashMapOf<String, BaseElement>()

    fun registrations(baseElement: BaseElement) {
        val id = baseElement.id
        localRef[id!!] = baseElement
    }

    override fun <T> createReference(id: String): Reference<T> {
        return ReferenceImpl(id)
    }

    inner  class ReferenceImpl<V>(override val id: String) : Reference<V> {
        override fun resolvedReference(): V? {
            return localRef[id] as V?
        }

    }

}
