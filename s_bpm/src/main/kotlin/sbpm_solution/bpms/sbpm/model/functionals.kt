package sbpm_solution.bpms.sbpm.model

import sbpm_solution.bpms.sbpm.decisiontable.DecisionTable

interface FunctionalAction : RootElement {
    val name: String?
}

interface ServiceTaskAction : FunctionalAction {
    val specification: String
    val method: String
}

interface ScriptAction : FunctionalAction {
    val body: String
}

interface DecisionTableAction : FunctionalAction {
    val table: DecisionTable
}

interface ExternalTaskAction : FunctionalAction {
}
