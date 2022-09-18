package sbpm_solution.bpms.sbpm.model

import java.util.stream.Collectors

interface Subject : RootElement {
    val name: String
    val states: List<State>
    fun findState(stateId: String): State? {
        return states.stream()
            .filter { state: State -> state.id.equals(stateId) }
            .findFirst()
            .orElse(null)
    }
    fun initialState(): State? {
        return states.stream()
            .filter { state: State -> state.isInitial && state.groupRef == null }
            .findFirst()
            .orElse(null)
    }
}



interface State : BaseElement {
    val name: String
    val isInitial: Boolean
    val isFinish: Boolean
    val isTerminate: Boolean
    val transitions: List<Transition>
    val groupRef: Reference<GroupState>?

    fun getSubject(): Subject {
        return processDefinition.findSubject(this)!!
    }

    fun getErrorTransition(): ErrorTransition?{
        return transitions.stream()
            .filter { transition-> transition is ErrorTransition }
            .map { transition-> transition as ErrorTransition }
            .findFirst().orElse(null)
    }

    fun getTimerTransition(): TimerTransition? {
        return transitions.stream()
            .filter { transition-> transition is TimerTransition }
            .map { transition-> transition as TimerTransition }
            .findFirst().orElse(null)
    }
}



interface Transition : BaseElement {
    val targetRef: Reference<State>
    val sourceRef: Reference<State>
    fun getState(): State{
        return targetRef.resolvedReference()!!
    }
}

interface ErrorTransition: Transition

sealed class TimerTransitionType {
    class DateExpressionType(val expression: String) : TimerTransitionType()
    class DurationExpressionType(val expression: String) : TimerTransitionType()
}

interface TimerTransition: Transition{
    val expression: TimerTransitionType
}

sealed class ActionDefinition {
    class ActionRef(val functionalActionRef: Reference<FunctionalAction>):ActionDefinition()
    class ActionBody(val functionalAction: FunctionalAction):ActionDefinition()
}

interface FunctionalState: State {
    val  action: ActionDefinition
    val inputDataAssociation: List<DataAssociation>
    fun actionIsRef(): Boolean {
       return action is ActionDefinition.ActionRef
    }
    fun getAction(): FunctionalAction {
        return when(action) {
            is ActionDefinition.ActionBody -> (action as ActionDefinition.ActionBody).functionalAction
            is ActionDefinition.ActionRef  -> (action as ActionDefinition.ActionRef).functionalActionRef.resolvedReference()!!
        }
    }


    fun getFunctionalTransitions(): List<FunctionalTransition>{
        val ret= transitions.stream()
            .filter{transition -> (transition is FunctionalTransition)}
            .map { transition -> transition as FunctionalTransition  }
            .collect(Collectors.toList())
        return ret
    }
}

interface FunctionalTransition: Transition {
    val name: String
    val outputDataAssociation: List<DataAssociation>
}

interface SendState: State {

    fun getTransitionSend(): TransitionSend?{
      val ret = transitions.stream()
          .filter{transition -> transition is TransitionSend}
          .map { transition-> transition as TransitionSend }
          .findFirst()
          .orElse(null)
      return ret
    }
}

interface TransitionSend: Transition{
    val messageDefinitionRef: Reference<MessageDefinition>
    var recientRef: Reference<Subject>
    val outputDataAssociation: List<DataAssociation>

    fun getMessageDefinition(): MessageDefinition{
        return messageDefinitionRef.resolvedReference()!!
    }

    fun getRecient(): Subject {
        return recientRef.resolvedReference()!!
    }
}

interface RecevivedState: State {
 fun getTransitionRecevid(): List<TransitionRecevid> {
     val ret = transitions.stream()
         .filter{transition-> transition is TransitionRecevid}
         .map { transition-> transition as TransitionRecevid }
         .collect(Collectors.toList())
     return ret
 }
}

interface TransitionRecevid : Transition{
    val messageDefinitionRef: Reference<MessageDefinition>
    val senderRef: Reference<Subject>
    val outDataAssociation: List<DataAssociation>

    fun getMessgeDefinition(): MessageDefinition {
         return  messageDefinitionRef.resolvedReference()!!
    }

    fun getSender(): Subject{
        return senderRef.resolvedReference()!!
    }

    fun EQ(senderId: String, messageId: String): Boolean {
        return senderRef.id == senderId && messageDefinitionRef.id == messageId
    }
}

interface JoinState : State {
    fun getJoinTransition(): JoinTransition? {
        if (isFinish|| isTerminate) {
            return null
        }
        val ret= transitions.stream()
            .filter{transition -> transition is JoinTransition}
            .map { transition-> transition as JoinTransition }
            .findFirst().get()
        return ret
    }
}

interface JoinTransition: Transition

interface ForkState : State{
    fun getForkTransitions(): List<ForkTransition> {
        val ret=transitions.stream()
            .filter{transition-> transition is ForkTransition}
            .map { transition-> transition as ForkTransition }
            .collect(Collectors.toList())
        return ret
    }
}

interface ForkTransition: Transition

interface GroupState: State {
   val loopCharacteristics: LoopCharacteristics?

   fun getStates(): List<State>{
       val subject= getSubject()
       val testId= id!!
       return subject.states.stream()
           .filter{state -> state.groupRef !=null }
           .filter{state ->  state.groupRef!!.id.equals(testId)}
           .collect(Collectors.toList())
   }

   fun initialState(): State{
       val grouStates= getStates()
       val ret = grouStates.stream()
           .filter{state-> state.isInitial}
           .findFirst()
           .get()
       return ret
   }
   fun groupTransition(): GroupTransition?{
       val ret=transitions.stream()
           .filter{transition -> transition is GroupTransition}
           .map { transition -> transition as GroupTransition }
           .findFirst().get()
       return ret
   }
}

interface GroupTransition: Transition