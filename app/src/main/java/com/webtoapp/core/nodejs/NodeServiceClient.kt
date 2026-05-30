package com.webtoapp.core.nodejs

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.core.shell.ShellLogger
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object NodeServiceClient {

    private const val TAG = "NodeServiceClient"

    private val callbackThread = HandlerThread("NodeServiceClient-cb").apply { start() }
    private val callbackHandler = Handler(callbackThread.looper)

    private val replyMessenger = Messenger(ReplyHandler(callbackThread.looper))

    @Volatile private var serviceMessenger: Messenger? = null
    @Volatile private var bindAttempted = false
    @Volatile private var appContext: Context? = null

    @Volatile private var connectDeferred = CompletableDeferred<Unit>()

    private val pendingStartRequests = ConcurrentHashMap<String, CompletableDeferred<StartResult>>()

    @Volatile private var lastServerUrl: String? = null

    sealed class StartResult {
        data class Success(val url: String, val port: Int) : StartResult()
        data class Failure(val message: String, val v8Exhausted: Boolean) : StartResult()
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            AppLogger.i(TAG, "已连接 :nodejs NodeService binding=$name")
            ShellLogger.i(TAG, "已连接 :nodejs NodeService binding=$name")
            serviceMessenger = Messenger(service)

            if (!connectDeferred.isCompleted) {
                connectDeferred.complete(Unit)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            AppLogger.w(TAG, ":nodejs NodeService 进程已断开 (可能已被 kill 重建)")
            ShellLogger.w(TAG, ":nodejs NodeService 进程已断开 (可能已被 kill 重建)")
            serviceMessenger = null
            lastServerUrl = null

            connectDeferred = CompletableDeferred()

            pendingStartRequests.values.forEach { def ->
                if (!def.isCompleted) {
                    def.complete(StartResult.Failure(":nodejs 进程已断开", v8Exhausted = false))
                }
            }
            pendingStartRequests.clear()
        }
    }

    private fun ensureBound(context: Context) {
        if (serviceMessenger != null) return
        synchronized(this) {
            if (serviceMessenger != null) return
            if (appContext == null) appContext = context.applicationContext
            val intent = Intent(appContext, NodeService::class.java)
            val ok = appContext!!.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            bindAttempted = ok
            if (!ok) {
                AppLogger.e(TAG, "bindService(NodeService) 失败 — manifest 是否正确声明了 :nodejs 进程？")
                ShellLogger.e(TAG, "bindService(NodeService) 失败 — manifest 是否正确声明了 :nodejs 进程？")
            }
        }
    }

    suspend fun startServer(
        context: Context,
        projectDir: String,
        entryFile: String,
        portPref: Int,
        envVars: Map<String, String>
    ): StartResult = withContext(Dispatchers.IO) {
        ensureBound(context)

        val connected = withTimeoutOrNull(5_000L) { connectDeferred.await() }
        if (connected == null || serviceMessenger == null) {
            return@withContext StartResult.Failure("无法连接 :nodejs 子进程 (bindService 超时)", v8Exhausted = false)
        }

        val requestId = UUID.randomUUID().toString()
        val deferred = CompletableDeferred<StartResult>()
        pendingStartRequests[requestId] = deferred

        val data = Bundle().apply {
            putString(NodeServiceProtocol.Keys.REQUEST_ID, requestId)
            putString(NodeServiceProtocol.Keys.PROJECT_DIR, projectDir)
            putString(NodeServiceProtocol.Keys.ENTRY_FILE, entryFile)
            putInt(NodeServiceProtocol.Keys.PORT_PREF, portPref)
            val envBundle = Bundle()
            envVars.forEach { (k, v) -> envBundle.putString(k, v) }
            putBundle(NodeServiceProtocol.Keys.ENV_VARS, envBundle)
        }
        val msg = Message.obtain().apply {
            what = NodeServiceProtocol.MSG_START_SERVER
            this.data = data
            replyTo = replyMessenger
        }
        try {
            serviceMessenger!!.send(msg)
        } catch (e: RemoteException) {
            pendingStartRequests.remove(requestId)
            return@withContext StartResult.Failure(":nodejs 子进程发包失败: ${e.message}", v8Exhausted = false)
        }

        val result = withTimeoutOrNull(60_000L) { deferred.await() }
            ?: StartResult.Failure("等 :nodejs 子进程回包超时", v8Exhausted = false)
        pendingStartRequests.remove(requestId)
        if (result is StartResult.Success) {
            lastServerUrl = result.url
        }
        result
    }

    suspend fun stopServer(context: Context) = withContext(Dispatchers.IO) {
        if (serviceMessenger == null) return@withContext
        val msg = Message.obtain().apply {
            what = NodeServiceProtocol.MSG_STOP_SERVER
            replyTo = replyMessenger
        }
        try {
            serviceMessenger?.send(msg)
        } catch (e: RemoteException) {
            AppLogger.w(TAG, "stopServer IPC 发包失败: ${e.message}")
        }
        lastServerUrl = null
    }

    suspend fun restartEngine(context: Context) = withContext(Dispatchers.IO) {
        ensureBound(context)
        val current = serviceMessenger
        if (current != null) {
            try {
                current.send(Message.obtain().apply { what = NodeServiceProtocol.MSG_KILL_ENGINE })
            } catch (e: RemoteException) {

                AppLogger.d(TAG, "KILL_ENGINE 发包时 IPC 已断: ${e.message}")
            }
        }

        val reconnected = withTimeoutOrNull(8_000L) { connectDeferred.await() }
        if (reconnected == null) {
            AppLogger.w(TAG, "restartEngine：等 :nodejs 重新 connect 超时")
        } else {
            AppLogger.i(TAG, "restartEngine：:nodejs 子进程已重建")
        }

        delay(200)
    }

    fun getCachedServerUrl(): String? = lastServerUrl

    fun isProbablyConnected(): Boolean = serviceMessenger != null

    suspend fun queryStatus(context: Context): Status = withContext(Dispatchers.IO) {
        ensureBound(context)
        val connected = withTimeoutOrNull(2_000L) { connectDeferred.await() }
        if (connected == null || serviceMessenger == null) {
            return@withContext Status(v8Started = false, serverRunning = false, serverUrl = null)
        }
        val deferred = CompletableDeferred<Status>()
        pendingStatusDeferred = deferred
        try {
            serviceMessenger!!.send(Message.obtain().apply {
                what = NodeServiceProtocol.MSG_QUERY_STATUS
                replyTo = replyMessenger
            })
        } catch (e: RemoteException) {
            return@withContext Status(false, false, null)
        }
        withTimeoutOrNull(2_000L) { deferred.await() }
            ?: Status(false, false, null)
    }

    data class Status(
        val v8Started: Boolean,
        val serverRunning: Boolean,
        val serverUrl: String?,
    )

    @Volatile private var pendingStatusDeferred: CompletableDeferred<Status>? = null

    private class ReplyHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            val data = msg.data ?: Bundle()
            val requestId = data.getString(NodeServiceProtocol.Keys.REQUEST_ID).orEmpty()
            when (msg.what) {
                NodeServiceProtocol.MSG_SERVER_STARTED -> {
                    val port = data.getInt(NodeServiceProtocol.Keys.ACTUAL_PORT, 0)
                    val url = data.getString(NodeServiceProtocol.Keys.SERVER_URL).orEmpty()
                    val def = pendingStartRequests.remove(requestId)
                    def?.complete(StartResult.Success(url, port))
                }
                NodeServiceProtocol.MSG_SERVER_FAILED -> {
                    val message = data.getString(NodeServiceProtocol.Keys.ERROR_MESSAGE).orEmpty()
                    val v8Exhausted = message.contains("V8 已 init 过且 server 不在跑")
                    val def = pendingStartRequests.remove(requestId)
                    def?.complete(StartResult.Failure(message, v8Exhausted))
                }
                NodeServiceProtocol.MSG_SERVER_STOPPED -> {

                }
                NodeServiceProtocol.MSG_STATUS -> {
                    val v8 = data.getBoolean(NodeServiceProtocol.Keys.V8_STARTED, false)
                    val running = data.getBoolean(NodeServiceProtocol.Keys.SERVER_RUNNING, false)
                    val url = data.getString(NodeServiceProtocol.Keys.SERVER_URL)
                    pendingStatusDeferred?.complete(Status(v8, running, url))
                    pendingStatusDeferred = null
                }
                else -> super.handleMessage(msg)
            }
        }
    }
}
