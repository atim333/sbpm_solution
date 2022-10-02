package sbpm_solution.bpms.sbpm.format

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import sbpm_solution.bpms.sbpm.model.ProcessDefinition
import java.io.*

interface Parser {
    fun parse(json: ProcessDefinitionDT): ProcessDefinition
    fun parse(input: InputStream): ProcessDefinition {
        val mapper = jacksonObjectMapper()
        val ret=mapper.readValue<ProcessDefinitionDT>(input)
        return parse(ret)
    }

    fun parse(f: File): ProcessDefinition? {
        FileInputStream(f).use { `is` -> return parse(`is`) }
    }

    fun parse(str: String): ProcessDefinition {
        return parse(ByteArrayInputStream(str.toByteArray(Charsets.UTF_8)))
    }
}

interface Composer {
    fun compose(resource: ProcessDefinition): ProcessDefinitionDT
    fun compose(stream: OutputStream, resource: ProcessDefinition){
        val json=compose(resource)
        val mapper = jacksonObjectMapper()
        mapper.writeValue(stream,json)
    }

    fun compose(f: File, resource: ProcessDefinition){
        val json=compose(resource)
        val mapper = jacksonObjectMapper()
        mapper.writeValue(f,json)
    }
}

open class DefinitionError : RuntimeException {
    var path = ""
    var path2: String? = null
    var description: String? = null

    constructor(path: String, cause: Throwable?) : super(cause) {
        this.path = path
    }

    constructor(path: String, description: String?, msg: String?) : super(msg) {
        this.path = path
        this.description = description
    }

    constructor(path: String, description: String?) : super(description) {
        this.path = path
        this.description = description
    }

    constructor(path: String, description: String?, msg: String?, path2: String?) : super(msg) {
        this.path = path
        this.path2 = path2
        this.description = description
    }
}

class IvalidReference: DefinitionError {
    constructor(path: String, description: String?) : super(path,description) {
    }
}