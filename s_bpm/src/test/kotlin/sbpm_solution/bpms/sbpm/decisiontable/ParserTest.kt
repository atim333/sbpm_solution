package sbpm_solution.bpms.sbpm.decisiontable
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

import org.junit.Test

class ParserTest {
    @Test
    fun test1() {
        val input = listOf(Input("A+B"))
        val output = listOf(Output("name"))

        val rule = listOf(
            Rule(
                inputEntry = listOf(InputEntry("A>5")),
                outputEntry = listOf(OutputEntry("A+6"))
            )
        )
        val tab = DecisionTable(
            input = input,
            output = output,
            rule = rule
        )

        val mapper = jacksonObjectMapper()
        val userJson = mapper.writeValueAsString(tab)
        println(userJson)
        println("IK")
    }

    @Test
    fun test2() {
        val json="""
          {"policy":"FIRST",
          "name":"",
          "input":[{"script":"A+B","label":""}],
          "output":[{"name":"name","label":""}],
          "rule":[{"description":"",
                   "inputEntry":[{"el":"A>5"}],
                   "outputEntry":[{"script":"A+6"}
                   ]}
          ]}
            
        """.trimIndent()
        val mapper = jacksonObjectMapper()
        val ret=mapper.readValue<DecisionTable>(json)
        println(ret)
    }
}


/*
{"policy":"FIRST","name":"","input":[{"script":"A+B","label":""}],"output":[{"name":"name","label":""}],"rule":[{"description":"","inputEntry":[{"el":"A>5"}],"outputEntry":[{"script":"A+6"}]}]}
 */

/*
class InputEntry(val el: String)

class OutputEntry(val script: String)

class Rule(
    val description: String = "",
    val inputEntry: List<InputEntry>,
    val outputEntry: List<OutputEntry>
)

 */