package com.webtoapp.ui.screens

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.webtoapp.core.privacy.IsolationConfig
import com.webtoapp.data.database.AppDatabase
import com.webtoapp.data.dao.WebAppDao
import com.webtoapp.data.model.ApkEncryptionConfig
import com.webtoapp.data.model.ApkExportConfig
import com.webtoapp.data.model.AppType
import com.webtoapp.data.model.WebApp
import com.webtoapp.data.repository.WebAppRepository
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The build APK screen reads `WebApp.apkExportConfig` but historically never wrote it
 * back, so encryption / isolation / background-run / notification / engine options had
 * to be re-entered on every build. [persistBuildScreenExportConfig] is the write path.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class)
class BuildApkExportConfigPersistTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: WebAppDao
    private lateinit var repository: WebAppRepository

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.webAppDao()
        repository = WebAppRepository(dao)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `persist writes screen options onto an app that never had an export config`() = runTest {
        val id = dao.insert(WebApp(name = "A", url = "https://a.com", appType = AppType.WEB))
        val merged = ApkExportConfig(
            encryptionConfig = ApkEncryptionConfig(enabled = true),
            isolationConfig = IsolationConfig(enabled = true),
            backgroundRunEnabled = true,
            notificationEnabled = true,
            engineType = "GECKOVIEW",
            perAppSigningEnabled = true,
            forceFullRebuild = true
        )

        persistBuildScreenExportConfig(repository, id, merged)

        val stored = dao.getWebAppById(id)!!
        assertThat(stored.apkExportConfig).isEqualTo(merged)
    }

    @Test
    fun `persist keeps fields the build screen does not manage`() = runTest {
        val storedConfig = ApkExportConfig(
            customPackageName = "com.example.custom",
            customVersionCode = 7,
            customVersionName = "2.3.4"
        )
        val id = dao.insert(
            WebApp(
                name = "A",
                url = "https://a.com",
                appType = AppType.WEB,
                apkExportConfig = storedConfig
            )
        )

        // The screen merges its options onto the stored config before persisting.
        val merged = storedConfig.copy(backgroundRunEnabled = true, forceFullRebuild = true)
        persistBuildScreenExportConfig(repository, id, merged)

        val stored = dao.getWebAppById(id)!!
        assertThat(stored.apkExportConfig?.customPackageName).isEqualTo("com.example.custom")
        assertThat(stored.apkExportConfig?.customVersionCode).isEqualTo(7)
        assertThat(stored.apkExportConfig?.backgroundRunEnabled).isTrue()
        assertThat(stored.apkExportConfig?.forceFullRebuild).isTrue()
    }

    @Test
    fun `persist skips the write when the stored config already matches`() = runTest {
        val config = ApkExportConfig(engineType = "GECKOVIEW", forceFullRebuild = true)
        val id = dao.insert(
            WebApp(
                name = "A",
                url = "https://a.com",
                appType = AppType.WEB,
                apkExportConfig = config,
                updatedAt = 1234L
            )
        )

        persistBuildScreenExportConfig(repository, id, config)

        // updateWebApp bumps updatedAt; an untouched timestamp proves no write ran.
        assertThat(dao.getWebAppById(id)?.updatedAt).isEqualTo(1234L)
    }

    @Test
    fun `persist is a no-op for a missing app`() = runTest {
        persistBuildScreenExportConfig(repository, 9999L, ApkExportConfig(forceFullRebuild = true))

        assertThat(dao.getCount()).isEqualTo(0)
    }
}
