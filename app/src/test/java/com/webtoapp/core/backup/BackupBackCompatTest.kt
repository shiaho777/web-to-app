package com.webtoapp.core.backup

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * Backward compatibility: old backups (v1-v4, before healthRecords existed)
 * must restore without crashing, with new fields defaulted.
 */
@RunWith(RobolectricTestRunner::class)
class BackupBackCompatTest {

    private val manager by lazy {
        DataBackupManager(RuntimeEnvironment.getApplication())
    }

    @Test
    fun `v1 backup without new arrays parses with defaults`() {
        val json = """
            {
              "version": 1,
              "exportTime": 1700000000000,
              "appCount": 1,
              "apps": [{"name": "Old App", "url": "https://example.com"}],
              "categories": []
            }
        """.trimIndent()
        val data = manager.parseBackupData(json)
        assertThat(data.version).isEqualTo(1)
        assertThat(data.apps).hasSize(1)
        assertThat(data.apps[0].name).isEqualTo("Old App")
        assertThat(data.healthRecords).isEmpty()
        assertThat(data.usageStats).isEmpty()
        // Current-model defaults merged in: never zero/absent.
        assertThat(data.apps[0].webViewConfig.pageZoomPercent).isEqualTo(100)
    }

    @Test
    fun `v5 backup with health records parses`() {
        val json = """
            {
              "version": 5,
              "exportTime": 1700000000000,
              "appCount": 1,
              "apps": [{"id": 7, "name": "App", "url": "https://example.com"}],
              "categories": [],
              "usageStats": [],
              "healthRecords": [
                {"id": 1, "appId": 7, "url": "https://example.com",
                 "status": "ONLINE", "responseTimeMs": 120, "httpStatusCode": 200,
                 "errorMessage": null, "checkedAt": 1700000001000}
              ]
            }
        """.trimIndent()
        val data = manager.parseBackupData(json)
        assertThat(data.healthRecords).hasSize(1)
        assertThat(data.healthRecords[0].appId).isEqualTo(7)
        assertThat(data.healthRecords[0].httpStatusCode).isEqualTo(200)
    }

    @Test
    fun `corrupt single app does not kill the whole parse`() {
        val json = """
            {
              "version": 4,
              "exportTime": 1700000000000,
              "appCount": 3,
              "apps": [
                {"name": "Good", "url": "https://example.com"},
                "not-an-object",
                42
              ],
              "categories": []
            }
        """.trimIndent()
        val data = manager.parseBackupData(json)
        assertThat(data.apps).hasSize(1)
        assertThat(data.apps[0].name).isEqualTo("Good")
    }

    @Test
    fun `empty backup parses to empty`() {
        val data = manager.parseBackupData("""{"version": 5}""")
        assertThat(data.apps).isEmpty()
        assertThat(data.categories).isEmpty()
        assertThat(data.appCount).isEqualTo(0)
    }
}
