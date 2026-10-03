package com.webtoapp.core.sample

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SampleSharedPackManagerTest {

    @Test
    fun `parseManifest reads pack sha and size`() {
        val manifest = SampleSharedPackManager.parseManifest(
            """
            {
              "packs": {
                "python-django-shared": {"sha256": "abc123", "bytes": 6521658},
                "python-flask-shared": {"sha256": "def456", "bytes": 573921}
              }
            }
            """.trimIndent()
        )

        assertThat(manifest).hasSize(2)
        assertThat(manifest["python-django-shared"]?.sha256).isEqualTo("abc123")
        assertThat(manifest["python-django-shared"]?.bytes).isEqualTo(6521658L)
        assertThat(manifest["python-flask-shared"]?.sha256).isEqualTo("def456")
    }

    @Test
    fun `parseManifest drops entries without sha256`() {
        val manifest = SampleSharedPackManager.parseManifest(
            """
            {
              "packs": {
                "good-pack": {"sha256": "abc123", "bytes": 10},
                "bad-pack": {"bytes": 10},
                "empty-sha": {"sha256": "", "bytes": 5}
              }
            }
            """.trimIndent()
        )

        assertThat(manifest.keys).containsExactly("good-pack")
    }

    @Test
    fun `parseManifest survives malformed input`() {
        assertThat(SampleSharedPackManager.parseManifest("not json")).isEmpty()
        assertThat(SampleSharedPackManager.parseManifest("{}")).isEmpty()
        assertThat(SampleSharedPackManager.parseManifest("""{"packs":{}}""")).isEmpty()
    }
}
