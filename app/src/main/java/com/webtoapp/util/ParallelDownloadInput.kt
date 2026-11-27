package com.webtoapp.util

import okhttp3.Call
import okhttp3.ConnectionPool
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.CancellationException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

internal class ParallelDownloadInput private constructor(
    start: Long,
    private val total: Long,
    private val firstPartBytes: Int,
    private val partBytes: Int,
    private val createCall: (ConnectionPool?, Long, Long) -> Call,
    private val readPart: (Call, Long, Long, ByteArray, Int, (Int) -> Unit) -> Unit,
    private val canRetry: (IOException) -> Boolean,
    private val onRetry: (Long, Long, IOException) -> Unit
) : InputStream() {
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

    private inner class Part(private val start: Long, private val end: Long) {
        val ready = CountDownLatch(1)
        @Volatile var receiving = false
            private set
        private val canceled = AtomicBoolean()
        private val call = AtomicReference<Call>()
        val task = FutureTask<ByteArray> {
            try {
                fetch()
            } finally {
                ready.countDown()
            }
        }

        private fun fetch(): ByteArray {
            val bytes = ByteArray((end - start + 1).toInt())
            var filled = 0
            var failures = 0
            while (true) {
                if (canceled.get()) throw IOException("Download part was cancelled")
                val offset = filled
                val connections = workerConnections.get().takeIf { failures == 0 }
                val request = createCall(connections, start + offset, end)
                call.set(request)
                if (canceled.get()) request.cancel()
                try {
                    readPart(request, start + offset, end, bytes, offset) { count ->
                        if (filled == 0) {
                            receiving = true
                            ready.countDown()
                        }
                        filled += count
                    }
                    return bytes
                } catch (error: IOException) {
                    if (canceled.get() || !canRetry(error) || ++failures >= MAX_PART_ATTEMPTS) throw error
                    connections?.evictAll()
                    if (filled == bytes.size) filled = offset
                    onRetry(start + filled, end, error)
                } finally {
                    call.compareAndSet(request, null)
                }
            }
        }

        fun await(): ByteArray = try {
            task.get()
        } catch (error: ExecutionException) {
            throw error.cause as? IOException ?: IOException(error.cause)
        } catch (error: InterruptedException) {
            Thread.currentThread().interrupt()
            throw IOException(error)
        } catch (error: CancellationException) {
            throw IOException(error)
        }

        fun cancel() {
            canceled.set(true)
            call.getAndSet(null)?.cancel()
            task.cancel(true)
            ready.countDown()
        }
    }

    companion object {
        const val LANES = 8
        private const val MAX_PART_ATTEMPTS = 5
        private val reserved = AtomicBoolean()
        private val workerConnections = object : ThreadLocal<ConnectionPool>() {
            override fun initialValue() = ConnectionPool(1, 15, TimeUnit.SECONDS)
        }
        private val workers = Executors.newFixedThreadPool(LANES) { task ->
            Thread(task, "WebDownloadRange").apply { isDaemon = true }
        }

        fun start(
            start: Long,
            total: Long,
            firstPartBytes: Int,
            partBytes: Int,
            createCall: (ConnectionPool?, Long, Long) -> Call,
            readPart: (Call, Long, Long, ByteArray, Int, (Int) -> Unit) -> Unit,
            canRetry: (IOException) -> Boolean,
            onRetry: (Long, Long, IOException) -> Unit
        ): ParallelDownloadInput? {
            if (start >= total || !reserved.compareAndSet(false, true)) return null
            return ParallelDownloadInput(start, total, firstPartBytes, partBytes, createCall, readPart, canRetry, onRetry)
        }
    }
}
