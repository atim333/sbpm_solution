package sbpm_solution.bpms.sbpm.format

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName

import sbpm_solution.bpms.sbpm.decisiontable.DecisionTable


@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
    //visible = true
)
@JsonSubTypes(
    JsonSubTypes.Type(name = "MessageDefinition", value = MessageDefinitionDT::class),
    JsonSubTypes.Type(name = "DataAssociation", value = DataAssociationDT::class),
    JsonSubTypes.Type(name = "LoopCharacteristics", value = LoopCharacteristicsDT::class),
    JsonSubTypes.Type(name = "ScriptAction", value = ScriptActionDT::class),
    JsonSubTypes.Type(name = "ServiceTaskAction", value = ServiceTaskActionDT::class),
    JsonSubTypes.Type(name = "DecisionTableAction", value = DecisionTableActionDT::class),
    JsonSubTypes.Type(name = "ExternalTaskAction", value = ExternalTaskActionDT::class),
    JsonSubTypes.Type(name = "Subject", value = SubjectDT::class),
    JsonSubTypes.Type(name = "ErrorTransition", value = ErrorTransitionDT::class),
    JsonSubTypes.Type(name = "TimerTransition", value = TimerTransitionDT::class),
    JsonSubTypes.Type(name = "FunctionalState", value = FunctionalStateDT::class),
    JsonSubTypes.Type(name = "FunctionalTransition", value = FunctionalTransitionDT::class),
    JsonSubTypes.Type(name = "SendState", value = SendStateDT::class),
    JsonSubTypes.Type(name = "TransitionSend", value = TransitionSendDT::class),
    JsonSubTypes.Type(name = "RecevivedState", value = RecevivedStateDT::class),
    JsonSubTypes.Type(name = "TransitionRecevid", value = TransitionRecevidDT::class),
    JsonSubTypes.Type(name = "JoinState", value = JoinStateDT::class),
    JsonSubTypes.Type(name = "JoinTransition", value = JoinTransitionDT::class),
    JsonSubTypes.Type(name = "ForkState", value = ForkStateDT::class),
    JsonSubTypes.Type(name = "ForkTransition", value = ForkTransitionDT::class),
    JsonSubTypes.Type(name = "GroupState", value = GroupStateDT::class),
    JsonSubTypes.Type(name = "GroupTransition", value = GroupTransitionDT::class),



    )
abstract class BaseDT

abstract class RootElementDT (
    val id: String
): BaseDT()

@JsonTypeName("MessageDefinition")
class MessageDefinitionDT(
    val name: String,
    id: String
): RootElementDT(id)

@JsonTypeName("DataAssociation")
class DataAssociationDT (
    val targetPath: String?,
    val target: String?,
    val sourcePath: String?,
    val sourceMapFunction: String?
):BaseDT()

@JsonTypeName("LoopCharacteristics")
class LoopCharacteristicsDT (
    val loopVariableName: String,
    val loopExpression: String
): BaseDT()


abstract class FunctionalActionDT(
    id: String,
    val name: String?
) : RootElementDT(id)

@JsonTypeName("ScriptAction")
class ScriptActionDT(id: String,
                     name: String?,
                     val body: String
) : FunctionalActionDT(
    id,
    name
)

@JsonTypeName("ServiceTaskAction")
class ServiceTaskActionDT(id: String,
                          name: String?,
                          val specification: String,
                          val method: String
): FunctionalActionDT(
id,
name
)
@JsonTypeName("DecisionTableAction")
class DecisionTableActionDT(id: String,
                            name: String?,
                            val table: DecisionTable
): FunctionalActionDT(
    id,
    name
)

@JsonTypeName("ExternalTaskAction")
class ExternalTaskActionDT (
    id: String,
    name: String?
): FunctionalActionDT(
    id,
    name
)

@JsonTypeName("Subject")
class  SubjectDT(
    id: String,
    val name: String,
    val states: List<StateDT>
) : RootElementDT(id)


abstract class StateDT(
    val id: String,
    val name: String,
    val isInitial: Boolean,
    val isFinish: Boolean,
    val isTerminate: Boolean,
    val transitions: List<TransitionDT>,
    val groupRef: String?
): BaseDT()

abstract class TransitionDT(
    val id: String,
    val targetRef:String,
    val sourceRef: String
): BaseDT()

@JsonTypeName("ErrorTransition")
class ErrorTransitionDT(
    id: String,
    targetRef:  String,
    sourceRef: String
):TransitionDT(id,targetRef,  sourceRef)

@JsonTypeName("TimerTransition")
class  TimerTransitionDT(
    id: String,
    targetRef:  String,
    sourceRef: String,
    val expression: String
): TransitionDT(id,targetRef,sourceRef)

@JsonTypeName("FunctionalState")
class FunctionalStateDT(
    id: String,
    name: String,
    isInitial: Boolean,
    isFinish: Boolean,
    isTerminate: Boolean,
    transitions: List<TransitionDT>,
    groupRef: String?,
    val  actionRef: String?,
    val  actionBody : FunctionalActionDT?,
    val inputDataAssociation: List<DataAssociationDT>
): StateDT(id,name,isInitial,isFinish,isTerminate,transitions,groupRef){
    init {
        if (actionRef==null &&actionBody==null){
            throw RuntimeException("Excected body or reference")
        } else if (actionRef!=null &&actionBody!=null){
            throw RuntimeException("Excected only One body or reference")
        }
    }
}

@JsonTypeName("FunctionalTransition")
class  FunctionalTransitionDT(
    id: String,
    targetRef:  String,
    sourceRef: String,
    val name: String,
    val outputDataAssociation: List<DataAssociationDT>
): TransitionDT(id,targetRef,sourceRef)

@JsonTypeName("SendState")
class SendStateDT(
    id:String,
    name: String,
    isInitial: Boolean,
    transitions: List<TransitionDT>,
    groupRef: String?
): StateDT(id,name,isInitial,false,false,transitions,groupRef)

@JsonTypeName("TransitionSend")
class  TransitionSendDT(
    id: String,
    targetRef:  String,
    sourceRef: String,
    val messageDefinitionRef: String,
    var recientRef: String,
    val outputDataAssociation: List<DataAssociationDT>
): TransitionDT(id,targetRef,sourceRef)

@JsonTypeName("RecevivedState")
class RecevivedStateDT(
    id:String,
    name: String,
    isInitial: Boolean,
    transitions: List<TransitionDT>,
    groupRef: String?
): StateDT(id,name,isInitial,false,false,transitions,groupRef)


@JsonTypeName("TransitionRecevid")
class TransitionRecevidDT(
    id: String,
    targetRef:  String,
    sourceRef: String,
    val messageDefinitionRef: String,
    val senderRef: String,
    val outDataAssociation: List<DataAssociationDT>
): TransitionDT(id,targetRef,sourceRef)

@JsonTypeName("JoinState")
class JoinStateDT(
    id:String,
    name: String,
    isFinish: Boolean,
    isTerminate: Boolean,
    transitions: List<TransitionDT>,
    groupRef: String?
): StateDT(id,name,false,isFinish,isTerminate,transitions,groupRef)

@JsonTypeName("JoinTransition")
class JoinTransitionDT(
    id: String,
    targetRef:  String,
    sourceRef: String
): TransitionDT(id,targetRef,sourceRef)

@JsonTypeName("ForkState")
class ForkStateDT(
    id:String,
    name: String,
    isInitial: Boolean,
    transitions: List<TransitionDT>,
    groupRef: String?
): StateDT(id,name,isInitial,false,false,transitions,groupRef)

@JsonTypeName("ForkTransition")
class  ForkTransitionDT(
    id: String,
    targetRef:  String,
    sourceRef: String
): TransitionDT(id,targetRef,sourceRef)

@JsonTypeName("GroupState")
class  GroupStateDT(
    id: String,
    name: String,
    isInitial: Boolean,
    isFinish: Boolean,
    isTerminate: Boolean,
    groupRef: String?,
    transitions: List<TransitionDT>,
    val loopCharacteristics: LoopCharacteristicsDT?
): StateDT(id,name,isInitial,isFinish,isTerminate,transitions,groupRef)

@JsonTypeName("GroupTransition")
class GroupTransitionDT(
    id: String,
    targetRef:  String,
    sourceRef: String
): TransitionDT(id,targetRef,sourceRef)


@JsonTypeName("ProcessDefinition")
class ProcessDefinitionDT (
    val name: String,
    val version: String,

    val inputDataAssociations: List<DataAssociationDT>,
    val mainSubject: String?,
    val rootElements: List<RootElementDT>
):BaseDT()
