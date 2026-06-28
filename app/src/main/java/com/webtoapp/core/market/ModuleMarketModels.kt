package com.webtoapp.core.market

import com.google.gson.annotations.SerializedName
import com.webtoapp.core.extension.ModuleAuthor
import com.webtoapp.core.extension.UrlMatchRule

data class ModuleMarketEntry(
    @SerializedName("id")
    val id: String,

    @SerializedName("path")
    val path: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String = "",

    @SerializedName("icon")
    val icon: String = "package",

    @SerializedName("category")
    val category: String = "OTHER",

    @SerializedName("tags")
    val tags: List<String> = emptyList(),

    @SerializedName("version")
    val version: String = "1.0.0",

    @SerializedName("minAppVersion")
    val minAppVersion: Int = 0,

    @SerializedName("author")
    val author: ModuleAuthor? = null,

    @SerializedName("runAt")
    val runAt: String = "DOCUMENT_END",

    @SerializedName("permissions")
    val permissions: List<String> = emptyList(),

    @SerializedName("urlMatches")
    val urlMatches: List<UrlMatchRule> = emptyList(),

    @SerializedName("hasCss")
    val hasCss: Boolean = false,

    @SerializedName("iconUrl")
    val iconUrl: String? = null,

    @SerializedName("sourceType")
    val sourceType: String = "CUSTOM",

    @SerializedName("storeId")
    val storeId: String? = null,

    @SerializedName("homepage")
    val homepage: String? = null
)

data class ModuleSubmission(

    @SerializedName("prNumber")
    val prNumber: Int? = null,

    @SerializedName("prUrl")
    val prUrl: String? = null,

    @SerializedName("submittedAt")
    val submittedAt: String? = null,

    @SerializedName("direct")
    val direct: Boolean = false,

    @SerializedName("submitter")
    val submitter: ModuleSubmitter? = null,

    @SerializedName("contributors")
    val contributors: List<ModuleSubmitter> = emptyList()
)

data class ModuleSubmitter(

    @SerializedName("login")
    val login: String = "",

    @SerializedName("name")
    val name: String = "",

    @SerializedName("avatarUrl")
    val avatarUrl: String = "",

    @SerializedName("profileUrl")
    val profileUrl: String = ""
)

data class ModuleSubmissionsRegistry(
    @SerializedName("schema")
    val schema: Int = 1,

    @SerializedName("generatedAt")
    val generatedAt: String = "",

    @SerializedName("submissions")
    val submissions: Map<String, ModuleSubmission> = emptyMap()
)

data class ModuleMarketRegistry(
    @SerializedName("schema")
    val schema: Int = 1,

    @SerializedName("updatedAt")
    val updatedAt: String = "",

    @SerializedName("modules")
    val modules: List<ModuleMarketEntry> = emptyList()
)

enum class MarketInstallState {
    NotInstalled,
    UpToDate,
    UpdateAvailable
}

data class InstallProgress(
    val label: String,
    val current: Int,
    val total: Int
) {
    val fraction: Float get() = if (total > 0) current.toFloat() / total else 0f
}

data class MarketModuleView(
    val entry: ModuleMarketEntry,
    val state: MarketInstallState,

    val installedVersion: String? = null,

    val submission: ModuleSubmission? = null
)
