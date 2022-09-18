package sbpm_solution.bpms.sbpm

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Test
import sbpm_solution.bpms.sbpm.decisiontable.DecisionTable

class ModelParserTest {
    @Test
    fun test1() {
        val ret=ProcessDefinitionDT (
            name="name",
            version="1.0",
           inputDataAssociations = listOf<DataAssociationDT>(),
           mainSubject=null,
           rootElements= listOf(
               ScriptActionDT(id="1", body="String", name=null)
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
    fun test2(){
        val ret=ProcessDefinitionDT (
            name="name",
            version="1.0",
            inputDataAssociations = listOf<DataAssociationDT>(),
            mainSubject=null,
            rootElements= listOf(
                MessageDefinitionDT(id="1", name="String")
            )
        )
        val mapper = jacksonObjectMapper()
        val json = mapper.writeValueAsString(ret)
        println(json)
        println("IK")
        val ret2=mapper.readValue<ProcessDefinitionDT>(json)
        println(ret2)
    }
}
/*
known type ids = [DataAssociationDT, ProcessDefinitionDT, ScriptAction, ScriptAction1]
at [Source: (String)"{"name":"name","version":"1.0",

 */