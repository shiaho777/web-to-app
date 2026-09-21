package com.webtoapp.core.agent.permission

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.google.gson.JsonObject
import com.webtoapp.core.agent.files.ProjectFileManager
import com.webtoapp.core.agent.plan.PlanManager
import com.webtoapp.core.agent.tool.ToolContext
import com.webtoapp.core.agent.tool.builtin.EnterPlanModeTool
import com.webtoapp.core.agent.tool.builtin.ExitPlanModeTool
import com.webtoapp.core.agent.tool.builtin.ReadFileTool
import com.webtoapp.core.agent.tool.builtin.WriteFileTool
import com.webtoapp.core.agent.todo.TodoManager
import com.webtoapp.data.dao.WebAppDao
import com.webtoapp.data.dao.WebAppStartupCandidate
import com.webtoapp.data.dao.WebAppSummary
import com.webtoapp.data.model.AiModel
import com.webtoapp.data.model.AiProvider
import com.webtoapp.data.model.ApiKeyConfig
import com.webtoapp.data.model.AppType
import com.webtoapp.data.model.ModelCapability
import com.webtoapp.data.model.SavedModel
import com.webtoapp.data.model.WebApp
import com.webtoapp.data.repository.WebAppRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Regression for #997: entering plan mode mid-turn deadlocked the agent.
 *
 * AgentEngine.runSequential checks permissions against the shared ToolContext
 * but executes every tool on `toolContext.copy(...)`. EnterPlanModeTool
 * publishes the generated plan path via `setActivePlanFile()` — on that
 * throwaway copy. The shared context's `effectivePlanFile()` therefore stayed
 * null, `checkPlan` denied every Write (including to the plan file itself),
 * and ExitPlanMode kept failing with "plan file is empty" forever.
 */
@RunWith(RobolectricTestRunner::class)
class PlanModePermissionTest {

    @Test
    fun `write to the plan file is allowed when plan mode was entered mid-turn`() = runBlocking {
        val harness = Harness()

        harness.enterPlanModeOnCallCopy()

        assertThat(harness.checkWrite(harness.planPath())).isEqualTo(PermissionDecision.Allow)
    }

    @Test
    fun `publishing the plan file on a per-call copy is visible to the shared context`() {
        val harness = Harness()

        harness.ctx.copy().setActivePlanFile(".plans/crisp-heron-flame.md")

        assertThat(harness.ctx.effectivePlanFile()).isEqualTo(".plans/crisp-heron-flame.md")
    }

    @Test
    fun `write to a non-plan path stays denied in plan mode`() = runBlocking {
        val harness = Harness()
        harness.enterPlanModeOnCallCopy()

        assertThat(harness.checkWrite("index.html")).isEqualTo(PermissionDecision.Deny)
        assertThat(harness.checkWrite("manifest.json")).isEqualTo(PermissionDecision.Deny)
    }

    @Test
    fun `read tools and ExitPlanMode stay allowed in plan mode`() = runBlocking {
        val harness = Harness()
        harness.enterPlanModeOnCallCopy()

        assertThat(harness.check(ReadFileTool(), pathArgs("index.html")))
            .isEqualTo(PermissionDecision.Allow)
        assertThat(harness.check(ExitPlanModeTool(harness.planManager), JsonObject()))
            .isEqualTo(PermissionDecision.Allow)
    }

    @Test
    fun `a plan file written this turn can be submitted for review`() = runBlocking {
        val harness = Harness()
        harness.enterPlanModeOnCallCopy()
        val planPath = harness.planPath()

        // Engine order: permission check on the shared context, then execute on
        // a fresh copy carrying the progress callback.
        assertThat(harness.checkWrite(planPath)).isEqualTo(PermissionDecision.Allow)
        val write = WriteFileTool().execute(
            JsonObject().apply {
                addProperty("path", planPath)
                addProperty("content", "# Plan\n\n- step one\n")
            },
            harness.ctx.copy()
        )
        assertThat(write.isError).isFalse()

        val exit = ExitPlanModeTool(harness.planManager).execute(JsonObject(), harness.ctx.copy())
        assertThat(exit.isError).isFalse()
        assertThat(exit.planReviewPath).isEqualTo(planPath)
    }

    @Test
    fun `the turn-start snapshot path also permits plan file writes`() = runBlocking {
        val harness = Harness(activePlanFile = ".plans/amber-arrow-cloud.md")

        // Snapshot path arrives via the constructor when a plan survived from
        // an earlier turn — no mid-turn publication involved.
        harness.checker.setMode(PermissionMode.Plan)

        assertThat(harness.checkWrite(".plans/amber-arrow-cloud.md"))
            .isEqualTo(PermissionDecision.Allow)
    }

    private class Harness(activePlanFile: String? = null) {
        val checker = PermissionChecker(PermissionPrompter())
        val planManager: PlanManager
        val ctx: ToolContext

        init {
            val appCtx = ApplicationProvider.getApplicationContext<Context>()
            val fileManager = ProjectFileManager(appCtx)
            planManager = PlanManager(SESSION_ID, fileManager, checker)
            ctx = ToolContext(
                androidContext = appCtx,
                sessionId = SESSION_ID,
                fileManager = fileManager,
                textModel = SavedModel(
                    model = AiModel(id = "m", name = "m", provider = AiProvider.OPENAI),
                    apiKeyId = "k",
                    capabilities = listOf(ModelCapability.TEXT)
                ),
                textApiKey = ApiKeyConfig(provider = AiProvider.OPENAI, apiKey = "key"),
                prompter = PermissionPrompter(),
                todos = TodoManager(),
                appRepository = WebAppRepository(FakeWebAppDao()),
                activePlanFile = activePlanFile
            )
        }

        /** Mirrors AgentEngine: the tool runs on a per-call copy of the shared context. */
        fun enterPlanModeOnCallCopy() = runBlocking {
            val result = EnterPlanModeTool(planManager).execute(JsonObject(), ctx.copy())
            assertThat(result.isError).isFalse()
        }

        fun planPath(): String = planManager.state.value.activePlanPath!!

        suspend fun checkWrite(path: String) = check(WriteFileTool(), pathArgs(path))

        suspend fun check(
            tool: com.webtoapp.core.agent.tool.Tool,
            args: JsonObject
        ) = checker.check(tool, args, ctx)
    }

    private companion object {
        const val SESSION_ID = "plan-test"

        fun pathArgs(path: String) = JsonObject().apply { addProperty("path", path) }
    }

    /** The repository eagerly reads three Flow properties at construction; the rest is never called here. */
    private class FakeWebAppDao : WebAppDao {
        override fun getAllWebApps(): Flow<List<WebApp>> = flowOf(emptyList())
        override fun getAllWebAppSummaries(): Flow<List<WebAppSummary>> = flowOf(emptyList())
        override fun getHttpWebApps(appType: AppType): Flow<List<WebApp>> = flowOf(emptyList())
        override suspend fun getStartupCandidatesWithoutIcons(appType: AppType, limit: Int): List<WebAppStartupCandidate> = throw NotImplementedError()
        override suspend fun getWebAppById(id: Long): WebApp? = throw NotImplementedError()
        override fun getWebAppByIdFlow(id: Long): Flow<WebApp?> = throw NotImplementedError()
        override suspend fun insert(webApp: WebApp): Long = throw NotImplementedError()
        override suspend fun update(webApp: WebApp) = throw NotImplementedError()
        override suspend fun delete(webApp: WebApp) = throw NotImplementedError()
        override suspend fun deleteById(id: Long) = throw NotImplementedError()
        override suspend fun updateActivationStatus(id: Long, activated: Boolean) = throw NotImplementedError()
        override suspend fun upgradeRemoteHttpUrls(appType: AppType, updatedAt: Long): Int = throw NotImplementedError()
        override suspend fun getCount(): Int = throw NotImplementedError()
        override fun searchWebApps(query: String): Flow<List<WebApp>> = throw NotImplementedError()
        override suspend fun insertAll(webApps: List<WebApp>): List<Long> = throw NotImplementedError()
        override suspend fun updateAll(webApps: List<WebApp>) = throw NotImplementedError()
        override suspend fun deleteAll(webApps: List<WebApp>) = throw NotImplementedError()
        override suspend fun deleteByIds(ids: List<Long>) = throw NotImplementedError()
        override suspend fun updateActivationStatusBatch(ids: List<Long>, activated: Boolean) = throw NotImplementedError()
        override suspend fun getWebAppsByIds(ids: List<Long>): List<WebApp> = throw NotImplementedError()
        override fun getActivatedWebApps(): Flow<List<WebApp>> = throw NotImplementedError()
        override fun getRecentWebApps(limit: Int): Flow<List<WebApp>> = throw NotImplementedError()
        override suspend fun countByName(name: String, excludeId: Long): Int = throw NotImplementedError()
        override suspend fun clearCategoryId(categoryId: Long) = throw NotImplementedError()
        override suspend fun getAllUrls(): List<String> = throw NotImplementedError()
    }
}
