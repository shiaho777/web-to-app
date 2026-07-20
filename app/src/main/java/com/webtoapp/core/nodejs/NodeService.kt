package com.webtoapp.core.nodejs

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.os.Process
import android.os.RemoteException
import com.webtoapp.core.linux.LocalDnsBridgeProxy
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.core.port.PortManager
import com.webtoapp.core.shell.ShellLogger
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class NodeService : Service() {

    companion object {
        private const val TAG = "NodeService"
        private const val MAX_HEALTH_CHECK_RETRIES = 60
        private const val HEALTH_CHECK_INTERVAL_MS = 500L
    }

    private val workerThread = HandlerThread("NodeService-worker").apply { start() }
    private val workerHandler = Handler(workerThread.looper)

    private val incomingMessenger = Messenger(IncomingHandler(this))

    @Volatile private var nodeThread: Thread? = null
    @Volatile private var currentPort: Int = 0
    @Volatile private var isRunning = false
    @Volatile private var dnsProxyStarted = false

    override fun onBind(intent: Intent?): IBinder = incomingMessenger.binder

    override fun onCreate() {
        super.onCreate()
        android.util.Log.i(TAG, ":nodejs 子进程 NodeService onCreate, pid=${Process.myPid()}")
        AppLogger.i(TAG, ":nodejs 子进程 NodeService onCreate, pid=${Process.myPid()}")
        ShellLogger.i(TAG, ":nodejs 子进程 NodeService onCreate, pid=${Process.myPid()}")
    }

    override fun onDestroy() {
        AppLogger.i(TAG, ":nodejs 子进程 NodeService onDestroy")
        try { stopServerInternal() } catch (_: Exception) {}
        workerThread.quitSafely()
        super.onDestroy()
    }

    private class IncomingHandler(service: NodeService) : Handler(Looper.getMainLooper()) {

        private val service = service

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                NodeServiceProtocol.MSG_START_SERVER -> {
                    val data = msg.data ?: Bundle()
                    val replyTo = msg.replyTo
                    service.workerHandler.post {
                        service.handleStartServer(data, replyTo)
                    }
                }
                NodeServiceProtocol.MSG_STOP_SERVER -> {
                    val replyTo = msg.replyTo
                    service.workerHandler.post {
                        service.stopServerInternal()
                        sendBack(replyTo, NodeServiceProtocol.MSG_SERVER_STOPPED, Bundle())
                    }
                }
                NodeServiceProtocol.MSG_KILL_ENGINE -> {
                    AppLogger.w(TAG, "收到 KILL_ENGINE，主动结束 :nodejs 进程，等待 Framework 重建")
                    ShellLogger.w(TAG, "收到 KILL_ENGINE，主动结束 :nodejs 进程，等待 Framework 重建")

                    Process.killProcess(Process.myPid())
                }
                NodeServiceProtocol.MSG_QUERY_STATUS -> {
                    val replyTo = msg.replyTo
                    val bundle = Bundle().apply {
                        putBoolean(NodeServiceProtocol.Keys.V8_STARTED, NodeBridge.isStarted())
                        putBoolean(NodeServiceProtocol.Keys.SERVER_RUNNING, service.isServerRunningInternal())
                        if (service.currentPort > 0 && service.isServerRunningInternal()) {
                            putInt(NodeServiceProtocol.Keys.ACTUAL_PORT, service.currentPort)
                            putString(
                                NodeServiceProtocol.Keys.SERVER_URL,
                                "http://127.0.0.1:${service.currentPort}"
                            )
                        }
                    }
                    sendBack(replyTo, NodeServiceProtocol.MSG_STATUS, bundle)
                }
                else -> super.handleMessage(msg)
            }
        }

        private fun sendBack(replyTo: Messenger?, what: Int, data: Bundle) {
            replyTo ?: return
            try {
                val response = Message.obtain().apply {
                    this.what = what
                    this.data = data
                }
                replyTo.send(response)
            } catch (e: RemoteException) {
                AppLogger.w(TAG, "回送 IPC 消息失败 (主进程可能已断开): ${e.message}")
            }
        }
    }

    private fun handleStartServer(data: Bundle, replyTo: Messenger?) {
        val requestId = data.getString(NodeServiceProtocol.Keys.REQUEST_ID).orEmpty()
        val projectDir = data.getString(NodeServiceProtocol.Keys.PROJECT_DIR).orEmpty()
        val entryFile = data.getString(NodeServiceProtocol.Keys.ENTRY_FILE) ?: "index.js"
        val portPref = data.getInt(NodeServiceProtocol.Keys.PORT_PREF, 0)
        val envBundle = data.getBundle(NodeServiceProtocol.Keys.ENV_VARS) ?: Bundle()
        val envVars = envBundle.keySet().associateWith { envBundle.getString(it).orEmpty() }

        android.util.Log.i(
            TAG,
            "handleStartServer: projectDir=$projectDir, entry=$entryFile, portPref=$portPref"
        )

        try {
            if (projectDir.isBlank()) {
                replyFailed(replyTo, requestId, "projectDir is empty")
                return
            }

            if (isServerRunningInternal() && currentPort > 0) {
                AppLogger.i(TAG, "Node server 已经在跑，复用 port=$currentPort")
                val out = Bundle().apply {
                    putString(NodeServiceProtocol.Keys.REQUEST_ID, requestId)
                    putInt(NodeServiceProtocol.Keys.ACTUAL_PORT, currentPort)
                    putString(NodeServiceProtocol.Keys.SERVER_URL, "http://127.0.0.1:$currentPort")
                }
                sendBackBlocking(replyTo, NodeServiceProtocol.MSG_SERVER_STARTED, out)
                return
            }

            android.util.Log.i(TAG, "调用 NodeBridge.loadNode...")
            if (!NodeBridge.loadJniBridge()) {
                android.util.Log.e(TAG, "NodeBridge.loadJniBridge 失败")
                replyFailed(
                    replyTo,
                    requestId,
                    "libnode_bridge.so 加载失败。导出的 NODEJS_APP 需包含该库；请用含原生 node_bridge 的构建器重新导出。"
                )
                return
            }
            if (!NodeBridge.loadNode(this)) {
                android.util.Log.e(TAG, "NodeBridge.loadNode 失败")
                val path = NodeDependencyManager.getNodeLibraryPath(this)
                replyFailed(
                    replyTo,
                    requestId,
                    "libnode.so 加载失败 ($path)。请确认 APK 含 16KB 对齐的 libnode.so，或在主机下载 Node 运行时后重新导出。"
                )
                return
            }
            android.util.Log.i(TAG, "NodeBridge.loadNode 成功, isStarted=${NodeBridge.isStarted()}")

            if (NodeBridge.isStarted()) {
                replyFailed(
                    replyTo,
                    requestId,
                    "V8 已 init 过且 server 不在跑，请发送 MSG_KILL_ENGINE 重建子进程"
                )
                return
            }

            val entryFilePath = File(projectDir, entryFile).absolutePath
            if (!File(entryFilePath).exists()) {
                replyFailed(replyTo, requestId, "入口文件不存在: $entryFile")
                return
            }

            val projectId = File(projectDir).name
            val serverPort = PortManager.allocateForNodeJs(
                projectId,
                portPref,
                conflictPolicy = if (portPref > 0) PortManager.ConflictPolicy.AUTO_KILL else PortManager.ConflictPolicy.REASSIGN
            )
            if (serverPort == PortManager.PORT_CONFLICT) {
                replyFailed(replyTo, requestId, "端口被占用: $portPref")
                return
            }
            if (serverPort < 0) {
                replyFailed(replyTo, requestId, "无法分配端口")
                return
            }
            currentPort = serverPort

            val bootstrapPath = File(projectDir, ".webtoapp_bootstrap.cjs")
            val entryEscaped = entryFilePath.replace("\\", "\\\\").replace("\"", "\\\"")
            bootstrapPath.writeText(
                """
                'use strict';
                (function () {
                    try {
                        var proxyUrl = process.env.HTTPS_PROXY || process.env.HTTP_PROXY || process.env.https_proxy || process.env.http_proxy;
                        if (proxyUrl) {
                            var undici = require('undici');
                            if (undici && undici.ProxyAgent && undici.setGlobalDispatcher) {
                                undici.setGlobalDispatcher(new undici.ProxyAgent(proxyUrl));
                            }
                        }
                    } catch (e) {
                        console.error('[wta-bootstrap] proxy dispatcher setup skipped:', (e && e.message) || e);
                    }
                })();
                process.on('uncaughtException', function (err) {
                    console.error('[wta-bootstrap] uncaughtException:', (err && (err.stack || err.message)) || err);
                });
                process.on('unhandledRejection', function (reason) {
                    console.error('[wta-bootstrap] unhandledRejection:', (reason && (reason.stack || reason.message)) || reason);
                });
                process.exit = function (code) {
                    console.error('[wta-bootstrap] process.exit(' + code + ') intercepted; keeping :nodejs process alive.');
                };
                setInterval(function () {}, 60000);
                try {
                    require("$entryEscaped");
                } catch (e) {
                    console.error('[wta-bootstrap] entry failed:', (e && (e.stack || e.message)) || e);
                }
                """.trimIndent()
            )

            val proxyPort = LocalDnsBridgeProxy.start()
            if (proxyPort > 0) {
                dnsProxyStarted = true
                AppLogger.i(TAG, "已启用 DNS 桥接代理 (port=$proxyPort) 供 Node.js 进程解析外部域名")
            }

            setEnvironmentVars(projectDir, serverPort, envVars)

            val args = arrayOf("node", bootstrapPath.absolutePath)
            android.util.Log.i(TAG, "启动 Node.js (子进程): bootstrap=${bootstrapPath.absolutePath}, port=$serverPort")
            AppLogger.i(TAG, "启动 Node.js 服务器 (子进程): ${args.joinToString(" ")}")
            ShellLogger.i(TAG, "启动 Node.js 服务器 (子进程): ${args.joinToString(" ")}, port=$serverPort")

            isRunning = true
            val outputCallback = object : NodeBridge.OutputCallback {
                override fun onOutput(line: String, isError: Boolean) {

                    if (isError) {
                        android.util.Log.w("$TAG-Node", line)
                        AppLogger.w(TAG, "[Node stderr] $line")
                        ShellLogger.w(TAG, "[Node stderr] $line")
                    } else {
                        android.util.Log.i("$TAG-Node", line)
                        AppLogger.d(TAG, "[Node] $line")
                        ShellLogger.d(TAG, "[Node] $line")
                    }
                }
            }

            nodeThread = Thread({
                try {
                    android.util.Log.i(TAG, "Node thread 启动")
                    val exitCode = NodeBridge.startNode(args, outputCallback)
                    android.util.Log.i(TAG, "Node.js 退出, exitCode=$exitCode")
                    AppLogger.i(TAG, "Node.js 退出, exitCode=$exitCode")
                } catch (e: Exception) {
                    android.util.Log.e(TAG, "Node.js 线程异常", e)
                    AppLogger.e(TAG, "Node.js 线程异常", e)
                } finally {
                    isRunning = false
                    if (currentPort > 0) {
                        PortManager.release(currentPort)
                        currentPort = 0
                    }
                }
            }, "NodeJS-Runtime").apply {
                isDaemon = true
                start()
            }

            try { Thread.sleep(300) } catch (_: InterruptedException) {}
            if (nodeThread?.isAlive != true) {
                replyFailed(replyTo, requestId, "Node.js 启动后立即退出")
                return
            }

            val ready = waitForServerReady(serverPort)
            if (ready) {
                AppLogger.i(TAG, "Node.js 服务器已启动: 127.0.0.1:$serverPort")
                ShellLogger.i(TAG, "Node.js 服务器已启动: 127.0.0.1:$serverPort")
                val out = Bundle().apply {
                    putString(NodeServiceProtocol.Keys.REQUEST_ID, requestId)
                    putInt(NodeServiceProtocol.Keys.ACTUAL_PORT, serverPort)
                    putString(NodeServiceProtocol.Keys.SERVER_URL, "http://127.0.0.1:$serverPort")
                }
                sendBackBlocking(replyTo, NodeServiceProtocol.MSG_SERVER_STARTED, out)
            } else {
                stopServerInternal()
                replyFailed(replyTo, requestId, "Node.js 服务器启动超时")
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "handleStartServer 异常", e)
            replyFailed(replyTo, requestId, "启动失败: ${e.message}")
        }
    }

    private fun stopServerInternal() {
        try {
            nodeThread?.let { thread ->
                if (thread.isAlive) {
                    thread.interrupt()
                    thread.join(2000)
                }
            }
        } catch (e: Exception) {
            AppLogger.w(TAG, "停止 Node.js 服务器异常: ${e.message}")
        } finally {
            if (currentPort > 0) {
                PortManager.release(currentPort)
            }
            if (dnsProxyStarted) {
                LocalDnsBridgeProxy.stop()
                dnsProxyStarted = false
            }
            nodeThread = null
            currentPort = 0
            isRunning = false
        }
    }

    private fun isServerRunningInternal(): Boolean = isRunning && nodeThread?.isAlive == true

    private fun setEnvironmentVars(projectDir: String, port: Int, envVars: Map<String, String>) {
        try {
            val envMap = mutableMapOf(
                "HOME" to filesDir.absolutePath,
                "TMPDIR" to cacheDir.absolutePath,
                "NODE_ENV" to "production",
                "PORT" to port.toString(),
                "HOST" to "127.0.0.1",
                "NODE_PATH" to File(projectDir, "node_modules").absolutePath
            )
            envMap.putAll(envVars)
            val dnsProxyPort = LocalDnsBridgeProxy.getListenPort()
            if (dnsProxyPort > 0) {
                LocalDnsBridgeProxy.proxyEnvFor(dnsProxyPort).forEach { (k, v) -> envMap[k] = v }
            }
            for ((key, value) in envMap) {
                try {
                    android.system.Os.setenv(key, value, true)
                } catch (e: Exception) {
                    AppLogger.w(TAG, "设置环境变量失败: $key=$value, ${e.message}")
                }
            }
        } catch (e: Exception) {
            AppLogger.w(TAG, "设置环境变量异常", e)
        }
    }

    private fun waitForServerReady(port: Int): Boolean {
        repeat(MAX_HEALTH_CHECK_RETRIES) { attempt ->
            var conn: HttpURLConnection? = null
            try {
                conn = URL("http://127.0.0.1:$port/").openConnection() as HttpURLConnection
                conn.connectTimeout = 500
                conn.readTimeout = 500
                conn.requestMethod = "GET"
                val code = conn.responseCode
                if (code in 200..499) {
                    AppLogger.i(TAG, "Node.js 服务器就绪 (尝试 ${attempt + 1})")
                    return true
                }
            } catch (e: Exception) {
                AppLogger.d(TAG, "Health check attempt failed: ${e.message}")
            } finally {
                try { conn?.disconnect() } catch (_: Exception) {}
            }
            if (nodeThread?.isAlive != true) return false
            try { Thread.sleep(HEALTH_CHECK_INTERVAL_MS) } catch (_: InterruptedException) { return false }
        }
        return false
    }

    private fun replyFailed(replyTo: Messenger?, requestId: String, message: String) {
        AppLogger.w(TAG, "回送 SERVER_FAILED: $message")
        ShellLogger.w(TAG, "回送 SERVER_FAILED: $message")
        sendBackBlocking(
            replyTo,
            NodeServiceProtocol.MSG_SERVER_FAILED,
            Bundle().apply {
                putString(NodeServiceProtocol.Keys.REQUEST_ID, requestId)
                putString(NodeServiceProtocol.Keys.ERROR_MESSAGE, message)
            }
        )
    }

    private fun sendBackBlocking(replyTo: Messenger?, what: Int, data: Bundle) {
        replyTo ?: return
        try {
            replyTo.send(Message.obtain().apply {
                this.what = what
                this.data = data
            })
        } catch (e: RemoteException) {
            AppLogger.w(TAG, "IPC 回包失败 (主进程已断开?): ${e.message}")
        }
    }
}
