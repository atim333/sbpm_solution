package sbpm_solution.bpms.sbpm.abstractMachine2

import sbpm_solution.bpms.sbpm.decisiontable.DecisionTableEvaluateError
import sbpm_solution.bpms.sbpm.model.*
import sbpm_solution.bpms.utils.EvalContext
import sbpm_solution.bpms.utils.converter.Convertor
import java.time.Duration
import java.time.LocalDateTime

internal object Actions {

    fun setTimer(localContext: Environment, token: Token) {
        val state: State = localContext.getState(token)
        if (state is FunctionalState || state is GroupState || state is RecevivedState) {
            val timerTransition = state.getTimerTransition()
            if (timerTransition != null) {
                val parentContext = localContext.getParentContext(token.tokenId)
                val subjectContext = localContext.context.getSubjectData(token.siid)
                val context = EvalContext(subjectContext, parentContext)
                val timerExpression = timerTransition.expression
                val dt: LocalDateTime =
                    try {
                        when (timerExpression) {
                            is TimerTransitionType.DateExpressionType -> {
                                val script = timerExpression.expression
                                val obj = localContext.evalSctipt(script, context)
                                Convertor.convert(obj, LocalDateTime::class.java)!!
                            }
                            is TimerTransitionType.DurationExpressionType -> {
                                val script = timerExpression.expression
                                val obj = localContext.evalSctipt(script, context)
                                val duration = Convertor.convert(obj, Duration::class.java)!!
                                LocalDateTime.from(duration.addTo(LocalDateTime.now()))
                            }
                        }
                    } catch (e: Throwable) {
                        val path = PathBuilder.getPath(timerTransition)
                        throw ScriptError2(path, e)
                    }

                localContext.context.setTimer(dt, token.tokenId)
            }
        }
    }

    fun doSend(localContext: Environment, token: Token, dbgMsg: Map<*, *>?) {
        val sendState: SendState = localContext.getState(token)
        val sender = sendState.getSubject()
        val piid: String = token.piid

        val transition = sendState.getTransitionSend()!!

        val messageDefinition = transition.getMessageDefinition()
        val recipient = transition.getRecient()

        val senderId = sender.id!!
        val recipientId: String = recipient.id!!

        if (!localContext.context.existSubjectInstance(piid, recipientId)) {
            val state: State = recipient.initialState()!!
            if (canStart(state, sender.id!!, messageDefinition.id!!)) {
                val receivedState = state as RecevivedState
                val siid = localContext.context.createSubjectInstance(piid, recipient.id!!)
                val tokenId = localContext.context.createToken(siid, state.id!!).tokenId
                receivedState.getTransitionRecevid()
                    .forEach { transitionReceived ->
                        localContext.context.createSubscriber(
                            tokenId,
                            transitionReceived.senderRef.id,
                            transitionReceived.messageDefinitionRef.id
                        )
                    }
            }
        }
        val msgContext: MutableMap<String, Any?> = LinkedHashMap()
        val parentContext = localContext.getParentContext(token.tokenId)
        var subjectContext = localContext.context.getSubjectData(token.siid)

        if (dbgMsg != null) {
            @Suppress("UNCHECKED_CAST")
            msgContext.putAll(dbgMsg as Map<String, Any?>)
        } else {
            val ipnutDataAssociations: List<DataAssociation> = transition.outputDataAssociation
            localContext.resolveAsoc(EvalContext(subjectContext, parentContext), msgContext, ipnutDataAssociations)
        }

        localContext.takeToken(token, transition.targetRef)
        val messageId = messageDefinition.id!!

        val subscribers: List<Subscriber> = localContext.context.getSubscribers(piid, recipientId, senderId, messageId)
        val used: MutableSet<String> = HashSet()

        for (subscriber in subscribers) {
            val tokenId: String = subscriber.tokenId
            val newToken = localContext.context.getToken(tokenId)
            val receivedState: RecevivedState = localContext.getState(newToken)
            val tgtTransition = receivedState.getTransitionRecevid().stream()
                .filter { transitionReceived ->
                    (transitionReceived.senderRef.id == senderId && transitionReceived.messageDefinitionRef.id == messageId)
                }
                .findFirst()
                .orElse(null)

            if (tgtTransition != null) {
                if (!used.contains(tokenId)) {
                    used.add(tokenId)
                    val debuger: Debuger? = localContext.debuger
                    if (debuger != null) {
                        if (debuger.isBp(newToken)) {
                            localContext.context.createSubscriberBreakPoint(subscriber, msgContext)
                            return
                        }
                    }
                    subjectContext = localContext.context.getSubjectData(newToken.siid)

                    val outputDataAssociations: List<DataAssociation> = tgtTransition.outDataAssociation
                    localContext.resolveAsoc(msgContext, subjectContext, outputDataAssociations)
                    localContext.context.setSubjectData(newToken.siid, subjectContext)
                    localContext.takeToken(newToken, tgtTransition.targetRef)
                    localContext.context.removeSubscribers(tokenId)
                }
            }
        }
    }


    private fun canStart(stateRecipient: State, senderId: String, messageId: String): Boolean {
        var result = false
        if (stateRecipient is RecevivedState) {
            result = stateRecipient.getTransitionRecevid().stream()
                .filter { transitionReceived ->
                    transitionReceived.EQ(
                        senderId, messageId
                    )
                }
                .findFirst()
                .isPresent
        }
        return result
    }

    fun doReceive(localContext:Environment, token: Token) {
        val receivedState = localContext.getState<RecevivedState>(token)
        val tokenId = token.tokenId

        receivedState.getTransitionRecevid().forEach { transitionReceived ->

            localContext.context.createSubscriber(
                tokenId,
                transitionReceived.senderRef.id,
                transitionReceived.messageDefinitionRef.id
            )
        }
    }

    fun doFunctional(localContext: Environment, token: Token) {
        val functionalState = localContext.getState<FunctionalState>(token)
        when (val functionalAction = functionalState.getAction()) {
            is ExternalTaskAction -> {
                createExternalTask(localContext, functionalState, token)
            }
            is ScriptAction -> {
                doFunctionalScript(localContext, functionalState, token)
            }
            is DecisionTableAction -> {
                doFunctionalDecisionTable(localContext, functionalState, token)
            }
            is ServiceTaskAction -> {
                doServiceTask(localContext, functionalState, token)
            }
            else -> {
                throw UnsupportedfunctionalAction(functionalAction)
            }
        }
    }

    fun doScripting(
        localContext: Environment,
        functionalState: FunctionalState,
        token: Token,
        call: (scriptContext: MutableMap<String, Any?>) -> String?
    ) {
        val parentContext = localContext.getParentContext(token.tokenId)
        var subjectContext: MutableMap<String, Any?> = localContext.context.getSubjectData(token.siid)
        val scriptContext =
            localContext.evalFunctionalStateInputAssoc(parentContext, subjectContext, functionalState)
        val trName: String? = call(scriptContext)

        if (functionalState.isFinish) {
            subjectContext = localContext.context.getSubjectData(token.siid)

            subjectContext.putAll(scriptContext)
            doFinish(localContext, token)
        } else {
            val transition: FunctionalTransition = checkFunctionalFunctionalTransition(
                trName!!,
                functionalState
            )
            subjectContext = localContext.evalFunctionalStateOutputAssoc(
                scriptContext,
                parentContext,
                subjectContext,
                transition,
                functionalState
            )
            localContext.context.setSubjectData(token.siid, subjectContext)
            localContext.takeToken(token, transition.targetRef)
        }
    }

    fun checkFunctionalFunctionalTransition(
        tr: String,
        functionalState: FunctionalState
    ): FunctionalTransition {
        return functionalState.getFunctionalTransitions().stream()
            .filter { cur: FunctionalTransition -> cur.name == tr }
            .findFirst()
            .orElse(null) ?: throw TansitionNotDefine(functionalState, tr)
    }

    private fun doFunctionalScript(localContext: Environment, functionalState: FunctionalState, token: Token) {
        val functionalAction = functionalState.getAction()
        val functionalScript = functionalAction as ScriptAction
        doScripting(localContext, functionalState, token) { ctx ->
            val script: String = functionalScript.body
            try {
                val ret = localContext.evalSctipt(script, ctx) as String?
                ret ?: ""
            } catch (tr: Throwable) {
                var path = PathBuilder.getPath(functionalState)
                val sufix = if (functionalState.actionIsRef()) ".functionalActionRef" else ".functionalAction"
                path += sufix
                throw ScriptError2(path, tr)
            }
        }
    }

    private fun doFunctionalDecisionTable(localContext: Environment, functionalState: FunctionalState, token: Token) {
        val functionalAction = functionalState.getAction()
        val functionalDecisionTable = functionalAction as DecisionTableAction

        doScripting(localContext, functionalState, token) { ctx ->
            try {
                val result = localContext.evaluateDecisionTable(functionalDecisionTable.table, ctx)
                result
            } catch (decisionTableEvaluateError: DecisionTableEvaluateError) {

                var path = PathBuilder.getPath(functionalState)
                val sufix = if (functionalState.actionIsRef()) ".functionalActionRef" else ".functionalAction"
                path += sufix
                val path2: String = decisionTableEvaluateError.path
                val cause: Throwable? = decisionTableEvaluateError.cause
                throw ScriptError2(path, path2, cause)
            }
        }
    }


    private fun createExternalTask(localContext: Environment,functionalState: FunctionalState, token: Token) {
        val action = functionalState.getAction() as ExternalTaskAction
        //   val ref = action.formRef
        //   var formKey: String? = ""
        //   if (ref != null) {
        //       formKey = ref.id
        //   }
        localContext.context.createExternalTask(token.tokenId, functionalState.id!!, "")
    }

    fun doServiceTask( localContext: Environment, functionalState: FunctionalState, token: Token) {
        val functionalAction = functionalState.getAction()
        val serviceTask = functionalAction as ServiceTaskAction
        val parentContext = localContext.getParentContext(token.tokenId)
        val subjectContext: MutableMap<String, Any?> =localContext.context.getSubjectData(token.siid)

        val scriptContext =
            localContext.evalFunctionalStateInputAssoc(parentContext, subjectContext, functionalState)

        val pair = localContext.context.serviceCall(
            serviceTask.specification,
            serviceTask.method,
            scriptContext
        )

        if (pair.first != "200") {
            val msg = if (pair.second == null) "null" else pair.second.toString()
            throw ErrorSignal(pair.first, token.stateId, msg)
        }
    }


    fun doFork(localContext: Environment, token: Token) {
        val forkState = localContext.getState<ForkState>(token)
        forkState.getForkTransitions()
            .forEach { transition: ForkTransition ->
                val stateId = transition.targetRef.id
                val newToken =
                    localContext.context.createToken(token.siid, stateId, token.tokenId)
                localContext.takeToken(newToken)
            }
    }

    fun doJoin(localContext: Environment, token: Token) {
        val joinState = localContext.getState<JoinState>(token)
        val parentId = token.parentId
        val parentToken = localContext.context.getToken(parentId!!)
        localContext.context.destroyToken(token)
        val childs = localContext.context.getChilds(parentId)
        if (childs.isEmpty()) {
            if (joinState.isFinish) {
                doFinish(localContext, token)
            } else {
                val transition: JoinTransition = joinState.getJoinTransition()!!
                localContext.takeToken(parentToken, transition.targetRef)
            }
        }
    }

    fun doGroup(localContext: Environment, token: Token) {
        val groupState = localContext.getState<GroupState>(token)
        var loopMaximum = 1
        val loopCharacteristics = groupState.loopCharacteristics
        if (loopCharacteristics != null) {
            val scriptBody = loopCharacteristics.loopExpression
            val loopVariableName = loopCharacteristics.loopVariableName

            val evalCtx = EvalContext(
                localContext.context.getSubjectData(token.siid),
                localContext.getParentContext(token.tokenId)
            )

            val evalResult = try {
                localContext.evalMvelValue(scriptBody, evalCtx)
            } catch (tr: Throwable) {
                val path = PathBuilder.getPath(loopCharacteristics)
                throw ScriptError2(path, tr)
            }

            loopMaximum = when (evalResult) {
                is List<*> -> {
                    evalResult.size
                }
                is Number -> {
                    evalResult.toInt()
                }
                else -> {
                    throw LoopCharacteristicsError(groupState.id!!, token.siid)
                }
            }
            val tokenData = LinkedHashMap<String,Any?>()
            tokenData["loopVariableName"] = loopVariableName
            tokenData["loopCounter"] = 0
            tokenData["loopExpression"] = evalResult

            localContext.context.setTokenData(token.tokenId,tokenData)
        }
        if (loopMaximum > 0) {
            val initialState = groupState.initialState()
            val newToken = localContext.context.createToken(token.siid, initialState.id!!, token.tokenId)
            localContext.takeToken(newToken)
        } else {
            if (groupState.isFinish) {
                doFinish(localContext, token)
            } else {
                val groupTransition: GroupTransition = groupState.groupTransition()!!
                localContext.context.setTokenData(token.tokenId, null)
                localContext.takeToken(token, groupTransition.targetRef)
            }
        }
    }

    fun doFinish(localContext: Environment, token: Token) {
        val parentId = token.parentId
        localContext.context.destroyToken(token)
        if (parentId == null) {
            localContext.context.destroySubjectInstanse(token.siid)
        } else {
            localContext.clear(parentId)
            val parentToken = localContext.context.getToken(parentId)
            val state: State = localContext.getState(parentToken)
            if (state is GroupState) {
                val local = localContext.context.getTokenData(parentToken.tokenId)
                if (local.isNotEmpty()) {
                    var loopCounter = Convertor.convert(local["loopCounter"], Int::class.java)!!
                    val loopExpression = local["loopExpression"]
                    // dub
                    val maxCounter: Int = if (loopExpression is List<*>) {
                        loopExpression.size
                    } else {
                        Convertor.convert(loopExpression, Int::class.java)!!
                    }

                    loopCounter += 1
                    if (maxCounter < loopCounter) {
                        local["loopCounter"] = loopCounter
                        localContext.context.setTokenData(parentToken.tokenId, local)
                        val initialState: State = state.initialState()
                        val newToken =
                            localContext.context.createToken(token.siid, initialState.id!!, token.tokenId)
                        localContext.takeToken(newToken)
                        return
                    }
                }
                if (state.isFinish) {
                    doFinish(localContext, parentToken)
                    return
                } else {
                    val groupTransition: GroupTransition = state.groupTransition()!!
                    localContext.context.setTokenData(parentToken.tokenId, null)
                    localContext.takeToken(parentToken, groupTransition.targetRef)
                }
            } else {
                doFinish(localContext, parentToken)
            }
        }
    }

    fun doError(localContext: Environment, token: Token) {
        val state: State = localContext.getState(token)
        localContext.clear(token.tokenId)
        val errorTransition = state.getErrorTransition()
        if (errorTransition != null) {
            localContext.takeToken(token, errorTransition.targetRef)
        } else {
            val parentId = token.parentId
            if (parentId == null) {
                localContext.error()
            } else {
                val parentToken = localContext.context.getToken(parentId)
                doError(localContext, parentToken)
            }
        }
    }

}