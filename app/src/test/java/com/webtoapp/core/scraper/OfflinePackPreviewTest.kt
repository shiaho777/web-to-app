package com.webtoapp.core.scraper

import android.app.Application
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import java.io.File
import java.net.InetAddress
import java.net.ServerSocket
import java.net.SocketException
import kotlin.concurrent.thread
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class)
class OfflinePackPreviewTest {
    private val context get() = ApplicationProvider.getApplicationContext<Application>()

    @Test
    fun `captured pages and images can be browsed locally and missing resources are listed`(): Unit = runBlocking {
        val pages = mapOf(
                "/" to ("text/html" to """<!doctype html><html>
                    <a href="/chapters/intro#part">Next chapter</a>
                    <img src="cover.svg"><img src="missing.png">
                    </html>""".trimIndent()),
                "/chapters/intro" to ("text/html" to """<html><p id="part">Next page</p><img src="../cover.svg"></html>"""),
                "/cover.svg" to ("image/svg+xml" to """<svg xmlns="http://www.w3.org/2000/svg" width="50" height="50"/>""")
        )
        val server = ServerSocket(0, 8, InetAddress.getByName("127.0.0.1"))
        val serving = thread(isDaemon = true) { serve(server, pages) }
        val origin = "http://127.0.0.1:${server.localPort}"
        val root = File(context.cacheDir, "offline-preview-test")
        val result = try {
            WebsiteScraper(context, root).scrape(WebsiteScraper.ScrapeConfig(url = "$origin/"))
        } finally {
            server.close()
            serving.join(2_000)
        }

        assertThat(result).isInstanceOf(WebsiteScraper.ScrapeResult.Success::class.java)
        result as WebsiteScraper.ScrapeResult.Success
        assertThat(result.missingUrls).containsExactly("$origin/missing.png")
        val preview = OfflinePackPreviewSite(result)
        val entry = read(preview, preview.entryUrl)
        assertThat(entry).contains("""href="chapters/intro.html#part"""")
        assertThat(entry).contains("""src="cover.svg"""")
        assertThat(entry).contains("""src="$origin/missing.png"""")
        assertThat(read(preview, "${preview.origin}/chapters/intro.html")).contains("""src="../cover.svg"""")
        assertThat(read(preview, "${preview.origin}/cover.svg")).contains("<svg")

        val missing = preview.intercept(Uri.parse("$origin/missing.png?token=test"), "GET")
        assertThat(missing.statusCode).isEqualTo(404)
        assertThat(preview.intercept(Uri.parse("${preview.origin}/favicon.ico"), "GET").statusCode).isEqualTo(204)
        assertThat(preview.missing.value).containsExactly("$origin/missing.png")
        assertThat(preview.allowsNavigation(Uri.parse("$origin/"))).isFalse()
        assertThat(preview.intercept(Uri.parse("${preview.origin}/../outside"), "GET").statusCode).isEqualTo(404)
        assertThat(preview.intercept(Uri.parse(preview.entryUrl), "POST").statusCode).isEqualTo(404)
    }

    private fun serve(server: ServerSocket, pages: Map<String, Pair<String, String>>) {
        while (!server.isClosed) {
            val socket = try {
                server.accept()
            } catch (error: SocketException) {
                if (server.isClosed) return
                throw error
            }
            socket.use {
                it.soTimeout = 2_000
                val input = it.getInputStream().bufferedReader()
                val path = input.readLine()?.split(' ')?.getOrNull(1)
                while (!input.readLine().isNullOrEmpty()) Unit
                val output = it.getOutputStream()
                val response = pages[path]
                if (response == null) {
                    output.write("HTTP/1.1 404 Not Found\r\nContent-Length: 0\r\nConnection: close\r\n\r\n".toByteArray())
                } else {
                    val (mime, body) = response
                    val bytes = body.toByteArray()
                    val headers = "HTTP/1.1 200 OK\r\nContent-Type: $mime\r\nContent-Length: ${bytes.size}\r\nConnection: close\r\n\r\n"
                    output.write(headers.toByteArray())
                    output.write(bytes)
                }
            }
        }
    }

    private fun read(preview: OfflinePackPreviewSite, url: String): String {
        val response = preview.intercept(Uri.parse(url), "GET")
        assertThat(response.statusCode).isEqualTo(200)
        return response.data.bufferedReader().use { it.readText() }
    }
}
