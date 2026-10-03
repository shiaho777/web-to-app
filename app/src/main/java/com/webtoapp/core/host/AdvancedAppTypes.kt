package com.webtoapp.core.host

import android.content.Context
import com.webtoapp.data.model.AppType

/**
 * App types that stay out of the default create flow. The code is always in the
 * host; the About switch only decides whether they can be created, previewed,
 * and exported.
 */
object AdvancedAppTypes {
    val TYPES: Set<AppType> = setOf(
        AppType.IMAGE,
        AppType.VIDEO,
        AppType.WORDPRESS,
        AppType.NODEJS_APP,
        AppType.PHP_APP,
        AppType.PYTHON_APP,
        AppType.GO_APP,
    )

    fun isAdvanced(type: AppType): Boolean = type in TYPES

    fun isUsable(context: Context, type: AppType): Boolean =
        !isAdvanced(type) ||
            HostRuntimePrefs.getInstance(context).isAdvancedFeaturesEnabledBlocking()
}
