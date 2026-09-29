package com.webtoapp.core.crypto

import android.annotation.SuppressLint
import android.content.Context
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.core.shell.ShellConfig
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap

@SuppressLint("StaticFieldLeak")
class SecureAssetLoader(private val context: Context) {

    companion object {
        private const val TAG = "SecureAssetLoader"

        @Volatile
        private var instance: SecureAssetLoader? = null

        fun getInstance(context: Context): SecureAssetLoader {
            return instance ?: synchronized(this) {
                instance ?: SecureAssetLoader(context.applicationContext).also { instance = it }
            }
        }
    }

    private val decryptor = AssetDecryptor(context)
    private val integrityChecker = IntegrityChecker(context)
    private val gson = com.webtoapp.util.GsonProvider.gson

    private val cache = ConcurrentHashMap<String, ByteArray>()

    var integrityCheckEnabled = true

    fun loadConfig(): ShellConfig? {
        return try {

            if (integrityCheckEnabled && !integrityChecker.quickCheck()) {
                AppLogger.w(TAG, "完整性检查失败，拒绝加载配置")
                return null
            }

            val configJson = loadAssetAsString(CryptoConstants.CONFIG_FILE)
            gson.fromJson(configJson, ShellConfig::class.java)

        } catch (e: Exception) {
            AppLogger.e(TAG, "加载配置失败", e)
            null
        }
    }

    fun loadHtml(htmlPath: String): String {
        val fullPath = if (htmlPath.startsWith("html/")) htmlPath else "html/$htmlPath"
        return loadAssetAsString(fullPath)
    }

    fun loadAsset(assetPath: String): ByteArray {

        cache[assetPath]?.let { return it }

        val data = decryptor.loadAsset(assetPath)

        if (data.size < 1024 * 1024) {
            cache[assetPath] = data
        }

        return data
    }

    fun loadAssetAsString(assetPath: String): String {
        return String(loadAsset(assetPath), Charsets.UTF_8)
    }

    fun openAsset(assetPath: String): InputStream {
        return loadAsset(assetPath).inputStream()
    }

    fun assetExists(assetPath: String): Boolean {
        return decryptor.assetExists(assetPath)
    }

    fun isEncrypted(assetPath: String): Boolean {
        return decryptor.isEncrypted(assetPath)
    }

    fun checkIntegrity(): IntegrityResult {
        return integrityChecker.check()
    }

    fun clearCache() {
        cache.clear()
        decryptor.clearCache()
    }

    fun preload(assetPaths: List<String>) {
        assetPaths.forEach { path ->
            try {
                loadAsset(path)
            } catch (e: Exception) {
                AppLogger.w(TAG, "预加载失败: $path", e)
            }
        }
    }
}
