package sbpm_solution.bpms.sbpm.model

import java.util.stream.Collectors

interface ProcessDefinition : BaseElement {
    var name: String
    var version: String
    val rootElements: MutableList<RootElement>
    fun <T> creareReference(id: String): Reference<T>

    fun getSubjects(): List<Subject> {
        return rootElements.stream()
            .filter{element-> element is Subject}
            .map { element -> element as Subject }
            .collect(Collectors.toList())
    }

    fun findSubject(state: State): Subject? {
        return rootElements.stream()
            .filter{element-> element is Subject}
            .map { element -> element as Subject }
            .filter{element -> element.findState(state.id!!) != null}
            .findFirst()
            .orElse(null)
    }
}