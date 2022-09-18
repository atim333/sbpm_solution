package sbpm_solution.bpms.sbpm.abstractMachine2

import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.Option
import org.mvel2.MVEL
import sbpm_solution.bpms.sbpm.decisiontable.DecisionTable
import sbpm_solution.bpms.sbpm.model.*
import sbpm_solution.bpms.utils.EvalContext
import sbpm_solution.bpms.utils.converter.Convertor
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.LinkedHashMap

internal abstract class Environment {
    var debuger: Debuger? = null
    var error: ErrorSignal? = null

    abstract val context: ExecutionContext
    private var curentToken: Token? = null
    private var queue: LinkedList<Token>? = null

    // тут может понадобится curentToken - построить path
    fun error() {
        val e = error
        if (e != null) {
            throw e
        }
    }

    fun set(token: Token) {
        curentToken = token
    }

    fun getToken(): Token? {
        return if (queue!!.isEmpty()) {
            null
        } else queue!!.removeFirst()
    }

    fun takeToken(token: Token, newRef: Reference<State>) {
        token.stateId = newRef.id
        context.saveToken(token)
        queue!!.addLast(token)
    }

    fun takeToken(token: Token) {
        context.saveToken(token)
        queue!!.addLast(token)
    }

    fun initQ() {
        queue = LinkedList<Token>()
    }

    fun clear(parentId: String) {
        val childs  = context.getChilds(parentId)
        childs.forEach { token : Token ->
            context.removeSubscribers(token.tokenId)
            context.removeTimer(token.tokenId)
        }
        queue = queue!!.stream()
            .filter { cur: Token -> cur.parentId != null }
            .filter { cur: Token -> cur.parentId != parentId }
            .collect(Collectors.toCollection { LinkedList() })
    }


    fun evalMvelValue(script: String, value: Any?): Any? {
        return evalScript(script, "value", value)
    }

    fun evalSctipt(script: String, value: Any?): Any? {
        return evalScript(script, "context", value)
    }

    fun evalScript(
        script: String,
        name: String,
        value: Any?
    ): Any? {
        val ctx: MutableMap<String, Any?> = LinkedHashMap<String,Any?>()
        ctx[name] = value
        ctx["RT"] =context.getRuntimeContext()
        return MVEL.eval(script, context)
    }

    fun evaluateDecisionTable(
        decisionTable: DecisionTable,
        scriptContext: MutableMap<String, Any?>
    ): String {
        val ctx: MutableMap<String, Any?> = LinkedHashMap<String,Any?>()
        ctx["context"] = scriptContext
        ctx["RT"] =context.getRuntimeContext()

        val ret: List<Pair<String, Any?>> = decisionTable.evaluate(ctx)
        // Разабратся с переходом и обагатить контекст
        var trName = ""
        for (pair in ret) {
            val name = pair.first
            val value = pair.second
            if (name == "") {
                if (value != null) {
                    trName = value.toString()
                }
            } else {
                scriptContext[name] = value
            }
        }
        return trName
    }


    fun <T : State> getState(token: Token): T {
        val processDefinitionId = token.processDefinitionId
        val processDefinition = context.getProcessDefinition(processDefinitionId)
        val stateId = token.stateId
        val stateRef: Reference<State> = processDefinition.createReference(stateId)
        val state = stateRef.resolvedReference() ?: throw StateNotDefine(stateId)
        @Suppress("UNCHECKED_CAST")
        return state as T
    }

    fun getParentContext(tokenId: String?): Map<String, Any?>? {
        if (tokenId != null) {
            val parentToken: Token = context.getToken(tokenId)
            val state: State = getState(parentToken)
            if (state is GroupState) {
                val local = context.getTokenData(tokenId)
                if (local.isEmpty()) {
                    return getParentContext(parentToken.parentId)
                }
                val loopVariableName = local["loopVariableName"] as String
                val loopExpression = local["loopExpression"]
                val loopCounter: Int = Convertor.convert(local["loopCounter"], Int::class.java)!!
                var loopVariable: Any = loopCounter
                if (loopExpression is List<*>) {
                    loopVariable = loopExpression[loopCounter]!!
                }
                val curentCtx: MutableMap<String, Any?> = LinkedHashMap<String, Any?>()
                curentCtx[loopVariableName] = loopVariable

                val parentCtx = getParentContext(parentToken.parentId)
                return EvalContext(curentCtx, parentCtx)
            } else {
                getParentContext(parentToken.parentId)
            }
        }
        return null
    }

    fun resolveAsoc(data: Any?, outputCtx: MutableMap<String, Any?>, dataAssociations: List<DataAssociation>) {
        if (dataAssociations.isEmpty()) {
            // to do хорошо бы проверить
            @Suppress("UNCHECKED_CAST")
            val dataM: Map<String, Any?> = data as Map<String, Any?>
            outputCtx.putAll(dataM)
        } else {
            for (dataAssociation in dataAssociations) {
                val sourcePath = dataAssociation.sourcePath
                val targetPath = dataAssociation.targetPath
                val target = dataAssociation.target
                val sourceMapFunction = dataAssociation.sourceMapFunction
                val inputPat: JsonPath = JsonPath.compile(sourcePath)
                var inputValue: Any? =
                    inputPat.read(data, Configuration.builder().options(Option.DEFAULT_PATH_LEAF_TO_NULL).build())
                if (!sourceMapFunction.isNullOrBlank()) {
                    inputValue = try {
                        evalMvelValue(sourceMapFunction, inputValue)
                    } catch (e: Throwable) {
                        val path = PathBuilder.getPath(dataAssociation)
                        throw ScriptError2(path, "sourceMapFunction", e)
                    }
                }
                val outputPat: JsonPath = JsonPath.compile(targetPath)
                if (!target.isNullOrEmpty()) {
                    outputPat.put(
                        outputCtx,
                        target,
                        inputValue,
                        Configuration.builder().options(Option.DEFAULT_PATH_LEAF_TO_NULL).build()
                    )
                } else {
                    if (targetPath!!.trim { it <= ' ' } == "$") {
                        // хорошо бы проверить
                        try {
                            @Suppress("UNCHECKED_CAST")
                            outputCtx.putAll(inputValue as Map<String, Any?>)
                        } catch (tr: Throwable) {
                            val path = PathBuilder.getPath(dataAssociation)
                            throw ConversionError(path, "targetPath", tr)
                        }
                    } else {
                        outputPat.set(
                            outputCtx,
                            inputValue,
                            Configuration.builder().options(Option.DEFAULT_PATH_LEAF_TO_NULL).build()
                        )
                    }
                }
            }
        }
    }

    fun evalFunctionalStateInputAssoc(
        parentCtx: Map<String, Any?>?,
        subjectCtx: MutableMap<String, Any?>,
        functionalState: FunctionalState
    ): MutableMap<String, Any?> {
        val evalCtx = EvalContext(subjectCtx, parentCtx)
        val inputDataAssociations: List<DataAssociation> = functionalState.inputDataAssociation
        val functionalAction = functionalState.getAction()
        return if (functionalAction is ServiceTaskAction) {
            val scriptContext = LinkedHashMap<String, Any?>()
            scriptContext["pathParams"] = LinkedHashMap<String, Any?>()
            scriptContext["jsonContent"] = LinkedHashMap<String, Any?>()
            scriptContext["queryParams"] = LinkedHashMap<String, Any?>()
            resolveAsoc(evalCtx, scriptContext, inputDataAssociations)
            scriptContext
        } else {
            if (inputDataAssociations.isEmpty()) {
                return subjectCtx
            }
            val scriptContext: MutableMap<String, Any?> = LinkedHashMap()
            resolveAsoc(evalCtx, scriptContext, inputDataAssociations)
            scriptContext
        }
    }

    fun evalFunctionalStateOutputAssoc(
        data: Any?,
        parentContext: Map<String, Any?>?,
        subjectContext: MutableMap<String, Any?>,
        transition: FunctionalTransition,
        functionalState: FunctionalState
    ): MutableMap<String, Any?> {
        val functionalAction = functionalState.getAction()
        if (functionalAction is ServiceTaskAction) {
            val outputDataAssociations: List<DataAssociation> = transition.outputDataAssociation
            if (outputDataAssociations.isNotEmpty()) {
                return if (data is MutableMap<*, *>) {
                    @Suppress("UNCHECKED_CAST")
                    val retData = data as MutableMap<String, Any?>
                    val evalCtx = EvalContext(retData, parentContext)
                    resolveAsoc(evalCtx, subjectContext, outputDataAssociations)
                    subjectContext
                } else {
                    // Вероятно понадобится функция лоступа к parentContext
                    resolveAsoc(data, subjectContext, outputDataAssociations)
                    subjectContext
                }
            } else {
                return when (data) {
                    null -> {
                        subjectContext
                    }
                    is Map<*, *> -> {
                        @Suppress("UNCHECKED_CAST")
                        val retData = data as Map<String, Any?>
                        subjectContext.putAll(retData)
                        subjectContext
                    }
                    else -> {
                        throw InvalidServiceResonce(functionalState, data.javaClass.simpleName)
                    }
                }
            }
        } else {
            @Suppress("UNCHECKED_CAST")
            val scriptContext = data as MutableMap<String, Any?>
            val outputDataAssociations: List<DataAssociation> = transition.outputDataAssociation

            return if (outputDataAssociations.isNotEmpty()) {
                resolveAsoc(EvalContext(scriptContext, parentContext), subjectContext, outputDataAssociations)
                subjectContext
            } else {
                subjectContext.putAll(scriptContext)
                subjectContext
            }
        }
    }
}