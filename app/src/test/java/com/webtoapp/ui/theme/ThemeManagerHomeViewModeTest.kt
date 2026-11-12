package com.webtoapp.ui.theme

import android.content.Context
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ThemeManagerHomeViewModeTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    private fun idleMain() {
        shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun defaultsToList() = runBlocking {
        val manager = ThemeManager(context)
        manager.setHomeViewMode(ThemeManager.HomeViewMode.LIST)
        idleMain()
        assertThat(manager.homeViewModeFlow.value).isEqualTo(ThemeManager.HomeViewMode.LIST)
    }

    @Test
    fun gridSelectionSurvivesANewManager() = runBlocking {
        val manager = ThemeManager(context)
        manager.setHomeViewMode(ThemeManager.HomeViewMode.GRID)
        idleMain()
        val reloaded = ThemeManager(context)
        idleMain()
        assertThat(reloaded.homeViewModeFlow.value).isEqualTo(ThemeManager.HomeViewMode.GRID)
    }
}