package com.webtoapp.core.php

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.junit.After
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.ServerSocket
import java.net.Socket
import java.net.URL
import java.security.MessageDigest
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.random.Random

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class PhpUploadRuntimeIntegrationTest {

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    private lateinit var workDir: File
    private lateinit var docRoot: File
    private lateinit var destDir: File
    private val servers = mutableListOf<Process>()

    @Before
    fun setUp() {
        val php = phpBinary()
        assumeTrue("php executable not available (set PHP_BINARY)", php != null)
        workDir = File(context.cacheDir.parentFile, "php-upload-it-${System.nanoTime()}").apply { mkdirs() }
        docRoot = File(workDir, "www").apply { mkdirs() }
        destDir = File(workDir, "dest").apply { mkdirs() }
        writeFixtures()
    }

    @After
    fun tearDown() {
        servers.forEach { process ->
            process.destroy()
            if (!process.waitFor(5, TimeUnit.SECONDS)) process.destroyForcibly()
        }
        if (::workDir.isInitialized) workDir.deleteRecursively()
    }

    @Test
    fun `server runs under the native cli-server sapi`() {
        val base = startServer()
        val response = get("$base/json.php")
        assertThat(response.code).isEqualTo(200)
        assertThat(json(response).get("sapi").asString).isEqualTo("cli-server")
    }

    @Test
    fun `small png upload is a native upload and moves to a writable directory`() {
        val base = startServer()
        val png = pngBytes()
        val body = Multipart()
            .field("dest_dir", destDir.absolutePath)
            .file("file", "test.png", "image/png", png)
        val file = uploadedFile(post("$base/upload.php", body), "file")

        assertThat(file.get("error").asInt).isEqualTo(0)
        assertThat(file.get("name").asString).isEqualTo("test.png")
        assertThat(file.get("type").asString).isEqualTo("image/png")
        assertThat(file.get("size").asInt).isEqualTo(png.size)
        assertThat(file.get("tmp_exists").asBoolean).isTrue()
        assertThat(file.get("is_uploaded_file").asBoolean).isTrue()
        assertThat(file.get("move_success").asBoolean).isTrue()
        assertThat(file.get("tmp_exists_after_move").asBoolean).isFalse()
        assertThat(File(file.get("dest").asString).readBytes()).isEqualTo(png)
        assertNoLeakedUploadFiles()
    }

    @Test
    fun `jpeg upload preserves mime type unicode filename and bytes`() {
        val base = startServer()
        val jpeg = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte()) +
            Random(7).nextBytes(4096) + byteArrayOf(0xFF.toByte(), 0xD9.toByte())
        val body = Multipart()
            .field("dest_dir", destDir.absolutePath)
            .file("photo", "фото-é.jpg", "image/jpeg", jpeg)
        val file = uploadedFile(post("$base/upload.php", body), "photo")

        assertThat(file.get("name").asString).isEqualTo("фото-é.jpg")
        assertThat(file.get("type").asString).isEqualTo("image/jpeg")
        assertThat(file.get("is_uploaded_file").asBoolean).isTrue()
        assertThat(file.get("move_success").asBoolean).isTrue()
        assertThat(file.get("sha256").asString).isEqualTo(sha256(jpeg))
        assertThat(File(file.get("dest").asString).readBytes()).isEqualTo(jpeg)
    }

    @Test
    fun `binary upload keeps every byte including trailing newlines and boundary lookalikes`() {
        val base = startServer()
        val binary = ByteArray(256 * 8) { (it % 256).toByte() } +
            "\r\n--boundary-lookalike\r\n\r\n".toByteArray() + byteArrayOf(0, 13, 10, 13, 10)
        val body = Multipart()
            .field("dest_dir", destDir.absolutePath)
            .file("blob", "data.bin", "application/octet-stream", binary)
        val file = uploadedFile(post("$base/upload.php", body), "blob")

        assertThat(file.get("size").asInt).isEqualTo(binary.size)
        assertThat(file.get("is_uploaded_file").asBoolean).isTrue()
        assertThat(file.get("move_success").asBoolean).isTrue()
        assertThat(File(file.get("dest").asString).readBytes()).isEqualTo(binary)
    }

    @Test
    fun `large upload within the configured limit succeeds`() {
        val base = startServer()
        val large = Random(42).nextBytes(8 * 1024 * 1024)
        val body = Multipart()
            .field("dest_dir", destDir.absolutePath)
            .file("video", "clip.mp4", "video/mp4", large)
        val file = uploadedFile(post("$base/upload.php", body), "video")

        assertThat(file.get("error").asInt).isEqualTo(0)
        assertThat(file.get("is_uploaded_file").asBoolean).isTrue()
        assertThat(file.get("move_success").asBoolean).isTrue()
        assertThat(file.get("sha256").asString).isEqualTo(sha256(large))
        assertNoLeakedUploadFiles()
    }

    @Test
    fun `file above upload_max_filesize reports UPLOAD_ERR_INI_SIZE`() {
        val base = startServer(extraIni = mapOf("upload_max_filesize" to "1M"))
        val body = Multipart()
            .field("dest_dir", destDir.absolutePath)
            .file("file", "big.bin", "application/octet-stream", ByteArray(2 * 1024 * 1024))
        val file = uploadedFile(post("$base/upload.php", body), "file")

        assertThat(file.get("error").asInt).isEqualTo(1)
        assertThat(file.get("tmp_name").asString).isEmpty()
        assertThat(file.get("is_uploaded_file").asBoolean).isFalse()
        assertThat(destDir.listFiles().orEmpty()).isEmpty()
        assertNoLeakedUploadFiles()
    }

    @Test
    fun `multiple files and text fields coexist in one request`() {
        val base = startServer()
        val payloads = List(3) { Random(it).nextBytes(1000 + it * 333) }
        val body = Multipart()
            .field("dest_dir", destDir.absolutePath)
            .field("title", "Model ünïcode")
            .field("tags[]", "a")
            .field("tags[]", "b")
        payloads.forEachIndexed { i, bytes -> body.file("files[]", "part$i.png", "image/png", bytes) }
        body.file("avatar", "avatar.jpg", "image/jpeg", pngBytes())
        val result = json(post("$base/upload.php", body))

        val postFields = result.getAsJsonObject("post")
        assertThat(postFields.get("title").asString).isEqualTo("Model ünïcode")
        assertThat(postFields.getAsJsonArray("tags").map { it.asString }).containsExactly("a", "b").inOrder()
        val files = result.getAsJsonObject("files")
        assertThat(files.keySet()).containsExactly("files[0]", "files[1]", "files[2]", "avatar")
        payloads.forEachIndexed { i, bytes ->
            val file = files.getAsJsonObject("files[$i]")
            assertThat(file.get("name").asString).isEqualTo("part$i.png")
            assertThat(file.get("is_uploaded_file").asBoolean).isTrue()
            assertThat(file.get("move_success").asBoolean).isTrue()
            assertThat(File(file.get("dest").asString).readBytes()).isEqualTo(bytes)
        }
        assertThat(files.getAsJsonObject("avatar").get("move_success").asBoolean).isTrue()
        assertNoLeakedUploadFiles()
    }

    @Test
    fun `empty uploads follow native php semantics`() {
        val base = startServer()
        val body = Multipart()
            .field("dest_dir", destDir.absolutePath)
            .file("empty", "empty.txt", "text/plain", ByteArray(0))
            .file("none", "", "application/octet-stream", ByteArray(0))
        val result = json(post("$base/upload.php", body))
        val files = result.getAsJsonObject("files")

        val empty = files.getAsJsonObject("empty")
        assertThat(empty.get("error").asInt).isEqualTo(0)
        assertThat(empty.get("size").asInt).isEqualTo(0)
        assertThat(empty.get("is_uploaded_file").asBoolean).isTrue()
        assertThat(empty.get("move_success").asBoolean).isTrue()
        assertThat(File(empty.get("dest").asString).length()).isEqualTo(0)

        val none = files.getAsJsonObject("none")
        assertThat(none.get("error").asInt).isEqualTo(4)
        assertThat(none.get("tmp_name").asString).isEmpty()
        assertThat(result.getAsJsonObject("post").has("none")).isFalse()
    }

    @Test
    fun `forged tmp_name form fields never become uploads`() {
        val base = startServer()
        val victim = File(workDir, "secret.txt").apply { writeText("secret") }
        val body = Multipart()
            .field("dest_dir", destDir.absolutePath)
            .field("file[tmp_name]", victim.absolutePath)
            .field("file[name]", "secret.txt")
            .field("file[error]", "0")
        val result = json(post("$base/upload.php", body))

        assertThat(result.getAsJsonObject("files").size()).isEqualTo(0)
        assertThat(victim.readText()).isEqualTo("secret")
        assertThat(destDir.listFiles().orEmpty()).isEmpty()
    }

    @Test
    fun `move_uploaded_file rejects arbitrary local files and lookalike temp paths`() {
        val base = startServer()
        val arbitrary = File(workDir, "config.php").apply { writeText("<?php return 1;") }
        val lookalike = File(context.cacheDir, "phpFAKE01").apply { writeText("forged") }

        listOf(arbitrary, lookalike).forEach { source ->
            val target = File(destDir, "stolen-${source.name}")
            val result = json(
                post(
                    "$base/forge.php",
                    Multipart().field("path", source.absolutePath).field("dest", target.absolutePath)
                )
            )
            assertThat(result.get("is_uploaded_file").asBoolean).isFalse()
            assertThat(result.get("move_success").asBoolean).isFalse()
            assertThat(result.get("source_exists").asBoolean).isTrue()
            assertThat(target.exists()).isFalse()
        }
        lookalike.delete()
    }

    @Test
    fun `move to an unwritable destination fails and the temp file is still cleaned up`() {
        val base = startServer()
        val notADirectory = File(workDir, "plain-file").apply { writeText("x") }
        val body = Multipart()
            .field("dest_dir", notADirectory.absolutePath)
            .file("file", "test.png", "image/png", pngBytes())
        val file = uploadedFile(post("$base/upload.php", body), "file")

        assertThat(file.get("is_uploaded_file").asBoolean).isTrue()
        assertThat(file.get("move_success").asBoolean).isFalse()
        assertThat(file.get("move_error").isJsonNull).isFalse()
        assertNoLeakedUploadFiles()
    }

    @Test
    fun `interrupted upload leaves no temp files and server keeps serving`() {
        val base = startServer()
        val port = URL(base).port
        Socket("127.0.0.1", port).use { socket ->
            val out = socket.getOutputStream()
            val head = "POST /upload.php HTTP/1.1\r\nHost: 127.0.0.1:$port\r\n" +
                "Content-Type: multipart/form-data; boundary=XYZ\r\nContent-Length: 4000000\r\n\r\n" +
                "--XYZ\r\nContent-Disposition: form-data; name=\"file\"; filename=\"cut.bin\"\r\n" +
                "Content-Type: application/octet-stream\r\n\r\n"
            out.write(head.toByteArray())
            out.write(ByteArray(256 * 1024))
            out.flush()
        }
        Thread.sleep(500)
        assertThat(get("$base/__health").code).isEqualTo(200)
        assertNoLeakedUploadFiles()

        val ok = uploadedFile(
            post("$base/upload.php", Multipart().file("file", "after.png", "image/png", pngBytes())),
            "file"
        )
        assertThat(ok.get("is_uploaded_file").asBoolean).isTrue()
    }

    @Test
    fun `concurrent uploads get distinct temp files and intact contents`() {
        val base = startServer()
        val payloads = List(8) { Random(100 + it).nextBytes(256 * 1024) }
        val pool = Executors.newFixedThreadPool(payloads.size)
        try {
            val results = pool.invokeAll(payloads.mapIndexed { i, bytes ->
                Callable {
                    uploadedFile(
                        post(
                            "$base/upload.php",
                            Multipart()
                                .field("dest_dir", destDir.absolutePath)
                                .field("delay_ms", "300")
                                .file("file", "c$i.bin", "application/octet-stream", bytes)
                        ),
                        "file"
                    )
                }
            }).map { it.get(60, TimeUnit.SECONDS) }

            assertThat(results.map { it.get("tmp_name").asString }.toSet()).hasSize(payloads.size)
            results.forEachIndexed { i, file ->
                assertThat(file.get("is_uploaded_file").asBoolean).isTrue()
                assertThat(file.get("move_success").asBoolean).isTrue()
                assertThat(File(file.get("dest").asString).readBytes()).isEqualTo(payloads[i])
            }
        } finally {
            pool.shutdownNow()
        }
        assertNoLeakedUploadFiles()
    }

    @Test
    fun `json post keeps php input raw body global and post fallback`() {
        val base = startServer()
        val payload = """{"name":"Model","tags":["a","b"],"n":3}"""
        val response = request("$base/json.php", "POST", payload.toByteArray(), "application/json")
        val result = json(response)

        assertThat(response.code).isEqualTo(200)
        assertThat(result.getAsJsonObject("input").get("name").asString).isEqualTo("Model")
        assertThat(result.get("raw_global").asString).isEqualTo(payload)
        assertThat(result.getAsJsonObject("post").get("n").asInt).isEqualTo(3)
        assertThat(result.get("script_name").asString).isEqualTo("/json.php")
    }

    @Test
    fun `urlencoded put body still populates post`() {
        val base = startServer()
        val response = request(
            "$base/json.php", "PUT", "a=1&b=two".toByteArray(), "application/x-www-form-urlencoded"
        )
        val post = json(response).getAsJsonObject("post")
        assertThat(post.get("a").asString).isEqualTo("1")
        assertThat(post.get("b").asString).isEqualTo("two")
    }

    @Test
    fun `multipart put uploads are native when request_parse_body is available`() {
        val base = startServer()
        assumeTrue(json(get("$base/json.php")).get("has_request_parse_body").asBoolean)
        val bytes = pngBytes()
        val body = Multipart()
            .field("dest_dir", destDir.absolutePath)
            .file("file", "put.png", "image/png", bytes)
        val file = json(request("$base/upload.php", "PUT", body.bytes(), body.contentType()))
            .getAsJsonObject("files").getAsJsonObject("file")
        assertThat(file.get("is_uploaded_file").asBoolean).isTrue()
        assertThat(File(file.get("dest").asString).readBytes()).isEqualTo(bytes)
    }

    @Test
    fun `routing sessions cookies headers and redirects work natively`() {
        val base = startServer()
        val first = get("$base/some/deep/route?x=1")
        assertThat(first.code).isEqualTo(200)
        assertThat(first.header("X-Test")).isEqualTo("yes")
        val firstJson = json(first)
        assertThat(firstJson.get("n").asInt).isEqualTo(1)
        assertThat(firstJson.get("uri").asString).isEqualTo("/some/deep/route?x=1")
        assertThat(firstJson.get("script_name").asString).isEqualTo("/index.php")
        val cookies = first.headers["Set-Cookie"].orEmpty()
        assertThat(cookies.any { it.startsWith("wta_test=v1") }).isTrue()
        val session = cookies.first { it.startsWith("PHPSESSID=") }.substringBefore(';')

        val second = get("$base/", cookie = session)
        val sessionDir = File(docRoot, ".sessions")
        assertWithMessage(
            "first=${first.text}\nsecond=${second.text}\ncookies=$cookies\n" +
                "sessions=${sessionDir.listFiles().orEmpty().map { "${it.name}:${it.length()}" }}\n" +
                serverDiagnostics(emptyList(), "")
        ).that(json(second).get("n").asInt).isEqualTo(2)

        val redirect = get("$base/redirect.php")
        assertThat(redirect.code).isEqualTo(302)
        assertThat(redirect.header("Location")).isEqualTo("/target")
    }

    @Test
    fun `session data written inside functions persists across requests`() {
        val base = startServer()
        val first = get("$base/fnsession.php")
        assertThat(json(first).get("m").asInt).isEqualTo(1)
        val session = first.headers["Set-Cookie"].orEmpty()
            .first { it.startsWith("PHPSESSID=") }.substringBefore(';')
        val second = get("$base/fnsession.php", cookie = session)
        assertWithMessage("first=${first.text} second=${second.text}")
            .that(json(second).get("m").asInt).isEqualTo(2)
    }

    @Test
    fun `static media honours byte ranges so players can seek`() {
        val base = startServer()
        val track = mediaTrack()
        val url = "$base/data/audio/track.mp3"

        val slice = raw(url, mapOf("Range" to "bytes=100-199"))
        assertThat(slice.code).isEqualTo(206)
        assertThat(slice.header("Content-Range")).isEqualTo("bytes 100-199/${track.size}")
        assertThat(slice.header("Accept-Ranges")).isEqualTo("bytes")
        assertThat(slice.header("Content-Type")).isEqualTo("audio/mpeg")
        assertThat(slice.body).isEqualTo(track.copyOfRange(100, 200))

        val suffix = raw(url, mapOf("Range" to "bytes=-64"))
        assertThat(suffix.code).isEqualTo(206)
        assertThat(suffix.body).isEqualTo(track.copyOfRange(track.size - 64, track.size))

        val first = raw(url, mapOf("Range" to "bytes=0-"))
        assertThat(first.code).isEqualTo(206)
        assertThat(first.header("Content-Range")).isEqualTo("bytes 0-2097151/${track.size}")
        assertThat(first.body).isEqualTo(track.copyOfRange(0, 2097152))

        val rest = raw(url, mapOf("Range" to "bytes=2097152-"))
        assertThat(rest.code).isEqualTo(206)
        assertThat(rest.header("Content-Range")).isEqualTo("bytes 2097152-${track.size - 1}/${track.size}")
        assertThat(rest.body).isEqualTo(track.copyOfRange(2097152, track.size))
    }

    @Test
    fun `static media range edge cases follow http semantics`() {
        val base = startServer()
        val track = mediaTrack()
        val url = "$base/data/audio/track.mp3"

        val unsatisfiable = raw(url, mapOf("Range" to "bytes=${track.size}-"))
        assertThat(unsatisfiable.code).isEqualTo(416)
        assertThat(unsatisfiable.header("Content-Range")).isEqualTo("bytes */${track.size}")

        val multi = raw(url, mapOf("Range" to "bytes=0-1,5-6"))
        assertThat(multi.code).isEqualTo(200)
        assertThat(multi.body).isEqualTo(track)

        val staleIfRange = raw(url, mapOf("Range" to "bytes=0-9", "If-Range" to "\"stale\""))
        assertThat(staleIfRange.code).isEqualTo(200)
        assertThat(staleIfRange.body).isEqualTo(track)

        val etag = raw(url, mapOf("Range" to "bytes=0-0")).header("ETag")!!
        val matchingIfRange = raw(url, mapOf("Range" to "bytes=0-9", "If-Range" to etag))
        assertThat(matchingIfRange.code).isEqualTo(206)
        assertThat(matchingIfRange.body).isEqualTo(track.copyOfRange(0, 10))

        val head = raw(url, mapOf("Range" to "bytes=0-9"), method = "HEAD")
        assertThat(head.code).isEqualTo(206)
        assertThat(head.header("Content-Length")).isEqualTo("10")
        assertThat(head.body).isEmpty()

        val plain = raw(url, emptyMap())
        assertThat(plain.code).isEqualTo(200)
        assertThat(plain.body).isEqualTo(track)
    }

    @Test
    fun `static files health and missing assets are served`() {
        val base = startServer()
        assertThat(get("$base/__health").text).isEqualTo("OK")
        val css = get("$base/style.css")
        assertThat(css.code).isEqualTo(200)
        assertThat(css.text).isEqualTo("body{color:red}")
        assertThat(get("$base/missing.png").code).isEqualTo(404)
        assertThat(get("$base/../etc/passwd").code).isNotEqualTo(200)
    }

    @Test
    fun `fatal errors return 500 with the router diagnostic header`() {
        val base = startServer()
        val response = get("$base/fail.php")
        assertThat(response.code).isEqualTo(500)
        val diagnostic = String(java.util.Base64.getDecoder().decode(response.header("X-Wta-Router-Error")!!))
        assertThat(diagnostic).contains("boom")
    }

    @Test
    fun `wordpress runtime command serves uploads natively too`() {
        val base = startServer(useWordPressCommand = true)
        val bytes = pngBytes()
        val file = uploadedFile(
            post(
                "$base/upload.php",
                Multipart().field("dest_dir", destDir.absolutePath).file("file", "wp.png", "image/png", bytes)
            ),
            "file"
        )
        assertThat(file.get("is_uploaded_file").asBoolean).isTrue()
        assertThat(file.get("move_success").asBoolean).isTrue()
        assertThat(File(file.get("dest").asString).readBytes()).isEqualTo(bytes)
    }

    private fun startServer(
        extraIni: Map<String, String> = emptyMap(),
        useWordPressCommand: Boolean = false
    ): String {
        val port = ServerSocket(0).use { it.localPort }
        val router = PhpServerLaunch.extractRouterScript(context)
        assertThat(File(router).readText()).contains("cli-server")
        val command = if (useWordPressCommand) {
            wordPressCommand(port, router)
        } else {
            phpAppCommand(port, router)
        }.toMutableList()
        val serverFlag = command.indexOf("-S")
        extraIni.forEach { (key, value) ->
            command.add(serverFlag, "$key=$value")
            command.add(serverFlag, "-d")
        }

        val builder = ProcessBuilder(command).directory(docRoot).redirectErrorStream(true)
        builder.environment().putAll(PhpServerLaunch.serverEnv("index.php"))
        val process = builder.start()
        servers += process
        val log = StringBuffer()
        val reader = Thread {
            runCatching { process.inputStream.bufferedReader().forEachLine { log.appendLine(it) } }
        }.apply { isDaemon = true; start() }

        val base = "http://127.0.0.1:$port"
        repeat(150) {
            if (!process.isAlive) {
                reader.join(2000)
                error("php server exited with ${process.exitValue()}: ${serverDiagnostics(command, log)}")
            }
            if (runCatching { get("$base/__health").code == 200 }.getOrDefault(false)) return base
            Thread.sleep(100)
        }
        error("php server did not become ready: ${serverDiagnostics(command, log)}")
    }

    private fun serverDiagnostics(command: List<String>, log: CharSequence): String {
        val errorLog = File(context.cacheDir, "php_app_errors.log")
        val logged = if (errorLog.isFile) errorLog.readText().takeLast(4000) else "(no php_app_errors.log)"
        return "command=$command\noutput=$log\nerror_log=$logged"
    }

    private fun phpAppCommand(port: Int, router: String): List<String> {
        val method = PhpAppRuntime::class.java.getDeclaredMethod(
            "buildPhpCommand",
            List::class.java,
            Int::class.javaPrimitiveType,
            String::class.java,
            String::class.java,
            Map::class.java,
            List::class.java,
            File::class.java
        )
        method.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        return method.invoke(
            PhpAppRuntime(context), listOf(phpBinary()!!), port, docRoot.absolutePath, router,
            emptyMap<String, Boolean>(), emptyList<Any>(), null
        ) as List<String>
    }

    private fun wordPressCommand(port: Int, router: String): List<String> {
        val runtimeClass = com.webtoapp.core.wordpress.WordPressPhpRuntime::class.java
        val method = runtimeClass.getDeclaredMethod(
            "buildPhpCommand",
            List::class.java,
            Int::class.javaPrimitiveType,
            String::class.java,
            String::class.java,
            List::class.java,
            File::class.java
        )
        method.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        return method.invoke(
            com.webtoapp.core.wordpress.WordPressPhpRuntime(context), listOf(phpBinary()!!), port,
            docRoot.absolutePath, router, emptyList<Any>(), null
        ) as List<String>
    }

    private fun assertNoLeakedUploadFiles() {
        val deadline = System.currentTimeMillis() + 3000
        var leaked: List<String>
        do {
            leaked = context.cacheDir.listFiles().orEmpty()
                .map { it.name }
                .filter { UPLOAD_TEMP_NAME.matches(it) && it != "phpFAKE01" }
            if (leaked.isEmpty()) return
            Thread.sleep(100)
        } while (System.currentTimeMillis() < deadline)
        assertThat(leaked).isEmpty()
    }

    private fun mediaTrack(): ByteArray {
        val bytes = Random(9).nextBytes(3 * 1024 * 1024 + 4321)
        File(docRoot, "data/audio").mkdirs()
        File(docRoot, "data/audio/track.mp3").writeBytes(bytes)
        return bytes
    }

    private data class RawResponse(val code: Int, val headers: Map<String, List<String>>, val body: ByteArray) {
        fun header(name: String): String? =
            headers.entries.firstOrNull { it.key.equals(name, ignoreCase = true) }?.value?.firstOrNull()
    }

    private fun raw(url: String, headers: Map<String, String>, method: String = "GET"): RawResponse {
        val conn = URL(url).openConnection() as HttpURLConnection
        try {
            conn.requestMethod = method
            conn.connectTimeout = 5000
            conn.readTimeout = 60000
            headers.forEach { (name, value) -> conn.setRequestProperty(name, value) }
            val code = conn.responseCode
            val stream = if (code >= 400) conn.errorStream else conn.inputStream
            val body = if (method == "HEAD") ByteArray(0) else stream?.use { it.readBytes() } ?: ByteArray(0)
            return RawResponse(code, conn.headerFields.filterKeys { it != null }, body)
        } finally {
            conn.disconnect()
        }
    }

    private fun uploadedFile(response: Response, field: String): JsonObject {
        assertThat(response.code).isEqualTo(200)
        return json(response).getAsJsonObject("files").getAsJsonObject(field)
    }

    private fun json(response: Response): JsonObject =
        JsonParser.parseString(response.text).asJsonObject

    private fun post(url: String, body: Multipart): Response =
        request(url, "POST", body.bytes(), body.contentType())

    private fun get(url: String, cookie: String? = null): Response =
        request(url, "GET", null, null, cookie)

    private fun request(
        url: String,
        method: String,
        body: ByteArray?,
        contentType: String?,
        cookie: String? = null
    ): Response {
        val conn = URL(url).openConnection() as HttpURLConnection
        try {
            conn.instanceFollowRedirects = false
            conn.connectTimeout = 5000
            conn.readTimeout = 60000
            conn.requestMethod = method
            cookie?.let { conn.setRequestProperty("Cookie", it) }
            if (body != null) {
                conn.doOutput = true
                contentType?.let { conn.setRequestProperty("Content-Type", it) }
                conn.setFixedLengthStreamingMode(body.size)
                conn.outputStream.use { it.write(body) }
            }
            val code = conn.responseCode
            val stream = if (code >= 400) conn.errorStream else conn.inputStream
            val text = stream?.use { it.readBytes().toString(Charsets.UTF_8) }.orEmpty()
            val headers = conn.headerFields.filterKeys { it != null }
            return Response(code, text, headers)
        } finally {
            conn.disconnect()
        }
    }

    private data class Response(val code: Int, val text: String, val headers: Map<String, List<String>>) {
        fun header(name: String): String? =
            headers.entries.firstOrNull { it.key.equals(name, ignoreCase = true) }?.value?.firstOrNull()
    }

    private class Multipart {
        private val boundary = "----wta-test-" + Random.nextLong().toULong().toString(16)
        private val out = ByteArrayOutputStream()

        fun field(name: String, value: String) = apply {
            out.write("--$boundary\r\nContent-Disposition: form-data; name=\"$name\"\r\n\r\n".toByteArray())
            out.write(value.toByteArray())
            out.write("\r\n".toByteArray())
        }

        fun file(name: String, filename: String, type: String, bytes: ByteArray) = apply {
            out.write(
                ("--$boundary\r\nContent-Disposition: form-data; name=\"$name\"; filename=\"$filename\"\r\n" +
                    "Content-Type: $type\r\n\r\n").toByteArray()
            )
            out.write(bytes)
            out.write("\r\n".toByteArray())
        }

        fun contentType() = "multipart/form-data; boundary=$boundary"

        fun bytes(): ByteArray = out.toByteArray() + "--$boundary--\r\n".toByteArray()
    }

    private fun writeFixtures() {
        File(docRoot, "upload.php").writeText(UPLOAD_PHP)
        File(docRoot, "forge.php").writeText(FORGE_PHP)
        File(docRoot, "json.php").writeText(JSON_PHP)
        File(docRoot, "index.php").writeText(INDEX_PHP)
        File(docRoot, "fnsession.php").writeText(FN_SESSION_PHP)
        File(docRoot, "redirect.php").writeText("<?php header('Location: /target'); echo 'moved';")
        File(docRoot, "fail.php").writeText("<?php throw new RuntimeException('boom');")
        File(docRoot, "style.css").writeText("body{color:red}")
    }

    private fun pngBytes(): ByteArray = byteArrayOf(
        0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
    ) + Random(1).nextBytes(63317 - 8)

    private fun sha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }

    private fun phpBinary(): String? {
        System.getenv("PHP_BINARY")?.takeIf { File(it).canExecute() }?.let { return it }
        return System.getenv("PATH").orEmpty().split(File.pathSeparator)
            .map { File(it, "php") }
            .firstOrNull { it.isFile && it.canExecute() }
            ?.absolutePath
    }

    private companion object {
        val UPLOAD_TEMP_NAME = Regex("^php[A-Za-z0-9]{6}$")

        val UPLOAD_PHP = """
            <?php
            header('Content-Type: application/json');
            ${'$'}destDir = ${'$'}_POST['dest_dir'] ?? null;
            ${'$'}flat = [];
            foreach (${'$'}_FILES as ${'$'}field => ${'$'}f) {
                if (is_array(${'$'}f['name'])) {
                    foreach (array_keys(${'$'}f['name']) as ${'$'}i) {
                        ${'$'}flat[${'$'}field . '[' . ${'$'}i . ']'] = [
                            'name' => ${'$'}f['name'][${'$'}i], 'type' => ${'$'}f['type'][${'$'}i],
                            'tmp_name' => ${'$'}f['tmp_name'][${'$'}i], 'error' => ${'$'}f['error'][${'$'}i],
                            'size' => ${'$'}f['size'][${'$'}i],
                        ];
                    }
                } else {
                    ${'$'}flat[${'$'}field] = ${'$'}f;
                }
            }
            usleep(((int) (${'$'}_POST['delay_ms'] ?? 0)) * 1000);
            ${'$'}out = ['post' => ${'$'}_POST, 'files' => new stdClass()];
            foreach (${'$'}flat as ${'$'}key => ${'$'}f) {
                ${'$'}r = [
                    'name' => ${'$'}f['name'], 'type' => ${'$'}f['type'], 'size' => ${'$'}f['size'],
                    'error' => ${'$'}f['error'], 'tmp_name' => ${'$'}f['tmp_name'],
                    'tmp_exists' => ${'$'}f['tmp_name'] !== '' && is_file(${'$'}f['tmp_name']),
                    'is_uploaded_file' => is_uploaded_file(${'$'}f['tmp_name']),
                ];
                if (${'$'}destDir !== null && ${'$'}f['error'] === UPLOAD_ERR_OK) {
                    ${'$'}dest = ${'$'}destDir . '/' . bin2hex(random_bytes(8)) . '.upload';
                    error_clear_last();
                    ${'$'}r['move_success'] = @move_uploaded_file(${'$'}f['tmp_name'], ${'$'}dest);
                    ${'$'}r['move_error'] = error_get_last()['message'] ?? null;
                    ${'$'}r['dest'] = ${'$'}dest;
                    clearstatcache();
                    ${'$'}r['tmp_exists_after_move'] = is_file(${'$'}f['tmp_name']);
                    if (${'$'}r['move_success']) {
                        ${'$'}r['sha256'] = hash_file('sha256', ${'$'}dest);
                    }
                }
                ${'$'}out['files']->{${'$'}key} = ${'$'}r;
            }
            echo json_encode(${'$'}out);
        """.trimIndent()

        val FORGE_PHP = """
            <?php
            header('Content-Type: application/json');
            ${'$'}path = ${'$'}_POST['path'];
            ${'$'}dest = ${'$'}_POST['dest'];
            echo json_encode([
                'is_uploaded_file' => is_uploaded_file(${'$'}path),
                'move_success' => @move_uploaded_file(${'$'}path, ${'$'}dest),
                'source_exists' => is_file(${'$'}path),
            ]);
        """.trimIndent()

        val JSON_PHP = """
            <?php
            header('Content-Type: application/json');
            echo json_encode([
                'sapi' => PHP_SAPI,
                'method' => ${'$'}_SERVER['REQUEST_METHOD'],
                'input' => json_decode(file_get_contents('php://input'), true),
                'raw_global' => ${'$'}GLOBALS['__HTTP_RAW_BODY'] ?? null,
                'post' => (object) ${'$'}_POST,
                'script_name' => ${'$'}_SERVER['SCRIPT_NAME'],
                'has_request_parse_body' => function_exists('request_parse_body'),
            ]);
        """.trimIndent()

        val FN_SESSION_PHP = """
            <?php
            session_start();
            function bump(): int
            {
                ${'$'}_SESSION['m'] = (${'$'}_SESSION['m'] ?? 0) + 1;
                return ${'$'}_SESSION['m'];
            }
            header('Content-Type: application/json');
            echo json_encode(['m' => bump()]);
        """.trimIndent()

        val INDEX_PHP = """
            <?php
            session_start();
            ${'$'}_SESSION['n'] = (${'$'}_SESSION['n'] ?? 0) + 1;
            setcookie('wta_test', 'v1', ['path' => '/']);
            header('X-Test: yes');
            header('Content-Type: application/json');
            echo json_encode([
                'n' => ${'$'}_SESSION['n'],
                'uri' => ${'$'}_SERVER['REQUEST_URI'],
                'script_name' => ${'$'}_SERVER['SCRIPT_NAME'],
                'session_id' => session_id(),
                'cookie' => ${'$'}_COOKIE['PHPSESSID'] ?? null,
                'session_file' => ${'$'}GLOBALS['__session_file'] ?? null,
                'polyfill' => function_exists('wta_session_polyfill_flush'),
                'pid' => getmypid(),
            ]);
        """.trimIndent()
    }
}
