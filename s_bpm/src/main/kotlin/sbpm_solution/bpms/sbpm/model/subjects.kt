package sbpm_solution.bpms.sbpm.model

interface Subject : RootElement{
    var name : String
    val states : MutableList<State>
    fun findState(stateId: String): State?{
        return states.stream().filter{state: State->state.id.equals(stateId)}
            .findFirst()
            .orElse(null)
    }
}

interface State : BaseElement{

}