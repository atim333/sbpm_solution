package sbpm_solution.bpms.sbpm.format.json

import sbpm_solution.bpms.sbpm.format.*
import sbpm_solution.bpms.sbpm.model.*

class JSONComposer: Composer {
    override fun compose(resource: ProcessDefinition): ProcessDefinitionDT{
       val name = resource.name
       val version= resource.version
       val mainSubject = if( resource.mainSubject!=null)
           resource.mainSubject!!.id
        else
            null
       val inputDataAssociations = composeDataAssociations(resource.inputDataAssociations)
       val rootElements  = composeRootElements(resource.rootElements)
       return ProcessDefinitionDT(
           name =name,
           version = version,
           mainSubject= mainSubject,
           inputDataAssociations = inputDataAssociations,
           rootElements = rootElements
       )
    }


    private fun composeDataAssociations(inputDataAssociations: List<DataAssociation>): List<DataAssociationDT>{
        val ret = mutableListOf<DataAssociationDT>()
        inputDataAssociations.forEach {
            item ->ret.add(DataAssociationDT(
                      targetPath = item.targetPath,
                      target = item.target,
                      sourcePath = item.sourcePath,
                      sourceMapFunction = item.sourceMapFunction
            ))
        }
        return ret.toList()
    }

    private fun composeRootElements(rootElements: List<RootElement>): List<RootElementDT>{
        val ret = mutableListOf<RootElementDT>()
        rootElements.forEach {
            item-> when (item){
                is MessageDefinition-> {
                    ret.add(MessageDefinitionDT(
                        id = item.id!!,
                        name = item.name
                    ))
                }
                is ServiceTaskAction -> {
                    ret.add(
                        composeServiceTaskAction(item)
                    )
                }
                is ScriptAction-> {
                    ret.add(
                        composeScriptAction(item)
                    )
                }
                is DecisionTableAction-> {
                    ret.add(
                        composeDecisionTableAction(item)
                    )
                }
                is ExternalTaskAction-> {
                    ret.add(
                        composeExternalTaskAction(item)
                    )
                }
                is Subject-> {
                    ret.add(
                        SubjectDT(
                            id = item.id!!,
                            name = item.name,
                            states = composeStates(item.states)
                        )
                    )
                }
            }
        }
        return ret.toList()
    }

    private fun composeServiceTaskAction(action: ServiceTaskAction): ServiceTaskActionDT{
        return ServiceTaskActionDT(
            id=action.id!!,
            specification = action.specification,
            method = action.method,
            name= action.name
        )
    }

    private fun composeScriptAction(action:ScriptAction): ScriptActionDT{
        return ScriptActionDT(
            id = action.id!!,
            name = action.name,
            body =action.body
        )
    }

    private fun composeDecisionTableAction(action:DecisionTableAction): DecisionTableActionDT{
        return DecisionTableActionDT(
            id = action.id!!,
            name = action.name,
            table =action.table
        )
    }

    private fun composeExternalTaskAction(action:ExternalTaskAction): ExternalTaskActionDT{
        return ExternalTaskActionDT(
            id = action.id!!,
            name = action.name
        )
    }

    private fun composeStates(states: List<State>): List<StateDT>{
        val ret = mutableListOf<StateDT>()

        states.forEach {
            item-> when (item){
              is FunctionalState->{
                  ret.add(composeFunctionalState(item))
                  }
              is SendState->{
                  ret.add(
                      SendStateDT(
                          id = item.id!!,
                          name = item.name,
                          isInitial = item.isInitial,
                          groupRef = item.groupRef?.id,
                          transitions = composeTransitons(item.transitions)
                      )
                  )
              }
              is RecevivedState->{
                  ret.add(
                      RecevivedStateDT(
                          id = item.id!!,
                          name = item.name,
                          isInitial = item.isInitial,
                          groupRef = item.groupRef?.id,
                          transitions = composeTransitons(item.transitions)
                      )
                  )
              }
              is JoinState->{
                  ret.add(
                      JoinStateDT(
                          id = item.id!!,
                          name = item.name,
                          isFinish = item.isFinish,
                          isTerminate= item.isTerminate,
                          groupRef = item.groupRef?.id,
                          transitions = composeTransitons(item.transitions)
                      )
                  )
              }
              is ForkState->{
                  ret.add(
                      ForkStateDT(
                          id = item.id!!,
                          name = item.name,
                          isInitial = item.isInitial,
                          groupRef = item.groupRef?.id,
                          transitions = composeTransitons(item.transitions)
                      )
                  )
              }
              is GroupState-> {
                  var loopCharacteristics: LoopCharacteristicsDT? = null
                  if (item.loopCharacteristics != null) {
                      loopCharacteristics = LoopCharacteristicsDT(
                          loopVariableName = item.loopCharacteristics!!.loopVariableName,
                          loopExpression = item.loopCharacteristics!!.loopExpression
                      )
                  }
                  ret.add(
                      GroupStateDT(
                          id = item.id!!,
                          name = item.name,
                          isInitial = item.isInitial,
                          isFinish = item.isFinish,
                          isTerminate = item.isTerminate,
                          groupRef = item.groupRef?.id,
                          transitions = composeTransitons(item.transitions),
                          loopCharacteristics = loopCharacteristics
                      )
                  )
              }
            }
        }
        return ret.toList()
    }


    private fun composeFunctionalState(state: FunctionalState ): FunctionalStateDT{
        var  actionRef: String? = null
        var  actionBody:FunctionalActionDT? = null
        when (val action= state.action){
            is ActionDefinition.ActionRef ->{
                actionRef = action.functionalActionRef.id
            }
            is   ActionDefinition.ActionBody ->{
                when (val body= action.functionalAction) {
                    is ServiceTaskAction ->{
                        actionBody= composeServiceTaskAction(body)
                    }
                    is ScriptAction -> {
                        actionBody=composeScriptAction(body)
                    }
                    is DecisionTableAction-> {
                        actionBody=composeDecisionTableAction(body)
                    }
                    is ExternalTaskAction-> {
                        actionBody=composeExternalTaskAction(body)
                    }
                }

            }
        }

        return FunctionalStateDT(
            id = state.id!!,
            name = state.name,
            isInitial = state.isInitial,
            isFinish = state.isFinish,
            isTerminate = state.isTerminate,
            groupRef = state.groupRef?.id,
            transitions = composeTransitons(state.transitions),
            actionBody = actionBody,
            actionRef = actionRef,
            inputDataAssociation = composeDataAssociations(state.inputDataAssociation)
        )
    }

    private fun composeTransitons(transitions : List<Transition>): List<TransitionDT>{
        val ret = mutableListOf<TransitionDT>()
        transitions.forEach {
            item-> when(item){
              is ErrorTransition->{
                  ret.add(
                      ErrorTransitionDT(
                          id =item.id!!,
                          targetRef = item.targetRef.id,
                          sourceRef = item.sourceRef.id
                      )
                  )
              }
              is TimerTransition->{
                  ret.add(
                      TimerTransitionDT(
                          id =item.id!!,
                          targetRef = item.targetRef.id,
                          sourceRef = item.sourceRef.id,
                          expression = item.expression
                      )
                  )
              }
              is FunctionalTransition->{
                  ret.add(
                      FunctionalTransitionDT(
                          id =item.id!!,
                          targetRef = item.targetRef.id,
                          sourceRef = item.sourceRef.id,
                          name = item.name,
                          outputDataAssociation = composeDataAssociations(item.outputDataAssociation)
                      )
                  )
              }
              is TransitionSend->{
                  ret.add(
                      TransitionSendDT(
                          id =item.id!!,
                          targetRef = item.targetRef.id,
                          sourceRef = item.sourceRef.id,
                          messageDefinitionRef=item.messageDefinitionRef.id,
                          recientRef = item.recientRef.id,
                          outputDataAssociation= composeDataAssociations(item.outputDataAssociation)
                      )
                  )
              }
              is TransitionRecevid->{
                  ret.add(
                      TransitionRecevidDT(
                          id =item.id!!,
                          targetRef = item.targetRef.id,
                          sourceRef = item.sourceRef.id,
                          messageDefinitionRef=item.messageDefinitionRef.id,
                          senderRef= item.senderRef.id,
                          outDataAssociation = composeDataAssociations(item.outDataAssociation)
                      )
                  )
              }
              is JoinTransition->{
                  ret.add(
                      JoinTransitionDT(
                          id =item.id!!,
                          targetRef = item.targetRef.id,
                          sourceRef = item.sourceRef.id
                      )
                  )
              }
              is ForkTransition->{
                  ret.add(
                      ForkTransitionDT(
                          id =item.id!!,
                          targetRef = item.targetRef.id,
                          sourceRef = item.sourceRef.id
                      )
                  )
              }
              is GroupTransition->{
                  ret.add(
                      GroupTransitionDT(
                          id =item.id!!,
                          targetRef = item.targetRef.id,
                          sourceRef = item.sourceRef.id
                      )
                  )
              }
            }
        }
        return ret.toList()
    }
}


