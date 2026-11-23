package com.webtoapp.core.agent.prompt

import com.google.common.truth.Truth.assertThat
import com.google.gson.JsonParser
import com.webtoapp.core.agent.skill.AgentSkill
import org.junit.Test

class SelectedContextSectionTest {
    @Test
    fun `selected resource names and skill instructions stay within JSON values`() {
        val appName = "Web \"App\"\n# Heading"
        val instructions = "Check files.\nReview results."
        val text = SelectedContextSection.build(
            language = PromptLang.EN,
            apps = listOf(SelectedContextSection.Reference("42", appName, "HTML")),
            modules = listOf(SelectedContextSection.Reference("module-id", "Module", "PLUGIN")),
            skills = listOf(AgentSkill("local-id", "review", "Description", instructions))
        )

        assertThat(text).contains("ReadAppFile")
        assertThat(text).contains("GetModule")
        val context = JsonParser.parseString(text.lineSequence().last()).asJsonObject
        val app = context.getAsJsonArray("apps")[0].asJsonObject
        assertThat(app.keySet()).containsExactly("id", "name", "type")
        assertThat(app["name"].asString).isEqualTo(appName)
        assertThat(context.getAsJsonArray("modules")[0].asJsonObject["id"].asString).isEqualTo("module-id")
        val skill = context.getAsJsonArray("skills")[0].asJsonObject
        assertThat(skill.keySet()).containsExactly("name", "instructions")
        assertThat(skill["instructions"].asString).isEqualTo(instructions)
    }

    @Test
    fun `empty selections add no prompt text`() {
        assertThat(SelectedContextSection.build(PromptLang.ZH, emptyList(), emptyList(), emptyList())).isEmpty()
    }
}
