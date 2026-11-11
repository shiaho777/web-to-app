package com.webtoapp.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.webtoapp.data.database.AppDatabase
import com.webtoapp.data.model.AppType
import com.webtoapp.data.model.SplashConfig
import com.webtoapp.data.model.SplashType
import com.webtoapp.data.model.WebApp
import com.webtoapp.data.repository.AppCategoryRepository
import com.webtoapp.data.repository.WebAppRepository
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
import java.io.File

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class)
class MainViewModelSplashSaveTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: WebAppRepository
    private lateinit var viewModel: MainViewModel
    private lateinit var splashDir: File

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @Before
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Default)
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = WebAppRepository(db.webAppDao())
        viewModel = MainViewModel(
            context as Application,
            repository,
            AppCategoryRepository(db.appCategoryDao())
        )
        splashDir = File(context.filesDir, "splash_media").apply { mkdirs() }
    }

    @After
    fun tearDown() {
        db.close()
        Dispatchers.resetMain()
    }

    @Test
    fun `saving after preview keeps the selected splash video`() = runBlocking {
        val splashFile = File(splashDir, "splash_video.mp4").apply {
            writeBytes(byteArrayOf(1, 2, 3))
        }
        selectSplash(splashFile)

        val appId = viewModel.saveAndPreview()
        assertThat(appId).isNotNull()
        assertThat(splashFile.exists()).isTrue()

        viewModel.saveApp()
        awaitSave()

        assertThat(splashFile.exists()).isTrue()
        assertThat(repository.getWebApp(appId!!)?.splashConfig?.mediaPath)
            .isEqualTo(splashFile.absolutePath)
    }

    @Test
    fun `saving a replaced splash deletes only the old file`() = runBlocking {
        val oldFile = File(splashDir, "splash_old.mp4").apply {
            writeBytes(byteArrayOf(1))
        }
        val newFile = File(splashDir, "splash_new.mp4").apply {
            writeBytes(byteArrayOf(2))
        }
        val appId = repository.createWebApp(
            WebApp(
                name = "Existing",
                url = "https://example.com",
                appType = AppType.WEB,
                splashEnabled = true,
                splashConfig = SplashConfig(
                    type = SplashType.VIDEO,
                    mediaPath = oldFile.absolutePath
                )
            )
        )
        viewModel.editApp(repository.getWebApp(appId)!!)

        viewModel.updateEditState {
            copy(
                splashMediaUri = Uri.fromFile(newFile),
                savedSplashPath = newFile.absolutePath
            )
        }

        viewModel.saveApp()
        awaitSave()

        assertThat(oldFile.exists()).isFalse()
        assertThat(newFile.exists()).isTrue()
        assertThat(repository.getWebApp(appId)?.splashConfig?.mediaPath)
            .isEqualTo(newFile.absolutePath)
    }

    private fun selectSplash(file: File) {
        viewModel.updateEditState {
            copy(
                name = "Splash preview",
                url = "https://example.com",
                splashEnabled = true,
                splashConfig = SplashConfig(type = SplashType.VIDEO),
                splashMediaUri = Uri.fromFile(file),
                savedSplashPath = file.absolutePath
            )
        }
    }

    private suspend fun awaitSave() {
        withTimeout(10_000) {
            viewModel.uiState.first { it is UiState.Success }
        }
    }
}
