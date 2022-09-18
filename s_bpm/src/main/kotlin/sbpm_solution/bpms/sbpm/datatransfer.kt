package sbpm_solution.bpms.sbpm

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import sbpm_solution.bpms.sbpm.decisiontable.DecisionTable
import sbpm_solution.bpms.sbpm.model.*
import sbpm_solution.bpms.sbpm.model.GroupState
import sbpm_solution.bpms.sbpm.modelimpl.ProcessDefinitionImpl
import sbpm_solution.bpms.sbpm.modelimpl.StateImpl


@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type"
    //visible = true
)
@JsonSubTypes(
    JsonSubTypes.Type(name = "ProcessDefinition", value = ProcessDefinitionDT::class),
    JsonSubTypes.Type(name = "ScriptAction", value = ScriptActionDT::class),
    JsonSubTypes.Type(name = "ServiceTaskAction", value = ServiceTaskActionDT::class),
    JsonSubTypes.Type(name = "DecisionTableAction", value = DecisionTableActionDT::class),
    JsonSubTypes.Type(name = "ExternalTaskAction", value = ExternalTaskActionDT::class),
    JsonSubTypes.Type(name = "DataAssociation", value = DataAssociationDT::class),
    JsonSubTypes.Type(name = "MessageDefinition", value = MessageDefinitionDT::class),
    JsonSubTypes.Type(name = "LoopCharacteristics", value = LoopCharacteristicsDT::class),
    JsonSubTypes.Type(name = "Subject", value = SubjectDT::class),
    JsonSubTypes.Type(name = "ErrorTransition", value = ErrorTransitionDT::class),
    JsonSubTypes.Type(name = "TimerTransition", value = TimerTransitionDT::class),
    JsonSubTypes.Type(name = "ActionRef", value = ActionRefDT::class),
    JsonSubTypes.Type(name = "ActionBody", value = ActionBodyDT::class),
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

abstract class BaseDT(val type: String)

class DataAssociationDT (
    val targetPath: String?,
    val target: String?,
    val sourcePath: String?,
    val sourceMapFunction: String?
): BaseDT("DataAssociation")

class LoopCharacteristicsDT (
    val loopVariableName: String,
    val loopExpression: String
): BaseDT("LoopCharacteristics")


class MessageDefinitionDT(
    val name: String,
    id: String
): RootElementDT("MessageDefinition",id)

abstract class RootElementDT (
    type:String,
    val id: String
): BaseDT(type)

abstract class FunctionalActionDT(
    type:String,
    id: String,
    val name: String?
) : RootElementDT(type,id)


class ScriptActionDT(id: String,
                     name: String?,
                     val body: String
) : FunctionalActionDT (
    "ScriptAction",
    id,
    name
)

class ServiceTaskActionDT(id: String,
                          name: String?,
                          val specification: String,
                          val method: String
): FunctionalActionDT (
    "ServiceTaskAction",
    id,
    name
)

class DecisionTableActionDT(id: String,
                           name: String?,
                           val table: DecisionTable
): FunctionalActionDT (
    "DecisionTableAction",
    id,
    name
)


class ExternalTaskActionDT (
    id: String,
    name: String?
): FunctionalActionDT (
    "DecisionTableAction",
    id,
    name
)


class  SubjectDT(
    id: String,
    val name: String,
    val states: List<StateDT>
) : RootElementDT("Subject",id)


abstract class StateDT(
    type: String,
    val id: String,
    val name: String,
    val isInitial: Boolean,
    val isFinish: Boolean,
    val isTerminate: Boolean,
    val transitions: List<TransitionDT>,
    val groupRef: String?
): BaseDT(type)

abstract class TransitionDT(
    type: String,
    val id: String,
    val targetRef:String,
    val sourceRef: String
): BaseDT(type)

class ErrorTransitionDT(
    id: String,
    targetRef:  String,
    sourceRef: String
):TransitionDT("ErrorTransition",id,targetRef,sourceRef)

enum class TimerTransitionTypeDT {
    DATE,
    DURATION
}

class  TimerTransitionDT(
    id: String,
    targetRef:  String,
    sourceRef: String,
    val expressionType:TimerTransitionTypeDT,
    val expression: String
):TransitionDT("TimerTransition",id,targetRef,sourceRef)

sealed class ActionDefinitionDT(
 type: String
) : BaseDT(type)

class ActionRefDT(val reference: String):ActionDefinitionDT("ActionRef")
class ActionBodyDT(val body: FunctionalActionDT):ActionDefinitionDT("ActionBody")

class FunctionalStateDT(
    id: String,
    name: String,
    isInitial: Boolean,
    isFinish: Boolean,
    isTerminate: Boolean,
    transitions: List<TransitionDT>,
    groupRef: String?,
    val  action: ActionDefinitionDT,
    val inputDataAssociation: List<DataAssociationDT>
):StateDT("FunctionalState",id,name,isInitial,isFinish,isTerminate,transitions,groupRef)

class  FunctionalTransitionDT(
    id: String,
    targetRef:  String,
    sourceRef: String,
    val name: String,
    val outputDataAssociation: List<DataAssociationDT>
):TransitionDT("FunctionalTransition",id,targetRef,sourceRef)


class SendStateDT(
    id:String,
    name: String,
    isInitial: Boolean,
    transitions: List<TransitionDT>,
    groupRef: String?
): StateDT("SendState",id,name,isInitial,false,false,transitions,groupRef)

class  TransitionSendDT(
    id: String,
    targetRef:  String,
    sourceRef: String,
    val messageDefinitionRef: String,
    var recientRef: String,
    val outputDataAssociation: List<DataAssociationDT>
):TransitionDT("TransitionSend",id,targetRef,sourceRef)


class RecevivedStateDT(
    id:String,
    name: String,
    isInitial: Boolean,
    transitions: List<TransitionDT>,
    groupRef: String?
): StateDT("RecevivedState",id,name,isInitial,false,false,transitions,groupRef)

class TransitionRecevidDT(
    id: String,
    targetRef:  String,
    sourceRef: String,
    val messageDefinitionRef: String,
    val senderRef: String,
    val outDataAssociation: List<DataAssociationDT>
):TransitionDT("TransitionRecevid",id,targetRef,sourceRef)

class JoinStateDT(
    id:String,
    name: String,
    isFinish: Boolean,
    isTerminate: Boolean,
    transitions: List<TransitionDT>,
    groupRef: String?
): StateDT("JoinState",id,name,false,isFinish,isTerminate,transitions,groupRef)

class JoinTransitionDT(
    id: String,
    targetRef:  String,
    sourceRef: String
):TransitionDT("JoinTransition",id,targetRef,sourceRef)


class ForkStateDT(
    id:String,
    name: String,
    isInitial: Boolean,
    transitions: List<TransitionDT>,
    groupRef: String?
): StateDT("ForkState",id,name,isInitial,false,false,transitions,groupRef)

class  ForkTransitionDT(
    id: String,
    targetRef:  String,
    sourceRef: String
):TransitionDT("ForkTransition",id,targetRef,sourceRef)

class  GroupStateDT(
    id: String,
    name: String,
    isInitial: Boolean,
    isFinish: Boolean,
    isTerminate: Boolean,
    groupRef: String,
    transitions: List<TransitionDT>,
    val loopCharacteristics: LoopCharacteristicsDT?
):StateDT("GroupState",id,name,isInitial,isFinish,isTerminate,transitions,groupRef)

class GroupTransitionDT(
    id: String,
    targetRef:  String,
    sourceRef: String
):TransitionDT("GroupTransition",id,targetRef,sourceRef)


class ProcessDefinitionDT (
    val name: String,
    val version: String,

    val inputDataAssociations: List<DataAssociationDT>,
    val mainSubject: String?,
    val rootElements: List<RootElementDT>
): BaseDT("ProcessDefinition")