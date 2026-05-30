package com.webtoapp.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.webtoapp.core.apkbuilder.ApkExportPreflightReport
import com.webtoapp.core.apkbuilder.ApkExportPreflightSeverity
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.design.WtaRowTone
import com.webtoapp.ui.design.WtaSectionDivider
import com.webtoapp.ui.design.WtaSettingCard
import com.webtoapp.ui.design.WtaSettingRow
import com.webtoapp.ui.design.WtaStatusBanner
import com.webtoapp.ui.design.WtaStatusTone

@Composable
fun ApkExportPreflightPanel(report: ApkExportPreflightReport) {
    val tone = when {
        report.hasErrors -> WtaStatusTone.Error
        report.warnings.isNotEmpty() -> WtaStatusTone.Warning
        else -> WtaStatusTone.Success
    }
    val message = when {
        report.hasErrors -> Strings.apkExportPreflightBlocked.format(report.errors.size)
        report.warnings.isNotEmpty() -> Strings.apkExportPreflightWarnings.format(report.warnings.size)
        else -> Strings.apkExportPreflightPassed
    }

    WtaStatusBanner(
        title = Strings.apkExportPreflightTitle,
        message = message,
        tone = tone
    )

    if (report.issues.isNotEmpty()) {
        val visibleIssues = report.issues.take(6)
        WtaSettingCard {
            visibleIssues.forEachIndexed { index, issue ->
                WtaSettingRow(
                    title = issue.title,
                    subtitle = issue.message,
                    icon = if (issue.severity == ApkExportPreflightSeverity.Error) {
                        Icons.Outlined.ErrorOutline
                    } else {
                        Icons.Outlined.WarningAmber
                    },
                    tone = if (issue.severity == ApkExportPreflightSeverity.Error) {
                        WtaRowTone.Danger
                    } else {
                        WtaRowTone.Normal
                    }
                ) {
                    Text(
                        text = if (issue.severity == ApkExportPreflightSeverity.Error) {
                            Strings.apkExportPreflightSeverityBlocking
                        } else {
                            Strings.apkExportPreflightSeverityNotice
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (issue.severity == ApkExportPreflightSeverity.Error) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.tertiary
                        }
                    )
                }
                if (index != visibleIssues.lastIndex) {
                    WtaSectionDivider()
                }
            }
        }
    }
}
