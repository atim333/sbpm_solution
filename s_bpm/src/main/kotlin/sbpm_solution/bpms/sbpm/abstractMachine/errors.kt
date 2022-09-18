package sbpm_solution.bpms.sbpm.abstractMachine

import sbpm_solution.bpms.sbpm.model.BaseElement
import sbpm_solution.bpms.sbpm.model.FunctionalAction
import sbpm_solution.bpms.sbpm.model.State

open class AbstractMachineException : RuntimeException {
    constructor(message: String?) : super(message) {}
    constructor(message: String?, cause: Throwable?) : super(message, cause) {}
    constructor(cause: Throwable?) : super(cause) {}
}

open class InvalidModelStructure(message: String?) : AbstractMachineException(message)

class StateNotDefine(val stateId: String) : InvalidModelStructure("Состояние $stateId Не определено")

class UnsupportedfunctionalAction(functionalAction: FunctionalAction) : AbstractMachineException(
    String.format(
        "Не потдерживается FunctionalAction [%s] ", functionalAction.javaClass.canonicalName
    )
)


class SubjectNotDefined(processDefinitionId: String, subjectId: String) :
    AbstractMachineException(
        String.format(
            "In the process of [%s] the subject [%s] is not defined",
            processDefinitionId,
            subjectId
        )
    )

class TansitionNotDefine(val state: State, val trasitionName: String) :
    AbstractMachineException(
        java.lang.String.format(
            "for the process [%s] from state  [%s], transition [%s] not define",
            state.processDefinition.id, state.id, trasitionName
        )
    ) {

    fun getPath(): String? {
        return PathBuilder.getPath(state)
    }
}

class InvalidServiceResonce(var errorSourse: BaseElement, var invaldType: String) :
    AbstractMachineException("Ошибка исполнения необходимо преобразование : $invaldType")

class ScriptError2 : AbstractMachineException {
    val path: String?
    val path2: String?

    constructor(path: String?, cause: Throwable) : super("Ошибка исполнения: " + cause.message, cause) {
        this.path = path
        this.path2 = null
    }

    constructor(path: String?, path2: String?, cause: Throwable?) : super(
        "Ошибка исполнения: " + cause?.message,
        cause
    ) {
        this.path = path
        this.path2 = path2
    }
}

class ConversionError(
    val path: String?,
    val path2: String?,
    cause: Throwable
) : AbstractMachineException("Ошибка преобразования: " + cause.message, cause)

class LoopCharacteristicsError(siid: String, stateId: String) :
    AbstractMachineException(String.format("invalid type LoopExpression siid [%s] state [%s]", siid, stateId))
