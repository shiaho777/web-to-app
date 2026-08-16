package com.webtoapp.core.agent.tool.builtin

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.webtoapp.core.agent.files.ProjectFileManager
import com.webtoapp.core.agent.permission.PermissionChecker
import com.webtoapp.core.agent.permission.PermissionPrompter
import com.webtoapp.core.agent.plan.PlanManager
import com.webtoapp.core.agent.tool.ToolRegistryFactory
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ClearAppCacheToolTest {

    private val tool = ClearAppCacheTool()

    @Test
    fun `tool advertises write semantics with a required appId schema`() {
        assertThat(tool.name).isEqualTo("ClearAppCache")
        // Cache clearing has side effects — must go through the permission prompt.
        assertThat(tool.isReadOnly()).isFalse()

        val args = com.google.gson.JsonObject().apply { addProperty("appId", 4) }
        assertThat(tool.activityDescription(args)).isEqualTo("Clearing cache for app 4")

        val schema = tool.parametersSchema.toString()
        assertThat(schema).contains("appId")
        assertThat(schema).contains("required")
    }

    @Test
    fun `description tells the LLM what is cleared and what is kept`() {
        assertThat(tool.description).contains("build cache")
        assertThat(tool.description).contains("origin storage")
        // The keep-list is what stops the LLM from promising a full data wipe.
        assertThat(tool.description).contains("Cookies")
    }

    @Test
    fun `tool is registered exactly once and registry names stay unique`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prompter = PermissionPrompter()
        val planManager = PlanManager(
            sessionId = "test",
            fileManager = ProjectFileManager(context),
            permissionChecker = PermissionChecker(prompter)
        )
        val registry = ToolRegistryFactory(planManager, imageRegistry = null)
            .build(hasImageModel = false)

        val matches = registry.all.filter { it.name == "ClearAppCache" }
        assertThat(matches).hasSize(1)
        assertThat(matches.single().isReadOnly()).isFalse()

        val names = registry.all.map { it.name }
        assertThat(names.distinct()).isEqualTo(names)
    }
}
