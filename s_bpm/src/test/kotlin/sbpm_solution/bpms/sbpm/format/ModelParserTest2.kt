package sbpm_solution.bpms.sbpm.format

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Test


class ModelParserTest2 {
    @Test
    fun test1() {
        val ret= ProcessDefinitionDT (
            name="name",
            version="1.0",
            inputDataAssociations = listOf<DataAssociationDT>(),
            mainSubject=null,
            rootElements= listOf(
                MessageDefinitionDT(id="45", name = "HHH")
            )
        )
        val mapper = jacksonObjectMapper()
        val json = mapper.writeValueAsString(ret)
        println(json)
        println("IK")
        val ret2=mapper.readValue<ProcessDefinitionDT>(json)
        println(ret2)
    }

    @Test
    fun testE() {
        val json="""
              {"type":"ProcessDefinition",
               "name":"name",
               "version":"1.0",
               "inputDataAssociations":[],
               "mainSubject":null,
               "rootElements":[{"type":"MessageDefinition","nam":"HHH","id":"45"}]}
             
        """.trimIndent()
        try {
            val mapper = jacksonObjectMapper()
            val ret2 = mapper.readValue<ProcessDefinitionDT>(json)
            println(ret2)
        } catch (tr: MissingKotlinParameterException){
             tr.path
             tr.printStackTrace()
        }
    }

    @Test
    fun testE1(){
         val ret=FunctionalStateDT(
            id= "67",
            name="HHH",
            isInitial=false,
            isFinish=true,
            isTerminate=true,
            transitions= listOf<TransitionDT>(),
            groupRef= null,
            actionRef="78",
            actionBody= null,
           inputDataAssociation=listOf<DataAssociationDT>()
        )
        val mapper = jacksonObjectMapper()
        val json = mapper.writeValueAsString(ret)
        println(json)
        println("IK")
    }

    @Test
    fun testE2(){
        val json="""
            {"type":"FunctionalState",
             "id":"67",
              "name":"HHH",
               "isInitial":false,
                "isFinish":true,
                "isTerminate":true,
                "transitions":[],
                "groupRef":null,
                "actionRef":null,
                "actionBody":null,
                "inputDataAssociation":[]}
            """
        try {
            val mapper = jacksonObjectMapper()
            val ret2 = mapper.readValue<FunctionalStateDT>(json)
            println(ret2)
        } catch (tr: JsonMappingException) { //Throwable){
            //tr.path
            tr.printStackTrace()
        }
    }
}

/*

{"type":"FunctionalState","id":"67","name":"HHH","isInitial":false,"isFinish":true,"isTerminate":true,"transitions":[],"groupRef":null,"actionRef":"78","actionBody":null,"inputDataAssociation":[]}

{"type":"ProcessDefinition","name":"name","version":"1.0","inputDataAssociations":[],"mainSubject":null,"rootElements":[{"type":"MessageDefinition","name":"HHH","id":"45"}]}
 */