package com.webtoapp.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.webtoapp.WebToAppApplication
import com.webtoapp.core.agent.mcp.HostMcpController
import com.webtoapp.core.agent.permission.ChoiceRequest
import com.webtoapp.core.agent.permission.PermissionRequest
import com.webtoapp.core.i18n.LanguageManager
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.ui.agent.components.ChoiceBottomSheet
import com.webtoapp.ui.agent.components.PermissionDialog
import com.webtoapp.ui.components.FirstLaunchLanguageScreen
import com.webtoapp.ui.design.WtaAlertDialog
import com.webtoapp.ui.navigation.AppNavigation
import com.webtoapp.ui.shell.ShellActivity
import com.webtoapp.ui.theme.CircularRevealOverlay
import com.webtoapp.ui.theme.LocalThemeRevealState
import com.webtoapp.ui.theme.WebToAppTheme
import com.webtoapp.ui.theme.rememberThemeRevealState
import com.webtoapp.ui.webview.WebViewActivity
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var showLanguageSelection by mutableStateOf(true)
    private var showOnboarding by mutableStateOf(true)

    private var showStartupPermissionRationale by mutableStateOf(false)
    private var showPermissionDeniedDialog by mutableStateOf(false)
    private var pendingStartupPermissions: List<String> = emptyList()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val denied = results.filterValues { !it }.keys
        if (denied.isNotEmpty()) {
            AppLogger.i("MainActivity", "Startup permissions denied: ${denied.joinToString()}")
            showPermissionDeniedDialog = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppLogger.lifecycle("MainActivity", "onCreate", "savedInstanceState=${savedInstanceState != null}")

        if (!isTaskRoot &&
            intent?.action == Intent.ACTION_MAIN &&
            intent?.hasCategory(Intent.CATEGORY_LAUNCHER) == true
        ) {
            AppLogger.i(
                "MainActivity",
                "Spurious launcher relaunch detected (task root exists), finishing to let system bring existing task forward"
            )
            finish()
            return
        }

        val isShell = try {
            val shellManager = WebToAppApplication.shellMode
            val result = shellManager.isShellMode()
            AppLogger.d("MainActivity", "Shell mode check: isShellMode=$result")
            result
        } catch (e: Exception) {
            AppLogger.e("MainActivity", "Shell mode check failed", e)
            false
        } catch (e: Error) {
            AppLogger.e("MainActivity", "Shell mode check critical error", Error(e))
            false
        }

        if (isShell) {

            AppLogger.i("MainActivity", "Entering shell mode, redirecting to ShellActivity")
            try {
                startActivity(Intent(this, ShellActivity::class.java))
                finish()
                return
            } catch (e: Exception) {
                AppLogger.e("MainActivity", "Failed to start ShellActivity", e)

            }
        }

        if (handleIncomingUrlIntent(intent)) {
            return
        }

        AppLogger.i("MainActivity", "Normal mode, showing main UI")

        try {
            enableEdgeToEdge()
        } catch (e: Exception) {
            AppLogger.w("MainActivity", "enableEdgeToEdge failed", e)
        }

        requestNecessaryPermissions()

        try {
            WindowCompat.setDecorFitsSystemWindows(window, false)
        } catch (e: Exception) {
            AppLogger.w("MainActivity", "setDecorFitsSystemWindows failed", e)
        }

        setContent {

            val themeRevealState = rememberThemeRevealState()

            WebToAppTheme { isDarkTheme ->

                val themeColors = MaterialTheme.colorScheme
                LaunchedEffect(isDarkTheme, themeColors.background) {
                    val bg = themeColors.background.toArgb()
                    val lightSurface = com.webtoapp.ui.shared.WindowHelper.isColorLight(bg)
                    val style = if (lightSurface) {
                        SystemBarStyle.light(bg, bg)
                    } else {
                        SystemBarStyle.dark(bg)
                    }
                    enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        window.isStatusBarContrastEnforced = false
                        window.isNavigationBarContrastEnforced = false
                    }
                    window.decorView.setBackgroundColor(bg)
                }

                val context = LocalContext.current
                val languageManager = remember { LanguageManager.getInstance(context) }
                // Nullable gate: a `true` initial would paint AppNavigation for
                // one frame, then flip to the language picker on first launch.
                val hasSelectedLanguage by remember {
                    languageManager.hasSelectedLanguageFlow.map<Boolean, Boolean?> { it }
                }.collectAsState(initial = null)
                val onboardingPrefs = remember { com.webtoapp.core.onboarding.OnboardingPrefs.getInstance(context) }
                val onboardingCompleted by remember {
                    onboardingPrefs.completedFlow.map<Boolean, Boolean?> { it }
                }.collectAsState(initial = null)
                val onboardingScope = rememberCoroutineScope()

                CompositionLocalProvider(
                    LocalThemeRevealState provides themeRevealState
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {

                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            if (hasSelectedLanguage == false && showLanguageSelection) {
                                FirstLaunchLanguageScreen(
                                    onLanguageSelected = {
                                        showLanguageSelection = false
                                    }
                                )
                            } else if (hasSelectedLanguage == true &&
                                onboardingCompleted == false &&
                                showOnboarding
                            ) {
                                com.webtoapp.ui.components.OnboardingScreen(
                                    onFinished = {
                                        showOnboarding = false
                                        onboardingScope.launch { onboardingPrefs.setCompleted(true) }
                                    }
                                )
                            } else if (hasSelectedLanguage != null && onboardingCompleted != null) {
                                AppNavigation()
                            }
                        }

                        CircularRevealOverlay(revealState = themeRevealState)
                    }
                }

                var mcpPermission by remember { mutableStateOf<PermissionRequest?>(null) }
                var mcpChoice by remember { mutableStateOf<ChoiceRequest?>(null) }
                LaunchedEffect(Unit) {
                    launch {
                        HostMcpController.prompter.requests.collect { mcpPermission = it }
                    }
                    launch {
                        HostMcpController.prompter.choices.collect { mcpChoice = it }
                    }
                }
                mcpPermission?.let { req ->
                    PermissionDialog(req) { response ->
                        mcpPermission = null
                        HostMcpController.prompter.respond(req.toolCallId, response)
                    }
                }
                mcpChoice?.let { req ->
                    ChoiceBottomSheet(req) { response ->
                        mcpChoice = null
                        HostMcpController.prompter.respondChoice(req.id, response)
                    }
                }

                if (showStartupPermissionRationale) {
                    WtaAlertDialog(
                        onDismissRequest = { showStartupPermissionRationale = false },
                        title = Strings.startupPermissionTitle,
                        text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            Strings.startupPermissionNotificationsMessage
                        } else {
                            Strings.startupPermissionStorageMessage
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                showStartupPermissionRationale = false
                                launchPendingStartupPermissions()
                            }) {
                                Text(Strings.startupPermissionContinue)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showStartupPermissionRationale = false }) {
                                Text(Strings.cancel)
                            }
                        }
                    )
                }

                if (showPermissionDeniedDialog) {
                    WtaAlertDialog(
                        onDismissRequest = { showPermissionDeniedDialog = false },
                        title = Strings.permissionDeniedTitle,
                        text = Strings.permissionDeniedMessage,
                        confirmButton = {
                            TextButton(onClick = {
                                showPermissionDeniedDialog = false
                                openAppSettings()
                            }) {
                                Text(Strings.installPermissionOpenSettings)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showPermissionDeniedDialog = false }) {
                                Text(Strings.close)
                            }
                        }
                    )
                }

            }
        }
    }

    private fun openAppSettings() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        } catch (e: Exception) {

            try {
                startActivity(Intent(Settings.ACTION_SETTINGS))
            } catch (e2: Exception) {

            }
        }
    }

    private fun requestNecessaryPermissions() {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        val needRequest = permissions.filter { perm ->
            ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED
        }

        if (needRequest.isNotEmpty()) {
            pendingStartupPermissions = needRequest
            showStartupPermissionRationale = true
        }
    }

    private fun launchPendingStartupPermissions() {
        val needRequest = pendingStartupPermissions.filter { perm ->
            ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED
        }
        if (needRequest.isNotEmpty()) {
            AppLogger.d("MainActivity", "Requesting permissions: ${needRequest.joinToString()}")
            permissionLauncher.launch(needRequest.toTypedArray())
        }
    }

    override fun onStart() {
        super.onStart()
        AppLogger.lifecycle("MainActivity", "onStart")
    }

    override fun onResume() {
        super.onResume()
        AppLogger.lifecycle("MainActivity", "onResume")
    }

    override fun onPause() {
        AppLogger.lifecycle("MainActivity", "onPause")
        super.onPause()
    }

    override fun onStop() {
        AppLogger.lifecycle("MainActivity", "onStop")
        super.onStop()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        AppLogger.lifecycle("MainActivity", "onNewIntent")
        setIntent(intent)

        val isShell = try {
            WebToAppApplication.shellMode.isShellMode()
        } catch (e: Exception) {
            AppLogger.e("MainActivity", "Shell mode re-check failed in onNewIntent", e)
            false
        } catch (e: Error) {
            AppLogger.e("MainActivity", "Shell mode re-check critical error in onNewIntent", Error(e))
            false
        }

        if (isShell) {
            AppLogger.i("MainActivity", "Shell mode onNewIntent, redirecting to ShellActivity")
            try {
                startActivity(Intent(this, ShellActivity::class.java).apply {
                    action = intent.action
                    data = intent.data
                    flags = intent.flags
                    intent.extras?.let { putExtras(it) }
                })
                finish()
                return
            } catch (e: Exception) {
                AppLogger.e("MainActivity", "Failed to redirect ShellActivity from onNewIntent", e)
            }
        }

        handleIncomingUrlIntent(intent)
    }

    private fun handleIncomingUrlIntent(intent: Intent?): Boolean {
        if (intent == null) return false

        val url: String? = when (intent.action) {
            Intent.ACTION_VIEW -> {

                intent.data?.toString()
            }
            Intent.ACTION_SEND -> {

                val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                extractUrlFromText(sharedText)
            }
            else -> null
        }

        if (!url.isNullOrBlank() && (url.startsWith("http://") || url.startsWith("https://"))) {
            AppLogger.i("MainActivity", "Handling incoming URL intent: $url")
            WebViewActivity.startWithUrl(this, url)

            return true
        }

        return false
    }

    private fun extractUrlFromText(text: String?): String? {
        if (text.isNullOrBlank()) return null

        val trimmed = text.trim()
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {

            return trimmed.split("\\s".toRegex()).firstOrNull()
        }

        val urlPattern = Regex("https?://[^\\s]+")
        return urlPattern.find(text)?.value
    }

    override fun onDestroy() {
        AppLogger.lifecycle("MainActivity", "onDestroy")
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        AppLogger.lifecycle("MainActivity", "onSaveInstanceState")
        super.onSaveInstanceState(outState)
    }
}
