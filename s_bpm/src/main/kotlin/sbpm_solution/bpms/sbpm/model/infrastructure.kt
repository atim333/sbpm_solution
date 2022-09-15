package sbpm_solution.bpms.sbpm.model

import java.util.stream.Collectors

interface ProcessDefinition : BaseElement {
    val name: String
    val version: String

    val rootElements: List<RootElement>

    fun <T> createReference(id: String): Reference<T>

    fun getSubjects(): List<Subject> {
        return rootElements.stream()
            .filter { rootElement: RootElement -> rootElement is Subject }
            .map { element: RootElement -> element as Subject }
            .collect(Collectors.toList())
    }

    fun findSubject(state: State): Subject? {
        return rootElements.stream()
            .filter { rootElement -> rootElement is Subject }
            .map { rootElement -> rootElement as Subject }
            .filter { rootElement -> rootElement.findState(state.id!!) != null }
            .findFirst()
            .orElse(null)
    }
}