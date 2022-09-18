package sbpm_solution.bpms.sbpm.abstractMachine2


class Debuger(val piid: String, val bp: Set<String>) {

    fun isBp(token: Token): Boolean {
        return if (token.piid == piid) {
            bp.contains(token.stateId)
        } else false
    }
}
