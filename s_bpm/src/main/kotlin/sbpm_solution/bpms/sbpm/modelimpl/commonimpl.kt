package sbpm_solution.bpms.sbpm.modelimpl

import sbpm_solution.bpms.sbpm.model.DataAssociation
import sbpm_solution.bpms.sbpm.model.LoopCharacteristics
import sbpm_solution.bpms.sbpm.model.MessageDefinition

class MessageDefinitionImpl(processDefinition: ProcessDefinitionImpl,
                            id:String,
                            override val name:String) :
    RootElementImpl(processDefinition,id), MessageDefinition

class LoopCharacteristicsImpl(processDefinition: ProcessDefinitionImpl,
                              override val loopVariableName: String,
                              override var loopExpression: String
) :
    BaseElementImpl(processDefinition),
    LoopCharacteristics {
}

class DataAssociationImpl(processDefinition: ProcessDefinitionImpl,
                          override val targetPath: String?=null,
                          override val target: String? = null,
                          override val sourcePath: String? = null,
                          override val sourceMapFunction: String? = null
) :
    BaseElementImpl(processDefinition),
    DataAssociation {
}
