package com.webtoapp.ui.screens

import com.google.common.truth.Truth.assertWithMessage
import org.junit.Test
import java.io.File

class FirstFiveMinutesGateTest {

    private fun source(relative: String): String {
        val candidates = listOf("app/src/main/java/com/webtoapp/$relative", "src/main/java/com/webtoapp/$relative")
        val file = candidates.map(::File).firstOrNull(File::exists)
            ?: error("Could not locate $relative from: $candidates")
        return file.readText()
    }

    @Test
    fun `startup update check stays mounted in navigation and fdroid gated`() {
        val navigation = source("ui/navigation/AppNavigation.kt")
        assertWithMessage("AppNavigation must mount StartupUpdateCheck")
            .that(navigation)
            .contains("com.webtoapp.ui.components.StartupUpdateCheck(")
        val startup = source("ui/components/StartupUpdateCheck.kt")
        assertWithMessage("StartupUpdateCheck must bail out on F-Droid builds")
            .that(startup)
            .contains("if (com.webtoapp.BuildConfig.FDROID) return")
        assertWithMessage("StartupUpdateCheck must honor the skipped version")
            .that(startup)
            .contains("prefs.skippedVersion()")
        assertWithMessage("StartupUpdateCheck must track the last seen version code")
            .that(startup)
            .contains("setLastSeenVersionCode")
    }

    @Test
    fun `onboarding gate sits between language selection and main navigation`() {
        val main = source("ui/MainActivity.kt")
        assertWithMessage("MainActivity must gate onboarding on completion state")
            .that(main)
            .contains("onboardingCompleted == false")
        assertWithMessage("MainActivity must render the onboarding screen")
            .that(main)
            .contains("com.webtoapp.ui.components.OnboardingScreen(")
        assertWithMessage("Finishing onboarding must persist completion")
            .that(main)
            .contains("onboardingPrefs.setCompleted(true)")
    }

    @Test
    fun `startup permissions ask with rationale before launching`() {
        val main = source("ui/MainActivity.kt")
        assertWithMessage("Permission collection must show the rationale dialog instead of launching directly")
            .that(main)
            .contains("showStartupPermissionRationale = true")
        assertWithMessage("Denied permissions must surface the degradation dialog")
            .that(main)
            .contains("showPermissionDeniedDialog = true")
        assertWithMessage("The unconsumed READ_MEDIA_IMAGES request must stay removed")
            .that(main)
            .doesNotContain("Manifest.permission.READ_MEDIA_IMAGES")
    }

    @Test
    fun `startup dialogs wait for the confirmed language and completed tour`() {
        val main = source("ui/MainActivity.kt")
        val beforeContent = main.substringBefore("setContent {")
        assertWithMessage("Language selection must be initialized outside the builder theme")
            .that(main.substringAfter("setContent {").substringBefore("WebToAppTheme {"))
            .contains("val hasSelectedLanguage = InitializeLanguage()")
        assertWithMessage("A startup dialog must not interrupt language selection or the tour")
            .that(main)
            .contains("val startupReady = hasSelectedLanguage == true && onboardingCompleted == true")
        listOf("requestNecessaryPermissions()", "checkShortcutPermission()").forEach { check ->
            assertWithMessage("$check must run inside the startup gate")
                .that(main.substringAfter("LaunchedEffect(startupReady)").substringBefore("CompositionLocalProvider("))
                .contains(check)
            assertWithMessage("$check must not run before composition")
                .that(beforeContent)
                .doesNotContain(check)
        }
    }

    @Test
    fun `empty state offers create and sample actions`() {
        val home = source("ui/screens/HomeScreen.kt")
        assertWithMessage("EmptyState must accept a create callback")
            .that(home)
            .contains("onCreateApp: () -> Unit = {}")
        assertWithMessage("EmptyState must offer the sample app action")
            .that(home)
            .contains("Strings.emptyStateSampleApp")
        val navigation = source("ui/navigation/AppNavigation.kt")
        assertWithMessage("Navigation must wire sample creation")
            .that(navigation)
            .contains("onCreateSampleApp = { viewModel.createSampleApp() }")
    }

    @Test
    fun `install entry points preflight unknown sources`() {
        val installer = source("core/apkbuilder/ApkInstaller.kt")
        assertWithMessage("ApkInstaller must expose the unknown-sources preflight")
            .that(installer)
            .contains("fun canInstallFromThisApp")
        assertWithMessage("ApkInstaller must open the unknown-sources settings page")
            .that(installer)
            .contains("ACTION_MANAGE_UNKNOWN_APP_SOURCES")
        listOf(
            "ui/screens/BuildApkScreen.kt",
            "ui/screens/FileManagerScreen.kt",
            "ui/screens/AboutScreen.kt"
        ).forEach { path ->
            assertWithMessage("$path must preflight install permission")
                .that(source(path))
                .contains("canInstallFromThisApp")
        }
    }
}
