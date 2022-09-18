package sbpm_solution.bpms.sbpm.abstractMachine

class Token(
    var tokenId: String,
    var parentId: String? = null,
    var siid: String,
    var piid: String,
    var stateId: String,
    var processDefinitionId: String
)

class Subscriber (
    val subscriberId: String,
    val tokenId: String,
    val senderId: String,
    val messageId: String
)


class ErrorSignal(val code: String, val stateId: String, val msg: String) :
    RuntimeException(msg)
