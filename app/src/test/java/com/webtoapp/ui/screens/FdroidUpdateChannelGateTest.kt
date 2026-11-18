package com.webtoapp.ui.screens

import com.google.common.truth.Truth.assertWithMessage
import org.junit.Test
import java.io.File

class FdroidUpdateChannelGateTest {

    private fun aboutSource(): String {
        val candidates = listOf(
            "app/src/main/java/com/webtoapp/ui/screens/AboutScreen.kt",
            "src/main/java/com/webtoapp/ui/screens/AboutScreen.kt",
        )
        val file = candidates.map(::File).firstOrNull(File::exists)
            ?: error("Could not locate AboutScreen.kt from: $candidates")
        return file.readText()
    }

    @Test
    fun `fdroid builds gate the update dialog and version pill check`() {
        val source = aboutSource()
        assertWithMessage("UpdateCheckDialog must stay gated by BuildConfig.FDROID")
            .that(source)
            .contains("if (showUpdateDialog && !com.webtoapp.BuildConfig.FDROID)")
        assertWithMessage("VersionPill must receive the FDROID update gate")
            .that(source)
            .contains("updateCheckEnabled = !com.webtoapp.BuildConfig.FDROID")
    }

    @Test
    fun `fdroid builds replace in-app downloads with the channel note`() {
        val source = aboutSource()
        assertWithMessage("Version history must branch on BuildConfig.FDROID before the download row")
            .that(source)
            .contains("if (com.webtoapp.BuildConfig.FDROID)")
        assertWithMessage("F-Droid builds must show the managed-updates note")
            .that(source)
            .contains("Strings.fdroidUpdatesManaged")
    }
}
