package com.webtoapp.core.port

import com.webtoapp.core.logging.AppLogger
import com.webtoapp.util.destroyForciblyCompat
import com.webtoapp.util.destroyGracefullyCompat
import com.webtoapp.util.isAliveCompat
import java.net.ServerSocket
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

object PortManager {

    private const val TAG = "PortManager"

    private const val STALE_THRESHOLD_MS = 30_000L

    private const val STALE_NO_PROCESS_THRESHOLD_MS = 120_000L

    private const val AUTO_KILL_WAIT_MS = 120L

    const val PORT_UNAVAILABLE = -1

    const val PORT_CONFLICT = -2

    enum class PortRange(val start: Int, val end: Int) {
        LOCAL_HTTP(18000, 18499),
        PHP(18500, 18999),
        NODEJS(19000, 19499),
        PYTHON(19500, 19999),
        GO(20000, 20499),
        GENERAL(20500, 21000);

        val size: Int get() = end - start + 1
    }

    enum class ConflictPolicy {
        REASSIGN,
        AUTO_KILL,
        ALERT;

        companion object {
            fun fromName(name: String?): ConflictPolicy {
                return when (name?.trim()?.uppercase()) {
                    "AUTO_KILL" -> AUTO_KILL
                    "ALERT" -> ALERT
                    else -> REASSIGN
                }
            }
        }
    }

    fun interface StopHandler {
        fun onStop(port: Int)
    }

    private val lock = ReentrantReadWriteLock()

    private val allocatedPorts = ConcurrentHashMap<Int, PortAllocation>()

    private val portProcesses = ConcurrentHashMap<Int, Process>()

    private val portStopHandlers = ConcurrentHashMap<Int, StopHandler>()

    data class PortAllocation(
        val port: Int,
        val owner: String,
        val range: PortRange,
        val allocatedAt: Long = System.currentTimeMillis(),
        val pid: Long = -1,
        val external: Boolean = false
    ) {
        val uptimeMs: Long get() = System.currentTimeMillis() - allocatedAt
    }

    data class RangeStats(
        val range: PortRange,
        val allocated: Int,
        val total: Int,
        val usagePercent: Float
    )

    fun allocate(
        range: PortRange,
        owner: String,
        preferredPort: Int = 0,
        conflictPolicy: ConflictPolicy = ConflictPolicy.REASSIGN
    ): Int {
        purgeStaleAllocations()

        if (preferredPort > 0) {
            val preferred = tryClaimPort(range, owner, preferredPort, allowPreferredOnly = true)
            if (preferred > 0) return preferred

            when (conflictPolicy) {
                ConflictPolicy.ALERT -> {
                    AppLogger.w(TAG, "首选端口 $preferredPort 冲突 (ALERT), owner=$owner")
                    return PORT_CONFLICT
                }
                ConflictPolicy.AUTO_KILL -> {
                    AppLogger.w(TAG, "首选端口 $preferredPort 被占用，AUTO_KILL 尝试释放, owner=$owner")
                    release(preferredPort)
                    waitUntilAvailable(preferredPort, AUTO_KILL_WAIT_MS)
                    val afterKill = tryClaimPort(range, owner, preferredPort, allowPreferredOnly = true)
                    if (afterKill > 0) return afterKill
                    AppLogger.w(TAG, "AUTO_KILL 后仍无法占用 $preferredPort，回退自动分配, owner=$owner")
                }
                ConflictPolicy.REASSIGN -> {
                    AppLogger.w(TAG, "首选端口 $preferredPort 不可用，自动分配, owner=$owner")
                }
            }
        }

        val fallback = tryClaimPort(range, owner, preferredPort = 0, allowPreferredOnly = false)
        if (fallback > 0) return fallback

        AppLogger.e(TAG, "无法为 $owner 分配端口，所有范围已满")
        return PORT_UNAVAILABLE
    }

    private fun tryClaimPort(
        range: PortRange,
        owner: String,
        preferredPort: Int,
        allowPreferredOnly: Boolean
    ): Int = lock.write {
        purgeStaleAllocationsLocked()

        if (preferredPort > 0) {
            if (canClaimLocked(preferredPort)) {
                return@write claimLocked(preferredPort, owner, resolveRange(preferredPort, range))
            }
            if (allowPreferredOnly) return@write PORT_UNAVAILABLE
        } else if (allowPreferredOnly) {
            return@write PORT_UNAVAILABLE
        }

        for (port in range.start..range.end) {
            if (canClaimLocked(port)) {
                return@write claimLocked(port, owner, range)
            }
        }

        if (range != PortRange.GENERAL) {
            for (port in PortRange.GENERAL.start..PortRange.GENERAL.end) {
                if (canClaimLocked(port)) {
                    return@write claimLocked(port, owner, PortRange.GENERAL)
                }
            }
        }

        PORT_UNAVAILABLE
    }

    private fun resolveRange(port: Int, fallback: PortRange): PortRange {
        return PortRange.entries.firstOrNull { port in it.start..it.end } ?: fallback
    }

    private fun canClaim(port: Int): Boolean = lock.read { canClaimLocked(port) }

    private fun canClaimLocked(port: Int): Boolean {
        if (port !in 1..65535) return false
        if (allocatedPorts.containsKey(port)) return false
        return isPortAvailable(port)
    }

    private fun claimLocked(port: Int, owner: String, range: PortRange): Int {
        allocatedPorts[port] = PortAllocation(port, owner, range)
        AppLogger.i(TAG, "分配端口 $port 给 $owner (范围: ${range.name})")
        return port
    }

    fun trackExternal(
        port: Int,
        owner: String,
        range: PortRange,
        pid: Long = -1
    ): Boolean = lock.write {
        if (port !in 1..65535) return@write false
        purgeStaleAllocationsLocked()
        allocatedPorts[port] = PortAllocation(
            port = port,
            owner = owner,
            range = range,
            pid = pid,
            external = true
        )
        AppLogger.i(TAG, "登记外部端口 $port 给 $owner (external)")
        true
    }

    fun release(port: Int) {
        if (port <= 0) return

        val handler: StopHandler?
        val process: Process?
        val owner: String?
        lock.write {
            handler = portStopHandlers.remove(port)
            process = portProcesses.remove(port)
            owner = allocatedPorts.remove(port)?.owner
        }

        if (owner != null) {
            AppLogger.i(TAG, "释放端口 $port (原使用者: $owner)")
        }

        try {
            handler?.onStop(port)
        } catch (e: Exception) {
            AppLogger.w(TAG, "端口 $port stop handler 失败: ${e.message}")
        }

        if (process != null) {
            terminateProcess(port, process)
        }
    }

    fun releaseByOwner(owner: String) {
        val toRelease: List<Int>
        lock.read {
            toRelease = allocatedPorts.filter { it.value.owner == owner }.keys.toList()
        }
        toRelease.forEach { port -> release(port) }
    }

    fun releaseAll() {
        val ports: List<Int>
        lock.read {
            ports = allocatedPorts.keys.toList()
        }
        ports.forEach { port -> release(port) }

        lock.write {
            portStopHandlers.clear()
            portProcesses.clear()
            allocatedPorts.clear()
        }
    }

    fun isAllocated(port: Int): Boolean = allocatedPorts.containsKey(port)

    fun getAllocation(port: Int): PortAllocation? = allocatedPorts[port]

    fun registerProcess(port: Int, process: Process, pid: Long = -1) = lock.write {
        portProcesses[port] = process
        allocatedPorts[port]?.let { allocation ->
            allocatedPorts[port] = allocation.copy(pid = if (pid > 0) pid else allocation.pid)
        }
    }

    fun registerStopHandler(port: Int, handler: StopHandler) {
        if (port <= 0) return
        portStopHandlers[port] = handler
    }

    fun unregisterStopHandler(port: Int) {
        if (port <= 0) return
        portStopHandlers.remove(port)
    }

    fun getProcess(port: Int): Process? = portProcesses[port]

    fun isProcessAlive(port: Int): Boolean {
        val process = portProcesses[port] ?: return false
        return process.isAliveCompat()
    }

    fun hasStopHandler(port: Int): Boolean = portStopHandlers.containsKey(port)

    fun getAllAllocations(): Map<Int, PortAllocation> {
        return allocatedPorts.toMap()
    }

    fun getAllocatedCount(range: PortRange): Int = lock.read {
        allocatedPorts.values.count { it.range == range }
    }

    fun isPortAvailable(port: Int): Boolean {
        return try {
            ServerSocket(port).use { true }
        } catch (_: Exception) {
            false
        }
    }

    fun findAvailablePort(range: PortRange): Int = lock.read {
        for (port in range.start..range.end) {
            if (!allocatedPorts.containsKey(port) && isPortAvailable(port)) {
                return@read port
            }
        }
        -1
    }

    fun purgeStaleAllocations(): Int {
        val stale = mutableListOf<Int>()
        lock.read {
            val now = System.currentTimeMillis()
            for ((port, alloc) in allocatedPorts) {
                val process = portProcesses[port]
                if (process != null) {
                    if (!process.isAliveCompat() && (now - alloc.allocatedAt) > STALE_THRESHOLD_MS) {
                        stale.add(port)
                    }
                } else if (!alloc.external) {
                    if ((now - alloc.allocatedAt) > STALE_NO_PROCESS_THRESHOLD_MS && isPortAvailable(port)) {
                        stale.add(port)
                    }
                }
            }
        }
        if (stale.isNotEmpty()) {
            lock.write {
                stale.forEach { port ->
                    val process = portProcesses[port]
                    val alloc = allocatedPorts[port] ?: return@forEach
                    val recyclable = if (process != null) {
                        !process.isAliveCompat()
                    } else if (alloc.external) {
                        false
                    } else {
                        isPortAvailable(port)
                    }
                    if (recyclable) {
                        portProcesses.remove(port)
                        portStopHandlers.remove(port)
                        allocatedPorts.remove(port)
                        AppLogger.w(TAG, "清理僵尸端口 $port (原使用者: ${alloc.owner}, 存活: ${alloc.uptimeMs}ms)")
                    }
                }
            }
        }
        return stale.size
    }

    private fun purgeStaleAllocationsLocked() {
        val now = System.currentTimeMillis()
        val stale = mutableListOf<Int>()
        for ((port, alloc) in allocatedPorts) {
            val process = portProcesses[port]
            if (process != null) {
                if (!process.isAliveCompat() && (now - alloc.allocatedAt) > STALE_THRESHOLD_MS) {
                    stale.add(port)
                }
            } else if (!alloc.external) {
                if ((now - alloc.allocatedAt) > STALE_NO_PROCESS_THRESHOLD_MS && isPortAvailable(port)) {
                    stale.add(port)
                }
            }
        }
        stale.forEach { port ->
            portProcesses.remove(port)
            portStopHandlers.remove(port)
            allocatedPorts.remove(port)?.let { alloc ->
                AppLogger.w(TAG, "清理僵尸端口 $port (原使用者: ${alloc.owner}, 存活: ${alloc.uptimeMs}ms)")
            }
        }
    }

    fun getRangeStats(): List<RangeStats> = lock.read {
        PortRange.entries.map { range ->
            val count = allocatedPorts.values.count { it.range == range }
            RangeStats(
                range = range,
                allocated = count,
                total = range.size,
                usagePercent = if (range.size > 0) count.toFloat() / range.size else 0f
            )
        }
    }

    private fun terminateProcess(port: Int, process: Process) {
        try {
            if (process.isAliveCompat()) {
                val exitedGracefully = process.destroyGracefullyCompat(timeoutMs = 150L)
                if (!exitedGracefully && process.isAliveCompat()) {
                    process.destroyForciblyCompat()
                    AppLogger.i(TAG, "强制终止端口 $port 关联的进程")
                } else {
                    AppLogger.i(TAG, "已终止端口 $port 关联的进程 (优雅退出)")
                }
            }
        } catch (e: Exception) {
            AppLogger.w(TAG, "终止端口 $port 进程失败: ${e.message}")
        }
    }

    private fun waitUntilAvailable(port: Int, timeoutMs: Long) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            if (canClaim(port)) return
            try {
                Thread.sleep(20L)
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
                return
            }
        }
    }

    fun allocateForLocalHttp(
        owner: String,
        preferred: Int = 0,
        conflictPolicy: ConflictPolicy = ConflictPolicy.REASSIGN
    ) = allocate(PortRange.LOCAL_HTTP, "localhttp:$owner", preferred, conflictPolicy)

    fun allocateForPhp(
        projectId: String,
        preferred: Int = 0,
        conflictPolicy: ConflictPolicy = ConflictPolicy.REASSIGN
    ) = allocate(PortRange.PHP, "php:$projectId", preferred, conflictPolicy)

    fun allocateForNodeJs(
        projectId: String,
        preferred: Int = 0,
        conflictPolicy: ConflictPolicy = ConflictPolicy.REASSIGN
    ) = allocate(PortRange.NODEJS, "nodejs:$projectId", preferred, conflictPolicy)

    fun allocateForPython(
        projectId: String,
        preferred: Int = 0,
        conflictPolicy: ConflictPolicy = ConflictPolicy.REASSIGN
    ) = allocate(PortRange.PYTHON, "python:$projectId", preferred, conflictPolicy)

    fun allocateForGo(
        projectId: String,
        preferred: Int = 0,
        conflictPolicy: ConflictPolicy = ConflictPolicy.REASSIGN
    ) = allocate(PortRange.GO, "go:$projectId", preferred, conflictPolicy)

    fun releasePhp(projectId: String) = releaseByOwner("php:$projectId")

    fun releaseNodeJs(projectId: String) = releaseByOwner("nodejs:$projectId")

    fun releasePython(projectId: String) = releaseByOwner("python:$projectId")

    fun releaseGo(projectId: String) = releaseByOwner("go:$projectId")

    fun getStats(): String {
        val sb = StringBuilder("端口使用统计:\n")
        getRangeStats().forEach { stat ->
            val pct = "%.1f%%".format(stat.usagePercent * 100)
            sb.append("  ${stat.range.name}: ${stat.allocated}/${stat.total} ($pct) [${stat.range.start}-${stat.range.end}]\n")
        }
        sb.append("  总计: ${allocatedPorts.size}\n")
        return sb.toString()
    }

    fun dumpAllocations() {
        AppLogger.d(TAG, "=== 端口分配详情 ===")
        allocatedPorts.forEach { (port, alloc) ->
            val uptime = formatDuration(alloc.uptimeMs)
            val alive = if (isProcessAlive(port)) "alive" else "dead/unknown"
            AppLogger.d(TAG, "  $port -> ${alloc.owner} (${alloc.range.name}) [$uptime] [$alive]")
        }
        AppLogger.d(TAG, getStats())
    }

    fun formatDuration(ms: Long): String {
        val seconds = ms / 1000
        return when {
            seconds < 60 -> "${seconds}s"
            seconds < 3600 -> "${seconds / 60}m ${seconds % 60}s"
            else -> "${seconds / 3600}h ${(seconds % 3600) / 60}m"
        }
    }
}

class PortConflictException(
    val port: Int,
    message: String = "Port $port is already in use"
) : Exception(message)
