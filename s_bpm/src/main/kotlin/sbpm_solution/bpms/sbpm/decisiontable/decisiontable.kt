package sbpm_solution.bpms.sbpm.decisiontable

import org.mvel2.MVEL
import sbpm_solution.bpms.sbpm.decisiontable.el.ELNodeParser

class DecisionTablePolicyError(message: String?) : RuntimeException(message)

class DecisionTableEvaluateError(path: String, cause: Throwable?) : RuntimeException(cause) {
    var path = ""

    init {
        this.path = path
    }
}


enum class PolicyType {
    UNIQUE, FIRST
}

class Input(val script: String, val label: String = "")

class Output(val name: String, val label: String = "")

class InputEntry(val el: String)

class OutputEntry(val script: String)

class Rule(
    val description: String = "",
    val inputEntry: List<InputEntry>,
    val outputEntry: List<OutputEntry>
)


class DecisionTable (
    val policy: PolicyType  = PolicyType.FIRST,
    val name: String = "",
    val input: List<Input>,
    val output: List<Output>,
    val rule: List<Rule>
) {

  fun evaluate(context: Map<String, Any?>): List<Pair<String, Any?>> {
    val inputObjects = ArrayList<Any?>()
    var path = "input"

    input.forEachIndexed { idx, inputItem ->
        try {
            val obj = evalScript(inputItem.script, context)
            inputObjects.add(obj)
        } catch (tr: Throwable) {
            throw DecisionTableEvaluateError("$path[$idx]", tr)
        }
    }

    path = "rule"
    // Есть Политка к примеру Уникальность или Первый
    val result: MutableList<MutableList<Pair<String, Any?>>> = ArrayList()
    rule.forEachIndexed { ruleIdx, rule ->
        var test = true
        val inputEntrys = rule.inputEntry
        // В цикле определем истину
        inputEntrys.forEachIndexed { idxinputEntry, inputEntry ->
            try {
                val node = ELNodeParser(inputEntry.el).parse()
                val res = node.evaluate(inputObjects[idxinputEntry])
                test = test && res
            } catch (tr: Throwable) {
                throw DecisionTableEvaluateError(
                    "$path[$ruleIdx].inputEntry[$idxinputEntry]", tr
                )
            }
        }
        // да условие выполнено - формируем результат
        if (test) {
            val outputEntrys = rule.outputEntry
            val localResult: MutableList<Pair<String, Any?>> = ArrayList()
            outputEntrys.forEachIndexed { idxoutputEntrys, outputEntry ->
                try {
                    val obj = evalScript(outputEntry.script, context)
                    val name = output[idxoutputEntrys].name
                    val pair = Pair(name, obj)
                    localResult.add(pair)
                } catch (tr: Throwable) {
                    throw DecisionTableEvaluateError(
                        "$path[$ruleIdx].outputEntry[$idxoutputEntrys]", tr
                    )
                }
            }
            // Кладем в результат
            result.add(localResult)
        }
    }
    // Все расчитали На основе политик возврашаем результат

    if (policy === PolicyType.UNIQUE && result.size != 1) {
        throw DecisionTablePolicyError("Ожидается уникальный результат")
    } else if (policy === PolicyType.FIRST && result.isEmpty()) {
        throw DecisionTablePolicyError("Pезультат не определен")
    }
    return result[0]
  }

  private fun evalScript(script: String?, context: Map<String, Any?>): Any? {
    if (script.isNullOrEmpty()) {
        return null
    }
    val ret = MVEL.eval(script, context)
    return ret
  }
}
