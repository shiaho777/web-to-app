package com.webtoapp.util

import okhttp3.Call
import okhttp3.ConnectionPool
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.CancellationException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutionException
import java.util.concurrent.FutureTask
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

internal class ParallelDownloadInput private constructor(
    start: Long,
    private val total: Long,
    private val firstPartBytes: Int,
    private val partBytes: Int,
    private val handoffAfterMs: Long,
    private val nanoTime: () -> Long,
    private val createCall: (ConnectionPool?, Long, Long) -> Call,
    private val readPart: (Call, Long, Long, ByteArray, Int, (Int) -> Unit) -> Unit,
    private val canRetry: (IOException) -> Boolean,
    private val onRetry: (Long, Long, IOException) -> Unit,
    private val onHandoff: (Long, Long) -> Unit
) : InputStream() {
    private class Lane {
        val connections = ConnectionPool(1, 15, TimeUnit.SECONDS)
        var bytesPerSecond = 0.0
    }

    private class RequestProgress(val call: Call, val started: Long, val offset: Int) {
        @Volatile var received = 0
        val handoff = AtomicReference<Lane>()
    }

    private val idleLanes = MutableList(LANES) { Lane() }
    private val pending = ArrayDeque<Part>()
    private var nextOffset = start
    private var current: ByteArrayInputStream? = null
    private val oneByte = ByteArray(1)
    private var closed = false

    init {
        enqueue(firstPartBytes)
        repeat(LANES * 2 - 1) { enqueue(partBytes) }
    }

    fun awaitFirstBytes(timeoutMs: Long): Boolean {
        val part = pending.first()
        return try {
            part.ready.await(timeoutMs, TimeUnit.MILLISECONDS) && part.receiving
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
            false
        }
    }

    override fun read(): Int = if (read(oneByte, 0, 1) == -1) -1 else oneByte[0].toInt() and 255

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        if (offset < 0 || length < 0 || offset > buffer.size - length) throw IndexOutOfBoundsException()
        if (length == 0) return 0
        if (closed) throw IOException("Download stream is closed")
        val previous = current?.read(buffer, offset, length) ?: -1
        if (previous >= 0) return previous
        val part = pending.firstOrNull() ?: return -1
        val bytes = part.await()
        pending.removeFirst()
        enqueue(partBytes)
        return ByteArrayInputStream(bytes).also { current = it }.read(buffer, offset, length)
    }

    private fun enqueue(bytes: Int) {
        if (nextOffset >= total) return
        val start = nextOffset
        val end = start + minOf(bytes.toLong(), total - start) - 1
        nextOffset = end + 1
        val part = Part(start, end)
        pending.addLast(part)
        workers.execute(part.task)
    }

    override fun close() {
        if (closed) return
        closed = true
        pending.forEach(Part::cancel)
        pending.clear()
        current = null
        reserved.set(false)
    }

    private fun takeLane() = synchronized(idleLanes) { idleLanes.removeAt(idleLanes.lastIndex) }

    private fun releaseLane(lane: Lane) = synchronized(idleLanes) { idleLanes.add(lane) }

    private fun replaceLane(active: Lane, preferred: Lane): Lane? = synchronized(idleLanes) {
        if (!idleLanes.remove(preferred)) return@synchronized null
        idleLanes.add(active)
        preferred
    }

    private fun measuredRate(bytes: Int, elapsedNanos: Long) = bytes * 1e9 / elapsedNanos.coerceAtLeast(1)

    private fun fasterLane(currentRate: Double, remaining: Int): Lane? = synchronized(idleLanes) {
        val candidate = idleLanes.maxByOrNull { it.bytesPerSecond } ?: return@synchronized null
        val fasterRate = candidate.bytesPerSecond
        candidate.takeIf {
            fasterRate > 0.0 && fasterRate >= 2 * currentRate &&
                remaining / fasterRate + MIN_HANDOFF_GAIN_SECONDS < remaining / currentRate
        }
    }

    private inner class Part(private val start: Long, private val end: Long) {
        private val size = (end - start + 1).toInt()
        val ready = CountDownLatch(1)
        @Volatile var receiving = false
            private set
        private val canceled = AtomicBoolean()
        private val activeRequest = AtomicReference<RequestProgress>()
        private var handoffRequested = false
        val task = FutureTask<ByteArray> {
            try {
                fetch()
            } finally {
                ready.countDown()
            }
        }

        private fun fetch(): ByteArray {
            val bytes = ByteArray(size)
            var lane = takeLane()
            var filled = 0
            var failures = 0
            var reuseConnection = true
            try {
                while (true) {
                    if (canceled.get()) throw IOException("Download part was cancelled")
                    val offset = filled
                    val connections = lane.connections.takeIf { reuseConnection }
                    val call = createCall(connections, start + offset, end)
                    val request = RequestProgress(call, nanoTime(), offset)
                    activeRequest.set(request)
                    if (canceled.get()) call.cancel()
                    try {
                        readPart(call, start + offset, end, bytes, offset) { count ->
                            if (filled == 0) {
                                receiving = true
                                ready.countDown()
                            }
                            filled += count
                            request.received += count
                        }
                        if (reuseConnection) lane.bytesPerSecond = measuredRate(request.received, nanoTime() - request.started)
                        return bytes
                    } catch (error: IOException) {
                        if (connections != null) {
                            lane.bytesPerSecond = 0.0
                            connections.evictAll()
                        }
                        if (canceled.get() || !canRetry(error) || ++failures >= MAX_PART_ATTEMPTS) throw error
                        if (filled == size) filled = offset
                        val replacement = request.handoff.get()?.let { replaceLane(lane, it) }
                        reuseConnection = replacement != null
                        if (replacement != null) {
                            lane = replacement
                            onHandoff(start + filled, end)
                        } else {
                            onRetry(start + filled, end, error)
                        }
                    } finally {
                        activeRequest.compareAndSet(request, null)
                    }
                }
            } finally {
                releaseLane(lane)
            }
        }

        private fun handOffSlowRequest() {
            if (handoffRequested || workers.queue.isNotEmpty()) return
            val request = activeRequest.get() ?: return
            val elapsed = nanoTime() - request.started
            if (elapsed < TimeUnit.MILLISECONDS.toNanos(handoffAfterMs)) return
            val received = request.received
            val remaining = size - request.offset - received
            if (remaining < minOf(32 * 1024, partBytes / 4)) return
            val faster = fasterLane(measuredRate(received, elapsed), remaining) ?: return
            handoffRequested = true
            request.handoff.set(faster)
            request.call.cancel()
        }

        fun await(): ByteArray {
            while (true) {
                try {
                    return task.get(250, TimeUnit.MILLISECONDS)
                } catch (_: TimeoutException) {
                    handOffSlowRequest()
                } catch (error: ExecutionException) {
                    throw error.cause as? IOException ?: IOException(error.cause)
                } catch (error: InterruptedException) {
                    Thread.currentThread().interrupt()
                    throw IOException(error)
                } catch (error: CancellationException) {
                    throw IOException(error)
                }
            }
        }

        fun cancel() {
            canceled.set(true)
            activeRequest.getAndSet(null)?.call?.cancel()
            task.cancel(true)
            ready.countDown()
        }
    }

    companion object {
        const val LANES = 8
        private const val MAX_PART_ATTEMPTS = 5
        private const val MIN_HANDOFF_GAIN_SECONDS = 0.75
        private val reserved = AtomicBoolean()
        private val workers = ThreadPoolExecutor(LANES, LANES, 0, TimeUnit.SECONDS, LinkedBlockingQueue()) { task ->
            Thread(task, "WebDownloadRange").apply { isDaemon = true }
        }

        fun start(
            start: Long,
            total: Long,
            firstPartBytes: Int,
            partBytes: Int,
            handoffAfterMs: Long,
            nanoTime: () -> Long,
            createCall: (ConnectionPool?, Long, Long) -> Call,
            readPart: (Call, Long, Long, ByteArray, Int, (Int) -> Unit) -> Unit,
            canRetry: (IOException) -> Boolean,
            onRetry: (Long, Long, IOException) -> Unit,
            onHandoff: (Long, Long) -> Unit
        ): ParallelDownloadInput? {
            if (start >= total || !reserved.compareAndSet(false, true)) return null
            return ParallelDownloadInput(
                start, total, firstPartBytes, partBytes, handoffAfterMs, nanoTime, createCall, readPart, canRetry, onRetry, onHandoff
            )
        }
    }
}
