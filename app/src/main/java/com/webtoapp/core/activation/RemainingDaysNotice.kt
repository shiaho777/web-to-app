package com.webtoapp.core.activation

/**
 * Days and hours still left on a local time-limited grant.
 * A remainder under one hour is reported as one hour, so the last hour is not
 * shown as zero days and zero hours.
 */
data class RemainingDaysNotice(
    val days: Long,
    val hours: Long,
)

/** Time-limited and combined cards are the ones that carry a day clock. */
fun ActivationCodeType.offersRemainingDays(): Boolean =
    this == ActivationCodeType.TIME_LIMITED || this == ActivationCodeType.COMBINED

private const val HOUR_MS = 60L * 60L * 1000L
private const val DAY_MS = 24L * HOUR_MS

/**
 * Notice for a successful local launch, or null when this grant should stay
 * quiet. The choice lives on the redeemed code. Permanent and usage-only
 * cards never qualify, and neither does an expired or used-up grant.
 */
fun remainingDaysNotice(
    status: ActivationStatus,
    code: ActivationCode?,
    nowMs: Long = System.currentTimeMillis(),
): RemainingDaysNotice? {
    if (code == null || !code.showRemainingDays) return null
    if (!code.type.offersRemainingDays()) return null
    if (!status.isActivated) return null
    val usageLimit = status.usageLimit
    if (usageLimit != null && status.usageCount >= usageLimit) return null
    val expire = status.expireTime ?: return null
    val remaining = expire - nowMs
    if (remaining <= 0L) return null
    val days = remaining / DAY_MS
    var hours = (remaining % DAY_MS) / HOUR_MS
    if (days == 0L && hours == 0L) hours = 1L
    return RemainingDaysNotice(days, hours)
}
