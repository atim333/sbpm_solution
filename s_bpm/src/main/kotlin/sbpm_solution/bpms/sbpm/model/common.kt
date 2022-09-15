package sbpm_solution.bpms.sbpm.model


interface UserForm : RootElement {
}

interface DataAssociation : BaseElement {
    val targetPath: String?
    val target: String?
    val sourcePath: String?
    val sourceMapFunction: String?
}

interface MessageDefinition : RootElement {
    val name: String
}

interface LoopCharacteristics : BaseElement {
    val loopVariableName: String
    val loopExpression: String
}
