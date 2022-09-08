package sbpm_solution.bpms.sbpm.model

interface FunctionalAction : RootElement {
    var name : String
}

interface ServiceTask : FunctionalAction {
    var specification : String?
    var method: String?
}

interface Script : FunctionalAction{
    var body: String
}

interface DecisinonTable: FunctionalAction{
    val table: MutableMap<String, Any?>
}

interface ExternalTask: FunctionalAction