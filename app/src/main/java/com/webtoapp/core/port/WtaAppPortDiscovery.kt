package com.webtoapp.core.port

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import com.webtoapp.core.logging.AppLogger
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONArray

object WtaAppPortDiscovery {

    private const val TAG = "WtaAppPortDiscovery"

    const val META_MARKER = "com.webtoapp.WTA_APP_MARKER"

    const val META_PROJECT_ID = "com.webtoapp.WTA_PROJECT_ID"

    const val META_PROJECT_NAME = "com.webtoapp.WTA_PROJECT_NAME"

    private const val QUERY_TIMEOUT_MS = 1500L

    data class WtaAppInfo(
        val packageName: String,
        val displayName: String,
        val projectId: String?,
        val projectName: String?,
        val versionName: String?,
        val versionCode: Long
    )

    data class RemoteAllocation(
        val port: Int,
        val owner: String,
        val range: String,
        val allocatedAt: Long,
        val pid: Long,
        val alive: Boolean
    )

    data class WtaAppPortReport(
        val app: WtaAppInfo,

        val responded: Boolean,
        val allocations: List<RemoteAllocation>,
        val errorMessage: String? = null
    )

    suspend fun listInstalledWtaApps(context: Context): List<WtaAppInfo> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val packages: List<PackageInfo> = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getInstalledPackages(
                    PackageManager.PackageInfoFlags.of(PackageManager.GET_META_DATA.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                pm.getInstalledPackages(PackageManager.GET_META_DATA)
            }
        } catch (e: Exception) {
            AppLogger.w(TAG, "getInstalledPackages failed: ${e.message}")
            emptyList()
        }

        packages.mapNotNull { pkg ->
            val appInfo = pkg.applicationInfo ?: return@mapNotNull null
            val meta = appInfo.metaData ?: return@mapNotNull null
            val marker = meta.get(META_MARKER)?.toString()?.lowercase()
            if (marker != "true") return@mapNotNull null

            val display = try {
                pm.getApplicationLabel(appInfo).toString()
            } catch (_: Exception) {
                pkg.packageName
            }

            WtaAppInfo(
                packageName = pkg.packageName,
                displayName = display,
                projectId = meta.getString(META_PROJECT_ID),
                projectName = meta.getString(META_PROJECT_NAME),
                versionName = pkg.versionName,
                versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) pkg.longVersionCode
                    else @Suppress("DEPRECATION") pkg.versionCode.toLong()
            )
        }.sortedBy { it.displayName.lowercase() }
    }

    suspend fun queryAllApps(context: Context): List<WtaAppPortReport> = coroutineScope {
        val apps = listInstalledWtaApps(context)
        if (apps.isEmpty()) return@coroutineScope emptyList()

        apps.map { app ->
            async(Dispatchers.IO) { queryApp(context, app) }
        }.awaitAll()
    }

    suspend fun queryApp(context: Context, app: WtaAppInfo): WtaAppPortReport = withContext(Dispatchers.IO) {
        val deferred = CompletableDeferred<Pair<Int, Bundle?>>()

        val resultReceiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context, intent: Intent) {

                deferred.complete(resultCode to (getResultExtras(false) ?: Bundle()))
            }
        }

        val intent = Intent(PortQueryReceiver.ACTION_PORT_QUERY).apply {
            setPackage(app.packageName)

            component = ComponentName(
                app.packageName,
                "com.webtoapp.core.port.PortQueryReceiver"
            )
        }

        return@withContext try {
            context.sendOrderedBroadcast(
                intent,
                 null,
                resultReceiver,
                 null,
                 0,
                 null,
                 null
            )

            val pair = withTimeoutOrNull(QUERY_TIMEOUT_MS) { deferred.await() }
            if (pair == null) {
                WtaAppPortReport(
                    app = app,
                    responded = false,
                    allocations = emptyList(),
                    errorMessage = "timeout"
                )
            } else {
                val (code, extras) = pair
                if (code == PortQueryReceiver.RESULT_CODE_OK && extras != null) {
                    val json = extras.getString(PortQueryReceiver.EXTRA_ALLOCATIONS).orEmpty()
                    val allocations = parseAllocations(json)
                    WtaAppPortReport(app = app, responded = true, allocations = allocations)
                } else {
                    WtaAppPortReport(
                        app = app,
                        responded = false,
                        allocations = emptyList(),
                        errorMessage = "code=$code"
                    )
                }
            }
        } catch (e: Exception) {
            AppLogger.w(TAG, "queryApp failed for ${app.packageName}: ${e.message}")
            WtaAppPortReport(
                app = app,
                responded = false,
                allocations = emptyList(),
                errorMessage = e.message
            )
        }
    }

    suspend fun releaseRemotePort(
        context: Context,
        packageName: String,
        port: Int
    ): Boolean = withContext(Dispatchers.IO) {
        val deferred = CompletableDeferred<Int>()

        val resultReceiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context, intent: Intent) {
                deferred.complete(resultCode)
            }
        }

        val intent = Intent(PortReleaseReceiver.ACTION_PORT_RELEASE).apply {
            setPackage(packageName)
            component = ComponentName(
                packageName,
                "com.webtoapp.core.port.PortReleaseReceiver"
            )
            putExtra(PortReleaseReceiver.EXTRA_PORT, port)
        }

        return@withContext try {
            context.sendOrderedBroadcast(
                intent, null, resultReceiver, null, 0, null, null
            )
            val code = withTimeoutOrNull(QUERY_TIMEOUT_MS) { deferred.await() }
            code == PortReleaseReceiver.RESULT_CODE_OK
        } catch (e: Exception) {
            AppLogger.w(TAG, "releaseRemotePort failed: ${e.message}")
            false
        }
    }

    private fun parseAllocations(json: String): List<RemoteAllocation> {
        if (json.isBlank()) return emptyList()
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                RemoteAllocation(
                    port = obj.optInt("port"),
                    owner = obj.optString("owner"),
                    range = obj.optString("range"),
                    allocatedAt = obj.optLong("allocatedAt"),
                    pid = obj.optLong("pid", -1L),
                    alive = obj.optBoolean("alive", false)
                )
            }
        } catch (e: Exception) {
            AppLogger.w(TAG, "parseAllocations failed: ${e.message}")
            emptyList()
        }
    }
}
