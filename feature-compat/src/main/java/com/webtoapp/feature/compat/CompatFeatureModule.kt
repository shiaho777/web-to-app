package com.webtoapp.feature.compat

import com.webtoapp.core.feature.FeatureContext
import com.webtoapp.core.feature.FeatureIds
import com.webtoapp.core.feature.FeatureModule

class CompatFeatureModule : FeatureModule {
    override val id: String = FeatureIds.COMPAT
    override val version: Int = 1

    override fun install(context: FeatureContext) {
    }
}
