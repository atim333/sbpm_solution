package sbpm_solution.bpms.sbpm.abstractMachine2

import sbpm_solution.bpms.sbpm.model.*


class AbstractMachine (val executionContext: ExecutionContext) {
    fun startProcess(
        processDefinitionId: String,
        userPiid: String?=null,
        subjectId: String?=null,
        data:  Any? =null,
        isDebug: Boolean = false
    ): String {
        val localContext = LocalContext()
        val processDefinition = executionContext.getProcessDefinition(processDefinitionId)

        val subjectRef: Reference<Subject> = if (subjectId == null) {
            if (processDefinition.mainSubject != null) {
                processDefinition.mainSubject
            }
            throw SubjectNotDefined(processDefinitionId, "null")
        } else {
            processDefinition.createReference(subjectId)
        }
        val subject: Subject =
            subjectRef.resolvedReference() ?: throw SubjectNotDefined(processDefinitionId, subjectRef.id)
        val startState = subject.initialState()!!

        val piid = executionContext.createProcessInstance(processDefinitionId, userPiid, isDebug)
        val siid = executionContext.createSubjectInstance(piid, subject.id!!)
        val token = executionContext.createToken(siid, startState.id!!)

        if (isDebug) {
            // TO DO processDefinition.bp!!
            localContext.debuger = Debuger(piid, processDefinition.bp!!)
        }

        localContext.set(token)

        val workdata = data ?: LinkedHashMap<String, Any>()
        val subjectCtx: MutableMap<String, Any?> = LinkedHashMap()

        localContext.resolveAsoc(workdata, subjectCtx, processDefinition.inputDataAssociations)
        executionContext.setSubjectData(siid, subjectCtx)

        excecute(localContext, token)
        return piid
    }

    private fun excecute(
        localContext: Environment,
        prmtoken: Token,
        prmSkipFirst: Boolean = false,
        prmdbgMessage: Map<*, *>? = null
    ) {
        localContext.initQ()
        var skipFirst = prmSkipFirst
        var token: Token? = prmtoken
        var dbgMessage = prmdbgMessage
        val debuger: Debuger? = localContext.debuger
        while (token != null) {
            localContext.set(token)
            Actions.setTimer(localContext, token)
            if (debuger != null){
                if (skipFirst){
                    skipFirst=false
                } else {
                    val state: State = localContext.getState(token)
                    if (state !is RecevivedState) {
                        if (debuger.isBp(token)) {
                            executionContext.createTokenBreakPoint(token)
                            token = localContext.getToken()
                            continue
                        }
                    }
                }
            }
            localContext.error = null

            try {

                when (localContext.getState(token) as State) {
                    is SendState -> {
                        Actions.doSend(localContext, token, dbgMessage)
                    }
                    is RecevivedState -> {
                        Actions.doReceive(localContext, token)
                    }
                    is FunctionalState -> {
                        Actions.doFunctional(localContext, token)
                    }
                    is ForkState -> {
                        Actions.doFork(localContext, token)
                    }
                    is JoinState -> {
                        Actions.doJoin(localContext, token)
                    }
                    is GroupState -> {
                        Actions.doGroup(localContext, token)
                    }
                }
                dbgMessage = null
                token = localContext.getToken()
            } catch (errorSignal: ErrorSignal) {
                localContext.error = errorSignal
                Actions.doError(localContext, token)
            }
        }
    }


    internal inner class LocalContext
        (override val context: ExecutionContext =executionContext)
        : Environment() {
   }
}