package com.webtoapp.ui.screens

import com.google.common.truth.Truth.assertThat
import com.webtoapp.core.apkbuilder.ApkExportPreflightIssue
import com.webtoapp.core.apkbuilder.ApkExportPreflightReport
import com.webtoapp.core.apkbuilder.ApkExportPreflightSeverity
import com.webtoapp.core.apkbuilder.BuildDiagnostic
import com.webtoapp.core.apkbuilder.BuildFailureCause
import com.webtoapp.core.apkbuilder.BuildStage
import com.webtoapp.data.model.ApkExportConfig
import com.webtoapp.data.model.WebApp
import java.io.File
import org.junit.Test

class BuildFailureReportTest {
    @Test
    fun `public report contains only structured diagnostics and local view retains raw context`() {
        val source = "https://user:password@example.invalid/private/route?token=fixture#fragment"
        val path = "/data/user/0/com.webtoapp/private-input.html"
        val detail = "private log line with authorization fixture"
        val log = File.createTempFile("wta-report", ".log").apply { writeText(detail) }
        try {
            val report = buildActionFailureReport(
                title = "Failure with $path",
                action = BuildFailureAction.BUILD_APK_FOR_SHARE,
                webApp = WebApp(
                    name = "Private project name",
                    url = source,
                    apkExportConfig = ApkExportConfig(
                        customVersionCode = 23,
                        customVersionName = "private-version-label",
                        engineType = "GECKOVIEW"
                    )
                ),
                summary = "Failed to read $path",
                logPath = log.path,
                diagnostic = BuildDiagnostic(
                    BuildStage.INPUT_PRECHECK, BuildFailureCause.INPUT_PRECHECK_FAILED,
                    mapOf("source" to source, "path" to path)
                ),
                preflight = ApkExportPreflightReport(listOf(
                    ApkExportPreflightIssue(ApkExportPreflightSeverity.Error, "htmlFiles[0]", "Bad input", detail, path),
                    ApkExportPreflightIssue(ApkExportPreflightSeverity.Warning, "private-key-name", "Other input", source)
                )),
                extraLines = listOf("apkPath=$path")
            )

            assertThat(report.publicDetails).contains("action=BUILD_APK_FOR_SHARE")
            assertThat(report.publicDetails).contains("stage=INPUT_PRECHECK")
            assertThat(report.publicDetails).contains("cause=INPUT_PRECHECK_FAILED")
            assertThat(report.publicDetails).contains("engine=GECKOVIEW")
            assertThat(report.publicDetails).contains("appVersionCode=23")
            assertThat(report.publicDetails).contains("preflightFields=htmlFiles")
            assertThat(report.publicDetails).contains("preflightErrors=1")
            listOf(source, "fixture", path, "Private project name", "private-version-label", log.path, "private-key-name").forEach {
                assertThat(report.publicDetails).doesNotContain(it)
            }
            assertThat(report.localDetails).contains(source)
            assertThat(report.localDetails).contains(path)
            assertThat(report.localDetails).contains(detail)
        } finally {
            log.delete()
        }
    }
}
