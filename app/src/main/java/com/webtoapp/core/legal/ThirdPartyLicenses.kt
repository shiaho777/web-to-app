package com.webtoapp.core.legal

import android.content.Context
import com.google.gson.Gson

data class LicenseEntry(
    val name: String,
    val version: String?,
    val spdx: String,
    val spdxNote: String?,
    val url: String
)

data class LicenseTextRef(
    val spdx: String,
    val asset: String?,
    val url: String
)

data class ProjectLicense(
    val spdx: String,
    val textAsset: String,
    val url: String
)

data class ThirdPartyLicenseCatalog(
    val projectLicense: ProjectLicense,
    val libraries: List<LicenseEntry>,
    val runtimes: List<LicenseEntry>,
    val licenseTexts: List<LicenseTextRef>
)

object ThirdPartyLicenses {

    const val CATALOG_ASSET = "legal/third_party_licenses.json"
    const val DOCS_PRIVACY_URL_EN = "https://shiaho777.github.io/web-to-app/privacy"
    const val DOCS_PRIVACY_URL_ZH = "https://shiaho777.github.io/web-to-app/zh/privacy"

    fun load(context: Context): ThirdPartyLicenseCatalog {
        val json = context.assets.open(CATALOG_ASSET).bufferedReader().use { it.readText() }
        return Gson().fromJson(json, ThirdPartyLicenseCatalog::class.java)
    }

    fun readLicenseText(context: Context, assetPath: String): String =
        context.assets.open(assetPath).bufferedReader().use { it.readText() }
}
