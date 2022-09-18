package sbpm_solution.bpms.sbpm.modelimpl

import sbpm_solution.bpms.sbpm.model.*


class  SubjectImpl(
    processDefinition: ProcessDefinitionImpl,
    id:String,
    override val name: String,
    override val states: List<State>
)
: RootElementImpl(processDefinition,id),
  Subject

abstract class StateImpl(
    processDefinition: ProcessDefinitionImpl,
    id:String,
    override val name: String,
    override val isInitial: Boolean,
    override val isFinish: Boolean,
    override val isTerminate: Boolean,
    override val transitions: List<Transition>,
    override val groupRef: Reference<GroupState>?
): BaseElementImpl(processDefinition,id),
   State

abstract class TransitionImpl(
    processDefinition: ProcessDefinitionImpl,
    id:String,
    override val targetRef: Reference<State>,
    override val sourceRef: Reference<State>
): BaseElementImpl(processDefinition,id),
   Transition

class ErrorTransitionImpl(
    processDefinition: ProcessDefinitionImpl,
    id:String,
    targetRef: Reference<State>,
    sourceRef: Reference<State>
): TransitionImpl(processDefinition,id,targetRef,sourceRef),
    ErrorTransition

class TimerTransitionImp(
    processDefinition: ProcessDefinitionImpl,
    id:String,
    targetRef: Reference<State>,
    sourceRef: Reference<State>,
    override  val expression: TimerTransitionType
): TransitionImpl(processDefinition,id,targetRef,sourceRef),
    TimerTransition

class FunctionalStateImpl(
    processDefinition: ProcessDefinitionImpl,
    id:String,
    name: String,
    isInitial: Boolean,
    isFinish: Boolean,
    isTerminate: Boolean,
    transitions: List<Transition>,
    groupRef: Reference<GroupState>?,
    override val action: ActionDefinition,
    override val inputDataAssociation: List<DataAssociation>
): StateImpl(processDefinition,id,name,isInitial,isFinish,isTerminate,transitions,groupRef),
    FunctionalState

class FunctionalTransitionImpl(
    processDefinition: ProcessDefinitionImpl,
    id:String,
    targetRef: Reference<State>,
    sourceRef: Reference<State>,
    override val name: String,
    override val outputDataAssociation: List<DataAssociation>
):TransitionImpl(processDefinition,id,targetRef,sourceRef),
    FunctionalTransition

class SendStateImpl(
    processDefinition: ProcessDefinitionImpl,
    id:String,
    name: String,
    isInitial: Boolean,
    transitions: List<Transition>,
    groupRef: Reference<GroupState>?
):StateImpl(processDefinition,id,name,isInitial,false,false,transitions,groupRef),
  SendState

class TransitionSendImpl(
    processDefinition: ProcessDefinitionImpl,
    id:String,
    targetRef: Reference<State>,
    sourceRef: Reference<State>,
    override val messageDefinitionRef: Reference<MessageDefinition>,
    override var recientRef: Reference<Subject>,
    override val outputDataAssociation: List<DataAssociation>
):TransitionImpl(processDefinition,id,targetRef,sourceRef),
    TransitionSend

class RecevivedStateImpl(
    processDefinition: ProcessDefinitionImpl,
    id:String,
    name: String,
    isInitial: Boolean,
    transitions: List<Transition>,
    groupRef: Reference<GroupState>?
):StateImpl(processDefinition,id,name,isInitial,false,false,transitions,groupRef),
  RecevivedState

class TransitionRecevidImpl(
    processDefinition: ProcessDefinitionImpl,
    id:String,
    targetRef: Reference<State>,
    sourceRef: Reference<State>,
    override val messageDefinitionRef: Reference<MessageDefinition>,
    override val senderRef: Reference<Subject>,
    override val outDataAssociation: List<DataAssociation>
):TransitionImpl(processDefinition,id,targetRef,sourceRef),
    TransitionRecevid

class JoinStateImpl(
    processDefinition: ProcessDefinitionImpl,
    id:String,
    name: String,
    isFinish: Boolean,
    isTerminate: Boolean,
    transitions: List<Transition>,
    groupRef: Reference<GroupState>?
): StateImpl(processDefinition,id,name,false,isFinish,isTerminate,transitions,groupRef),
   JoinState

class JoinTransitionImpl(
    processDefinition: ProcessDefinitionImpl,
    id:String,
    targetRef: Reference<State>,
    sourceRef: Reference<State>
):TransitionImpl(processDefinition,id,targetRef,sourceRef),
  JoinTransition

class ForkStateImpl(
    processDefinition: ProcessDefinitionImpl,
    id:String,
    name: String,
    isInitial: Boolean,
    transitions: List<Transition>,
    groupRef: Reference<GroupState>?
): StateImpl(processDefinition,id,name,isInitial,false,false,transitions,groupRef),
    ForkState

class ForkTransitionImpl(
    processDefinition: ProcessDefinitionImpl,
    id:String,
    targetRef: Reference<State>,
    sourceRef: Reference<State>
):TransitionImpl(processDefinition,id,targetRef,sourceRef),
  ForkTransition

class GroupStateImpl(
    processDefinition: ProcessDefinitionImpl,
    id:String,
    name: String,
    isInitial: Boolean,
    isFinish: Boolean,
    isTerminate: Boolean,
    transitions: List<Transition>,
    groupRef: Reference<GroupState>?,
    override val loopCharacteristics: LoopCharacteristics?

): StateImpl(processDefinition,id,name,isInitial,isFinish,isTerminate,transitions,groupRef),
   GroupState