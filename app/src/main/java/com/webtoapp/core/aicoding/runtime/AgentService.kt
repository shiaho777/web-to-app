package com.webtoapp.core.aicoding.runtime

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.webtoapp.R
import com.webtoapp.core.aicoding.llm.DefaultLlmGateway
import com.webtoapp.core.aicoding.llm.LlmGateway
import com.webtoapp.core.aicoding.llm.LlmMessage
import com.webtoapp.core.aicoding.agent.AbortController
import com.webtoapp.core.aicoding.agent.AgentEngine
import com.webtoapp.core.aicoding.agent.AgentEvent
import com.webtoapp.core.aicoding.permission.PermissionChecker
import com.webtoapp.core.aicoding.permission.PermissionMode
import com.webtoapp.core.aicoding.permission.PermissionPrompter
import com.webtoapp.core.aicoding.tool.ToolContext
import com.webtoapp.core.aicoding.tool.ToolRegistry
import com.webtoapp.core.ai.LiteLLMModelRegistry
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AgentService : Service() {

    private val binder = LocalBinder()
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var turnJob: Job? = null
    private var abortController: AbortController? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var inForeground = false

    private val gatewayLazy = lazy { DefaultLlmGateway.create(this) }
    val gateway: LlmGateway get() = gatewayLazy.value

    val permissionPrompter = PermissionPrompter()
    val permissionChecker = PermissionChecker(permissionPrompter, initialMode = PermissionMode.Default)

    private val _events = MutableSharedFlow<AgentEvent>(
        replay = 0,
        extraBufferCapacity = 16384,
        onBufferOverflow = BufferOverflow.SUSPEND
    )
    val events: SharedFlow<AgentEvent> = _events.asSharedFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    inner class LocalBinder : Binder() {
        fun service(): AgentService = this@AgentService
    }

    override fun onCreate() {
        super.onCreate()
        ensureChannel()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        promoteToForeground(Strings.aiCodingNotifIdle)
        return START_NOT_STICKY
    }

    fun start(request: AgentRequest) {
        turnJob?.cancel()
        promoteToForeground(Strings.aiCodingNotifRunning)
        acquireWakeLock()
        _isRunning.value = true

        val abort = AbortController().also { abortController = it }
        val engine = AgentEngine(gateway, permissionChecker, abort)

        turnJob = scope.launch {
            try {
                val ctx = ToolContext(
                    androidContext = this@AgentService,
                    sessionId = request.sessionId,
                    fileManager = request.toolContext.fileManager,
                    textModel = request.toolContext.textModel,
                    textApiKey = request.toolContext.textApiKey,
                    imageModel = request.toolContext.imageModel,
                    imageApiKey = request.toolContext.imageApiKey,
                    prompter = permissionPrompter,
                    todos = request.toolContext.todos,
                    readFiles = request.toolContext.readFiles,
                    activePlanFile = request.toolContext.activePlanFile,
                    skillRegistry = request.toolContext.skillRegistry
                )
                engine.run(
                    AgentEngine.Input(
                        systemPrompt = request.systemPrompt,
                        history = request.history,
                        userMessage = request.userMessage,
                        toolContext = ctx,
                        registry = request.registry,
                        temperature = request.temperature,
                        maxTurns = request.maxTurns,
                        maxTokens = resolveMaxOutputTokens(ctx.textModel.model)
                    )
                ).collect { _events.emit(it) }
            } catch (t: Throwable) {
                _events.emit(AgentEvent.Failed(t.message ?: "service crashed"))
            } finally {
                _isRunning.value = false
                releaseWakeLock()
                abortController = null
            }
        }
    }

    private fun resolveMaxOutputTokens(model: com.webtoapp.data.model.AiModel): Int {
        val fromRegistry = runCatching {
            LiteLLMModelRegistry.getInstance(this).getMaxOutputTokens(model.id, model.provider)
        }.getOrNull()
        return fromRegistry?.takeIf { it > 0 }?.coerceAtMost(MAX_OUTPUT_TOKENS_CEILING)
            ?: DEFAULT_MAX_OUTPUT_TOKENS
    }

    fun cancel() {
        abortController?.abort()
        turnJob?.cancel()
        _isRunning.value = false
    }

    override fun onDestroy() {
        super.onDestroy()
        turnJob?.cancel()
        scope.cancel()
        releaseWakeLock()
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(CHANNEL_ID) == null) {
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, Strings.aiCodingTitle, NotificationManager.IMPORTANCE_LOW)
            )
        }
    }

    private fun promoteToForeground(text: String) {
        if (inForeground) return
        startForeground(NOTIFICATION_ID, buildNotification(text))
        inForeground = true
    }

    private fun buildNotification(text: String) = NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle(Strings.aiCodingTitle)
        .setContentText(text)
        .setOngoing(true)
        .setOnlyAlertOnce(true)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setContentIntent(
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        )
        .build()

    private fun acquireWakeLock() {
        if (wakeLock?.isHeld == true) return
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WebToApp:AgentTurn").apply {
            setReferenceCounted(false)
            acquire(WAKE_LOCK_TIMEOUT_MS)
        }
    }

    private fun releaseWakeLock() {
        runCatching { if (wakeLock?.isHeld == true) wakeLock?.release() }
        wakeLock = null
    }

    data class AgentRequest(
        val sessionId: String,
        val systemPrompt: String,
        val history: List<LlmMessage>,
        val userMessage: String,
        val toolContext: ToolContext,
        val registry: ToolRegistry,
        val temperature: Float = 0.7f,
        val maxTurns: Int = 24
    )

    companion object {
        private const val CHANNEL_ID = "aicoding_agent_v3"
        private const val NOTIFICATION_ID = 1201
        private const val WAKE_LOCK_TIMEOUT_MS = 20L * 60 * 1000
        private const val DEFAULT_MAX_OUTPUT_TOKENS = 8192
        private const val MAX_OUTPUT_TOKENS_CEILING = 32768
    }
}
