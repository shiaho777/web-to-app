package com.webtoapp.core.activation

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import com.webtoapp.core.logging.AppLogger
import java.security.MessageDigest
import java.util.UUID
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object DeviceIdGenerator {

    private const val TAG = "DeviceIdGenerator"
    private const val PREFS_NAME = "device_id_prefs"
    private const val PREVIEW_PREFS_NAME = "preview_device_id_prefs"
    private const val KEY_DEVICE_ID = "device_id"
    private const val KEY_DEVICE_ID_HMAC = "device_id_hmac"
    private const val KEY_DEVICE_ID_PACKAGE = "device_id_package"
    private const val HMAC_SECRET = "WTA_DeviceId_Integrity_2024"
    private const val LEGACY_PACKAGE_MARKER = "<legacy-no-package>"

    @SuppressLint("HardwareIds")
    fun getDeviceId(context: Context): String {
        val packageName = runCatching { context.packageName }.getOrNull().orEmpty()
        return getDeviceId(context, packageName)
    }

    @SuppressLint("HardwareIds")
    fun getDeviceId(context: Context, packageName: String): String {
        val safePackage = packageName.trim()
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val savedId = prefs.getString(KEY_DEVICE_ID, null)
        val savedHmac = prefs.getString(KEY_DEVICE_ID_HMAC, null)
        val savedPackage = prefs.getString(KEY_DEVICE_ID_PACKAGE, null)

        if (!savedId.isNullOrBlank() && !savedHmac.isNullOrBlank()) {
            val expectedHmac = if (savedPackage == null) {
                computeLegacyHmac(savedId)
            } else if (savedPackage == LEGACY_PACKAGE_MARKER) {
                computeLegacyHmac(savedId)
            } else {
                computeHmac(savedId, savedPackage)
            }
            if (constantTimeEquals(savedHmac, expectedHmac)) {

                if (savedPackage == null) {
                    prefs.edit()
                        .putString(KEY_DEVICE_ID_PACKAGE, LEGACY_PACKAGE_MARKER)
                        .apply()
                    AppLogger.i(TAG, "Legacy device ID preserved and marked for package=$safePackage")
                } else if (savedPackage == LEGACY_PACKAGE_MARKER) {
                    AppLogger.i(TAG, "Legacy device ID reused for package=$safePackage")
                }
                return savedId
            }
            AppLogger.w(TAG, "Device ID integrity check failed — regenerating")
        }

        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )

        val deviceId = if (!androidId.isNullOrBlank() && androidId != "9774d56d682e549c") {
            computeDeviceId(androidId, safePackage)
        } else {
            computeDeviceId(UUID.randomUUID().toString().replace("-", ""), safePackage)
        }

        val hmac = computeHmac(deviceId, safePackage)
        prefs.edit()
            .putString(KEY_DEVICE_ID, deviceId)
            .putString(KEY_DEVICE_ID_HMAC, hmac)
            .putString(KEY_DEVICE_ID_PACKAGE, safePackage)
            .apply()

        AppLogger.i(TAG, "Device ID generated and persisted for package=$safePackage")
        return deviceId
    }

    @SuppressLint("HardwareIds")
    fun previewDeviceId(context: Context, packageName: String): String {
        val safePackage = packageName.trim()
        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        val seed = if (!androidId.isNullOrBlank() && androidId != "9774d56d682e549c") {
            androidId
        } else {
            val prefs = context.getSharedPreferences(PREVIEW_PREFS_NAME, Context.MODE_PRIVATE)
            val key = "fallback_$safePackage"
            prefs.getString(key, null) ?: UUID.randomUUID().toString().replace("-", "").also {
                prefs.edit().putString(key, it).apply()
            }
        }
        return computeDeviceId(seed, safePackage)
    }

    fun computeDeviceId(androidId: String, packageName: String): String {
        val input = buildString {
            append(androidId)
            append('|')
            append(packageName.trim())
        }
        return hashString(input)
    }

    private fun hashString(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }.take(32)
    }

    private fun computeHmac(data: String, packageName: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        val keySpec = SecretKeySpec(HMAC_SECRET.toByteArray(), "HmacSHA256")
        mac.init(keySpec)
        val payload = "$data|$packageName".toByteArray()
        val hmacBytes = mac.doFinal(payload)
        return hmacBytes.joinToString("") { "%02x".format(it) }
    }

    private fun computeLegacyHmac(data: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        val keySpec = SecretKeySpec(HMAC_SECRET.toByteArray(), "HmacSHA256")
        mac.init(keySpec)
        val hmacBytes = mac.doFinal(data.toByteArray())
        return hmacBytes.joinToString("") { "%02x".format(it) }
    }

    private fun constantTimeEquals(a: String, b: String): Boolean {
        if (a.length != b.length) return false
        var result = 0
        for (i in a.indices) {
            result = result or (a[i].code xor b[i].code)
        }
        return result == 0
    }
}
