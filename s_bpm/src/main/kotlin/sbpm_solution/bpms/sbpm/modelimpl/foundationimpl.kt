package sbpm_solution.bpms.sbpm.modelimpl

import sbpm_solution.bpms.sbpm.model.BaseElement
import sbpm_solution.bpms.sbpm.model.ProcessDefinition
import sbpm_solution.bpms.sbpm.model.RootElement

abstract class BaseElementImpl(
    override val processDefinition: ProcessDefinition,
    override val id:String? = null
) : BaseElement {
    init {
        if (!id.isNullOrBlank()) {
            val pd = processDefinition as ProcessDefinitionImpl
            pd.registrations(this)
        }
    }
}


abstract class RootElementImpl(processDefinition: ProcessDefinitionImpl,id:String?) :
    BaseElementImpl(processDefinition,id), RootElement
