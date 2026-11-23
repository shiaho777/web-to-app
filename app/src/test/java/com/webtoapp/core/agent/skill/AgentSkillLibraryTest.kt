package com.webtoapp.core.agent.skill

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class AgentSkillLibraryTest {
    @get:Rule
    val temporary = TemporaryFolder()

    @Test
    fun `skills are imported once and selected by content ID`() {
        val directory = temporary.newFolder()
        val library = AgentSkillLibrary(directory)
        val markdown = "---\nname: audit\ndescription: \"Review: web accessibility\"\n---\nCheck keyboard focus."
        val skill = library.import(markdown.byteInputStream(), "SKILL.md")
        assertThat(skill.name).isEqualTo("audit")
        assertThat(skill.description).isEqualTo("Review: web accessibility")
        assertThat(skill.instructions).isEqualTo("Check keyboard focus.")
        assertThat(library.import(markdown.byteInputStream(), "renamed.md").id).isEqualTo(skill.id)

        val reopened = AgentSkillLibrary(directory)
        assertThat(reopened.list()).containsExactly(skill)
        assertThat(reopened.selected(listOf("../other", skill.id, skill.id))).containsExactly(skill)

        reopened.remove(skill.id)
        assertThat(reopened.selected(listOf(skill.id))).isEmpty()
        assertThat(reopened.list()).isEmpty()
    }

    @Test
    fun `selected instructions are bounded across skills`() {
        val library = AgentSkillLibrary(temporary.newFolder())
        val first = library.import("a".repeat(20_000).byteInputStream(), "first.md")
        val second = library.import("b".repeat(20_000).byteInputStream(), "second.md")
        val third = library.import("# Review\nStart with the changelog.".byteInputStream(), "third.md")

        assertThat(third.name).isEqualTo("Review")
        assertThat(library.selected(listOf(first.id, second.id, third.id)))
            .containsExactly(first, third).inOrder()
    }

    @Test
    fun `input must be bounded UTF-8 text`() {
        val library = AgentSkillLibrary(temporary.newFolder())
        val oversized = ByteArray(AgentSkillLibrary.maxBytes + 1) { 65 }

        assertThrows(IllegalArgumentException::class.java) { library.import(oversized.inputStream(), "large.md") }
        assertThrows(IllegalArgumentException::class.java) {
            library.import(byteArrayOf(0xc3.toByte(), 0x28).inputStream(), "broken.md")
        }
        assertThat(library.list()).isEmpty()
    }
}
