package com.webtoapp.core.activation

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RemainingDaysNoticeTest {

    private val now = 1_700_000_000_000L

    private fun status(
        expireTime: Long? = now + 2 * DAY + 3 * HOUR,
        activated: Boolean = true,
        usageCount: Int = 0,
        usageLimit: Int? = null,
    ) = ActivationStatus(
        isActivated = activated,
        expireTime = expireTime,
        usageCount = usageCount,
        usageLimit = usageLimit,
    )

    private fun code(
        type: ActivationCodeType = ActivationCodeType.TIME_LIMITED,
        show: Boolean = true,
    ) = ActivationCode(code = "DAYS-001", type = type, showRemainingDays = show)

    @Test
    fun `opted-in time limit reports days and hours`() {
        val notice = remainingDaysNotice(status(), code(), now)
        assertThat(notice).isEqualTo(RemainingDaysNotice(days = 2, hours = 3))
    }

    @Test
    fun `combined cards qualify`() {
        val notice = remainingDaysNotice(
            status(),
            code(type = ActivationCodeType.COMBINED),
            now
        )
        assertThat(notice).isEqualTo(RemainingDaysNotice(days = 2, hours = 3))
    }

    @Test
    fun `flag off stays quiet`() {
        assertThat(remainingDaysNotice(status(), code(show = false), now)).isNull()
    }

    @Test
    fun `permanent and usage-only stay quiet even with the flag`() {
        assertThat(
            remainingDaysNotice(status(), code(ActivationCodeType.PERMANENT), now)
        ).isNull()
        assertThat(
            remainingDaysNotice(status(), code(ActivationCodeType.USAGE_LIMITED), now)
        ).isNull()
    }

    @Test
    fun `missing code expired grant and unused grant stay quiet`() {
        assertThat(remainingDaysNotice(status(), null, now)).isNull()
        assertThat(remainingDaysNotice(status(activated = false), code(), now)).isNull()
        assertThat(remainingDaysNotice(status(expireTime = now - 1), code(), now)).isNull()
        assertThat(remainingDaysNotice(status(expireTime = null), code(), now)).isNull()
    }

    @Test
    fun `used-up combined grant stays quiet`() {
        assertThat(
            remainingDaysNotice(status(usageCount = 2, usageLimit = 2), code(), now)
        ).isNull()
    }

    @Test
    fun `under one hour still shows one hour`() {
        val notice = remainingDaysNotice(status(expireTime = now + 20 * 60 * 1000), code(), now)
        assertThat(notice).isEqualTo(RemainingDaysNotice(days = 0, hours = 1))
    }

    @Test
    fun `under one day keeps the hours`() {
        val notice = remainingDaysNotice(status(expireTime = now + 5 * HOUR), code(), now)
        assertThat(notice).isEqualTo(RemainingDaysNotice(days = 0, hours = 5))
    }

    @Test
    fun `missing json field stays off and an explicit flag round-trips`() {
        val legacy = ActivationCode.fromJson(
            """{"code":"OLD-1","type":"TIME_LIMITED","timeLimitMs":86400000}"""
        )
        assertThat(legacy!!.showRemainingDays).isFalse()

        val saved = code().toJson()
        val restored = ActivationCode.fromJson(saved)
        assertThat(restored!!.showRemainingDays).isTrue()
    }

    companion object {
        private const val HOUR = 60L * 60L * 1000L
        private const val DAY = 24L * HOUR
    }
}
