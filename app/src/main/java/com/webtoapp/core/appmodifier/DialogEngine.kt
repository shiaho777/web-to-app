package com.webtoapp.core.appmodifier

import com.webtoapp.util.GsonProvider
import java.io.File
import java.util.Locale
import java.util.zip.ZipFile

enum class DialogEngineStyle {
    MODERN,
    MATERIAL,
    FULLSCREEN,
    BOTTOM_SHEET,
    COMPACT
}

enum class DialogEngineAnimation {
    NONE,
    FADE,
    SCALE,
    SLIDE
}

enum class DialogEngineComponentType {
    TITLE,
    MESSAGE,
    IMAGE,
    TEXT_FIELD,
    PASSWORD,
    CHECKBOX,
    SWITCH,
    LIST,
    BUTTON,
    PROGRESS
}

enum class DialogEngineAction {
    CLOSE,
    LAUNCH_TARGET,
    OPEN_URL,
    NONE
}

data class DialogEngineComponent(
    val id: String,
    val type: DialogEngineComponentType,
    val text: String = "",
    val hint: String = "",
    val selected: Boolean = false,
    val enabled: Boolean = true,
    val action: DialogEngineAction = DialogEngineAction.NONE,
    val actionValue: String = "",
    val widthFraction: Float = 1f,
    val heightDp: Int = 48,
    val backgroundColor: String = "#FFFFFFFF",
    val textColor: String = "#FF202124"
)

data class DialogEngineProject(
    val enabled: Boolean = false,
    val title: String = "Dialog",
    val message: String = "",
    val style: DialogEngineStyle = DialogEngineStyle.MODERN,
    val animation: DialogEngineAnimation = DialogEngineAnimation.FADE,
    val widthFraction: Float = 0.88f,
    val heightDp: Int = 0,
    val cornerRadiusDp: Int = 24,
    val elevationDp: Int = 12,
    val backgroundColor: String = "#FFFFFFFF",
    val contentColor: String = "#FF202124",
    val accentColor: String = "#FF6750A4",
    val scrimAlpha: Float = 0.72f,
    val dismissOnOutside: Boolean = true,
    val dismissOnBack: Boolean = true,
    val primaryText: String = "Continue",
    val secondaryText: String = "Close",
    val primaryAction: DialogEngineAction = DialogEngineAction.LAUNCH_TARGET,
    val secondaryAction: DialogEngineAction = DialogEngineAction.CLOSE,
    val primaryActionValue: String = "",
    val secondaryActionValue: String = "",
    val components: List<DialogEngineComponent> = listOf(
        DialogEngineComponent(
            id = "title",
            type = DialogEngineComponentType.TITLE,
            text = "Dialog",
            heightDp = 32
        ),
        DialogEngineComponent(
            id = "message",
            type = DialogEngineComponentType.MESSAGE,
            text = "",
            heightDp = 72
        )
    ),
    val xmlSource: String = "",
    val javaSource: String = "",
    val smaliSource: String = ""
) {
    fun toJson(): String = GsonProvider.gson.toJson(this)

    companion object {
        fun fromJson(json: String?): DialogEngineProject? {
            if (json.isNullOrBlank()) return null
            return runCatching {
                GsonProvider.gson.fromJson(json, DialogEngineProject::class.java)
            }.getOrNull()
        }
    }
}

data class DialogAnalysisResult(
    val dialogClasses: List<String>,
    val dialogLayouts: List<String>,
    val resourceReferences: List<String>,
    val xmlEntries: List<String>,
    val dexEntries: List<String>,
    val warnings: List<String>
) {
    val hasDialogs: Boolean
        get() = dialogClasses.isNotEmpty() || dialogLayouts.isNotEmpty()
}

object DialogEngineAnalyzer {
    private val dialogTokens = listOf(
        "AlertDialog",
        "DialogFragment",
        "android.app.Dialog",
        "androidx.compose.material3.AlertDialog",
        "BottomSheetDialog",
        "setContentView"
    )

    fun analyze(apkFile: File): DialogAnalysisResult {
        if (!apkFile.exists()) {
            return DialogAnalysisResult(
                emptyList(), emptyList(), emptyList(), emptyList(), emptyList(),
                listOf("APK_NOT_FOUND")
            )
        }

        val dialogClasses = linkedSetOf<String>()
        val dialogLayouts = linkedSetOf<String>()
        val resources = linkedSetOf<String>()
        val xmlEntries = linkedSetOf<String>()
        val dexEntries = linkedSetOf<String>()
        val warnings = linkedSetOf<String>()

        runCatching {
            ZipFile(apkFile).use { zip ->
                val entries = zip.entries().toList()
                for (entry in entries) {
                    val name = entry.name
                    if (name.startsWith("res/layout") && name.endsWith(".xml")) {
                        xmlEntries += name
                        val bytes = zip.getInputStream(entry).use { it.readBytes() }
                        val sample = bytes.toString(Charsets.UTF_8)
                        if (sample.contains("Dialog", ignoreCase = true) ||
                            sample.contains("AlertDialog", ignoreCase = true) ||
                            name.lowercase(Locale.US).contains("dialog")
                        ) {
                            dialogLayouts += name
                        }
                        val resMatches = Regex("@(?:layout|drawable|color|string)/[A-Za-z0-9_]+")
                            .findAll(sample)
                            .map { it.value }
                        resources += resMatches
                    }
                    if (name.endsWith(".dex")) {
                        val bytes = zip.getInputStream(entry).use { it.readBytes() }
                        val printable = extractPrintableAscii(bytes)
                        val found = dialogTokens.filter { token ->
                            printable.contains(token, ignoreCase = true)
                        }
                        if (found.isNotEmpty()) {
                            dexEntries += name
                            found.forEach { dialogClasses += it }
                        }
                    }
                }
            }
        }.onFailure {
            warnings += "APK_ANALYSIS_FAILED:${it.message.orEmpty()}"
        }

        if (dialogClasses.isEmpty() && dialogLayouts.isEmpty()) {
            warnings += "NO_DIALOGS_DETECTED"
        }

        return DialogAnalysisResult(
            dialogClasses = dialogClasses.toList(),
            dialogLayouts = dialogLayouts.toList(),
            resourceReferences = resources.toList(),
            xmlEntries = xmlEntries.toList(),
            dexEntries = dexEntries.toList(),
            warnings = warnings.toList()
        )
    }

    private fun extractPrintableAscii(bytes: ByteArray): String {
        val max = bytes.size.coerceAtMost(8 * 1024 * 1024)
        val out = StringBuilder(max / 8)
        var run = 0
        for (i in 0 until max) {
            val value = bytes[i].toInt() and 0xff
            val printable = value in 32..126
            if (printable) {
                out.append(value.toChar())
                run++
            } else {
                run = 0
            }
            if (run > 512) break
        }
        return out.toString()
    }
}
