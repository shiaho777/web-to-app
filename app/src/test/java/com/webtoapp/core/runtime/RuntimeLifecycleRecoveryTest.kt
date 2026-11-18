package com.webtoapp.core.runtime

import com.google.common.truth.Truth.assertWithMessage
import com.webtoapp.core.nodejs.NodeServiceProtocol
import org.junit.Test
import java.io.File

class RuntimeLifecycleRecoveryTest {

    private fun javaRoot(): File = listOf("app/src/main/java", "src/main/java").asSequence()
        .map(::File).firstOrNull(File::exists) ?: error("Cannot locate java directory")

    private fun readSanitized(relativePath: String): String {
        val file = File(javaRoot(), relativePath)
        assertWithMessage("Parity harness cannot find source file: $relativePath")
            .that(file.isFile).isTrue()
        var s = file.readText()
        s = Regex("\"\"\".*?\"\"\"", RegexOption.DOT_MATCHES_ALL).replace(s, "\"\"")
        s = Regex("\"(?:\\\\.|[^\"\\\\])*+\"").replace(s, "\"\"")
        s = Regex("/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL).replace(s, " ")
        s = s.lines().joinToString("\n") { it.substringBefore("//") }
        return s
    }

    @Test
    fun `v8 exhaustion travels as a structured flag, not a localized string match`() {
        val client = readSanitized("com/webtoapp/core/nodejs/NodeServiceClient.kt")
        val failedBranch = client.substringAfter("NodeServiceProtocol.MSG_SERVER_FAILED ->")
            .substringBefore("NodeServiceProtocol.MSG_SERVER_STOPPED")
        assertWithMessage("the client must read the structured flag")
            .that(failedBranch).contains("data.getBoolean(NodeServiceProtocol.Keys.V8_EXHAUSTED")
        assertWithMessage("no localized substring matching may remain")
            .that(failedBranch).doesNotContain("message.contains(")
        val service = readSanitized("com/webtoapp/core/nodejs/NodeService.kt")
        assertWithMessage("the V8-already-initialized failure must set the flag")
            .that(service).contains("v8Exhausted = true")
        assertWithMessage("replyFailed must carry the flag in the bundle")
            .that(service).contains("putBoolean(NodeServiceProtocol.Keys.V8_EXHAUSTED")
        assertWithMessage("protocol must define the flag key")
            .that(NodeServiceProtocol.Keys.V8_EXHAUSTED).isEqualTo("v8Exhausted")
    }

    @Test
    fun `start timeout escalates a stuck V8 thread to process kill`() {
        val service = readSanitized("com/webtoapp/core/nodejs/NodeService.kt")
        val timeoutBlock = service.substringAfter("val wasAlive = nodeThread?.isAlive == true")
            .substringBefore("} catch (e: Exception) {")
        assertWithMessage("the stop result must be captured")
            .that(timeoutBlock).contains("val threadStuck = stopServerInternal()")
        assertWithMessage("the reply must be sent before self-terminating")
            .that(timeoutBlock.indexOf("replyFailed")).isLessThan(timeoutBlock.indexOf("Process.killProcess"))
        assertWithMessage("a stuck thread must kill the process for a clean rebuild")
            .that(timeoutBlock).contains("if (threadStuck)")
    }

    @Test
    fun `node runtime resets state when the child process dies`() {
        val client = readSanitized("com/webtoapp/core/nodejs/NodeServiceClient.kt")
        assertWithMessage("disconnect must notify death listeners")
            .that(client).contains("serviceDeathListeners.forEach")
        val runtime = readSanitized("com/webtoapp/core/nodejs/NodeRuntime.kt")
        assertWithMessage("the runtime must register a death listener")
            .that(runtime).contains("NodeServiceClient.addServiceDeathListener(serviceDeathListener)")
        val listener = runtime.substringAfter("private val serviceDeathListener")
            .substringBefore("fun isNodeAvailable()")
        assertWithMessage("the listener must release the tracked port")
            .that(listener).contains("PortManager.release(port)")
        assertWithMessage("the listener must reset the running state")
            .that(listener).contains("_serverState.value = ServerState.Stopped")
    }

    @Test
    fun `local http start failure releases the claimed port even when bind never succeeded`() {
        val server = readSanitized("com/webtoapp/core/webview/LocalHttpServer.kt")
        assertWithMessage("the claimed port must be tracked before bind")
            .that(server).contains("claimedPort = allocatedPort")
        val catchBlock = server.substringAfter("} catch (e: Exception) {")
            .substringBefore("fun stop()")
        assertWithMessage("failure cleanup must fall back to the claimed port")
            .that(catchBlock).contains("if (actualPort > 0) actualPort else claimedPort")
    }

    @Test
    fun `polling notifications dedupe by content fingerprint`() {
        val service = readSanitized("com/webtoapp/core/notification/NotificationPollingService.kt")
        assertWithMessage("a bounded seen-set must exist")
            .that(service).contains("seenNotificationFingerprints")
        val showFn = service.substringAfter("private fun showPollNotification")
        assertWithMessage("already-seen content must be skipped")
            .that(showFn).contains("if (seenNotificationFingerprints.containsKey(fingerprint)) return")
        assertWithMessage("the notification id must be content-stable, not time-derived")
            .that(showFn).doesNotContain("System.currentTimeMillis()")
    }

    @Test
    fun `websocket explicit stop suppresses the destroy-time restart`() {
        val service = readSanitized("com/webtoapp/core/notification/NotificationWebSocketService.kt")
        val stopFn = service.substringAfter("fun stop(context: Context)")
            .substringBefore("fun isServiceRunning()")
        assertWithMessage("explicit stop must raise the stop flag")
            .that(stopFn).contains("stopRequested = true")
        val destroy = service.substringAfter("override fun onDestroy()")
            .substringBefore("override fun onTaskRemoved")
        assertWithMessage("destroy must not restart after an explicit stop")
            .that(destroy).contains("if (!stopRequested && shouldReconnect.get()")
        val startFn = service.substringAfter("fun start(")
            .substringBefore("fun stop(context: Context)")
        assertWithMessage("a fresh start must clear the stop flag")
            .that(startFn).contains("stopRequested = false")
    }

    @Test
    fun `python post-start crash releases port and proxy and surfaces an error state`() {
        val runtime = readSanitized("com/webtoapp/core/python/PythonRuntime.kt")
        val watcher = runtime.substringAfter("private fun attachProcessExitWatcher")
        assertWithMessage("the watcher must release the port")
            .that(watcher).contains("PortManager.release(port)")
        assertWithMessage("the watcher must drop the DNS proxy refcount")
            .that(watcher).contains("LocalDnsBridgeProxy.stop()")
        assertWithMessage("the watcher must surface an error state, not stay Running")
            .that(watcher).contains("_serverState.value = ServerState.Error(Strings.pythonProcessExited(exitCode))")
    }
}
