package com.webtoapp.core.dns

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.data.model.DnsConfig
import com.webtoapp.data.model.DnsProvider
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.dnsoverhttps.DnsOverHttps
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.concurrent.ConcurrentHashMap

class DnsManager(private val context: Context) {

    companion object {
        private const val TAG = "DnsManager"
        private const val MAX_CACHE_ENTRIES = 512
        private const val CACHE_TTL_MS = 5 * 60 * 1000L

        fun isDohSupported(): Boolean {
            return try {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
            } catch (e: Exception) {
                AppLogger.w(TAG, "Failed to check DoH support: ${e.message}")
                false
            }
        }
    }

    private val client = OkHttpClient.Builder().build()

    private data class CachedAddresses(val addresses: List<InetAddress>, val expiresAtMs: Long)

    private val dnsCache = object : LinkedHashMap<String, CachedAddresses?>(256, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, CachedAddresses?>?): Boolean =
            size > MAX_CACHE_ENTRIES
    }

    @Volatile
    private var currentConfig: DnsConfig? = null

    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    fun applyDnsConfig(config: DnsConfig) {
        val previousConfig = currentConfig
        currentConfig = config

        if (config.provider == "SYSTEM" || config.effectiveDohUrl.isBlank()) {
            clearDnsConfig()
            return
        }

        AppLogger.d(TAG, "Applying DoH config: provider=${config.provider}, mode=${config.dohMode}")

        if (config.bypassSystemDns) {
            setupBypassSystemDns()
        } else if (previousConfig?.bypassSystemDns == true) {
            clearBypassSystemDns()
        }
    }

    fun clearDnsConfig() {
        currentConfig = null
        synchronized(dnsCache) { dnsCache.clear() }
        clearBypassSystemDns()
        AppLogger.d(TAG, "DNS config cleared, using system default")
    }

    fun getCurrentDohUrl(): String? {
        val config = currentConfig ?: return null
        return config.effectiveDohUrl.takeIf { it.isNotBlank() }
    }

    fun getCurrentConfig(): DnsConfig? = currentConfig

    /**
     * Families the active network has a routable address for. Unknown when the
     * link cannot be read, so callers do not drop a family by mistake.
     */
    internal fun reachableFamilies(): ReachableFamilies {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return ReachableFamilies.UNKNOWN
            val network = cm.activeNetwork ?: return ReachableFamilies.UNKNOWN
            val props = cm.getLinkProperties(network) ?: return ReachableFamilies.UNKNOWN
            familiesFromLinkAddresses(props.linkAddresses.map { it.address })
        } catch (e: Exception) {
            AppLogger.w(TAG, "Failed to read link addresses: ${e.message}")
            ReachableFamilies.UNKNOWN
        }
    }

    fun resolveWithDoh(hostname: String): List<InetAddress> {
        val config = currentConfig ?: throw UnknownHostException("No DoH config set")
        val dohUrl = config.effectiveDohUrl

        if (dohUrl.isBlank()) {
            throw UnknownHostException("DoH URL is blank")
        }

        synchronized(dnsCache) {
            val cached = dnsCache[hostname]
            if (cached != null) {
                if (cached.expiresAtMs > System.currentTimeMillis()) return cached.addresses
                dnsCache.remove(hostname)
            }
        }

        AppLogger.d(TAG, "Resolving $hostname via DoH: $dohUrl")

        val dohDns = DnsOverHttps.Builder()
            .client(client)
            .url(dohUrl.toHttpUrlOrNull() ?: throw UnknownHostException("Invalid DoH URL: $dohUrl"))
            .includeIPv6(true)
            .build()

        val result = try {
            dohDns.lookup(hostname)
        } catch (e: Exception) {
            AppLogger.e(TAG, "DoH resolution failed for $hostname", e)
            if (config.dohMode == "strict") {
                throw UnknownHostException("DoH resolution failed in strict mode: ${e.message}")
            }

            Dns.SYSTEM.lookup(hostname)
        }

        if (result.isNotEmpty()) {
            synchronized(dnsCache) {
                dnsCache[hostname] = CachedAddresses(result, System.currentTimeMillis() + CACHE_TTL_MS)
            }
        }
        return result
    }

    fun createDohOkHttpClient(): OkHttpClient {
        val config = currentConfig ?: return OkHttpClient.Builder().build()
        val dohUrl = config.effectiveDohUrl

        if (dohUrl.isBlank() || config.provider == "SYSTEM") {
            return OkHttpClient.Builder().build()
        }

        val dohDns = DnsOverHttps.Builder()
            .client(client)
            .url(dohUrl.toHttpUrlOrNull() ?: return OkHttpClient.Builder().build())
            .includeIPv6(true)
            .build()

        return OkHttpClient.Builder()
            .dns(dohDns)
            .build()
    }

    private fun setupBypassSystemDns() {
        clearBypassSystemDns()
        AppLogger.i(
            TAG,
            "bypassSystemDns has no effect on the system WebView engine; DoH is enforced through the local DNS proxy bridge. The Gecko engine applies it as TRR-only mode."
        )
    }

    private fun clearBypassSystemDns() {
        networkCallback?.let {
            try {
                val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                cm.unregisterNetworkCallback(it)
            } catch (e: Exception) {
                AppLogger.w(TAG, "Failed to unregister network callback: ${e.message}")
            }
            networkCallback = null
        }
    }

    fun clearCache() {
        synchronized(dnsCache) { dnsCache.clear() }
    }
}
