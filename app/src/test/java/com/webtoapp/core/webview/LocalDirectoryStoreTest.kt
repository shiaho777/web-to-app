package com.webtoapp.core.webview

import android.content.Context
import android.util.Base64
import androidx.documentfile.provider.DocumentFile
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.json.JSONObject
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class LocalDirectoryStoreTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `relative paths reject escape and accept nested names`() {
        assertThat(parseDirectoryPath("")).isEmpty()
        assertThat(parseDirectoryPath("/fonts/")).containsExactly("fonts").inOrder()
        assertThat(parseDirectoryPath("fonts/../secret")).isNull()
        assertThat(parseDirectoryPath("..")).isNull()
        assertThat(parseDirectoryPath("fonts/../../etc/passwd")).isNull()
        assertThat(parseDirectoryPath("a\u0000b")).isNull()
        assertThat(parseDirectoryPath("fonts/My Font.ttf")).containsExactly("fonts", "My Font.ttf").inOrder()
    }

    @Test
    fun `list read write and delete stay inside the granted folder`() {
        val parent = folder.newFolder("parent")
        val rootDir = File(parent, "granted").apply { mkdirs() }
        File(parent, "secret.txt").writeText("secret")
        val store = storeFor(rootDir)

        val written = JSONObject(store.write("fonts/Demo.ttf", base64("hello"), append = false))
        assertThat(written.getBoolean("ok")).isTrue()
        assertThat(File(rootDir, "fonts/Demo.ttf").readText()).isEqualTo("hello")

        val appended = JSONObject(store.write("fonts/Demo.ttf", base64("-more"), append = true))
        assertThat(appended.getBoolean("ok")).isTrue()
        assertThat(File(rootDir, "fonts/Demo.ttf").readText()).isEqualTo("hello-more")

        val replaced = JSONObject(store.write("fonts/Demo.ttf", base64("new"), append = false))
        assertThat(replaced.getBoolean("ok")).isTrue()
        assertThat(File(rootDir, "fonts/Demo.ttf").readText()).isEqualTo("new")
        store.write("fonts/Demo.ttf", base64("hello-more"), append = false)

        val listed = JSONObject(store.list("fonts"))
        assertThat(listed.getBoolean("ok")).isTrue()
        assertThat(listed.getJSONArray("entries").getJSONObject(0).getString("name")).isEqualTo("Demo.ttf")

        val read = JSONObject(store.read("fonts/Demo.ttf", 6, 3))
        assertThat(read.getBoolean("ok")).isTrue()
        assertThat(String(Base64.decode(read.getString("data"), Base64.DEFAULT))).isEqualTo("mor")
        assertThat(read.getBoolean("eof")).isFalse()

        val escaped = JSONObject(store.read("../secret.txt", 0, 16))
        assertThat(escaped.getBoolean("ok")).isFalse()
        assertThat(escaped.getString("error")).isEqualTo("BAD_PATH")
        assertThat(File(parent, "secret.txt").readText()).isEqualTo("secret")

        val removed = JSONObject(store.delete("fonts/Demo.ttf"))
        assertThat(removed.getBoolean("ok")).isTrue()
        assertThat(File(rootDir, "fonts/Demo.ttf").exists()).isFalse()

        val rootDelete = JSONObject(store.delete(""))
        assertThat(rootDelete.getString("error")).isEqualTo("BAD_PATH")
        assertThat(rootDir.exists()).isTrue()
    }

    @Test
    fun `read only grant refuses writes`() {
        val rootDir = folder.newFolder("readonly")
        val store = LocalDirectoryStore(
            context = context,
            grantKey = "read-only",
            rootOverride = { DocumentFile.fromFile(rootDir) },
            writableOverride = false
        )
        val result = JSONObject(store.write("note.txt", base64("x"), append = false))
        assertThat(result.getString("error")).isEqualTo("READ_ONLY")
        assertThat(File(rootDir, "note.txt").exists()).isFalse()
    }

    @Test
    fun `missing grant reports not granted`() {
        val store = LocalDirectoryStore(context, "missing-${System.nanoTime()}")
        val info = JSONObject(store.info())
        assertThat(info.getBoolean("ok")).isTrue()
        assertThat(info.getBoolean("granted")).isFalse()
        assertThat(JSONObject(store.list("")).getString("error")).isEqualTo("NO_GRANT")
    }

    private fun storeFor(root: File): LocalDirectoryStore {
        return LocalDirectoryStore(
            context = context,
            grantKey = "test-${root.name}",
            rootOverride = { DocumentFile.fromFile(root) },
            writableOverride = true
        )
    }

    private fun base64(text: String): String =
        Base64.encodeToString(text.toByteArray(), Base64.NO_WRAP)
}
