package com.webtoapp.ui.viewmodel

import android.app.Application
import android.database.sqlite.SQLiteFullException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.webtoapp.data.dao.WebAppDao
import com.webtoapp.data.database.AppDatabase
import com.webtoapp.data.model.AppType
import com.webtoapp.data.model.GalleryConfig
import com.webtoapp.data.model.GalleryItem
import com.webtoapp.data.model.GalleryItemType
import com.webtoapp.data.model.HtmlConfig
import com.webtoapp.data.model.HtmlFile
import com.webtoapp.data.model.HtmlFileType
import com.webtoapp.data.model.WebApp
import com.webtoapp.data.repository.AppCategoryRepository
import com.webtoapp.data.repository.WebAppRepository
import com.webtoapp.ui.screens.GalleryEditorDraft
import java.io.File
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class)
class EditorSaveFlowTest {
    private val context: Application get() = ApplicationProvider.getApplicationContext()
    private lateinit var database: AppDatabase

    @Before
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Default)
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    }

    @After
    fun tearDown() {
        database.close()
        Dispatchers.resetMain()
    }

    @Test
    fun `gallery waits for the insert, retains imports after failure and can retry`(): Unit = runBlocking {
        val realDao = database.webAppDao()
        val insertStarted = CompletableDeferred<Unit>()
        val failInsert = CompletableDeferred<Unit>()
        val dao = object : WebAppDao by realDao {
            override suspend fun insert(webApp: WebApp): Long {
                if (!failInsert.isCompleted) {
                    insertStarted.complete(Unit)
                    failInsert.await()
                    throw SQLiteFullException("simulated database failure")
                }
                return realDao.insert(webApp)
            }
        }
        val viewModel = mainViewModel(dao)
        val editor = EditorSaveViewModel()
        val draft = GalleryEditorDraft()
        val media = File(context.filesDir, "gallery-draft.mp4").apply { writeText("test media") }
        val item = GalleryItem(path = media.path, type = GalleryItemType.VIDEO)
        draft.trackImportedItem(item)
        val config = GalleryConfig(items = listOf(item))

        fun submit(): CompletableDeferred<Boolean> {
            val finished = CompletableDeferred<Boolean>()
            draft.isSaving = true
            assertThat(editor.submit(onFinished = {
                draft.finishSave(it)
                finished.complete(it)
            }) {
                viewModel.saveGalleryApp("Retained gallery", config, null)
            }).isTrue()
            return finished
        }

        val first = submit()
        withTimeout(5_000) { insertStarted.await() }
        assertThat(editor.state.value).isInstanceOf(EditorSaveState.Saving::class.java)
        assertThat(editor.submit { error("Duplicate save reached the database") }).isFalse()
        assertThat(realDao.getCount()).isEqualTo(0)

        failInsert.complete(Unit)
        assertThat(withTimeout(5_000) { first.await() }).isFalse()
        assertThat(editor.state.value).isInstanceOf(EditorSaveState.Failed::class.java)
        assertThat(media.exists()).isTrue()
        assertThat(draft.importedItems).containsKey(item.id)

        editor.dismissError()
        assertThat(withTimeout(5_000) { submit().await() }).isTrue()
        assertThat(editor.state.value).isEqualTo(EditorSaveState.Saved)
        assertThat(realDao.getAllWebApps().first().single().galleryConfig?.items).containsExactly(item)
        assertThat(draft.importedItems).isEmpty()
        assertThat(media.exists()).isTrue()
    }

    @Test
    fun `failed HTML update preserves the old project and imported draft until a retry commits`(): Unit = runBlocking {
        val realDao = database.webAppDao()
        val projects = File(context.filesDir, "html_projects")
        val oldFile = File(projects, "published/index.html").apply {
            parentFile?.mkdirs()
            writeText("<html><body>Published</body></html>")
        }
        val input = File(context.cacheDir, "html_temp/index.html").apply {
            parentFile?.mkdirs()
            writeText("<html><body>Draft</body></html>")
        }
        val savedId = realDao.insert(WebApp(
            name = "Published",
            url = "",
            appType = AppType.HTML,
            htmlConfig = HtmlConfig(projectId = "published", files = listOf(htmlFile(oldFile)))
        ))
        var failUpdate = true
        val dao = object : WebAppDao by realDao {
            override suspend fun updatePreservingPublishedIdentity(webApp: WebApp, formerAutoPackageName: String?): Boolean {
                if (failUpdate) throw SQLiteFullException("simulated database failure")
                return realDao.updatePreservingPublishedIdentity(webApp, formerAutoPackageName)
            }
        }
        val viewModel = mainViewModel(dao)
        val config = HtmlConfig(files = listOf(htmlFile(input)))

        val failed = runCatching { viewModel.updateHtmlApp(savedId, "Updated", config, null) }
        assertThat(failed.exceptionOrNull()).isInstanceOf(SQLiteFullException::class.java)
        assertThat(realDao.getWebAppById(savedId)?.htmlConfig?.projectId).isEqualTo("published")
        assertThat(projects.list().orEmpty()).asList().containsExactly("published")
        assertThat(oldFile.readText()).contains("Published")
        assertThat(input.readText()).contains("Draft")

        failUpdate = false
        viewModel.updateHtmlApp(savedId, "Updated", config, null)
        val after = checkNotNull(realDao.getWebAppById(savedId))
        assertThat(after.name).isEqualTo("Updated")
        assertThat(File(checkNotNull(after.htmlConfig).files.single().path).readText()).contains("Draft")
        assertThat(oldFile.exists()).isFalse()
        assertThat(input.exists()).isFalse()
    }

    private fun mainViewModel(dao: WebAppDao): MainViewModel =
        MainViewModel(context, WebAppRepository(dao), AppCategoryRepository(database.appCategoryDao()))

    private fun htmlFile(file: File): HtmlFile = HtmlFile(file.name, file.path, HtmlFileType.HTML)
}
