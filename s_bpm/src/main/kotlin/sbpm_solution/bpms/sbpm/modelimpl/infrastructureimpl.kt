package sbpm_solution.bpms.sbpm.modelimpl

import sbpm_solution.bpms.sbpm.model.*



class ProcessDefinitionImpl(

): BaseElement, ProcessDefinition {
    override val id: String?
        get() = name+":"+version
    override val processDefinition = this
    override var name: String=""
    override var version: String=""

    override val inputDataAssociations: MutableList<DataAssociation> = ArrayList()
    override var mainSubject: Reference<Subject>? = null
    override val rootElements: MutableList<RootElement> = ArrayList()

    override var bp: MutableSet<String>? = null

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
            @Suppress("UNCHECKED_CAST")
            return localRef[id] as V?
        }

    }

}
