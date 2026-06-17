package com.webtoapp.clone

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.UUID

class CloneLauncherActivity : Activity() {

    companion object {
        private const val CONFIG_ASSET = "clone_config.json"
        private const val SPLASH_IMAGE_ASSET = "splash_media.png"
        private const val SPLASH_VIDEO_ASSET = "splash_media.mp4"
        private const val PREFS_NAME = "clone_activation"
    }

    private val handler = Handler(Looper.getMainLooper())

    private var config: CloneConfig? = null
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        config = loadConfig()
        if (config == null) {
            launchTargetAndFinish()
            return
        }

        val cfg = config!!
        if (cfg.splashOrientation == "LANDSCAPE") {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }

        val activationAppId = -(kotlin.math.abs(cfg.targetPackage.hashCode().toLong()) + 100L)

        if (cfg.activationEnabled) {
            handleActivation(cfg, activationAppId)
        } else {
            proceedAfterActivation(cfg)
        }
    }

    private fun loadConfig(): CloneConfig? {
        return try {
            val json = assets.open(CONFIG_ASSET).bufferedReader().use { it.readText() }
            val obj = JSONObject(json)
            val dialog = obj.optJSONObject("activationDialog")
            CloneConfig(
                targetPackage = obj.optString("targetPackage", ""),
                splashEnabled = obj.optBoolean("splashEnabled", false),
                splashType = obj.optString("splashType", "IMAGE"),
                splashDuration = obj.optInt("splashDuration", 3),
                splashClickToSkip = obj.optBoolean("splashClickToSkip", true),
                splashOrientation = obj.optString("splashOrientation", "PORTRAIT"),
                splashFillScreen = obj.optBoolean("splashFillScreen", true),
                splashEnableAudio = obj.optBoolean("splashEnableAudio", false),
                splashVideoStartMs = obj.optLong("splashVideoStartMs", 0),
                splashVideoEndMs = obj.optLong("splashVideoEndMs", 5000),
                activationEnabled = obj.optBoolean("activationEnabled", false),
                activationCodes = toStringList(obj.optJSONArray("activationCodes")),
                activationRequireEveryTime = obj.optBoolean("activationRequireEveryTime", false),
                activationDialogTitle = dialog?.optString("title", "") ?: "",
                activationDialogSubtitle = dialog?.optString("subtitle", "") ?: "",
                activationDialogInputLabel = dialog?.optString("inputLabel", "") ?: "",
                activationDialogButtonText = dialog?.optString("buttonText", "") ?: "",
                remoteActivationEnabled = obj.optBoolean("remoteActivationEnabled", false),
                remoteVerifyUrl = obj.optString("remoteVerifyUrl", ""),
                remoteOfflinePolicy = obj.optString("remoteOfflinePolicy", "ALLOW_CACHED"),
                announcementEnabled = obj.optBoolean("announcementEnabled", false),
                announcementTitle = obj.optString("announcementTitle", ""),
                announcementContent = obj.optString("announcementContent", ""),
                announcementContentIsHtml = obj.optBoolean("announcementContentIsHtml", false),
                announcementLinkUrl = obj.optString("announcementLinkUrl", ""),
                announcementLinkText = obj.optString("announcementLinkText", ""),
                originalLauncherActivity = obj.optString("originalLauncherActivity", "")
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun toStringList(arr: org.json.JSONArray?): List<String> {
        if (arr == null) return emptyList()
        return (0 until arr.length()).map { arr.optString(it, "") }
    }

    private fun handleActivation(cfg: CloneConfig, appId: Long) {
        if (cfg.activationRequireEveryTime) {
            clearActivation(appId)
            showActivationDialog(cfg, appId)
            return
        }

        if (cfg.remoteActivationEnabled) {
            Thread {
                val activated = isActivated(appId) && checkRemoteActivation(cfg)
                handler.post {
                    if (activated) {
                        proceedAfterActivation(cfg)
                    } else {
                        showActivationDialog(cfg, appId)
                    }
                }
            }.start()
        } else {
            if (isActivated(appId)) {
                proceedAfterActivation(cfg)
            } else {
                showActivationDialog(cfg, appId)
            }
        }
    }

    private fun showActivationDialog(cfg: CloneConfig, appId: Long) {
        val title = cfg.activationDialogTitle.ifBlank { "Activation Required" }
        val subtitle = cfg.activationDialogSubtitle.ifBlank { "Enter activation code to continue" }
        val inputLabel = cfg.activationDialogInputLabel.ifBlank { "Activation Code" }
        val buttonText = cfg.activationDialogButtonText.ifBlank { "Activate" }

        val input = EditText(this).apply {
            hint = inputLabel
            setSingleLine(true)
            val pad = dp(16)
            setPadding(pad, pad, pad, pad)
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val pad = dp(20)
            setPadding(pad, dp(8), pad, dp(8))
            addView(TextView(this@CloneLauncherActivity).apply {
                text = subtitle
                textSize = 14f
            })
            addView(input)
        }

        val scrollView = ScrollView(this).apply { addView(container) }

        val dialog = AlertDialog.Builder(this)
            .setTitle(title)
            .setView(scrollView)
            .setCancelable(false)
            .setPositiveButton(buttonText) { _, _ -> }
            .setNegativeButton("Cancel") { _, _ -> finish() }
            .create()

        dialog.setCanceledOnTouchOutside(false)
        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val code = input.text.toString().trim()
            if (code.isBlank()) {
                input.error = "Please enter activation code"
                return@setOnClickListener
            }
            Thread {
                val result = verifyCode(cfg, code)
                handler.post {
                    if (result) {
                        saveActivation(appId)
                        dialog.dismiss()
                        proceedAfterActivation(cfg)
                    } else {
                        input.error = "Invalid activation code"
                    }
                }
            }.start()
        }
    }

    private fun verifyCode(cfg: CloneConfig, inputCode: String): Boolean {
        if (cfg.remoteActivationEnabled) {
            return verifyRemoteCode(cfg, inputCode)
        }
        val normalized = normalizeCode(inputCode)
        return cfg.activationCodes.any { validCode ->
            val normalizedValid = normalizeCode(validCode)
            constantTimeEquals(normalized, normalizedValid) ||
                constantTimeEquals(sha256(normalized), normalizedValid)
        }
    }

    private fun verifyRemoteCode(cfg: CloneConfig, inputCode: String): Boolean {
        return try {
            val deviceId = generateDeviceId()
            val normalized = normalizeCode(inputCode)
            val conn = (URL(cfg.remoteVerifyUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                connectTimeout = 10000
                readTimeout = 10000
            }
            val requestBody = """{"code":"$normalized","deviceId":"$deviceId","packageName":"$packageName"}"""
            conn.outputStream.use { it.write(requestBody.toByteArray()) }
            if (conn.responseCode == 200) {
                val response = conn.inputStream.bufferedReader().use { it.readText() }
                val result = JSONObject(response)
                result.optBoolean("success", false)
            } else {
                cfg.remoteOfflinePolicy == "ALLOW"
            }
        } catch (e: Exception) {
            cfg.remoteOfflinePolicy == "ALLOW" || cfg.remoteOfflinePolicy == "ALLOW_CACHED"
        }
    }

    private fun checkRemoteActivation(cfg: CloneConfig): Boolean {
        return try {
            val deviceId = generateDeviceId()
            val conn = (URL(cfg.remoteVerifyUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                connectTimeout = 5000
                readTimeout = 5000
            }
            val requestBody = """{"code":"","deviceId":"$deviceId","packageName":"$packageName","checkOnly":true}"""
            conn.outputStream.use { it.write(requestBody.toByteArray()) }
            if (conn.responseCode == 200) {
                val response = conn.inputStream.bufferedReader().use { it.readText() }
                val result = JSONObject(response)
                result.optBoolean("success", false)
            } else {
                cfg.remoteOfflinePolicy == "ALLOW_CACHED"
            }
        } catch (e: Exception) {
            cfg.remoteOfflinePolicy == "ALLOW_CACHED" || cfg.remoteOfflinePolicy == "ALLOW"
        }
    }

    private fun proceedAfterActivation(cfg: CloneConfig) {
        if (cfg.announcementEnabled && cfg.announcementTitle.isNotBlank()) {
            showAnnouncement(cfg)
        } else {
            showSplash(cfg)
        }
    }

    private fun showAnnouncement(cfg: CloneConfig) {
        val content = cfg.announcementContent
        val title = cfg.announcementTitle

        val textView = TextView(this).apply {
            text = if (cfg.announcementContentIsHtml) {
                @Suppress("DEPRECATION")
                android.text.Html.fromHtml(content)
            } else {
                content
            }
            textSize = 14f
            val pad = dp(20)
            setPadding(pad, pad, pad, pad)
        }

        val scrollView = ScrollView(this).apply { addView(textView) }

        val builder = AlertDialog.Builder(this)
            .setTitle(title)
            .setView(scrollView)
            .setCancelable(false)

        if (cfg.announcementLinkText.isNotBlank() && cfg.announcementLinkUrl.isNotBlank()) {
            builder.setPositiveButton(cfg.announcementLinkText) { _, _ ->
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(cfg.announcementLinkUrl)))
                } catch (e: Exception) { }
                showSplash(cfg)
            }
            builder.setNegativeButton("Close") { _, _ -> showSplash(cfg) }
        } else {
            builder.setPositiveButton("OK") { _, _ -> showSplash(cfg) }
        }

        builder.create().show()
    }

    private fun showSplash(cfg: CloneConfig) {
        if (!cfg.splashEnabled) {
            launchTargetAndFinish()
            return
        }

        val splashFile = extractSplashMedia(cfg.splashType) ?: run {
            launchTargetAndFinish()
            return
        }

        val rootView = FrameLayout(this).apply { setBackgroundColor(Color.BLACK) }

        if (cfg.splashType == "IMAGE") {
            val imageView = ImageView(this).apply {
                scaleType = if (cfg.splashFillScreen) ImageView.ScaleType.CENTER_CROP else ImageView.ScaleType.FIT_CENTER
                setImageURI(Uri.fromFile(splashFile))
            }
            rootView.addView(imageView, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            ))
        } else {
            val surfaceView = SurfaceView(this)
            rootView.addView(surfaceView, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            ))
            surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceCreated(holder: SurfaceHolder) {
                    try {
                        mediaPlayer = MediaPlayer().apply {
                            setDataSource(splashFile.absolutePath)
                            setSurface(holder.surface)
                            val volume = if (cfg.splashEnableAudio) 1f else 0f
                            setVolume(volume, volume)
                            isLooping = false
                            setOnPreparedListener { mp ->
                                mp.seekTo(cfg.splashVideoStartMs.toInt())
                                mp.start()
                                if (cfg.splashVideoEndMs > cfg.splashVideoStartMs) {
                                    handler.postDelayed({
                                        if (mp.isPlaying) mp.pause()
                                        launchTargetAndFinish()
                                    }, cfg.splashVideoEndMs - cfg.splashVideoStartMs)
                                }
                            }
                            setOnCompletionListener { launchTargetAndFinish() }
                            prepareAsync()
                        }
                    } catch (e: Exception) {
                        launchTargetAndFinish()
                    }
                }
                override fun surfaceChanged(h: SurfaceHolder, f: Int, w: Int, ht: Int) {}
                override fun surfaceDestroyed(h: SurfaceHolder) {
                    mediaPlayer?.release()
                    mediaPlayer = null
                }
            })
        }

        val countdownText = TextView(this).apply {
            setTextColor(Color.WHITE)
            textSize = 12f
            setPadding(dp(12), dp(6), dp(12), dp(6))
            setBackgroundColor(0x99000000.toInt())
            visibility = View.GONE
        }
        rootView.addView(countdownText, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            android.view.Gravity.TOP or android.view.Gravity.END
        ).apply { setMargins(0, dp(16), dp(16), 0) })

        if (cfg.splashClickToSkip) {
            rootView.setOnClickListener { launchTargetAndFinish() }
        }

        setContentView(rootView)

        if (cfg.splashType == "IMAGE") {
            var countdown = cfg.splashDuration
            countdownText.visibility = View.VISIBLE
            countdownText.text = "${countdown}s"
            handler.post(object : Runnable {
                override fun run() {
                    countdown--
                    if (countdown > 0) {
                        countdownText.text = "${countdown}s"
                        handler.postDelayed(this, 1000)
                    } else {
                        launchTargetAndFinish()
                    }
                }
            })
        }
    }

    private fun extractSplashMedia(splashType: String): File? {
        val assetName = if (splashType == "VIDEO") SPLASH_VIDEO_ASSET else SPLASH_IMAGE_ASSET
        return try {
            val cacheFile = File(cacheDir, assetName)
            assets.open(assetName).use { input ->
                FileOutputStream(cacheFile).use { output -> input.copyTo(output) }
            }
            cacheFile
        } catch (e: Exception) {
            null
        }
    }

    private fun launchTargetAndFinish() {
        try {
            val cfg = config
            var launched = false

            if (cfg != null && cfg.originalLauncherActivity.isNotBlank()) {
                try {
                    val intent = Intent().apply {
                        setClassName(packageName, cfg.originalLauncherActivity)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    }
                    startActivity(intent)
                    launched = true
                } catch (e: Exception) {
                    android.util.Log.d("CloneLauncher", "Direct launch failed: ${e.message}, trying getLaunchIntentForPackage")
                }
            }

            if (!launched) {
                val launchIntent = packageManager.getLaunchIntentForPackage(cfg?.targetPackage ?: "")
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(launchIntent)
                }
            }
        } catch (e: Exception) { }
        finishAndRemoveTask()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        handler.removeCallbacksAndMessages(null)
    }

    private fun dp(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    private fun isActivated(appId: Long): Boolean {
        return getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .getBoolean("activated_$appId", false)
    }

    private fun saveActivation(appId: Long) {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putBoolean("activated_$appId", true)
            .putLong("activated_time_$appId", System.currentTimeMillis())
            .apply()
    }

    private fun clearActivation(appId: Long) {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit()
            .remove("activated_$appId")
            .remove("activated_time_$appId")
            .apply()
    }

    private fun generateDeviceId(): String {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        var id = prefs.getString("device_id", null)
        if (id == null) {
            id = UUID.randomUUID().toString()
            prefs.edit().putString("device_id", id).apply()
        }
        return id
    }

    private fun normalizeCode(code: String): String {
        return code.replace("-", "").replace(" ", "").uppercase().trim()
    }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest((input + "WebToApp_Salt_2024").toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
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

data class CloneConfig(
    val targetPackage: String = "",
    val splashEnabled: Boolean = false,
    val splashType: String = "IMAGE",
    val splashDuration: Int = 3,
    val splashClickToSkip: Boolean = true,
    val splashOrientation: String = "PORTRAIT",
    val splashFillScreen: Boolean = true,
    val splashEnableAudio: Boolean = false,
    val splashVideoStartMs: Long = 0,
    val splashVideoEndMs: Long = 5000,
    val activationEnabled: Boolean = false,
    val activationCodes: List<String> = emptyList(),
    val activationRequireEveryTime: Boolean = false,
    val activationDialogTitle: String = "",
    val activationDialogSubtitle: String = "",
    val activationDialogInputLabel: String = "",
    val activationDialogButtonText: String = "",
    val remoteActivationEnabled: Boolean = false,
    val remoteVerifyUrl: String = "",
    val remoteOfflinePolicy: String = "ALLOW_CACHED",
    val announcementEnabled: Boolean = false,
    val announcementTitle: String = "",
    val announcementContent: String = "",
    val announcementContentIsHtml: Boolean = false,
    val announcementLinkUrl: String = "",
    val announcementLinkText: String = "",
    val originalLauncherActivity: String = ""
)
