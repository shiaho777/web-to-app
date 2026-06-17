package com.webtoapp.core.appmodifier

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.webtoapp.core.apkbuilder.ArscEditor
import com.webtoapp.core.apkbuilder.AxmlEditor
import com.webtoapp.core.apkbuilder.AxmlRebuilder
import com.webtoapp.core.apkbuilder.JarSigner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.ui.splash.SplashLauncherActivity
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import com.webtoapp.core.apkbuilder.ZipUtils
import com.webtoapp.util.AppConstants

class AppCloner(private val context: Context) {

    companion object {
        private val SANITIZE_FILENAME_REGEX = AppConstants.SANITIZE_FILENAME_REGEX
    }

    private val axmlEditor = AxmlEditor()
    private val axmlRebuilder = AxmlRebuilder()
    private val arscEditor = ArscEditor()
    private val signer = JarSigner(context)

    private val outputDir = File(context.getExternalFilesDir(null), "cloned_apks").apply { mkdirs() }
    private val tempDir = File(context.cacheDir, "clone_temp").apply { mkdirs() }

    private val ICON_PATHS = listOf(
        "res/mipmap-mdpi-v4/ic_launcher.png" to 48,
        "res/mipmap-hdpi-v4/ic_launcher.png" to 72,
        "res/mipmap-xhdpi-v4/ic_launcher.png" to 96,
        "res/mipmap-xxhdpi-v4/ic_launcher.png" to 144,
        "res/mipmap-xxxhdpi-v4/ic_launcher.png" to 192
    )

    private val ROUND_ICON_PATHS = listOf(
        "res/mipmap-mdpi-v4/ic_launcher_round.png" to 48,
        "res/mipmap-hdpi-v4/ic_launcher_round.png" to 72,
        "res/mipmap-xhdpi-v4/ic_launcher_round.png" to 96,
        "res/mipmap-xxhdpi-v4/ic_launcher_round.png" to 144,
        "res/mipmap-xxxhdpi-v4/ic_launcher_round.png" to 192
    )

    suspend fun createModifiedShortcut(
        config: AppModifyConfig,
        onProgress: (Int, String) -> Unit = { _, _ -> }
    ): AppModifyResult = withContext(Dispatchers.IO) {

        val safeProgress: suspend (Int, String) -> Unit = { p, t ->
            withContext(Dispatchers.Main) {
                try {
                    onProgress(p, t)
                } catch (e: Exception) {
                    AppLogger.e("AppCloner", "Progress callback error: ${e.message}")
                }
            }
        }

        try {
            safeProgress(10, "准备图标...")

            val iconBitmap = prepareIcon(config)
            val icon = IconCompat.createWithBitmap(iconBitmap)

            safeProgress(50, "创建快捷方式...")

            val payload = AppModifyPayload(
                targetPackage = config.originalApp.packageName,
                splashEnabled = config.splashEnabled,
                splashConfig = config.splashConfig,
                activationEnabled = config.activationEnabled,
                activationCodes = config.activationCodes,
                activationRequireEveryTime = config.activationRequireEveryTime,
                activationDialogConfig = config.activationDialogConfig,
                activationRemoteConfig = config.activationRemoteConfig,
                announcementEnabled = config.announcementEnabled,
                announcement = config.announcement
            )

            val needsSplashLauncher = payload.needsLauncher() &&
                run {

                    val sp = config.splashConfig.mediaPath
                    val splashOk = !config.splashEnabled ||
                        (!sp.isNullOrBlank() && java.io.File(sp).exists())
                    splashOk
                }

            AppLogger.d(
                "AppCloner",
                "Shortcut config: splashEnabled=${config.splashEnabled}, " +
                    "activationEnabled=${config.activationEnabled} (${config.activationCodes.size} codes), " +
                    "announcementEnabled=${config.announcementEnabled}, " +
                    "needsSplashLauncher=$needsSplashLauncher"
            )

            val launchIntent = if (needsSplashLauncher) {

                Intent(context, SplashLauncherActivity::class.java).apply {

                    action = Intent.ACTION_VIEW
                    putExtra(SplashLauncherActivity.EXTRA_PAYLOAD_JSON, payload.toJson())
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
            } else {

                context.packageManager.getLaunchIntentForPackage(config.originalApp.packageName)?.apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                } ?: return@withContext AppModifyResult.Error("无法获取应用启动 Intent")
            }

            if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
                return@withContext AppModifyResult.Error("当前启动器不支持创建快捷方式")
            }

            val shortcutId = "modified_${config.originalApp.packageName}_${System.currentTimeMillis()}"
            val shortcutInfo = ShortcutInfoCompat.Builder(context, shortcutId)
                .setShortLabel(config.newAppName.take(10))
                .setLongLabel(config.newAppName.take(25))
                .setIcon(icon)
                .setIntent(launchIntent)
                .build()

            safeProgress(80, "请求创建...")

            val result = withContext(Dispatchers.Main) {
                ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null)
            }

            iconBitmap.recycle()

            if (result) {
                safeProgress(100, "Done")
                AppModifyResult.ShortcutSuccess
            } else {
                AppModifyResult.Error("创建快捷方式失败，请检查权限设置")
            }

        } catch (e: Exception) {
            AppLogger.e("AppCloner", "Failed to create shortcut", e)
            AppLogger.e("AppCloner", "Operation failed", e)
            AppModifyResult.Error(e.message ?: "创建快捷方式失败")
        }
    }

    private fun prepareIcon(config: AppModifyConfig): Bitmap {

        config.newIconPath?.let { path ->
            loadBitmapFromPath(path)?.let { return it }
        }

        config.originalApp.icon?.let { drawable ->
            return drawableToBitmap(drawable)
        }

        return createDefaultIcon()
    }

    private fun loadBitmapFromPath(path: String): Bitmap? {
        return try {
            when {
                path.startsWith("/") -> BitmapFactory.decodeFile(path)
                path.startsWith("content://") || path.startsWith("file://") -> {
                    context.contentResolver.openInputStream(Uri.parse(path))?.use {
                        BitmapFactory.decodeStream(it)
                    }
                }
                else -> BitmapFactory.decodeFile(path)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 192
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 192

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
    }

    private fun createDefaultIcon(): Bitmap {
        val size = 192
        return Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).apply {
            val canvas = Canvas(this)
            canvas.drawColor(0xFF6200EE.toInt())
        }
    }

    private val manifestRewriter = CloneManifestRewriter()

    suspend fun cloneAndInstall(
        config: AppModifyConfig,
        onProgress: (Int, String) -> Unit = { _, _ -> }
    ): AppModifyResult = withContext(Dispatchers.IO) {

        val safeProgress: suspend (Int, String) -> Unit = { p, t ->
            withContext(Dispatchers.Main) {
                try {
                    onProgress(p, t)
                } catch (e: Exception) {
                    AppLogger.e("AppCloner", "Progress callback error: ${e.message}")
                }
            }
        }

        try {
            AppLogger.d("AppCloner", "Starting clone: ${config.originalApp.packageName}")
            safeProgress(0, "准备克隆...")

            val sourceApk = File(config.originalApp.apkPath)
            if (!sourceApk.exists()) {
                AppLogger.e("AppCloner", "Source APK not found: ${config.originalApp.apkPath}")
                return@withContext AppModifyResult.Error("无法访问原应用 APK")
            }

            AppLogger.d("AppCloner", "Source APK size: ${sourceApk.length()} bytes")

            val newPackageName = generateClonePackageName(config.originalApp.packageName)
            AppLogger.d("AppCloner", "New package name: $newPackageName")

            val hasModifications = CloneConfigBuilder.hasAnyModification(config)
            AppLogger.d("AppCloner", "Has modifications: $hasModifications")

            safeProgress(10, "复制 APK...")

            val unsignedApk = File(tempDir, "clone_unsigned.apk")
            val signedApk = File(outputDir, "${sanitizeFileName(config.newAppName)}_clone.APK")

            unsignedApk.delete()
            signedApk.delete()

            safeProgress(20, "修改包名和应用名...")

            var iconBitmap: Bitmap? = null
            try {
                iconBitmap = config.newIconPath?.let { loadBitmapFromPath(it) }
                    ?: config.originalApp.icon?.let { drawableToBitmap(it) }
            } catch (e: Exception) {
                AppLogger.e("AppCloner", "Failed to load icon: ${e.message}")
            }

            val cloneHostDex = loadCloneHostDex()
            if (hasModifications && cloneHostDex == null) {
                AppLogger.w("AppCloner", "Modifications requested but clone_host.dex not found; proceeding without injection")
            }

            val splashMediaPath = if (CloneConfigBuilder.needsSplashMedia(config)) {
                CloneConfigBuilder.getSplashMediaPath(config)
            } else null

            val configJson = if (hasModifications) {
                CloneConfigBuilder.buildJson(config)
            } else null

            val splashType = CloneConfigBuilder.getSplashType(config)

            AppLogger.d("AppCloner", "Modifying APK with injection: dex=${cloneHostDex != null}, config=${configJson != null}, splash=${splashMediaPath != null}")
            modifyApk(
                sourceApk = sourceApk,
                outputApk = unsignedApk,
                originalPackageName = config.originalApp.packageName,
                newPackageName = newPackageName,
                originalAppName = config.originalApp.appName,
                newAppName = config.newAppName,
                iconBitmap = iconBitmap,
                injectDex = cloneHostDex,
                injectConfigJson = configJson,
                injectSplashMediaPath = splashMediaPath,
                injectSplashType = splashType,
                useManifestRewriter = hasModifications && cloneHostDex != null
            ) { progress ->

            }

            AppLogger.d("AppCloner", "APK modification complete, size: ${unsignedApk.length()} bytes")

            iconBitmap?.recycle()

            safeProgress(70, "签名 APK...")

            AppLogger.d("AppCloner", "Signing...")
            val signSuccess = signer.sign(unsignedApk, signedApk)
            if (!signSuccess) {
                AppLogger.e("AppCloner", "Signing failed")
                unsignedApk.delete()
                return@withContext AppModifyResult.Error("APK 签名失败")
            }

            AppLogger.d("AppCloner", "Signing complete, final size: ${signedApk.length()} bytes")

            debugApkStructure(signedApk)

            safeProgress(90, "准备安装...")

            unsignedApk.delete()

            withContext(Dispatchers.Main) {
                installApk(signedApk)
            }

            safeProgress(100, "Done")
            AppModifyResult.CloneSuccess(signedApk.absolutePath)

        } catch (e: Exception) {
            AppLogger.e("AppCloner", "Cloning failed", e)
            AppLogger.e("AppCloner", "Operation failed", e)
            AppModifyResult.Error(e.message ?: "克隆失败: ${e.javaClass.simpleName}")
        }
    }

    private fun loadCloneHostDex(): ByteArray? {
        return try {
            context.assets.open("clone_host/clone_host.dex").use { it.readBytes() }
        } catch (e: Exception) {
            AppLogger.w("AppCloner", "clone_host.dex asset not found: ${e.message}")
            null
        }
    }

    private fun generateClonePackageName(originalPackageName: String): String {

        val maxLen = originalPackageName.length.coerceAtMost(128)

        val timestamp = System.currentTimeMillis()
        val random = (0..9999).random()
        val uniqueId = ((timestamp xor random.toLong()) and 0x7FFFFFFF).toString(36)

        val suffixLen = (maxLen - 2).coerceAtLeast(1)
        val rawSuffix = if (uniqueId.length >= suffixLen) {
            uniqueId.take(suffixLen)
        } else {
            uniqueId.padEnd(suffixLen, '0')
        }

        val normalizedSuffix = normalizePackageSegment(rawSuffix)

        return "c.$normalizedSuffix"
    }

    private fun normalizePackageSegment(segment: String): String {
        if (segment.isEmpty()) return "a"
        val chars = segment.lowercase().toCharArray()
        chars[0] = when {
            chars[0] in 'a'..'z' -> chars[0]
            chars[0] in '0'..'9' -> ('a' + (chars[0] - '0'))
            else -> 'a'
        }
        return String(chars)
    }

    private fun modifyApk(
        sourceApk: File,
        outputApk: File,
        originalPackageName: String,
        newPackageName: String,
        originalAppName: String,
        newAppName: String,
        iconBitmap: Bitmap?,
        injectDex: ByteArray? = null,
        injectConfigJson: String? = null,
        injectSplashMediaPath: String? = null,
        injectSplashType: String = "IMAGE",
        useManifestRewriter: Boolean = false,
        onProgress: (Int) -> Unit = {}
    ) {
        var originalLauncherActivity: String? = null

        ZipFile(sourceApk).use { zipIn ->
            ZipOutputStream(FileOutputStream(outputApk)).use { zipOut ->
                val entries = zipIn.entries().toList()
                    .sortedWith(compareBy<ZipEntry> { it.name != "resources.arsc" })
                val entryNames = entries.map { it.name }.toSet()

                val existingDexNumbers = entryNames.mapNotNull { name ->
                    if (name.matches(Regex("^classes\\d*\\.dex$"))) {
                        if (name == "classes.dex") 1
                        else name.removePrefix("classes").removeSuffix(".dex").toIntOrNull()
                    } else null
                }.sorted()
                val nextDexNumber = (existingDexNumbers.maxOrNull() ?: 0) + 1
                val newDexName = "classes${nextDexNumber}.dex"
                AppLogger.d("AppCloner", "Injecting DEX as: $newDexName (existing: $existingDexNumbers)")

                val allEntryCount = entries.size + (if (injectDex != null) 1 else 0)
                var processedCount = 0

                entries.forEach { entry ->
                    processedCount++
                    onProgress((processedCount * 100) / allEntryCount)

                    when {

                        entry.name.startsWith("META-INF/") &&
                        (entry.name.endsWith(".SF") || entry.name.endsWith(".RSA") ||
                         entry.name.endsWith(".DSA") || entry.name == "META-INF/MANIFEST.MF") -> {
                        }

                        entry.name == "AndroidManifest.xml" -> {
                            try {
                                val originalData = zipIn.getInputStream(entry).readBytes()
                                AppLogger.d("AppCloner", "AndroidManifest.xml original size: ${originalData.size} bytes")

                                val modifiedData: ByteArray
                                if (useManifestRewriter) {
                                    AppLogger.d("AppCloner", "Using CloneManifestRewriter for injection")
                                    val rewriteResult = manifestRewriter.rewrite(originalData, originalPackageName, newPackageName)
                                    modifiedData = rewriteResult.axmlData
                                    originalLauncherActivity = rewriteResult.originalLauncherActivity
                                    AppLogger.d("AppCloner", "Original launcher activity: $originalLauncherActivity")
                                } else if (originalPackageName == "com.webtoapp") {
                                    modifiedData = axmlEditor.modifyPackageName(originalData, newPackageName)
                                } else {
                                    modifiedData = axmlRebuilder.expandAndModify(
                                        originalData,
                                        originalPackageName,
                                        newPackageName
                                    )
                                }

                                AppLogger.d("AppCloner", "AndroidManifest.xml size after modification: ${modifiedData.size} bytes")
                                writeEntryDeflated(zipOut, entry.name, modifiedData)
                            } catch (e: Exception) {
                                AppLogger.e("AppCloner", "Failed to modify AndroidManifest.xml: ${e.message}", e)
                                copyEntry(zipIn, zipOut, entry)
                            }
                        }

                        entry.name == "resources.arsc" -> {
                            try {
                                val originalData = zipIn.getInputStream(entry).readBytes()
                                AppLogger.d("AppCloner", "resources.arsc original size: ${originalData.size} bytes")

                                var modifiedData = arscEditor.modifyAppName(
                                    originalData, originalAppName, newAppName
                                )
                                modifiedData = arscEditor.modifyIconPathsToPng(modifiedData)

                                AppLogger.d("AppCloner", "resources.arsc size after modification: ${modifiedData.size} bytes")
                                writeEntryStored(zipOut, entry.name, modifiedData)
                            } catch (e: Exception) {
                                AppLogger.e("AppCloner", "Failed to modify resources.arsc: ${e.message}", e)
                                copyEntry(zipIn, zipOut, entry)
                            }
                        }

                        injectConfigJson != null && entry.name == "assets/clone_config.json" -> {
                            AppLogger.d("AppCloner", "Replacing existing clone_config.json")
                            writeEntryDeflated(zipOut, entry.name, injectConfigJson.toByteArray(Charsets.UTF_8))
                        }

                        injectSplashMediaPath != null && (
                            entry.name == "assets/splash_media.png" ||
                            entry.name == "assets/splash_media.mp4"
                        ) -> {
                            AppLogger.d("AppCloner", "Skipping existing splash media: ${entry.name}")
                        }

                        iconBitmap != null && isIconEntry(entry.name) -> {
                            replaceIconEntry(zipOut, entry.name, iconBitmap)
                        }

                        else -> {
                            copyEntry(zipIn, zipOut, entry)
                        }
                    }
                }

                if (injectDex != null) {
                    AppLogger.d("AppCloner", "Injecting DEX: $newDexName (${injectDex.size} bytes)")
                    writeEntryDeflated(zipOut, newDexName, injectDex)
                    processedCount++
                    onProgress((processedCount * 100) / allEntryCount)
                }

                if (injectConfigJson != null) {
                    val finalConfigJson = if (originalLauncherActivity != null) {
                        try {
                            val obj = com.google.gson.JsonParser.parseString(injectConfigJson).asJsonObject
                            obj.addProperty("originalLauncherActivity", originalLauncherActivity)
                            obj.toString()
                        } catch (e: Exception) {
                            injectConfigJson
                        }
                    } else {
                        injectConfigJson
                    }
                    AppLogger.d("AppCloner", "Injecting clone_config.json (${finalConfigJson.length} chars)")
                    writeEntryDeflated(zipOut, "assets/clone_config.json", finalConfigJson.toByteArray(Charsets.UTF_8))
                }

                if (injectSplashMediaPath != null) {
                    val splashFile = File(injectSplashMediaPath)
                    if (splashFile.exists()) {
                        val splashAssetName = if (injectSplashType == "VIDEO") "assets/splash_media.mp4" else "assets/splash_media.png"
                        AppLogger.d("AppCloner", "Injecting splash media: $splashAssetName from ${splashFile.absolutePath} (${splashFile.length()} bytes)")
                        val splashBytes = splashFile.readBytes()
                        if (injectSplashType == "VIDEO") {
                            writeEntryStored(zipOut, splashAssetName, splashBytes)
                        } else {
                            writeEntryDeflated(zipOut, splashAssetName, splashBytes)
                        }
                    } else {
                        AppLogger.w("AppCloner", "Splash media file not found: ${splashFile.absolutePath}")
                    }
                }

                if (iconBitmap != null &&
                    entryNames.contains("res/drawable/ic_launcher_foreground.xml")
                ) {
                    addAdaptiveIconPngs(zipOut, iconBitmap, entryNames)
                }
            }
        }
    }

    private fun isIconEntry(entryName: String): Boolean {

        if (ICON_PATHS.any { it.first == entryName } ||
            ROUND_ICON_PATHS.any { it.first == entryName }) {
            return true
        }

        val iconPatterns = listOf(
            "ic_launcher.png",
            "ic_launcher_round.png",
            "ic_launcher_foreground.png",
            "ic_launcher_background.png"
        )
        return iconPatterns.any { pattern ->
            entryName.endsWith(pattern) &&
            (entryName.contains("mipmap") || entryName.contains("drawable"))
        }
    }

    private fun replaceIconEntry(zipOut: ZipOutputStream, entryName: String, bitmap: Bitmap) {

        var size = ICON_PATHS.find { it.first == entryName }?.second
            ?: ROUND_ICON_PATHS.find { it.first == entryName }?.second

        if (size == null) {
            size = when {
                entryName.contains("xxxhdpi") -> 192
                entryName.contains("xxhdpi") -> 144
                entryName.contains("xhdpi") -> 96
                entryName.contains("hdpi") -> 72
                entryName.contains("mdpi") -> 48
                entryName.contains("ldpi") -> 36
                else -> 96
            }
        }

        val iconBytes = when {

            entryName.contains("round") -> {
                createRoundIcon(bitmap, size)
            }

            entryName.contains("foreground") -> {
                createAdaptiveForegroundIcon(bitmap, size)
            }

            else -> {
                scaleBitmapToPng(bitmap, size)
            }
        }

        writeEntryStored(zipOut, entryName, iconBytes)
    }

    private fun createAdaptiveForegroundIcon(bitmap: Bitmap, size: Int): ByteArray {
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val safeZoneSize = (size * 72f / 108f).toInt()
        val padding = (size - safeZoneSize) / 2f

        val scale = Math.min(safeZoneSize.toFloat() / bitmap.width, safeZoneSize.toFloat() / bitmap.height)
        val scaledWidth = (bitmap.width * scale).toInt()
        val scaledHeight = (bitmap.height * scale).toInt()

        val left = padding + (safeZoneSize - scaledWidth) / 2f
        val top = padding + (safeZoneSize - scaledHeight) / 2f

        val destRect = android.graphics.RectF(left, top, left + scaledWidth, top + scaledHeight)
        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
        }

        canvas.drawBitmap(bitmap, null, destRect, paint)

        val baos = ByteArrayOutputStream()
        output.compress(Bitmap.CompressFormat.PNG, 100, baos)
        output.recycle()
        return baos.toByteArray()
    }

    private fun scaleBitmapToPng(bitmap: Bitmap, size: Int): ByteArray {
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val scale = Math.min(size.toFloat() / bitmap.width, size.toFloat() / bitmap.height)
        val scaledWidth = (bitmap.width * scale).toInt()
        val scaledHeight = (bitmap.height * scale).toInt()

        val left = (size - scaledWidth) / 2f
        val top = (size - scaledHeight) / 2f

        val destRect = android.graphics.RectF(left, top, left + scaledWidth, top + scaledHeight)
        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
        }

        canvas.drawBitmap(bitmap, null, destRect, paint)

        val baos = ByteArrayOutputStream()
        output.compress(Bitmap.CompressFormat.PNG, 100, baos)
        output.recycle()
        return baos.toByteArray()
    }

    private fun createRoundIcon(bitmap: Bitmap, size: Int): ByteArray {
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
        }

        val rect = android.graphics.RectF(0f, 0f, size.toFloat(), size.toFloat())
        canvas.drawOval(rect, paint)

        paint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)

        val scale = Math.min(size.toFloat() / bitmap.width, size.toFloat() / bitmap.height)
        val scaledWidth = (bitmap.width * scale).toInt()
        val scaledHeight = (bitmap.height * scale).toInt()

        val left = (size - scaledWidth) / 2f
        val top = (size - scaledHeight) / 2f

        val destRect = android.graphics.RectF(left, top, left + scaledWidth, top + scaledHeight)
        canvas.drawBitmap(bitmap, null, destRect, paint)

        val baos = ByteArrayOutputStream()
        output.compress(Bitmap.CompressFormat.PNG, 100, baos)
        output.recycle()
        return baos.toByteArray()
    }

    private fun writeEntryDeflated(zipOut: ZipOutputStream, name: String, data: ByteArray) {
        ZipUtils.writeEntryDeflated(zipOut, name, data)
    }

    private fun writeEntryStored(zipOut: ZipOutputStream, name: String, data: ByteArray) {
        ZipUtils.writeEntryStored(zipOut, name, data)
    }

    private fun copyEntry(zipIn: ZipFile, zipOut: ZipOutputStream, entry: ZipEntry) {
        ZipUtils.copyEntryPreserveMethod(zipIn, zipOut, entry)
    }

    private fun addAdaptiveIconPngs(
        zipOut: ZipOutputStream,
        bitmap: Bitmap,
        existingEntryNames: Set<String>
    ) {
        val foregroundPng = "res/drawable/ic_launcher_foreground.png"
        if (!existingEntryNames.contains(foregroundPng)) {
            val iconBytes = scaleBitmapToPng(bitmap, 108)
            writeEntryDeflated(zipOut, foregroundPng, iconBytes)
        }
    }

    private fun sanitizeFileName(name: String): String {
        return name.replace(SANITIZE_FILENAME_REGEX, "_").take(50)
    }

    private fun debugApkStructure(apkFile: File) {
        try {
            val pm = context.packageManager
            val flags = PackageManager.GET_ACTIVITIES or
                    PackageManager.GET_SERVICES or
                    PackageManager.GET_PROVIDERS

            val info = pm.getPackageArchiveInfo(apkFile.absolutePath, flags)

            if (info == null) {
                AppLogger.e("AppCloner", "getPackageArchiveInfo returned null; can't parse APK: ${apkFile.absolutePath}")
            } else {
                val appInfo = info.applicationInfo
                val flagsApp = appInfo?.flags ?: 0
                val isDebuggable = (flagsApp and ApplicationInfo.FLAG_DEBUGGABLE) != 0
                val isTestOnly = (flagsApp and ApplicationInfo.FLAG_TEST_ONLY) != 0

                AppLogger.d("AppCloner", "Parsed cloned APK: packageName=${info.packageName}, " +
                        "versionName=${info.versionName}, " +
                        "activities=${info.activities?.size ?: 0}, " +
                        "services=${info.services?.size ?: 0}, " +
                        "providers=${info.providers?.size ?: 0}, " +
                        "debuggable=$isDebuggable, testOnly=$isTestOnly, appFlags=0x${flagsApp.toString(16)}")
            }
        } catch (e: Exception) {
            AppLogger.e("AppCloner", "Exception while parsing cloned APK: ${apkFile.absolutePath}", e)
        }
    }

    private fun installApk(apkFile: File): Boolean {
        return try {
            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    apkFile
                )
            } else {
                Uri.fromFile(apkFile)
            }

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(intent)
            true
        } catch (e: Exception) {
            AppLogger.e("AppCloner", "Operation failed", e)
            false
        }
    }
}
