package com.webtoapp.core.aicoding.plan

import com.webtoapp.core.aicoding.files.ProjectFileManager
import com.webtoapp.core.aicoding.permission.PermissionChecker
import com.webtoapp.core.aicoding.permission.PermissionMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

class PlanManager(
    private val sessionId: String,
    private val fileManager: ProjectFileManager,
    private val permissionChecker: PermissionChecker
) {

    data class State(
        val active: Boolean = false,
        val activePlanPath: String? = null,
        val approvedPlanContent: String? = null
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    fun enter(): EnterResult {
        if (_state.value.active) return EnterResult.AlreadyActive(_state.value.activePlanPath!!)
        val slug = generateUniqueSlug()
        val relPath = "$PLANS_DIR/$slug.md"

        fileManager.resolveSafe(sessionId, relPath)?.parentFile?.mkdirs()
        permissionChecker.setMode(PermissionMode.Plan)
        _state.value = State(active = true, activePlanPath = relPath, approvedPlanContent = null)
        return EnterResult.Entered(relPath)
    }

    fun exit(): ExitResult {
        val current = _state.value
        if (!current.active) return ExitResult.NotActive
        val planPath = current.activePlanPath
        val content = planPath?.let { fileManager.readText(sessionId, it) }
        if (content.isNullOrBlank()) {

            return ExitResult.NoPlanWritten(planPath)
        }
        permissionChecker.setMode(PermissionMode.Default)
        _state.value = State(active = false, activePlanPath = planPath, approvedPlanContent = content)
        return ExitResult.Approved(planPath!!, content)
    }

    fun planExists(): Boolean {
        val path = _state.value.activePlanPath ?: return false
        return fileManager.exists(sessionId, path)
    }

    private fun generateUniqueSlug(): String {
        repeat(SLUG_ATTEMPTS) {
            val slug = randomSlug()
            val rel = "$PLANS_DIR/$slug.md"
            if (!fileManager.exists(sessionId, rel)) return slug
        }

        return "${randomSlug()}-${Random.nextInt(1000, 9999)}"
    }

    private fun randomSlug(): String {
        val a = ADJECTIVES.random()
        val n1 = NOUNS.random()
        val n2 = NOUNS.random()
        return "$a-$n1-$n2"
    }

    sealed class EnterResult {
        data class Entered(val planPath: String) : EnterResult()
        data class AlreadyActive(val planPath: String) : EnterResult()
    }

    sealed class ExitResult {
        object NotActive : ExitResult()
        data class NoPlanWritten(val planPath: String?) : ExitResult()
        data class Approved(val planPath: String, val content: String) : ExitResult()
    }

    companion object {
        const val PLANS_DIR = ".plans"
        private const val SLUG_ATTEMPTS = 8

        private val ADJECTIVES = listOf(
            "amber", "azure", "bold", "bright", "calm", "clear", "cool", "crisp",
            "dawn", "deep", "eager", "fair", "fierce", "gentle", "golden", "keen",
            "light", "lucky", "noble", "quiet", "rapid", "sharp", "silent", "sleek",
            "soft", "steady", "still", "swift", "tidy", "vivid", "warm", "wise"
        )

        private val NOUNS = listOf(
            "arrow", "blade", "brook", "cloud", "comet", "crane", "creek", "delta",
            "dove", "dune", "eagle", "ember", "falcon", "fern", "flame", "forge",
            "frost", "grove", "harbor", "hawk", "heron", "leaf", "lotus", "maple",
            "marsh", "meadow", "moon", "ocean", "orchid", "peak", "pine", "pond",
            "rain", "river", "sage", "spark", "stone", "storm", "summit", "tiger",
            "trail", "valley", "wave", "willow"
        )
    }
}
