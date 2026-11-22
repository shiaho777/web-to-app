package com.webtoapp.ui.splash

import android.content.Intent
import com.webtoapp.ui.components.PremiumButton
import com.webtoapp.core.logging.AppLogger
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.webtoapp.core.appmodifier.AppModifyPayload
import com.webtoapp.core.appmodifier.DialogEngineAction
import com.webtoapp.core.appmodifier.DialogEngineComponentType
import com.webtoapp.core.appmodifier.DialogEngineProject
import com.webtoapp.data.model.Announcement
import com.webtoapp.data.model.SplashConfig
import com.webtoapp.data.model.SplashOrientation
import com.webtoapp.data.model.SplashType
import com.webtoapp.ui.components.announcement.toUiTemplate
import com.webtoapp.ui.design.WtaLoadingIndicator
import com.webtoapp.ui.theme.WebToAppTheme
import com.webtoapp.util.normalizeExternalIntentUrl
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class SplashLauncherActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PAYLOAD_JSON = "app_modify_payload"
        const val EXTRA_PAYLOAD_SIGNATURE = "app_modify_payload_sig"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This activity is exported because pinned shortcuts are replayed by the
        // launcher app — so the payload is caller-controlled input. Verify the
        // host-issued HMAC before trusting targetPackage / verifyUrl / dialog text
        // / mediaPath; a forged payload must never reach activation or splash IO.
        val payloadJson = intent.getStringExtra(EXTRA_PAYLOAD_JSON)
        if (!com.webtoapp.core.appmodifier.PayloadIntegrity.verify(
                this, payloadJson, intent.getStringExtra(EXTRA_PAYLOAD_SIGNATURE)
            )
        ) {
            AppLogger.w("SplashLauncherActivity", "Rejected unsigned or tampered payload, finishing")
            finish()
            return
        }

        val payload = AppModifyPayload.fromJson(payloadJson)
        if (payload == null || payload.targetPackage.isBlank()) {
            AppLogger.w("SplashLauncherActivity", "Missing or invalid payload, finishing")
            finish()
            return
        }

        val splashPath = payload.splashConfig.mediaPath
        val hasValidSplash = payload.splashEnabled &&
            splashPath != null &&
            File(splashPath).exists()

        if (hasValidSplash && payload.splashConfig.orientation == SplashOrientation.LANDSCAPE) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }

        setContent {
            WebToAppTheme { _ ->
                SplashLauncherScreen(
                    payload = payload,
                    hasValidSplash = hasValidSplash,
                    onLaunchTarget = { launchTargetApp(payload.targetPackage) }
                )
            }
        }
    }

    private fun launchTargetApp(packageName: String) {
        try {
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                startActivity(launchIntent)
            }
        } catch (e: Exception) {
            AppLogger.e("SplashLauncherActivity", "Operation failed", e)
        }

        finishAndRemoveTask()
    }
}

@Composable
fun SplashLauncherScreen(
    payload: AppModifyPayload,
    hasValidSplash: Boolean,
    onLaunchTarget: () -> Unit
) {
    val context = LocalContext.current
    val activation = com.webtoapp.WebToAppApplication.activation

    val splashConfig = payload.splashConfig
    val splashPath = splashConfig.mediaPath
    val splashType = splashConfig.type
    val splashDuration = splashConfig.duration
    val clickToSkip = splashConfig.clickToSkip
    val videoStartMs = splashConfig.videoStartMs
    val videoEndMs = splashConfig.videoEndMs
    val fillScreen = splashConfig.fillScreen
    val enableAudio = splashConfig.enableAudio

    val activationEnabled = payload.activationEnabled
    val activationRequireEveryTime = payload.activationRequireEveryTime
    val activationCodes = payload.activationCodes
    val remoteConfig = payload.activationRemoteConfig
    val announcement = payload.announcement
    val announcementEnabled = payload.announcementEnabled

    val activationAppId = remember(payload.targetPackage) {
        -(kotlin.math.abs(payload.targetPackage.hashCode().toLong()) + 100L)
    }

    var isActivated by remember { mutableStateOf(!activationEnabled) }
    var showActivationDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (activationEnabled) {
            // Same gate as shell/preview: remote re-verifies the remembered code
            // (always under "every launch", else only when the cached result can't
            // carry this launch); local codes re-check the remembered card.
            val ok = if (remoteConfig.enabled) {
                activation.resolveRemoteStartup(
                    activationAppId,
                    activation.buildRemoteRequest(
                        verifyUrl = remoteConfig.verifyUrl,
                        publicKeyBase64 = remoteConfig.publicKeyBase64,
                        offlinePolicy = remoteConfig.offlinePolicy,
                        deliverUrl = remoteConfig.deliverUrl,
                        encryptUrl = remoteConfig.encryptUrl,
                        aesKeyBase64 = remoteConfig.aesKeyBase64,
                        deviceBound = remoteConfig.deviceBound,
                        trustMode = remoteConfig.trustMode,
                        pinnedCertificate = remoteConfig.pinnedCertificate,
                        pinnedCertHost = remoteConfig.pinnedCertHost,
                        packageName = payload.targetPackage
                    ),
                    reverifyEveryLaunch = activationRequireEveryTime
                )
            } else if (activationRequireEveryTime) {
                activation.resolveRelaunchActivation(activationAppId, activationCodes)
            } else {
                activation.resolveStartupActivation(activationAppId)
            }
            isActivated = ok
            showActivationDialog = !ok
        }
    }

    var showAnnouncementDialog by remember { mutableStateOf(false) }
    var showCustomDialog by remember {
        mutableStateOf(payload.dialogEngineEnabled && payload.dialogEngineProject.enabled)
    }
    var remainingDaysNotice by remember {
        mutableStateOf<com.webtoapp.core.activation.RemainingDaysNotice?>(null)
    }

    val scope = rememberCoroutineScope()

    var showSplash by remember { mutableStateOf(false) }
    var countdown by remember { mutableIntStateOf(
        if (splashType == SplashType.VIDEO) ((videoEndMs - videoStartMs) / 1000).toInt() else splashDuration
    ) }

    suspend fun continueAfterActivationGate() {
        if (payload.dialogEngineEnabled && payload.dialogEngineProject.enabled && showCustomDialog) {
            return
        }
        // Respect showOnce/version pinning like the in-app announcement paths; without
        // this gate the dialog re-appeared on every clone launch.
        val announceNow = announcementEnabled && announcement.title.isNotEmpty() &&
            com.webtoapp.WebToAppApplication.announcement.shouldShowAnnouncement(activationAppId, announcement)
        if (announceNow) {
            showAnnouncementDialog = true
        } else if (hasValidSplash && splashPath != null) {
            showSplash = true
        } else {
            onLaunchTarget()
        }
    }

    LaunchedEffect(isActivated) {
        if (!isActivated) return@LaunchedEffect
        val notice = if (activationEnabled && !remoteConfig.enabled) {
            activation.remainingDaysNoticeForLaunch(activationAppId, activationCodes)
        } else {
            null
        }
        if (notice != null) {
            remainingDaysNotice = notice
        } else {
            continueAfterActivationGate()
        }
    }

    fun onAnnouncementDismiss() {
        showAnnouncementDialog = false
        scope.launch {
            com.webtoapp.WebToAppApplication.announcement.markAnnouncementShown(activationAppId, announcement.version)
        }
        if (hasValidSplash && splashPath != null) {
            showSplash = true
        } else {
            onLaunchTarget()
        }
    }

    LaunchedEffect(Unit) {
        if (!activationEnabled && !announcementEnabled && !hasValidSplash &&
            !(payload.dialogEngineEnabled && payload.dialogEngineProject.enabled)
        ) {
            onLaunchTarget()
        }
    }

    if (showSplash && splashType == SplashType.IMAGE) {
        LaunchedEffect(countdown) {
            if (countdown > 0) {
                delay(1000L)
                countdown--
            } else {
                showSplash = false
                onLaunchTarget()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {

        if (!isActivated) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = com.webtoapp.core.i18n.Strings.appNeedsActivation,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                PremiumButton(onClick = { showActivationDialog = true }) {
                    Text(com.webtoapp.core.i18n.Strings.enterActivationCode)
                }
            }
        }

        AnimatedVisibility(
            visible = showSplash && splashPath != null,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(200))
        ) {
            SplashContent(
                splashType = splashType,
                splashPath = splashPath!!,
                countdown = countdown,
                clickToSkip = clickToSkip,
                videoStartMs = videoStartMs,
                videoEndMs = videoEndMs,
                fillScreen = fillScreen,
                enableAudio = enableAudio,
                showCountdown = splashConfig.showCountdown,
                onSkip = {
                    showSplash = false
                    onLaunchTarget()
                }
            )
        }

        if (!showActivationDialog && !showAnnouncementDialog && !showCustomDialog && !showSplash &&
            remainingDaysNotice == null && isActivated
        ) {
            WtaLoadingIndicator(color = Color.White)
        }
    }

    if (showCustomDialog) {
        DialogEngineRuntimeDialog(
            project = payload.dialogEngineProject,
            onPrimary = { action, value ->
                showCustomDialog = false
                executeDialogEngineAction(
                    action = action,
                    value = value,
                    targetPackage = payload.targetPackage,
                    context = context,
                    onLaunchTarget = onLaunchTarget
                )
                if (action == DialogEngineAction.CLOSE ||
                    action == DialogEngineAction.NONE
                ) {
                    scope.launch { continueAfterActivationGate() }
                }
            },
            onSecondary = { action, value ->
                showCustomDialog = false
                executeDialogEngineAction(
                    action = action,
                    value = value,
                    targetPackage = payload.targetPackage,
                    context = context,
                    onLaunchTarget = onLaunchTarget
                )
                if (action == DialogEngineAction.CLOSE ||
                    action == DialogEngineAction.NONE
                ) {
                    scope.launch { continueAfterActivationGate() }
                }
            },
            onDismiss = {
                if (payload.dialogEngineProject.dismissOnOutside ||
                    payload.dialogEngineProject.dismissOnBack
                ) {
                    showCustomDialog = false
                    scope.launch { continueAfterActivationGate() }
                }
            }
        )
    }

    if (showActivationDialog) {
        val dialogConfig = payload.activationDialogConfig
        com.webtoapp.ui.components.EnhancedActivationDialog(
            onDismiss = { showActivationDialog = false },
            onActivate = { code ->
                val result = if (remoteConfig.enabled) {
                    activation.verifyRemoteActivation(
                        activationAppId,
                        code,
                        activation.buildRemoteRequest(
                            verifyUrl = remoteConfig.verifyUrl,
                            publicKeyBase64 = remoteConfig.publicKeyBase64,
                            offlinePolicy = remoteConfig.offlinePolicy,
                            deliverUrl = remoteConfig.deliverUrl,
                            encryptUrl = remoteConfig.encryptUrl,
                            aesKeyBase64 = remoteConfig.aesKeyBase64,
                            deviceBound = remoteConfig.deviceBound,
                            trustMode = remoteConfig.trustMode,
                            pinnedCertificate = remoteConfig.pinnedCertificate,
                            pinnedCertHost = remoteConfig.pinnedCertHost,
                            packageName = payload.targetPackage
                        )
                    )
                } else {
                    activation.verifyActivationCodeWithObjects(
                        activationAppId,
                        code,
                        activationCodes
                    )
                }
                if (result is com.webtoapp.core.activation.ActivationResult.Success ||
                    result is com.webtoapp.core.activation.ActivationResult.AlreadyActivated
                ) {
                    isActivated = true
                }
                result
            },
            customTitle = dialogConfig.title,
            customSubtitle = dialogConfig.subtitle,
            customInputLabel = dialogConfig.inputLabel,
            customButtonText = dialogConfig.buttonText
        )
    }

    remainingDaysNotice?.let { notice ->
        com.webtoapp.ui.components.RemainingDaysNoticeDialog(
            notice = notice,
            onDismiss = {
                remainingDaysNotice = null
                scope.launch { continueAfterActivationGate() }
            }
        )
    }

    if (showAnnouncementDialog) {
        AnnouncementDialog(
            announcement = announcement,
            onDismiss = { onAnnouncementDismiss() },
            onLinkClick = { url ->
                val safeUrl = normalizeExternalUrlForIntent(url)
                if (safeUrl.isNotEmpty()) {
                    val intent = android.content.Intent(
                        android.content.Intent.ACTION_VIEW,
                        android.net.Uri.parse(safeUrl)
                    )
                    context.startActivity(intent)
                } else {
                    AppLogger.w("SplashLauncherActivity", "Blocked invalid announcement link: $url")
                }
            }
        )
    }
}

private fun executeDialogEngineAction(
    action: DialogEngineAction,
    value: String,
    targetPackage: String,
    context: android.content.Context,
    onLaunchTarget: () -> Unit
) {
    when (action) {
        DialogEngineAction.LAUNCH_TARGET -> onLaunchTarget()
        DialogEngineAction.OPEN_URL -> {
            val safeUrl = normalizeExternalIntentUrl(value)
            if (safeUrl.isNotEmpty()) {
                runCatching {
                    context.startActivity(
                        android.content.Intent(
                            android.content.Intent.ACTION_VIEW,
                            android.net.Uri.parse(safeUrl)
                        )
                    )
                }
            }
        }
        DialogEngineAction.CLOSE,
        DialogEngineAction.NONE -> Unit
    }
}

@Composable
private fun DialogEngineRuntimeDialog(
    project: DialogEngineProject,
    onPrimary: (DialogEngineAction, String) -> Unit,
    onSecondary: (DialogEngineAction, String) -> Unit,
    onDismiss: () -> Unit
) {
    val containerColor = parseDialogColor(project.backgroundColor, Color.White)
    val contentColor = parseDialogColor(project.contentColor, Color.Black)
    val accentColor = parseDialogColor(project.accentColor, MaterialTheme.colorScheme.primary)

    com.webtoapp.ui.design.WtaAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(project.widthFraction.coerceIn(0.5f, 1f)),
        properties = androidx.compose.ui.window.DialogProperties(
            dismissOnBackPress = project.dismissOnBack,
            dismissOnClickOutside = project.dismissOnOutside
        ),
        title = project.title.ifBlank { null },
        confirmButton = {
            TextButton(onClick = {
                onPrimary(project.primaryAction, project.primaryActionValue)
            }) {
                Text(project.primaryText.ifBlank { "Continue" })
            }
        },
        dismissButton = if (project.secondaryText.isNotBlank()) {
            {
                TextButton(onClick = {
                    onSecondary(project.secondaryAction, project.secondaryActionValue)
                }) {
                    Text(project.secondaryText)
                }
            }
        } else null,
        content = {
            if (project.message.isNotBlank()) {
                Text(
                    text = project.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor
                )
            }
            project.components.forEach { component ->
                DialogEngineRuntimeComponent(
                    component = component,
                    accentColor = accentColor,
                    contentColor = contentColor
                )
            }
        }
    )
}

@Composable
private fun DialogEngineRuntimeComponent(
    component: com.webtoapp.core.appmodifier.DialogEngineComponent,
    accentColor: Color,
    contentColor: Color
) {
    when (component.type) {
        DialogEngineComponentType.TITLE -> Text(
            text = component.text,
            style = MaterialTheme.typography.titleMedium,
            color = contentColor,
            modifier = Modifier.fillMaxWidth(component.widthFraction.coerceIn(0.1f, 1f))
        )
        DialogEngineComponentType.MESSAGE -> Text(
            text = component.text,
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor,
            modifier = Modifier.fillMaxWidth(component.widthFraction.coerceIn(0.1f, 1f))
        )
        DialogEngineComponentType.TEXT_FIELD,
        DialogEngineComponentType.PASSWORD -> {
            var value by remember(component.id) { mutableStateOf("") }
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                enabled = component.enabled,
                label = if (component.text.isNotBlank()) ({ Text(component.text) }) else null,
                placeholder = if (component.hint.isNotBlank()) ({ Text(component.hint) }) else null,
                visualTransformation = if (component.type == DialogEngineComponentType.PASSWORD) {
                    androidx.compose.ui.text.input.PasswordVisualTransformation()
                } else androidx.compose.ui.text.input.VisualTransformation.None,
                modifier = Modifier.fillMaxWidth(component.widthFraction.coerceIn(0.1f, 1f))
            )
        }
        DialogEngineComponentType.CHECKBOX -> {
            var checked by remember(component.id) { mutableStateOf(component.selected) }
            Row(
                modifier = Modifier.fillMaxWidth(component.widthFraction.coerceIn(0.1f, 1f)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = checked,
                    onCheckedChange = { checked = it },
                    enabled = component.enabled,
                    colors = CheckboxDefaults.colors(checkedColor = accentColor)
                )
                Text(component.text, color = contentColor)
            }
        }
        DialogEngineComponentType.SWITCH -> {
            var checked by remember(component.id) { mutableStateOf(component.selected) }
            Row(
                modifier = Modifier.fillMaxWidth(component.widthFraction.coerceIn(0.1f, 1f)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(component.text, color = contentColor)
                Switch(
                    checked = checked,
                    onCheckedChange = { checked = it },
                    enabled = component.enabled
                )
            }
        }
        DialogEngineComponentType.PROGRESS -> LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth(component.widthFraction.coerceIn(0.1f, 1f))
                .height(component.heightDp.dp),
            color = accentColor
        )
        DialogEngineComponentType.IMAGE -> Spacer(
            modifier = Modifier
                .fillMaxWidth(component.widthFraction.coerceIn(0.1f, 1f))
                .height(component.heightDp.dp)
                .background(parseDialogColor(component.backgroundColor, Color.LightGray))
        )
        DialogEngineComponentType.LIST,
        DialogEngineComponentType.BUTTON -> {
            Text(
                text = component.text,
                color = contentColor,
                modifier = Modifier.fillMaxWidth(component.widthFraction.coerceIn(0.1f, 1f))
            )
        }
    }
}

private fun parseDialogColor(value: String, fallback: Color): Color {
    return runCatching {
        val normalized = value.removePrefix("#")
        val argb = normalized.toLong(16)
        val a = ((argb ushr 24) and 0xff).toInt()
        val r = ((argb ushr 16) and 0xff).toInt()
        val g = ((argb ushr 8) and 0xff).toInt()
        val b = (argb and 0xff).toInt()
        Color(red = r, green = g, blue = b, alpha = if (normalized.length <= 6) 255 else a)
    }.getOrDefault(fallback)
}

private fun normalizeExternalUrlForIntent(rawUrl: String): String {
    return normalizeExternalIntentUrl(rawUrl)
}

@Composable
fun AnnouncementDialog(
    announcement: Announcement,
    onDismiss: () -> Unit,
    onLinkClick: (String) -> Unit
) {
    com.webtoapp.ui.components.announcement.AnnouncementDialog(
        config = com.webtoapp.ui.components.announcement.AnnouncementConfig(
            announcement = announcement,
            template = announcement.template.toUiTemplate(),
            customIconBitmap = announcement.customIconPath?.let { p ->
                try { com.webtoapp.util.BoundedBitmaps.decodeBoundedBitmapFile(p) } catch (e: Exception) { null }
            }
        ),
        onDismiss = onDismiss,
        onLinkClick = { url -> onLinkClick(url) }
    )
}

@Composable
fun SplashContent(
    splashType: SplashType,
    splashPath: String,
    countdown: Int,
    clickToSkip: Boolean,
    videoStartMs: Long,
    videoEndMs: Long,
    fillScreen: Boolean,
    enableAudio: Boolean = false,
    showCountdown: Boolean = true,
    onSkip: () -> Unit
) {
    val context = LocalContext.current
    val videoDurationMs = videoEndMs - videoStartMs
    val contentScaleMode = if (fillScreen) ContentScale.Crop else ContentScale.Fit

    var videoRemainingMs by remember { mutableLongStateOf(videoDurationMs) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .then(
                if (clickToSkip) {
                    Modifier.clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onSkip() }
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        when (splashType) {
            SplashType.IMAGE -> {
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(context)
                            .data(File(splashPath))
                            .crossfade(true)
                            .build()
                    ),
                    contentDescription = com.webtoapp.core.i18n.Strings.splashScreen,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = contentScaleMode
                )
            }
            SplashType.VIDEO -> {
                var mediaPlayer by remember { mutableStateOf<android.media.MediaPlayer?>(null) }
                var isPlayerReady by remember { mutableStateOf(false) }

                LaunchedEffect(isPlayerReady) {
                    if (!isPlayerReady) return@LaunchedEffect
                    while (true) {
                        // Re-read the live player each iteration: a back press during the
                        // splash releases it and nulls the state mid-poll (issue #612).
                        val mp = mediaPlayer ?: return@LaunchedEffect
                        try {
                            if (!mp.isPlaying) {
                                delay(50)
                                continue
                            }
                            val currentPos = mp.currentPosition

                            videoRemainingMs = (videoEndMs - currentPos).coerceAtLeast(0L)
                            if (currentPos >= videoEndMs) {
                                mp.pause()
                                onSkip()
                                break
                            }
                        } catch (e: IllegalStateException) {
                            // Player released between iterations; the splash is going away.
                            return@LaunchedEffect
                        }
                        delay(100)
                    }
                }

                AndroidView(
                    factory = { ctx ->
                        android.view.SurfaceView(ctx).apply {
                            holder.addCallback(object : android.view.SurfaceHolder.Callback {
                                override fun surfaceCreated(holder: android.view.SurfaceHolder) {
                                    try {
                                        mediaPlayer = android.media.MediaPlayer().apply {
                                            setDataSource(splashPath)
                                            setSurface(holder.surface)

                                            val volume = if (enableAudio) 1f else 0f
                                            setVolume(volume, volume)
                                            isLooping = false
                                            setOnPreparedListener { mp ->
                                                // prepareAsync may still be in flight when a
                                                // back press releases the player; skip stale
                                                // callbacks instead of touching a released
                                                // instance (issue #612).
                                                if (mediaPlayer !== mp) return@setOnPreparedListener
                                                try {
                                                    seekTo(videoStartMs.toInt())
                                                    start()
                                                    isPlayerReady = true
                                                } catch (e: IllegalStateException) {
                                                    AppLogger.e("SplashLauncherActivity", "Splash player released before prepared", e)
                                                }
                                            }
                                            setOnCompletionListener { mp ->
                                                if (mediaPlayer === mp) onSkip()
                                            }
                                            prepareAsync()
                                        }
                                    } catch (e: Exception) {
                                        AppLogger.e("SplashLauncherActivity", "Operation failed", e)
                                        onSkip()
                                    }
                                }
                                override fun surfaceChanged(h: android.view.SurfaceHolder, f: Int, w: Int, ht: Int) {}
                                override fun surfaceDestroyed(h: android.view.SurfaceHolder) {
                                    // Detach callbacks before releasing so in-flight
                                    // prepare/completion notifications cannot touch a
                                    // released player.
                                    mediaPlayer?.setOnPreparedListener(null)
                                    mediaPlayer?.setOnCompletionListener(null)
                                    mediaPlayer?.release()
                                    mediaPlayer = null
                                }
                            })
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                DisposableEffect(Unit) {
                    onDispose {
                        mediaPlayer?.setOnPreparedListener(null)
                        mediaPlayer?.setOnCompletionListener(null)
                        mediaPlayer?.release()
                        mediaPlayer = null
                    }
                }
            }
        }

        val displayTime = if (splashType == SplashType.VIDEO) {
            ((videoRemainingMs + 999) / 1000).toInt()
        } else {
            countdown
        }
        val showNumber = showCountdown && displayTime > 0
        if (com.webtoapp.ui.shared.splashCornerChipVisible(showCountdown, displayTime, clickToSkip)) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(16.dp),
                shape = MaterialTheme.shapes.small,
                color = Color.Black.copy(alpha = 0.6f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showNumber) {
                        Text(
                            text = "${displayTime}s",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    if (clickToSkip) {
                        if (showNumber) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("|", color = Color.White.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = com.webtoapp.core.i18n.Strings.skip,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
