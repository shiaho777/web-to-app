package com.webtoapp.core.i18n

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Shortcut cold starts never compose the builder screen, which used to be the
 * only place that published the saved language. Strings then stayed on the
 * Chinese default (#1391).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class HostLanguageTest {

    @Test
    fun `apply publishes the chosen language before any activity`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val original = Strings.currentLanguage.value
        try {
            Strings.setLanguage(AppLanguage.CHINESE)
            HostLanguage.apply(context, AppLanguage.ENGLISH)
            assertThat(Strings.currentLanguage.value).isEqualTo(AppLanguage.ENGLISH)
            assertThat(Strings.pressAgainToExit).isEqualTo("Press back again to exit")
        } finally {
            Strings.setLanguage(original)
        }
    }

    @Test
    fun `applyBlocking reads the saved language`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val original = Strings.currentLanguage.value
        try {
            runBlocking {
                LanguageManager.getInstance(context).setLanguage(AppLanguage.ENGLISH)
            }
            Strings.setLanguage(AppLanguage.CHINESE)
            HostLanguage.applyBlocking(context)
            assertThat(Strings.currentLanguage.value).isEqualTo(AppLanguage.ENGLISH)
            assertThat(Strings.pressAgainToExit).isEqualTo("Press back again to exit")
        } finally {
            Strings.setLanguage(original)
        }
    }
}
