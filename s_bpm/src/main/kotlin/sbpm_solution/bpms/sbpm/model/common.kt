package sbpm_solution.bpms.sbpm.model

interface DataAssociation : BaseElement{
   var targetPath: String?
   var target: String?
   var sourcePath: String?
   var sourceMapFunction: String?
}

interface MessageDefinition : BaseElement{
    var name: String?
}

interface LoopCharacteristics : BaseElement{
    var loopVaribleName: String?
    var loopExpression: String?
}
