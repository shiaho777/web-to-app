package com.webtoapp.core.stats

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.webtoapp.data.model.WebApp

@Entity(
    tableName = "app_usage_stats",
    indices = [
        Index(value = ["appId"], unique = true),
        Index(value = ["lastUsedAt"]),
        Index(value = ["launchCount"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = WebApp::class,
            parentColumns = ["id"],
            childColumns = ["appId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class AppUsageStats(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val appId: Long,
    val launchCount: Int = 0,
    val totalUsageMs: Long = 0,
    val lastUsedAt: Long = 0,
    val lastSessionDurationMs: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
) {

    val formattedTotalUsage: String
        get() = StatsFormat.formatDuration(totalUsageMs)

    val formattedLastUsed: String
        get() = StatsFormat.formatRelative(lastUsedAt)

    val formattedLastSession: String
        get() = StatsFormat.formatDuration(lastSessionDurationMs)
}

enum class HealthStatus {
    UNKNOWN,
    ONLINE,
    SLOW,
    OFFLINE
}

@Entity(
    tableName = "app_health_records",
    indices = [
        Index(value = ["appId"]),
        Index(value = ["checkedAt"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = WebApp::class,
            parentColumns = ["id"],
            childColumns = ["appId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class AppHealthRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val appId: Long,
    val url: String,
    val status: HealthStatus = HealthStatus.UNKNOWN,
    val responseTimeMs: Long = 0,
    val httpStatusCode: Int = 0,
    val errorMessage: String? = null,
    val checkedAt: Long = System.currentTimeMillis()
)

data class AppHealthSummary(
    val appId: Long,
    val latestStatus: HealthStatus,
    val latestResponseTimeMs: Long,
    val lastCheckedAt: Long,
    val uptimePercent: Float
)
