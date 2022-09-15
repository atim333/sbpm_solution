package sbpm_solution.bpms.sbpm.modelimpl

import sbpm_solution.bpms.sbpm.decisiontable.DecisionTable
import sbpm_solution.bpms.sbpm.model.*

abstract class FunctionalActionImpl(processDefinition: ProcessDefinitionImpl,
                                    id: String?,
                                    override val name: String? = null
) :
    RootElementImpl(processDefinition,id),
    FunctionalAction

class ServiceTaskActionImpl(processDefinition: ProcessDefinitionImpl,
                      id: String? = null,
                      name: String? = null,
                      override val specification: String,
                      override val method: String
) :
    FunctionalActionImpl(processDefinition,id,name),
    ServiceTaskAction

class ScriptActionImpl(processDefinition: ProcessDefinitionImpl,
                 id: String?  = null,
                 name: String? = null,
                 override val body: String
) :
    FunctionalActionImpl(processDefinition,id,name),
    ScriptAction

class DecisionTableActionImpl(processDefinition: ProcessDefinitionImpl,
                        id: String? = null,
                        name: String? = null,
                        override val table: DecisionTable
) :
    FunctionalActionImpl(processDefinition,id,name),
    DecisionTableAction

class ExternalTaskActionImpl(processDefinition: ProcessDefinitionImpl,
                       id: String? = null,
                       name: String? = null
) :
    FunctionalActionImpl(processDefinition,id,name),
    ExternalTaskAction


