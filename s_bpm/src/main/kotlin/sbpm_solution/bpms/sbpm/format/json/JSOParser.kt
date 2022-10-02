package sbpm_solution.bpms.sbpm.format.json

import sbpm_solution.bpms.sbpm.format.*
import sbpm_solution.bpms.sbpm.model.*
import sbpm_solution.bpms.sbpm.modelimpl.*
import com.jayway.jsonpath.InvalidPathException
import com.jayway.jsonpath.JsonPath
import org.mvel2.CompileException
import org.mvel2.compiler.ExpressionCompiler
import sbpm_solution.bpms.sbpm.decisiontable.el.ELNodeParser


class JSOParser : Parser {
    override fun parse(json: ProcessDefinitionDT): ProcessDefinition{

        val model=ModelBuilder()
        val processDefinition =  model.build(json)
        val  modelValidator =  ModelValidator(processDefinition)
        val ret = modelValidator.validate()
        return ret
    }



    private class ModelBuilder(
        val processDefinition: ProcessDefinitionImpl =  ProcessDefinitionImpl()
    ) {
        fun build(json: ProcessDefinitionDT) : ProcessDefinitionImpl{
            processDefinition.name = json.name
            processDefinition.version= json.version
            if (json.mainSubject!=null){
                processDefinition.mainSubject= processDefinition.createReference(json.mainSubject)
            }
            buildDataAssociations(processDefinition.inputDataAssociations,json.inputDataAssociations)
            buildRootElements(processDefinition.rootElements,json.rootElements)

            return processDefinition
        }

        private fun buildDataAssociations(dataAssociations: MutableList<DataAssociation>,json: List<DataAssociationDT>){
            json.forEach {
                dataAssociations.add(
                    DataAssociationImpl(processDefinition= processDefinition,
                                        targetPath=it.targetPath,
                                        target= it.target,
                                        sourcePath=it.sourcePath,
                                        sourceMapFunction=it.sourceMapFunction
                    )
                )
            }
        }

        private fun buildRootElements( rootElements: MutableList<RootElement>,json: List<RootElementDT>){
            json.forEach {
                when(it){
                   is MessageDefinitionDT -> {
                       rootElements.add(
                           MessageDefinitionImpl(
                               processDefinition = processDefinition,
                               id = it.id,
                               name= it.name)
                       )
                   }
                   is ScriptActionDT -> {
                       rootElements.add(
                           ScriptActionImpl(
                               processDefinition =processDefinition,
                               id  = it.id,
                               name = it.name,
                               body= it.body)
                       )
                   }
                   is ServiceTaskActionDT -> {
                       rootElements.add(
                           ServiceTaskActionImpl(
                               processDefinition = processDefinition,
                               id = it.id,
                               name = it.name,
                               specification = it.specification,
                               method = it.method)
                       )
                   }
                   is DecisionTableActionDT -> {
                       rootElements.add(
                           DecisionTableActionImpl(
                                  processDefinition = processDefinition,
                                  id = it.id,
                                  name = it.name,
                                  table = it.table)
                       )
                   }
                   is ExternalTaskActionDT -> {
                       rootElements.add(
                       ExternalTaskActionImpl(
                               processDefinition = processDefinition,
                               id = it.id,
                               name = it.name)
                       )
                   }
                   is SubjectDT -> {
                       rootElements.add(
                           buildSubject(it)
                       )
                   }
                }

            }
        }

        private fun buildSubject(json : SubjectDT) : SubjectImpl{
           val ret =SubjectImpl (
                     processDefinition= processDefinition,
                     id= json.id,
                     name= json.name
            )
            buildStates(ret.states, json.states)
            return ret
        }

        private fun buildStates(states: MutableList<State>,json : List<StateDT>){
            json.forEach {
                when(it){
                    is FunctionalStateDT->{
                        states.add(buildFunctionalState(it))
                    }
                    is SendStateDT -> {
                        states.add(buildSendState(it))
                    }
                    is RecevivedStateDT -> {
                        states.add(buildRecevivedState(it))
                    }
                    is JoinStateDT -> {
                        states.add(buildJoinStateDT(it))
                    }
                    is ForkStateDT -> {
                        states.add(buildForkStateDT(it))
                    }
                    is GroupStateDT -> {
                        states.add(buildGroupStateDT(it))
                    }
                }
            }
        }

        private fun buildActionDefinition(json: FunctionalStateDT):ActionDefinition{
            if (json.actionRef!=null){
               return ActionDefinition.ActionRef(processDefinition.createReference(json.actionRef))
            }
            val ret:FunctionalAction = when(val body=json.actionBody!!){
                is ScriptActionDT -> {
                    ScriptActionImpl(
                        processDefinition =processDefinition,
                        id  = body.id,
                        name = body.name,
                        body= body.body
                    )
                }
                is ServiceTaskActionDT -> {
                    ServiceTaskActionImpl(
                        processDefinition = processDefinition,
                        id = body.id,
                        name = body.name,
                        specification = body.specification,
                        method = body.method
                    )
                }
                is DecisionTableActionDT -> {
                    DecisionTableActionImpl(
                        processDefinition = processDefinition,
                        id = body.id,
                        name = body.name,
                        table = body.table
                    )
                }
                is ExternalTaskActionDT -> {
                    ExternalTaskActionImpl(
                        processDefinition = processDefinition,
                        id = body.id,
                        name = body.name
                    )
                }
                else -> {
                  throw RuntimeException("Not inposible")
                }

            }
            return ActionDefinition.ActionBody(ret)
        }

        private fun buildTransition(transitions: MutableList<Transition>,json: List<TransitionDT>) {
            json.forEach {
                when(it){
                   is ErrorTransitionDT->{
                       transitions.add(buildErrorTransition(it))
                   }
                   is TimerTransitionDT -> {
                       transitions.add(buildTimerTransition(it))
                   }
                   is FunctionalTransitionDT -> {
                      transitions.add(buildFunctionalTransition(it))
                   }
                   is TransitionSendDT -> {
                       transitions.add(buildTransitionSend(it))
                   }
                   is TransitionRecevidDT -> {
                       transitions.add(buildTransitionRecevid(it))
                   }
                   is JoinTransitionDT -> {
                       transitions.add(buildJoinTransition(it))
                   }
                   is ForkTransitionDT -> {
                       transitions.add(buildForkTransition(it))
                   }
                   is GroupTransitionDT-> {
                       transitions.add(buildGroupTransition(it))
                   }
                }
            }
        }

        private fun buildErrorTransition(json: ErrorTransitionDT): ErrorTransition{
            return ErrorTransitionImpl(
                processDefinition= processDefinition,
                id=json.id,
                targetRef= processDefinition.createReference(json.targetRef),
                sourceRef= processDefinition.createReference(json.sourceRef)
            )
        }

        private fun buildTimerTransition(json: TimerTransitionDT): TimerTransition{
            return TimerTransitionImp(
                processDefinition= processDefinition,
                id= json.id,
                targetRef= processDefinition.createReference(json.targetRef),
                sourceRef= processDefinition.createReference(json.sourceRef),
                expression=json.expression
            )
        }

        private fun buildFunctionalTransition(json: FunctionalTransitionDT ): FunctionalTransition{
          val ret= FunctionalTransitionImpl(
              processDefinition= processDefinition,
              id= json.id,
              targetRef= processDefinition.createReference(json.targetRef),
              sourceRef= processDefinition.createReference(json.sourceRef),
              name= json.name,
            )
            buildDataAssociations(ret.outputDataAssociation,json.outputDataAssociation)
            return ret
       }

        private fun buildTransitionSend(json: TransitionSendDT): TransitionSend{
          val ret= TransitionSendImpl(
              processDefinition= processDefinition,
              id= json.id,
              targetRef= processDefinition.createReference(json.targetRef),
              sourceRef= processDefinition.createReference(json.sourceRef),
              messageDefinitionRef= processDefinition.createReference(json.messageDefinitionRef),
              recientRef= processDefinition.createReference(json.recientRef)
            )
            buildDataAssociations(ret.outputDataAssociation,json.outputDataAssociation)
            return ret
        }

        private fun buildTransitionRecevid(json: TransitionRecevidDT): TransitionRecevid{
          val ret=TransitionRecevidImpl(
              processDefinition= processDefinition,
              id= json.id,
              targetRef= processDefinition.createReference(json.targetRef),
              sourceRef= processDefinition.createReference(json.sourceRef),
              messageDefinitionRef= processDefinition.createReference(json.messageDefinitionRef),
              senderRef= processDefinition.createReference(json.senderRef)
            )
            buildDataAssociations(ret.outDataAssociation,json.outDataAssociation)
            return ret
        }

        private fun buildJoinTransition(json: JoinTransitionDT): JoinTransition{
          return JoinTransitionImpl(
              processDefinition= processDefinition,
              id= json.id,
              targetRef= processDefinition.createReference(json.targetRef),
              sourceRef= processDefinition.createReference(json.sourceRef)
            )
        }

        private fun buildForkTransition(json: ForkTransitionDT): ForkTransition{
           return ForkTransitionImpl(
               processDefinition= processDefinition,
               id= json.id,
               targetRef= processDefinition.createReference(json.targetRef),
               sourceRef= processDefinition.createReference(json.sourceRef)
            )
        }

        private fun buildGroupTransition(json: GroupTransitionDT): GroupTransition{
            return GroupTransitionImpl(
                processDefinition= processDefinition,
                id= json.id,
                targetRef= processDefinition.createReference(json.targetRef),
                sourceRef= processDefinition.createReference(json.sourceRef)
            )
        }

        private fun buildGroupRef(ref:String?):Reference<GroupState>? =
            if (ref == null) null else processDefinition.createReference(ref)

        private fun buildFunctionalState(json : FunctionalStateDT): FunctionalState{
           val ret= FunctionalStateImpl(
                processDefinition = processDefinition,
                id = json.id,
                name= json.name,
                isInitial= json.isInitial,
                isFinish = json.isFinish,
                isTerminate= json.isTerminate,
                groupRef = buildGroupRef(json.groupRef),
                action = buildActionDefinition(json)
            )

            buildDataAssociations(ret.inputDataAssociation,json.inputDataAssociation)
            buildTransition(ret.transitions,json.transitions)
            return ret
        }

        private fun buildSendState(json : SendStateDT): SendState {
            val ret= SendStateImpl(
                processDefinition = processDefinition,
                id = json.id,
                name= json.name,
                isInitial= json.isInitial,
                groupRef = buildGroupRef(json.groupRef)
            )
            buildTransition(ret.transitions,json.transitions)
            return ret
        }

        private fun buildRecevivedState(json : RecevivedStateDT): RecevivedState{
            val ret= RecevivedStateImpl(
                processDefinition = processDefinition,
                id = json.id,
                name= json.name,
                isInitial= json.isInitial,
                groupRef = buildGroupRef(json.groupRef)
            )
            buildTransition(ret.transitions,json.transitions)
            return ret
        }

        private fun buildJoinStateDT(json : JoinStateDT): JoinState{
           val ret =JoinStateImpl(
               processDefinition = processDefinition,
               id = json.id,
               name= json.name,
               isFinish= json.isFinish,
               isTerminate= json.isTerminate,
               groupRef= buildGroupRef(json.groupRef)
            )
            buildTransition(ret.transitions,json.transitions)
            return ret
        }

        private fun buildForkStateDT(json : ForkStateDT): ForkState{
          val ret = ForkStateImpl(
              processDefinition = processDefinition,
              id = json.id,
              name= json.name,
              isInitial= json.isInitial,
              groupRef= buildGroupRef(json.groupRef)
            )
            buildTransition(ret.transitions,json.transitions)
            return ret
        }

        private fun buildGroupStateDT(json : GroupStateDT): GroupState{
           var loopCharacteristics: LoopCharacteristics?= null
           if (json.loopCharacteristics!= null){
               loopCharacteristics = LoopCharacteristicsImpl(
                   processDefinition = processDefinition,
                   loopVariableName = json.loopCharacteristics.loopVariableName,
                   loopExpression = json.loopCharacteristics.loopExpression
               )
           }
           val ret= GroupStateImpl(
               processDefinition = processDefinition,
               id = json.id,
               name= json.name,
               isInitial= json.isInitial,
               isFinish= json.isFinish,
               isTerminate= json.isTerminate,
               groupRef= buildGroupRef(json.groupRef),
               loopCharacteristics=  loopCharacteristics
            )
            buildTransition(ret.transitions,json.transitions)
            return ret
        }
    }

    private class ModelValidator(
        val processDefinition: ProcessDefinitionImpl
    ) {
        fun validate() : ProcessDefinitionImpl{

            if (processDefinition.getSubjects().isEmpty()) {
                throw DefinitionError("", "Бизнес процесс не содержит субьектов")
            }
            if (processDefinition.mainSubject != null){
                checkReference("mainSubject",processDefinition.mainSubject!!,Subject::class.java,"Ошибка в определение процесса ссылка на главный субьект не определена")
            }
            validateDataAssociations("inputDataAssociations",processDefinition.inputDataAssociations)
            validateRootElements("rootElements",processDefinition.rootElements)
            return processDefinition
        }

        fun  checkReference(path: String,ref:Reference<*>,type: Class<*>,errorMessage: String)  {
            val test=ref.resolvedReference()
            if (test==null){
                throw DefinitionError(path, errorMessage)
            }

            if (ref.javaClass != type) {
                throw IvalidReference(path, errorMessage)
            }
        }

        fun validateDataAssociations(path: String,inputDataAssociations: List<DataAssociation>){
            val errMsg= "Ошибка в определение ассоциации данных"
            inputDataAssociations.forEachIndexed {idx, row ->
                var error=validateJsonPath(row.targetPath)
                if (error!=null){
                    throw DefinitionError("$path[$idx].targetPath",errMsg+"("+error+")")
                }
                error=validateJsonPath(row.sourcePath)
                if (error!=null){
                    throw DefinitionError("$path[$idx].sourcePath",errMsg+"("+error+")")
                }
                if (row.sourceMapFunction != null) {
                    error=validateScript(row.sourceMapFunction)
                    if (error!=null) {
                        throw DefinitionError("$path[$idx].sourcePath", errMsg + "(" + error + ")")
                    }
                }
            }
        }

        fun validateJsonPath(script: String?): String? {
            if (script.isNullOrEmpty()) {
                return "json path can not be null or empty"
            }
            try {
                JsonPath.compile(script)
            } catch (ex: InvalidPathException) {
                val errr = ex.message
                return "$script $errr"
            }
            return null
        }

        fun validateScript( script: String?): String? {
                try {
                    val compiler = ExpressionCompiler(script)
                    compiler.compile()
                } catch (tr: CompileException) {
                    return tr.message
                }
               return null
        }

        fun validateRootElements( path: String,rootElements: List<RootElement> ){
            rootElements.forEachIndexed { idx, row ->
                val localPath="$path[$idx]"
                when (row){
                    is ScriptAction->{
                        validateScriptAction(localPath,row)
                    }
                    is DecisionTableAction->{
                        validateDecisionTableAction(localPath,row)
                    }
                    is Subject -> {
                        validateSubject(localPath,row)
                    }
                }
            }
        }

        fun validateScriptAction(path: String,row: ScriptAction){
            val error=validateScript(row.body)
            if (error!=null) {
                throw DefinitionError("$path.body", "Ошибка в определение скрипта($error)")
            }
        }

        fun validateDecisionTableAction(path: String,row: DecisionTableAction){
            val localPath="$path.table"
            row.table.input.forEachIndexed { idx, input ->
                val inputPath = "$localPath.input[$idx]"
                val error = validateScript(input.script)
                if (error!=null) {
                    throw DefinitionError("$inputPath.script", "Ошибка в определение скрипта($error)")
                }
            }
            row.table.rule.forEachIndexed { ruleIdx,rule ->
                val rulePath = "$localPath.rule[$ruleIdx]"
                rule.inputEntry.forEachIndexed{ inputEntryIdx, inputEntry ->
                    val inputEntryPath = "$rulePath.inputEntry[$inputEntryIdx]"
                    try {
                        ELNodeParser(inputEntry.el).parse()
                    } catch (tr: Throwable) {
                       var trErr= ""
                       if (tr.message!=null){
                           trErr= tr.message!!
                       }
                       throw DefinitionError("$inputEntryPath.el", "Ошибка в определение скрипта($trErr)")
                    }
                }
                rule.outputEntry.forEachIndexed{ outputEntryIdx,outputEntry ->
                    val outputEntryPath = "$rulePath.outputEntry[$outputEntryIdx]"
                    val error=validateScript(outputEntry.script)
                    if (error!=null) {
                        throw DefinitionError("$outputEntryPath.script", "Ошибка в определение скрипта" + "(" + error + ")")
                    }
                }
            }
        }

        fun validateSubject(path: String, row: Subject ){
            if (row.initialState()==null){
                throw DefinitionError(path,"Начальное состояние не определено")
            }
            if (!row.existEndState()){
                throw DefinitionError(path,"Конечное состояние не определено")
            }
            validateStates(path,row.states)
        }


        fun validateStates(path: String, states: List<State>){
            val localPath="$path.states"
            states.forEachIndexed { idx, state ->
                val statePath="$localPath[$idx]"
                validateState(statePath,state)
                when(state){
                    is FunctionalState->{
                        validateFunctionalState(statePath,state)
                    }
                    is SendState -> {
                        validateSendState(statePath,state)
                    }
                    is RecevivedState -> {
                        validateRecevivedState(statePath,state)
                    }
                    is JoinState -> {
                        validateJoinState(statePath,state)
                    }
                    is ForkState -> {
                        validateForkState(statePath,state)
                    }
                    is GroupState -> {
                        validateGroupState(statePath,state)
                    }
                }
            }
        }

        fun validateState(path:String,state:State){
            if (state.groupRef!=null){
                checkReference("$path.groupRef",
                    state.groupRef!!,
                    GroupState::class.java,"Ссылка на группу не определена"
                )
            }
        }
        fun validateTransition(path:String,transition:Transition){
            if (transition.sourceRef.resolvedReference()==null){
                throw DefinitionError("$path.sourceRef", "Источник не определен")
            }
            if (transition.targetRef.resolvedReference()==null){
                throw DefinitionError("$path.targetRef", "Цль не определена")
            }
        }

        fun validateTimerTransition(path:String,transition: TimerTransition){
            validateTransition(path,transition)
            val error=validateScript(transition.expression)
            if (error!=null) {
                throw DefinitionError("$path.expression", "Ошибка в определение скрипта($error)")
            }
        }

        fun  validateFunctionalTransition(path:String,transition: FunctionalTransition){
            validateTransition(path,transition)
            validateDataAssociations("$path.outputDataAssociation",transition.outputDataAssociation)
        }

        fun validateFunctionalState(path: String,state: FunctionalState ){
             if (state.actionIsRef()){
                 checkReference("$path.actionRef",(state.action as ActionDefinition.ActionRef).functionalActionRef,
                     FunctionalAction::class.java,"Ссылка на функциональное действие не определена")
             } else {
                 val body = (state.action as ActionDefinition.ActionBody).functionalAction
                 val localPath="$path.actionBody"
                 when (body){
                     is ScriptAction->{
                         validateScriptAction(localPath,body)
                     }
                     is DecisionTableAction->{
                         validateDecisionTableAction(localPath,body)
                     }
                 }
             }
             validateDataAssociations("$path.inputDataAssociation",state.inputDataAssociation)
             if (state.isFinish||state.isFinish){
                 state.transitions.forEachIndexed { idx,transition->
                     val trPath="$path.transitions[$idx]"
                     when (transition){
                         is ErrorTransition -> {
                             validateTransition(trPath,transition)
                         }
                         is TimerTransition -> {
                             validateTimerTransition(trPath,transition)
                         }
                         else -> {
                             throw DefinitionError(trPath,"Не допустимый тип перехода")
                         }
                     }
                 }
             } else {
                 val nameSet: MutableSet<String> = mutableSetOf()
                 var isErrorTransition = false
                 var isTimerTransition = false
                 state.transitions.forEachIndexed { idx,transition->
                     val trPath="$path.transitions[$idx]"
                     when (transition) {
                       is ErrorTransition->{
                           if (isErrorTransition){
                               throw DefinitionError(trPath,"Переход по ошибке определен повторно ")
                           }
                           validateTransition(trPath,transition)
                           isErrorTransition= true
                       }
                       is TimerTransition->{
                           if (isTimerTransition){
                               throw DefinitionError(trPath,"Переход по таймеру определен повторно ")
                           }
                           validateTimerTransition(trPath,transition)
                           isTimerTransition = true
                       }
                       is FunctionalTransition->{
                           if (nameSet.contains(transition.name)){
                               throw DefinitionError("$trPath.name","Переход определен повторно")
                           }
                           validateFunctionalTransition(trPath,transition)
                           nameSet.add(transition.name)
                       }
                       else -> {
                           throw DefinitionError(trPath,"Не допустимый тип перехода")
                       }
                   }
                 }
                 if (nameSet.isEmpty()){
                     throw DefinitionError("$path.transitions","Переходы не определены")
                 }
             }
        }

        fun validateTransitionSend(path: String,transition :TransitionSend){
            validateTransition(path,transition)
            checkReference(
                "$path.messageDefinitionRef",
                transition.messageDefinitionRef,
                MessageDefinition::class.java,
                "Cсылка на сообщение не определена"
            )
            checkReference(
                "$path.recientRef",
                transition.recientRef,
                Subject::class.java,
                "Cсылка на субьект не определена"
            )
            validateDataAssociations("$path.outputDataAssociation",transition.outputDataAssociation)
        }

        fun validateSendState(path: String,state: SendState){
            if (state.isFinish||state.isTerminate){
                throw DefinitionError(path,"Состояние не может быть конечным")
            }
            var isTransitionSend = false
            var isErrorTransition = false
            var isTimerTransition = false
            state.transitions.forEachIndexed { idx, transition ->
                val trPath="$path.transitions[$idx]"
                  when(transition){
                     is ErrorTransition->{
                         if (isErrorTransition){
                             throw DefinitionError(trPath,"Переход по ошибке определен повторно ")
                         }
                         validateTransition(trPath,transition)
                         isErrorTransition= true
                     }
                     is TimerTransition->{
                         if (isTimerTransition){
                             throw DefinitionError(trPath,"Переход по таймеру определен повторно ")
                         }
                         validateTimerTransition(trPath,transition)
                         isTimerTransition = true
                     }
                     is TransitionSend -> {
                         if (isTransitionSend){
                             throw DefinitionError(trPath,"Переход определен повторно")
                         }
                         validateTransitionSend(trPath,transition)
                         isTransitionSend= true
                     }
                     else -> {
                         throw DefinitionError(trPath,"Не допустимый тип перехода")
                     }
                 }

            }
        }

        fun validateTransitionRecevid(path: String,transition :TransitionRecevid){
            validateTransition(path,transition)
            checkReference(
                "$path.messageDefinitionRef",
                transition.messageDefinitionRef,
                MessageDefinition::class.java,
                "Cсылка на сообщение не определена"
            )
            checkReference(
                "$path.senderRef",
                transition.senderRef,
                Subject::class.java,
                "Cсылка на субьект не определена"
            )
            validateDataAssociations("$path.outDataAssociation",transition.outDataAssociation)
        }

        fun validateRecevivedState(path: String,state: RecevivedState){
            if (state.isFinish||state.isTerminate){
                throw DefinitionError(path,"Состояние не может быть конечным")
            }
            val nameSet: MutableSet<String> = mutableSetOf()
            var isErrorTransition = false
            var isTimerTransition = false
            state.transitions.forEachIndexed { idx, transition ->
                val trPath = "$path.transitions[$idx]"
                when(transition){
                    is ErrorTransition->{
                        if (isErrorTransition){
                            throw DefinitionError(trPath,"Переход по ошибке определен повторно ")
                        }
                        validateTransition(trPath,transition)
                        isErrorTransition= true
                    }
                    is TimerTransition->{
                        if (isTimerTransition){
                            throw DefinitionError(trPath,"Переход по таймеру определен повторно ")
                        }
                        validateTimerTransition(trPath,transition)
                        isTimerTransition = true
                    }
                    is TransitionRecevid ->{
                        val name= transition.senderRef.id+"-"+transition.messageDefinitionRef.id
                        if (nameSet.contains(name)){
                            throw DefinitionError(trPath,"Переход определен повторно")
                        }
                        validateTransitionRecevid(trPath,transition)
                        nameSet.add(name)
                    }
                    else -> {
                        throw DefinitionError(trPath,"Не допустимый тип перехода")
                    }
                }
            }
        }

        fun validateJoinState(path: String,state: JoinState){
           if (state.isInitial){
               throw DefinitionError(path,"Состояние не может быть начальным")
           }
           if (state.isFinish || state.isTerminate){
                if (state.transitions.size>0){
                    throw DefinitionError(path,"Определение перехода из конечного состояния недопустимо")
                }
            } else {
                var isJoinTransition = false
                state.transitions.forEachIndexed { idx, transition ->
                    val trPath = "$path.transitions[$idx]"
                    if (transition is JoinTransition){
                        if (isJoinTransition){
                            throw DefinitionError(trPath,"Переход определен повторно")
                        }
                        validateTransition(path,transition)
                        isJoinTransition = true
                    }
                    else {
                      throw DefinitionError(trPath,"Не допустимый тип перехода")
                    }
                }
            }


        }

        fun validateForkState(path: String,state: ForkState){
            if (state.isFinish || state.isTerminate) {
                throw DefinitionError(path,"Состояние не может быть конечным")
            }
            val nameSet: MutableSet<String> = mutableSetOf()
            state.transitions.forEachIndexed { idx, transition ->
                val trPath = "$path.transitions[$idx]"
                if (transition is ForkTransition){
                    val name = transition.targetRef.id
                    if (nameSet.contains(name)){
                        throw DefinitionError(trPath,"Переход определен повторно")
                    }
                    validateTransition(path,transition)
                    nameSet.add(transition.targetRef.id)
                }
                else {
                    throw DefinitionError(trPath,"Не допустимый тип перехода")
                }
            }
        }

        fun validateGroupState(path: String,state: GroupState){
           if (state.loopCharacteristics != null){
               val error=validateScript(state.loopCharacteristics!!.loopExpression)
               if (error!=null) {
                   throw DefinitionError("$path.loopCharacteristics.loopExpression", "Ошибка в определение скрипта($error)")
               }

               val existInitialState=state.getStates().stream()
                   .filter{ it.isInitial}
                   .findFirst()
                   .isPresent
               if (!existInitialState){
                   throw DefinitionError(path,"Для группы начальное состояние не определено")
               }
               val existEndlState=state.getStates().stream()
                   .filter{it.isFinish || it.isTerminate}
                   .findFirst()
                   .isPresent
               if (!existEndlState){
                   throw DefinitionError(path,"Для группы конечное состояние не определено")
               }
               if (state.groupTransition()!=null&&(state.isFinish || state.isTerminate)){
                   throw DefinitionError(path,"Данный переход не допустим")
               } else{
                   var isGroupTransition = false
                   var isErrorTransition = false
                   var isTimerTransition = false
                   state.transitions.forEachIndexed { idx, transition ->
                       val trPath = "$path.transitions[$idx]"
                       when(transition){
                           is ErrorTransition->{
                               if (isErrorTransition){
                                   throw DefinitionError(trPath,"Переход по ошибке определен повторно ")
                               }
                               validateTransition(trPath,transition)
                               isErrorTransition= true
                           }
                           is TimerTransition->{
                               if (isTimerTransition){
                                   throw DefinitionError(trPath,"Переход по таймеру определен повторно ")
                               }
                               validateTimerTransition(trPath,transition)
                               isTimerTransition = true
                           }
                           is GroupTransition->{
                               if (isGroupTransition){
                                   throw DefinitionError(trPath,"Переход из группы определен повторно")
                               }
                               validateTransition(trPath,transition)
                               isGroupTransition = true
                           }
                           else -> {
                               throw DefinitionError(trPath,"Не допустимый тип перехода")
                           }
                       }
                   }
               }
           }
        }
    }
}