package sbpm_solution.bpms.sbpm.model

interface Reference<T> {
    val id: String
    fun resolvedReference(): T?
}

interface BaseElement {
   var id: String
   var processDefinition: ProcessDefinition

   fun <T> getReference(): Reference<T>? {
       if (id.isNullOrBlank()){
           return null
       }
       return processDefinition.creareReference(id)
   }
}

interface RootElement : BaseElement