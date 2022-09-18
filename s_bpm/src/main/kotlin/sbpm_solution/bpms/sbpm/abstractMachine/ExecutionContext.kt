package sbpm_solution.bpms.sbpm.abstractMachine

import sbpm_solution.bpms.sbpm.model.ProcessDefinition
import java.time.LocalDateTime

interface ExecutionContext {

    fun getProcessDefinition(processDefinitionId: String): ProcessDefinition
    fun createProcessInstance(
        processDefinitionId: String,
        userPiid: String?,
        isdebug: Boolean
    ): String

    fun createSubjectInstance(piid: String, subjectId: String): String
    fun getSubjectData(siid: String): MutableMap<String, Any?>
    fun setSubjectData(siid: String, data: Map<String, Any?>)
    fun destroySubjectInstanse(siid: String)

    fun createToken(siid: String, stateId: String, parentId: String?=null): Token
    fun getToken(tokenId: String): Token
    fun getChilds(tokenId: String): List<Token>
    fun saveToken(token: Token)
    fun destroyToken(token: Token)

    fun getTokenData(tokenId: String): MutableMap<String, Any?>
    fun setTokenData(tokenId: String, data: Map<String, Any?>?)

    fun existSubjectInstance(piid: String, subjectId: String): Boolean
    fun createSubscriber(tokenId: String, senderId: String, messageId: String): Subscriber
    fun getSubscribers(piid: String, recipientId: String, senderId: String, messageId: String): List<Subscriber>
    fun removeSubscribers(tokenid: String)
    fun removeTimer(tokenId: String)

    fun setTimer(dt: LocalDateTime, tokenId: String)
    fun createTokenBreakPoint(token: Token)

    fun createExternalTask(tokenId: String, name: String, formKey: String?)
    // TO DO ПЕРЕДЕЛАТЬ ЛОГИКУ ТОЧКА ОСТАНОВА СУЩЬНОСТЬ ТАКАЯ ЖЕ КАК TOKEN
    fun createSubscriberBreakPoint(subscriber: Subscriber, msg: Any?)

    fun serviceCall(
        inter: String,
        method: String,
        inputData: Map<String, Any?>
    ): Pair<String, Any?>

    fun getRuntimeContext(): Map<String, Any?>
}