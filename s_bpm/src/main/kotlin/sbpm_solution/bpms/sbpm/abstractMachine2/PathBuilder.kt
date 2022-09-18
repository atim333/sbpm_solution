package sbpm_solution.bpms.sbpm.abstractMachine2

import sbpm_solution.bpms.sbpm.model.*

object PathBuilder {
    fun getPath(tgtElement: BaseElement): String? {
        if (tgtElement is ProcessDefinition) {
            return "$"
        }
        val processDefinition = tgtElement.processDefinition
        val lst: List<*> = processDefinition.inputDataAssociations
        val idx = lst.indexOf(tgtElement)
        return if (idx >= 0) {
            "inputDataAssociations[$idx]"
        } else {
            findInRootElements(processDefinition.rootElements, tgtElement)
        }
    }

    private fun findInRootElements(rootElements: List<RootElement>, tgtElement: BaseElement): String? {
        val base = "rootElements"
        for (i in rootElements.indices) {
            val source = rootElements[i]
            if (source is Subject) {
                if (source === tgtElement) {
                    return "$base[$i]"
                }
                val path = findInSubject("$base[$i]", source, tgtElement)
                if (path != null) {
                    return path
                }
            } else if (source is ScriptAction) {
// to do
            } else if (source is DecisionTableAction) {
// to do
            }
        }
        return null
    }

    private fun findInSubject(root: String, subject: Subject, tgtElement: BaseElement): String? {
        val base = "$root.states"
        val states: List<State> = subject.states

        for (i in states.indices) {
            val state = states[i]
            if (state === tgtElement) {
                return "$base[$i]"
            }
            val path = findInTransitions("$base[$i]", state.transitions, tgtElement)
            if (path != null) {
                return path
            }
            if (state is FunctionalState) {
                val functionalState = state
                val inputDataAssociations: List<DataAssociation> = functionalState.inputDataAssociation
                val idx = inputDataAssociations.indexOf(tgtElement)
                if (idx >= 0) {
                    return "$base[$i].inputDataAssociations[$idx]"
                }
                if (!functionalState.actionIsRef()) {
                    val functionalAction = functionalState.getAction()
                    if (functionalAction === tgtElement) {
                        return "$base[$i].functionalAction"
                    }
                }
            } else if (state is GroupState) {
                val loopCharacteristics = state.loopCharacteristics
                if (loopCharacteristics === tgtElement) {
                    return "$base[$i].loopCharacteristics"
                }
            }
        }
        return null
    }
    private fun findInTransitions(root: String, transitions: List<Transition>, tgtElement: BaseElement): String? {
        val base = "$root.transitions"
        for (i in transitions.indices) {
            val transition = transitions[i]
            if (transition === tgtElement) {
                return "$base[$i]"
            }
            if (transition is FunctionalTransition) {
                val dataAssociations: List<DataAssociation> = transition.outputDataAssociation
                val idx = dataAssociations.indexOf(tgtElement)
                if (idx >= 0) {
                    return "$base[$i].outputDataAssociations[$idx]"
                }

                // outputDataAssociations
            } else if (transition is TransitionSend) {
                val dataAssociations: List<DataAssociation> = transition.outputDataAssociation
                val idx = dataAssociations.indexOf(tgtElement)
                if (idx >= 0) {
                    return "$base[$i].outputDataAssociations[$idx]"
                }
            } else if (transition is TransitionRecevid) {
                val dataAssociations: List<DataAssociation> = transition.outDataAssociation
                val idx = dataAssociations.indexOf(tgtElement)
                if (idx >= 0) {
                    return "$base[$i].outputDataAssociations[$idx]"
                }
            }
        }
        return null
    }
}
