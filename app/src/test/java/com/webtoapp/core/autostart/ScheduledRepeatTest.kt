package com.webtoapp.core.autostart

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ScheduledRepeatTest {

    private lateinit var context: Context
    private lateinit var manager: AutoStartManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences(AutoStartManager.PREFS_NAME, Context.MODE_PRIVATE)
            .edit().clear().commit()
        manager = AutoStartManager(context)
    }

    @Test
    fun `repeat flag round-trips through the stored schedule`() {
        manager.setScheduledStart(appId = 0L, enabled = true, time = "08:00", repeat = false)
        assertThat(manager.getScheduledStartConfig()?.repeat).isFalse()

        manager.setScheduledStart(appId = 0L, enabled = true, time = "08:00", repeat = true)
        assertThat(manager.getScheduledStartConfig()?.repeat).isTrue()
    }

    @Test
    fun `single-shot schedule is cleared after trigger instead of rescheduled`() {
        manager.setScheduledStart(appId = 0L, enabled = true, time = "08:00", repeat = false)

        manager.rescheduleAfterTrigger()

        assertThat(manager.getScheduledStartConfig()).isNull()
    }

    @Test
    fun `repeating schedule survives the trigger`() {
        manager.setScheduledStart(appId = 0L, enabled = true, time = "08:00", repeat = true)

        manager.rescheduleAfterTrigger()

        assertThat(manager.getScheduledStartConfig()).isNotNull()
        assertThat(manager.getScheduledStartConfig()?.repeat).isTrue()
    }

    @Test
    fun `legacy stored schedule without the flag defaults to repeating`() {
        context.getSharedPreferences(AutoStartManager.PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putLong(AutoStartManager.KEY_SCHEDULED_START_APP_ID, 0L)
            .putString(AutoStartManager.KEY_SCHEDULED_TIME, "09:30")
            .commit()

        assertThat(manager.getScheduledStartConfig()?.repeat).isTrue()
    }
}
