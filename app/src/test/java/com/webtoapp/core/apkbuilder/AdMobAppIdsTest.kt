package com.webtoapp.core.apkbuilder

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AdMobAppIdsTest {

    @Test
    fun `only the publisher app id form is accepted`() {
        assertThat(AdMobAppIds.isValid("ca-app-pub-3940256099942544~3347511713")).isTrue()
        assertThat(AdMobAppIds.isValid("")).isFalse()
        assertThat(AdMobAppIds.isValid("ca-app-pub-3940256099942544/6300978111")).isFalse()
        assertThat(AdMobAppIds.isValid("\${admobAppId}")).isFalse()
    }

    @Test
    fun `a bad id is refused unless test mode substitutes the sample`() {
        assertThat(AdMobAppIds.manifestValue("", testMode = false)).isNull()
        assertThat(AdMobAppIds.manifestValue("ca-app-pub-1/2", testMode = false)).isNull()
        assertThat(AdMobAppIds.manifestValue("", testMode = true)).isEqualTo(AdMobAppIds.SAMPLE)
        assertThat(AdMobAppIds.manifestValue(AdMobAppIds.SAMPLE, testMode = false))
            .isEqualTo(AdMobAppIds.SAMPLE)
    }
}
