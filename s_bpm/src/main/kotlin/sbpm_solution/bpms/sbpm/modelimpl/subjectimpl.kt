package sbpm_solution.bpms.sbpm.modelimpl

import sbpm_solution.bpms.sbpm.model.*


class  SubjectImpl(
    processDefinition: ProcessDefinitionImpl,
    id:String,
    override val name: String,
    override val states: MutableList<State> = ArrayList()
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
    override val groupRef: Reference<GroupState>?,
    override val transitions: MutableList<Transition> = ArrayList(),
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
    override  val expression: String
): TransitionImpl(processDefinition,id,targetRef,sourceRef),
    TimerTransition

class FunctionalStateImpl(
    processDefinition: ProcessDefinitionImpl,
    id:String,
    name: String,
    isInitial: Boolean,
    isFinish: Boolean,
    isTerminate: Boolean,
    groupRef: Reference<GroupState>?,
    override val action: ActionDefinition,
    override val inputDataAssociation: MutableList<DataAssociation> = ArrayList()
): StateImpl(processDefinition,id,name,isInitial,isFinish,isTerminate,groupRef),
    FunctionalState

class FunctionalTransitionImpl(
    processDefinition: ProcessDefinitionImpl,
    id:String,
    targetRef: Reference<State>,
    sourceRef: Reference<State>,
    override val name: String,
    override val outputDataAssociation: MutableList<DataAssociation> = ArrayList()
):TransitionImpl(processDefinition,id,targetRef,sourceRef),
    FunctionalTransition

class SendStateImpl(
    processDefinition: ProcessDefinitionImpl,
    id:String,
    name: String,
    isInitial: Boolean,
    groupRef: Reference<GroupState>?
):StateImpl(processDefinition,id,name,isInitial,false,false,groupRef),
  SendState

class TransitionSendImpl(
    processDefinition: ProcessDefinitionImpl,
    id:String,
    targetRef: Reference<State>,
    sourceRef: Reference<State>,
    override val messageDefinitionRef: Reference<MessageDefinition>,
    override var recientRef: Reference<Subject>,
    override val outputDataAssociation: MutableList<DataAssociation> = ArrayList()
):TransitionImpl(processDefinition,id,targetRef,sourceRef),
    TransitionSend

class RecevivedStateImpl(
    processDefinition: ProcessDefinitionImpl,
    id:String,
    name: String,
    isInitial: Boolean,
    groupRef: Reference<GroupState>?
):StateImpl(processDefinition,id,name,isInitial,false,false,groupRef),
  RecevivedState

class TransitionRecevidImpl(
    processDefinition: ProcessDefinitionImpl,
    id:String,
    targetRef: Reference<State>,
    sourceRef: Reference<State>,
    override val messageDefinitionRef: Reference<MessageDefinition>,
    override val senderRef: Reference<Subject>,
    override val outDataAssociation: MutableList<DataAssociation> = ArrayList()
):TransitionImpl(processDefinition,id,targetRef,sourceRef),
    TransitionRecevid

class JoinStateImpl(
    processDefinition: ProcessDefinitionImpl,
    id:String,
    name: String,
    isFinish: Boolean,
    isTerminate: Boolean,
    groupRef: Reference<GroupState>?
): StateImpl(processDefinition,id,name,false,isFinish,isTerminate,groupRef),
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
    groupRef: Reference<GroupState>?
): StateImpl(processDefinition,id,name,isInitial,false,false,groupRef),
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
     groupRef: Reference<GroupState>?,
    override val loopCharacteristics: LoopCharacteristics?
): StateImpl(processDefinition,id,name,isInitial,isFinish,isTerminate,groupRef),
   GroupState

class GroupTransitionImpl(
    processDefinition: ProcessDefinitionImpl,
    id:String,
    targetRef: Reference<State>,
    sourceRef: Reference<State>
):TransitionImpl(processDefinition,id,targetRef,sourceRef),
    GroupTransition
