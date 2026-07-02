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
import com.webtoapp.data.model.Announcement
import com.webtoapp.data.model.SplashConfig
import com.webtoapp.data.model.SplashOrientation
import com.webtoapp.data.model.SplashType
import com.webtoapp.ui.components.announcement.toUiTemplate
import com.webtoapp.ui.theme.WebToAppTheme
import com.webtoapp.util.normalizeExternalIntentUrl
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

class SplashLauncherActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PAYLOAD_JSON = "app_modify_payload"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val payload = AppModifyPayload.fromJson(intent.getStringExtra(EXTRA_PAYLOAD_JSON))
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
            if (activationRequireEveryTime && !remoteConfig.enabled) {
                activation.resetActivation(activationAppId)
                isActivated = false
                showActivationDialog = true
            } else if (remoteConfig.enabled) {
                val remoteRequest = activation.buildRemoteRequest(
                    verifyUrl = remoteConfig.verifyUrl,
                    publicKeyBase64 = remoteConfig.publicKeyBase64,
                    offlinePolicy = remoteConfig.offlinePolicy
                )
                if (activationRequireEveryTime) {
                    val result = activation.reverifyRemoteWithCachedCode(activationAppId, remoteRequest)
                    isActivated = result is com.webtoapp.core.activation.ActivationResult.Success || result is com.webtoapp.core.activation.ActivationResult.AlreadyActivated
                    showActivationDialog = !isActivated
                } else {
                    val ok = activation.isActivated(activationAppId).first() &&
                        activation.isRemoteStartupAllowed(activationAppId, remoteRequest)
                    isActivated = ok
                    showActivationDialog = !ok
                }
            } else {
                val ok = activation.resolveStartupActivation(activationAppId)
                isActivated = ok
                showActivationDialog = !ok
            }
        }
    }

    var showAnnouncementDialog by remember { mutableStateOf(false) }

    var showSplash by remember { mutableStateOf(false) }
    var countdown by remember { mutableIntStateOf(
        if (splashType == SplashType.VIDEO) ((videoEndMs - videoStartMs) / 1000).toInt() else splashDuration
    ) }

    LaunchedEffect(isActivated) {
        if (isActivated) {

            if (announcementEnabled && announcement.title.isNotEmpty()) {
                showAnnouncementDialog = true
            } else {

                if (hasValidSplash && splashPath != null) {
                    showSplash = true
                } else {
                    onLaunchTarget()
                }
            }
        }
    }

    fun onAnnouncementDismiss() {
        showAnnouncementDialog = false
        if (hasValidSplash && splashPath != null) {
            showSplash = true
        } else {
            onLaunchTarget()
        }
    }

    LaunchedEffect(Unit) {
        if (!activationEnabled && !announcementEnabled && !hasValidSplash) {
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
                onSkip = {
                    showSplash = false
                    onLaunchTarget()
                }
            )
        }

        if (!showActivationDialog && !showAnnouncementDialog && !showSplash && isActivated) {
            CircularProgressIndicator(color = Color.White)
        }
    }

    if (showActivationDialog) {
        val dialogConfig = payload.activationDialogConfig
        val scope = rememberCoroutineScope()
        var activationError by remember { mutableStateOf<String?>(null) }
        ActivationDialog(
            onDismiss = { showActivationDialog = false },
            errorMessage = activationError,
            onActivate = { code ->
                if (remoteConfig.enabled) {
                    activationError = null
                    scope.launch {
                        val result = activation.verifyRemoteActivation(
                            activationAppId,
                            code,
                            activation.buildRemoteRequest(
                                verifyUrl = remoteConfig.verifyUrl,
                                publicKeyBase64 = remoteConfig.publicKeyBase64,
                                offlinePolicy = remoteConfig.offlinePolicy
                            )
                        )
                        if (result is com.webtoapp.core.activation.ActivationResult.Success) {
                            isActivated = true
                            showActivationDialog = false
                        } else if (result is com.webtoapp.core.activation.ActivationResult.Invalid) {
                            activationError = result.message
                        } else {
                            activationError = com.webtoapp.core.i18n.Strings.invalidActivationCode
                        }
                    }
                } else {
                    activationError = null
                    scope.launch {
                        val result = activation.verifyActivationCodeWithObjects(
                            activationAppId,
                            code,
                            activationCodes
                        )
                        when (result) {
                            is com.webtoapp.core.activation.ActivationResult.Success,
                            is com.webtoapp.core.activation.ActivationResult.AlreadyActivated -> {
                                isActivated = true
                                showActivationDialog = false
                            }
                            is com.webtoapp.core.activation.ActivationResult.Invalid -> {
                                activationError = result.message.ifBlank {
                                    com.webtoapp.core.i18n.Strings.invalidActivationCode
                                }
                            }
                            else -> {
                                activationError = com.webtoapp.core.i18n.Strings.invalidActivationCode
                            }
                        }
                    }
                }
            },
            customTitle = dialogConfig.title,
            customSubtitle = dialogConfig.subtitle,
            customInputLabel = dialogConfig.inputLabel,
            customButtonText = dialogConfig.buttonText
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

private fun normalizeExternalUrlForIntent(rawUrl: String): String {
    return normalizeExternalIntentUrl(rawUrl)
}

@Composable
fun ActivationDialog(
    onDismiss: () -> Unit,
    onActivate: (String) -> Unit,
    customTitle: String = "",
    customSubtitle: String = "",
    customInputLabel: String = "",
    customButtonText: String = "",
    errorMessage: String? = null
) {
    var code by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val displayError = error ?: errorMessage?.takeIf { it.isNotBlank() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(customTitle.ifBlank { com.webtoapp.core.i18n.Strings.activateApp }) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(customSubtitle.ifBlank { com.webtoapp.core.i18n.Strings.enterCodeToContinue })
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = code,
                    onValueChange = {
                        code = it
                        error = null
                    },
                    label = { Text(customInputLabel.ifBlank { com.webtoapp.core.i18n.Strings.activationCode }) },
                    singleLine = true,
                    isError = displayError != null,
                    supportingText = displayError?.let { { Text(it) } }
                )
            }
        },
        confirmButton = {
            PremiumButton(
                onClick = {
                    if (code.isBlank()) {
                        error = com.webtoapp.core.i18n.Strings.pleaseEnterActivationCode
                    } else {
                        onActivate(code)
                    }
                }
            ) {
                Text(customButtonText.ifBlank { com.webtoapp.core.i18n.Strings.activate })
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(com.webtoapp.core.i18n.Strings.btnCancel)
            }
        }
    )
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
            template = announcement.template.toUiTemplate()
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
                    mediaPlayer?.let { mp ->

                        while (!mp.isPlaying) {
                            delay(50)
                            if (mediaPlayer == null) return@LaunchedEffect
                        }

                        while (mp.isPlaying) {
                            val currentPos = mp.currentPosition

                            videoRemainingMs = (videoEndMs - currentPos).coerceAtLeast(0L)
                            if (currentPos >= videoEndMs) {
                                mp.pause()
                                onSkip()
                                break
                            }
                            delay(100)
                        }
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
                                            setOnPreparedListener {
                                                seekTo(videoStartMs.toInt())
                                                start()
                                                isPlayerReady = true
                                            }
                                            setOnCompletionListener { onSkip() }
                                            prepareAsync()
                                        }
                                    } catch (e: Exception) {
                                        AppLogger.e("SplashLauncherActivity", "Operation failed", e)
                                        onSkip()
                                    }
                                }
                                override fun surfaceChanged(h: android.view.SurfaceHolder, f: Int, w: Int, ht: Int) {}
                                override fun surfaceDestroyed(h: android.view.SurfaceHolder) {
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
                        mediaPlayer?.release()
                        mediaPlayer = null
                    }
                }
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            shape = MaterialTheme.shapes.small,
            color = Color.Black.copy(alpha = 0.6f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                val displayTime = if (splashType == SplashType.VIDEO) ((videoRemainingMs + 999) / 1000).toInt() else countdown
                if (displayTime > 0) {
                    Text(
                        text = "${displayTime}s",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (clickToSkip) {
                    if (displayTime > 0) {
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
