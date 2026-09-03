package com.webtoapp.core.agent.runtime

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
import com.webtoapp.core.agent.llm.DefaultLlmGateway
import com.webtoapp.core.agent.llm.LlmGateway
import com.webtoapp.core.agent.llm.LlmMessage
import com.webtoapp.core.agent.engine.AbortController
import com.webtoapp.core.agent.engine.AgentEngine
import com.webtoapp.core.agent.engine.AgentEvent
import com.webtoapp.core.agent.permission.PermissionChecker
import com.webtoapp.core.agent.permission.PermissionMode
import com.webtoapp.core.agent.permission.PermissionPrompter
import com.webtoapp.core.agent.session.AgentMessage
import com.webtoapp.core.agent.session.RecordedToolCall
import com.webtoapp.core.agent.session.SessionStore
import com.webtoapp.core.agent.session.ThinkingSegmentData
import com.webtoapp.core.agent.tool.FileChange
import com.webtoapp.core.agent.tool.ToolContext
import com.webtoapp.core.agent.tool.ToolRegistry
import com.webtoapp.core.agent.tool.ToolResult
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.logging.AppLogger
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

    /**
     * Snapshot of the currently running (or just-finished) turn, so the UI can resume
     * an in-flight turn after being destroyed and recreated (e.g. user left the Agent
     * screen, Activity got reclaimed, then came back). Null when no turn is active.
     */
    private val _activeTurn = MutableStateFlow<TurnSnapshot?>(null)
    val activeTurn: StateFlow<TurnSnapshot?> = _activeTurn.asStateFlow()

    inner class LocalBinder : Binder() {
        fun service(): AgentService = this@AgentService
    }

    override fun onCreate() {
        super.onCreate()
        ensureChannel()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        promoteToForeground(Strings.agentNotifIdle)
        return START_NOT_STICKY
    }

    fun start(request: AgentRequest) {
        turnJob?.cancel()
        promoteToForeground(Strings.agentNotifRunning)
        acquireWakeLock()
        _isRunning.value = true

        val abort = AbortController().also { abortController = it }
        val engine = AgentEngine(gateway, permissionChecker, abort)
        val store = request.sessionStore
        val sessionId = request.sessionId
        val accumulator = TurnAccumulator(sessionId)
        currentSessionId = sessionId
        currentStore = store
        currentAccumulator = accumulator

        _activeTurn.value = TurnSnapshot(
            sessionId = sessionId,
            isRunning = true,
            draftMessage = null
        )

        turnJob = scope.launch {
            try {
                val ctx = ToolContext(
                    androidContext = this@AgentService,
                    sessionId = sessionId,
                    fileManager = request.toolContext.fileManager,
                    textModel = request.toolContext.textModel,
                    textApiKey = request.toolContext.textApiKey,
                    imageModel = request.toolContext.imageModel,
                    imageApiKey = request.toolContext.imageApiKey,
                    prompter = permissionPrompter,
                    todos = request.toolContext.todos,
                    appRepository = request.toolContext.appRepository,
                    readFiles = request.toolContext.readFiles,
                    activePlanFile = request.toolContext.activePlanFile
                )
                // Single collection source: broadcast to UI AND persist in the same
                // collector so persistence no longer depends on any UI scope.
                engine.run(
                    AgentEngine.Input(
                        systemPrompt = request.systemPrompt,
                        history = request.history,
                        userMessage = request.userMessage,
                        toolContext = ctx,
                        registry = request.registry,
                        temperature = request.temperature,
                        maxTurns = request.maxTurns,
                        maxTokens = null
                    )
                ).collect { ev ->
                    // Broadcast to UI first so live streaming stays responsive.
                    _events.emit(ev)
                    // Then persist (best-effort; never let a storage failure kill the turn).
                    if (store != null) {
                        runCatching { persistEvent(store, sessionId, accumulator, ev) }
                            .onFailure { AppLogger.w(TAG, "persist failed: ${it.message}") }
                    }
                }
            } catch (t: Throwable) {
                val ev = AgentEvent.Failed(t.message ?: "service crashed")
                _events.emit(ev)
                if (store != null) {
                    runCatching { persistEvent(store, sessionId, accumulator, ev) }
                }
            } finally {
                // Only clear state if THIS turn is still the active one — a cancelled
                // turn's finally can run after a new start() installed its snapshot,
                // and blindly flipping isRunning would corrupt the new turn's state.
                if (currentSessionId == sessionId) {
                    _isRunning.value = false
                }
                if (currentSessionId == sessionId && _activeTurn.value?.sessionId == sessionId) {
                    _activeTurn.value = _activeTurn.value?.copy(isRunning = false)
                }
                releaseWakeLock()
                if (abortController == null || currentSessionId == sessionId) {
                    abortController = null
                }
            }
        }
    }

    private suspend fun persistEvent(
        store: SessionStore,
        sessionId: String,
        acc: TurnAccumulator,
        ev: AgentEvent
    ) {
        // Flush helper: writes the draft AND syncs _activeTurn so the UI can hide the
        // draft from the history list (otherwise it renders twice: once as a saved
        // bubble, once in the live StreamingBubble).
        suspend fun flush(force: Boolean = false) {
            val draft = acc.maybeFlushDraft(store, force)
            if (draft != null) {
                _activeTurn.value = TurnSnapshot(
                    sessionId = sessionId,
                    isRunning = true,
                    draftMessage = draft
                )
            }
        }
        when (ev) {
            is AgentEvent.TextDelta -> {
                acc.applyTextDelta(ev.accumulated)
                flush()
            }
            is AgentEvent.ThinkingDelta -> {
                acc.applyThinkingDelta(ev.segmentId, ev.delta)
                flush()
            }
            AgentEvent.ThinkingTurnEnded -> {
                acc.freezeCurrentThinkingSegment()
                flush()
            }
            is AgentEvent.ToolCallStarted -> {
                acc.startTool(ev.toolCallId, ev.name)
                flush()
            }
            is AgentEvent.ToolCallArgsDelta -> {
                acc.appendToolArgs(ev.toolCallId, ev.delta)
            }
            is AgentEvent.ToolProgress -> {
                acc.updateToolProgress(ev.toolCallId, ev.accumulated)
                flush()
            }
            is AgentEvent.ToolExecuting -> {
                acc.updateToolActivity(ev.toolCallId, ev.activity ?: ev.name)
                flush()
            }
            is AgentEvent.ToolFinished -> {
                acc.finishTool(ev.toolCallId, ev.name, ev.argumentsJson, ev.result)
                flush(force = true)
            }
            is AgentEvent.FileChanged -> {
                acc.recordProducedFile(ev.change.path)
            }
            is AgentEvent.Completed -> {
                val msg = acc.buildFinalMessage(
                    summaryFallback = ev.summary,
                    isError = false
                )
                if (msg != null) {
                    store.finalizeDraft(sessionId, msg)
                    _activeTurn.value = TurnSnapshot(
                        sessionId = sessionId,
                        isRunning = false,
                        draftMessage = msg
                    )
                } else {
                    // Degenerate turn produced nothing durable; drop the draft so we
                    // don't leave an empty stub message in the session.
                    acc.draftId?.let { store.dropDraft(sessionId, it) }
                    _activeTurn.value = null
                }
            }
            is AgentEvent.Failed -> {
                val msg = acc.buildFinalMessage(
                    summaryFallback = null,
                    isError = true,
                    errorSuffix = ev.message
                )
                if (msg != null) {
                    store.finalizeDraft(sessionId, msg)
                }
                _activeTurn.value = null
            }
            is AgentEvent.PlanReviewRequired -> {
                val msg = acc.buildFinalMessage(
                    summaryFallback = null,
                    isError = false
                )
                if (msg != null) {
                    store.finalizeDraft(sessionId, msg)
                    _activeTurn.value = TurnSnapshot(
                        sessionId = sessionId,
                        isRunning = false,
                        draftMessage = msg
                    )
                }
            }
            AgentEvent.Aborted -> {
                val msg = acc.buildFinalMessage(
                    summaryFallback = null,
                    isError = true,
                    errorSuffix = null,
                    aborted = true
                )
                if (msg != null) {
                    store.finalizeDraft(sessionId, msg)
                } else {
                    acc.draftId?.let { store.dropDraft(sessionId, it) }
                }
                _activeTurn.value = null
            }
            is AgentEvent.PermissionDenied, is AgentEvent.Usage, is AgentEvent.Notice,
            is AgentEvent.ApkBuilt,
            AgentEvent.Started -> {
                // No persistence impact.
            }
        }
    }

    private var currentSessionId: String? = null
    private var currentStore: SessionStore? = null
    private var currentAccumulator: TurnAccumulator? = null

    fun cancel() {
        // Signal abort FIRST so the engine's own abort checkpoints (AgentEvent.Aborted)
        // fire while the flow is still alive and the collector can persist the turn.
        abortController?.abort()
        val job = turnJob
        val sessionId = currentSessionId
        val store = currentStore
        val acc = currentAccumulator
        if (job != null) {
            // Give the engine a short window to emit Aborted (and let this collector
            // persist it) before the job is killed outright.
            scope.launch {
                kotlinx.coroutines.withTimeoutOrNull(ABORT_PERSIST_GRACE_MS) { job.join() }
                job.cancel()
            }
        }
        if (store != null && sessionId != null && acc != null) {
            // Belt-and-braces persistence in case the engine was suspended somewhere
            // that never observes the abort flag: finalize (or drop) the draft here so
            // no orphaned "running" draft with phantom tool entries survives.
            scope.launch {
                kotlinx.coroutines.withTimeoutOrNull(ABORT_PERSIST_GRACE_MS) {
                    runCatching { persistAbortedTurn(store, sessionId, acc) }
                }
            }
        }
        _isRunning.value = false
    }

    /**
     * Fallback finalization for an aborted turn, mirroring the engine's Aborted branch:
     * finalize the draft as aborted, or drop it when nothing substantive accumulated.
     * Safe to race with the engine's own Aborted handling — both paths produce the same
     * final message for the same draftId, and finalizeDraft is an upsert.
     */
    private suspend fun persistAbortedTurn(store: SessionStore, sessionId: String, acc: TurnAccumulator) {
        acc.finishRunningToolsAsAborted()
        val msg = acc.buildFinalMessage(
            summaryFallback = null,
            isError = true,
            errorSuffix = null,
            aborted = true
        )
        if (msg != null) {
            store.finalizeDraft(sessionId, msg)
            _activeTurn.value = null
        } else {
            acc.draftId?.let { store.dropDraft(sessionId, it) }
            _activeTurn.value = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        turnJob?.cancel()
        scope.cancel()
        releaseWakeLock()
        _activeTurn.value = null
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(CHANNEL_ID) == null) {
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, Strings.agentTitle, NotificationManager.IMPORTANCE_LOW)
            )
        }
    }

    private fun promoteToForeground(text: String) {
        if (inForeground) return
        val notification = buildNotification(text)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        inForeground = true
    }

    private fun buildNotification(text: String) = NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle(Strings.agentTitle)
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
        val pm = getSystemService(POWER_SERVICE) as PowerManager
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
        val sessionStore: SessionStore? = null,
        val temperature: Float = 0.7f,
        val maxTurns: Int = 24
    )

    /**
     * Live snapshot of a turn, exposed to the UI so it can resume display of an
     * in-flight turn after recreation.
     */
    data class TurnSnapshot(
        val sessionId: String,
        val isRunning: Boolean,
        val draftMessage: AgentMessage?
    )

    companion object {
        private const val TAG = "AgentService"
        private const val ABORT_PERSIST_GRACE_MS = 2000L
        private const val CHANNEL_ID = "aicoding_agent_v3"
        private const val NOTIFICATION_ID = 1201
        private const val WAKE_LOCK_TIMEOUT_MS = 20L * 60 * 1000
    }
}

/**
 * Accumulates a single turn's streaming output on the service side so it can be
 * persisted as a draft (and finalized) independently of any UI scope. Mirrors the
 * ViewModel's in-memory buffers but is purely data-oriented and persistence-driven.
 */
private class TurnAccumulator(private val sessionId: String) {
    private val text = StringBuilder()
    private val thinkingSegments = mutableListOf<ThinkingSegment>()
    private val tools = LinkedHashMap<String, RecordedToolCall>()
    private val toolArgs = LinkedHashMap<String, StringBuilder>()
    private val producedFiles = mutableListOf<String>()

    internal var draftId: String? = null
        private set

    private var lastFlushAt = 0L
    private val startedAt: Long = System.currentTimeMillis()

    private val inlineMarkerRegex = Regex("\u2063(?:TC|TH):[^\u2063]+\u2063")

    private data class ThinkingSegment(
        val id: String,
        var content: String,
        val startedAt: Long,
        var frozenDurationMs: Long? = null
    )

    fun applyTextDelta(accumulated: String) {
        text.setLength(0)
        text.append(accumulated)
    }

    fun applyThinkingDelta(segmentId: String, delta: String) {
        val now = System.currentTimeMillis()
        val idx = thinkingSegments.indexOfFirst { it.id == segmentId }
        if (idx < 0) {
            thinkingSegments += ThinkingSegment(segmentId, delta, now)
        } else {
            thinkingSegments[idx].content = thinkingSegments[idx].content + delta
        }
    }

    fun freezeCurrentThinkingSegment() {
        val now = System.currentTimeMillis()
        thinkingSegments.forEach { seg ->
            if (seg.frozenDurationMs == null) {
                seg.frozenDurationMs = now - seg.startedAt
            }
        }
    }

    fun startTool(id: String, name: String) {
        tools[id] = RecordedToolCall(
            toolCallId = id,
            name = name,
            argumentsJson = "",
            resultPreview = RecordedToolCall.RUNNING_SENTINEL,
            ok = true
        )
        toolArgs.remove(id)
    }

    fun appendToolArgs(id: String, delta: String) {
        toolArgs.getOrPut(id) { StringBuilder() }.append(delta)
        tools[id]?.let { tools[id] = it.copy(argumentsJson = toolArgs[id].toString()) }
    }

    fun updateToolProgress(id: String, accumulated: String) {
        val prev = tools[id] ?: return
        val capped = if (accumulated.length <= PREVIEW_CAP) accumulated
                     else accumulated.takeLast(PREVIEW_CAP)
        tools[id] = prev.copy(resultPreview = capped)
    }

    fun updateToolActivity(id: String, activity: String) {
        val prev = tools[id] ?: return
        tools[id] = prev.copy(activity = activity)
    }

    fun finishTool(id: String, name: String, argumentsJson: String, result: ToolResult) {
        val prev = tools[id]
        tools[id] = RecordedToolCall(
            toolCallId = id,
            name = name,
            argumentsJson = argumentsJson,
            resultPreview = result.text.take(2000),
            ok = !result.isError,
            activity = prev?.activity
        )
        toolArgs.remove(id)
    }

    fun recordProducedFile(path: String) {
        if (path !in producedFiles) producedFiles += path
    }

    /**
     * Marks tools still showing the running sentinel as failed/aborted so a cancelled
     * turn doesn't persist phantom "running" tool entries in the session history.
     */
    fun finishRunningToolsAsAborted() {
        val keys = tools.keys.toList()
        keys.forEach { id ->
            val t = tools[id] ?: return@forEach
            if (t.resultPreview == RecordedToolCall.RUNNING_SENTINEL) {
                tools[id] = t.copy(
                    resultPreview = Strings.agentAbortedHint,
                    ok = false
                )
            }
        }
        toolArgs.clear()
    }

    /**
     * Throttled draft upsert. Writes at most once per [FLUSH_INTERVAL_MS], unless
     * [force] is set (used for tool-finish boundaries and other durable events).
     */
    suspend fun maybeFlushDraft(store: SessionStore, force: Boolean = false): AgentMessage? {
        val now = System.currentTimeMillis()
        if (!force && now - lastFlushAt < FLUSH_INTERVAL_MS) return null
        lastFlushAt = now
        val draft = buildDraftMessage() ?: return null
        store.upsertAssistantDraft(sessionId, draft)
        return draft
    }

    private fun buildDraftMessage(): AgentMessage? {
        // Keep inline markers (both tool and thinking) in content: the timeline renderer
        // turns them into interleaved Tool/Thinking segments. Strip only to judge whether
        // there is any substantive content.
        val raw = text.toString().trim()
        val joined = joinedThinking()
        val segs = buildThinkingSegmentData()
        val hasSubstance = stripAllMarkers(raw).isNotBlank() || !joined.isNullOrBlank() || tools.isNotEmpty()
        if (!hasSubstance) return null
        val id = draftId ?: java.util.UUID.randomUUID().toString().also { draftId = it }
        return AgentMessage(
            id = id,
            role = AgentMessage.Role.ASSISTANT,
            content = raw,
            thinking = joined,
            thinkingSegments = segs,
            thinkingDurationMs = totalThinkingDurationMs(),
            toolCalls = tools.values.toList(),
            producedFiles = producedFiles.toList()
        )
    }

    fun buildFinalMessage(
        summaryFallback: String?,
        isError: Boolean,
        errorSuffix: String? = null,
        aborted: Boolean = false
    ): AgentMessage? {
        val raw = text.toString().trim()
        val joined = joinedThinking()
        val segs = buildThinkingSegmentData()
        val hasSubstance = stripAllMarkers(raw).isNotBlank() || !joined.isNullOrBlank() || tools.isNotEmpty()
        if (!hasSubstance) return null

        val baseText = if (stripAllMarkers(raw).isNotBlank()) raw
                       else stripAllMarkers(summaryFallback.orEmpty()).trim()

        val content = buildString {
            append(baseText)
            if (aborted && baseText.isNotBlank()) {
                append("\n\n")
                append(Strings.agentAbortedHint)
            }
            if (errorSuffix != null) {
                if (isNotEmpty()) append("\n\n")
                append(Strings.agentErrorPrefix.format(errorSuffix))
            }
        }.ifBlank { Strings.agentNoOutput }

        val id = draftId ?: java.util.UUID.randomUUID().toString().also { draftId = it }
        return AgentMessage(
            id = id,
            role = AgentMessage.Role.ASSISTANT,
            content = content,
            thinking = joined,
            thinkingSegments = segs,
            thinkingDurationMs = totalThinkingDurationMs(),
            toolCalls = tools.values.toList(),
            producedFiles = producedFiles.toList(),
            isError = isError || aborted
        )
    }

    private fun buildThinkingSegmentData(): List<ThinkingSegmentData> {
        if (thinkingSegments.isEmpty()) return emptyList()
        val now = System.currentTimeMillis()
        return thinkingSegments.map { seg ->
            ThinkingSegmentData(
                id = seg.id,
                content = seg.content.trim(),
                durationMs = seg.frozenDurationMs ?: (now - seg.startedAt).coerceAtLeast(0L)
            )
        }
    }

    private fun stripAllMarkers(s: String): String =
        if (s.isEmpty() || '\u2063' !in s) s else inlineMarkerRegex.replace(s, "")

    private fun joinedThinking(): String? {
        val joined = thinkingSegments.joinToString("\n\n") { it.content.trim() }
            .replace(Regex("\n{3,}"), "\n\n")
            .trim()
        return joined.takeIf { it.isNotBlank() }
    }

    private fun totalThinkingDurationMs(): Long? {
        if (thinkingSegments.isEmpty()) return null
        val now = System.currentTimeMillis()
        val total = thinkingSegments.sumOf { seg ->
            seg.frozenDurationMs ?: (now - seg.startedAt).coerceAtLeast(0L)
        }
        return total.takeIf { it > 0 }
    }

    companion object {
        // Draft persistence cadence: each flush rewrites the whole sessions blob in
        // DataStore, so this trades crash-recovery granularity against streaming-time
        // serialization cost (500ms keeps at most ~1s of output at risk).
        private const val FLUSH_INTERVAL_MS = 500L
        private const val PREVIEW_CAP = 2000
    }
}