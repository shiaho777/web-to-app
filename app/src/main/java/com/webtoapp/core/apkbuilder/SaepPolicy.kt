package com.webtoapp.core.apkbuilder

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.TypedValue
import com.google.gson.Gson
import java.io.File
import java.util.zip.ZipFile

/** Packaging-only opt-in; neither the host nor the unexported template declares SAEP. */
internal object SaepPolicy {
    const val TEMPLATE_METADATA = "com.webtoapp.SAEP_POLICY_TEMPLATE"
    const val POLICY_METADATA = "com.obric.agentrobots.POLICY_JSON"
    const val SHELL_ACTIVITY = "com.webtoapp.ui.shell.ShellActivity"
    const val MAX_BYTES = 10 * 1024
    const val VERSION = 1

    data class TemplateResource(val id: Int, val path: String, val activity: String? = null)

    /**
     * Locate the template's compiled policy resource, tolerating resource paths
     * renamed by aapt/R8. Returns null only when the template predates the
     * marker entirely — every other anomaly still fails loudly.
     */
    @Suppress("DEPRECATION")
    fun findTemplateResource(context: Context, apk: File): TemplateResource? {
        val pm = context.packageManager
        val info = checkNotNull(pm.getPackageArchiveInfo(
            apk.absolutePath, PackageManager.GET_META_DATA
        )) { "SAEP: cannot parse shell template" }
        val app = ApplicationInfo(checkNotNull(info.applicationInfo) {
            "SAEP: shell template has no application info"
        }).apply {
            sourceDir = apk.absolutePath
            publicSourceDir = apk.absolutePath
        }
        val id = app.metaData?.getInt(TEMPLATE_METADATA, 0) ?: 0
        if (id == 0) return null
        val resources = pm.getResourcesForApplication(app)
        check(resources.getResourceTypeName(id) == "raw") { "SAEP: policy must reference a raw resource" }
        val value = TypedValue()
        resources.getValue(id, value, true)
        val path = value.string?.toString()
        check(value.type == TypedValue.TYPE_STRING && path != null &&
            path.startsWith("res/") && path.split('/').none { it == ".." || it == "." }
        ) { "SAEP: invalid policy resource path" }
        ZipFile(apk).use { zip ->
            check(zip.getEntry(path)?.isDirectory == false) { "SAEP: policy resource is absent from APK" }
        }
        return TemplateResource(id, path)
    }

    /** Enabled exports additionally require the real enabled shell Activity. */
    @Suppress("DEPRECATION")
    fun resolveTemplate(context: Context, apk: File): TemplateResource {
        val resource = checkNotNull(findTemplateResource(context, apk)) {
            "SAEP: rebuild the shell template; policy resource marker is missing"
        }
        val info = checkNotNull(context.packageManager.getPackageArchiveInfo(
            apk.absolutePath, PackageManager.GET_ACTIVITIES
        )) { "SAEP: cannot parse shell template" }
        val activity = checkNotNull(info.activities?.singleOrNull {
            it.name == SHELL_ACTIVITY && it.enabled
        }) { "SAEP: shell activity is missing or disabled" }
        return resource.copy(activity = activity.name)
    }

    fun generate(
        packageName: String,
        activityName: String,
        appName: String,
        updatedAt: Long
    ): ByteArray {
        require(packageName.matches(Regex("[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z][A-Za-z0-9_]*)+"))) {
            "SAEP: invalid application ID"
        }
        require(activityName.isNotBlank()) { "SAEP: activity name must not be blank" }
        require(updatedAt >= 0) { "SAEP: timestamp must be nonnegative" }
        val actions = linkedMapOf(
            "global_disable" to false,
            "screenshot_disable" to false,
            "input_disable" to false
        )
        val policy = linkedMapOf<String, Any>(
            "schema" to "AGRP-Policy/1.0",
            "policy_version" to VERSION,
            "package" to packageName,
            "updated_at" to updatedAt.toString(),
            "default_policy" to mapOf("app" to actions),
            "scope" to linkedMapOf(
                "app" to actions,
                "activities" to mapOf(activityName to mapOf(
                    "name" to appName.ifBlank { "Main screen" },
                    "page_scope" to actions
                )),
                "agent_intents" to linkedMapOf(
                    "modify_content" to false,
                    "post_content" to false,
                    "delete_content" to false,
                    "account_incentive" to false
                )
            )
        )
        return Gson().toJson(policy).toByteArray(Charsets.UTF_8).also {
            require(it.size <= MAX_BYTES) { "SAEP: policy exceeds 10 KiB" }
        }
    }
}
