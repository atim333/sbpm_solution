package sbpm_solution.bpms.sbpm.model

import java.util.stream.Collector
import java.util.stream.Collectors

interface Subject : RootElement{
    var name : String
    val states : MutableList<State>
    fun findState(stateId: String): State?{
        return states.stream().filter{state: State->state.id.equals(stateId)}
            .findFirst()
            .orElse(null)
    }
}

interface State : BaseElement{
    var name: String?
    var isInital: Boolean
    var isFinish: Boolean
    var isTerminate: Boolean
    val transitions: MutableList<Transition>
    var groupRef: Reference<GroupState>?

    fun getSubject(): Subject{
        return processDefinition!!.findSubject(this)!!
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
    var targetRef: Reference<State>
    var sourceRef: Reference<State>
    fun getState(): State?{
        return targetRef?.resolvedReference()
    }
}

interface ErrorTransition: Transition

interface TimerTransition: Transition{
    var dateException: String?
    var durationException: String?
}

interface FunctionalState: State {
    var functionalAction: FunctionalAction?
    var functionalActionRef: Reference<FunctionalAction>?

    fun getAction(): FunctionalAction{
        if (functionalAction!=null){
            return functionalAction!!
        }
        val ret = functionalActionRef!!.resolvedReference()
        return ret!!
    }

    fun actionIsRef(): Boolean{
        return functionalActionRef != null
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
    var name: String?
    val outputDataAssociation: MutableList<DataAssociation>
}

interface SendSrate: State {
    fun transitionSendExist(): Boolean{
      val ret= transitions.stream()
          .filter{transition-> transition is TransitionSend}
          .findFirst()
          .isPresent
      return ret
    }

    fun getTransitionSend(): TransitionSend{
      val ret = transitions.stream()
          .filter{transition -> transition is TransitionSend}
          .map { transition-> transition as TransitionSend }
          .findFirst().get()
      return ret
    }
}

interface TransitionSend: Transition{
    var messageDefinitionRef: Reference<MessageDefinition>?
    var recientRef: Reference<Subject>?
    val outputDataAssociation: MutableList<DataAssociation>

    fun getMessageDefinition(): MessageDefinition{
        return messageDefinitionRef!!.resolvedReference()!!
    }

    fun getRecient(): Subject {
        return recientRef!!.resolvedReference()!!
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
    var messageDefinitionRef: Reference<MessageDefinition>
    var senderRef: Reference<Subject>
    val outDataAssociation: MutableList<DataAssociation>

    fun getMessgeDefinition(): MessageDefinition? {
        if (messageDefinitionRef != null){
            messageDefinitionRef.resolvedReference()
        }
        return null
    }

    fun getSender(): Subject{
        return senderRef.resolvedReference()!!
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

interface JoinTransition

interface ForkState : State{
    fun getForkTransitions(): List<ForkTransition> {
        val ret=transitions.stream()
            .filter{transition-> transition is ForkTransition}
            .map { transition-> transition as ForkTransition }
            .collect(Collectors.toList())
        return ret
    }
}

interface ForkTransition

interface GroupState: State {
   var loopCharacteristics: LoopCharacteristics?

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
           .filter{state-> state.isInital}
           .findFirst()
           .get()
       return ret
   }
   fun groupTransition(): GroupTransition?{
       if (isFinish || isTerminate){
          null
       }
       val ret=transitions.stream()
           .filter{transition -> transition is GroupTransition}
           .map { transition -> transition as GroupTransition }
           .findFirst().get()
       return ret
   }
}

interface GroupTransition: Transition