package com.webtoapp.data.dao

import android.content.Context
import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import com.webtoapp.data.database.AppDatabase
import com.webtoapp.data.database.addHomeSortIndex
import com.webtoapp.data.model.AppType
import com.webtoapp.data.model.WebApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class)
class WebAppDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: WebAppDao

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.webAppDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `startup candidates only return web apps without icons`() = runTest {
        dao.insert(
            WebApp(name = "A", url = "http://a.com", appType = AppType.WEB)
        )
        dao.insert(
            WebApp(name = "B", url = "http://b.com", iconPath = "file:///icon.png", appType = AppType.WEB)
        )
        dao.insert(
            WebApp(name = "C", url = "https://c.com", appType = AppType.HTML)
        )

        val candidates = dao.getStartupCandidatesWithoutIcons(AppType.WEB, 10)

        assertThat(candidates).hasSize(1)
        assertThat(candidates.single().name).isEqualTo("A")
    }

    @Test
    fun `upgrade remote http urls only touches web apps`() = runTest {
        val webId = dao.insert(WebApp(name = "A", url = "http://a.com", appType = AppType.WEB))
        dao.insert(WebApp(name = "B", url = "http://b.com", appType = AppType.HTML))

        val updated = dao.upgradeRemoteHttpUrls(AppType.WEB, 123L)

        assertThat(updated).isEqualTo(1)
        assertThat(dao.getWebAppById(webId)?.url).isEqualTo("https://a.com")
    }

    @Test
    fun `getAllWebAppSummaries returns lightweight rows with flag columns`() = runTest {
        dao.insert(
            WebApp(
                name = "A",
                url = "https://a.com",
                appType = AppType.WEB,
                iconPath = "file:///a.png",
                categoryId = 7L,
                activationEnabled = true,
                adBlockEnabled = false,
                announcementEnabled = true,
            )
        )
        dao.insert(
            WebApp(
                name = "B",
                url = "",
                appType = AppType.HTML,
                activationEnabled = false,
                adBlockEnabled = true,
                announcementEnabled = false,
            )
        )

        val summaries = dao.getAllWebAppSummaries().first()

        assertThat(summaries).hasSize(2)
        val byName = summaries.associateBy { it.name }
        val a = byName.getValue("A")
        assertThat(a.url).isEqualTo("https://a.com")
        assertThat(a.iconPath).isEqualTo("file:///a.png")
        assertThat(a.appType).isEqualTo(AppType.WEB)
        assertThat(a.categoryId).isEqualTo(7L)
        assertThat(a.activationEnabled).isTrue()
        assertThat(a.adBlockEnabled).isFalse()
        assertThat(a.announcementEnabled).isTrue()
        assertThat(a.homeSortIndex).isEqualTo(0)
        assertThat(a.createdAt).isGreaterThan(0L)

        val b = byName.getValue("B")
        assertThat(b.url).isEmpty()
        assertThat(b.iconPath).isNull()
        assertThat(b.appType).isEqualTo(AppType.HTML)
        assertThat(b.categoryId).isNull()
        assertThat(b.activationEnabled).isFalse()
        assertThat(b.adBlockEnabled).isTrue()
        assertThat(b.announcementEnabled).isFalse()
    }

    @Test
    fun `setHomeSortOrder writes indices without touching timestamps`() = runTest {
        val older = dao.insert(
            WebApp(name = "Older", url = "https://older.example", createdAt = 10, updatedAt = 10)
        )
        val newer = dao.insert(
            WebApp(name = "Newer", url = "https://newer.example", createdAt = 5, updatedAt = 20)
        )
        val beforeOlder = dao.getWebAppById(older)!!
        val beforeNewer = dao.getWebAppById(newer)!!

        dao.setHomeSortOrder(listOf(older, newer))

        val afterOlder = dao.getWebAppById(older)!!
        val afterNewer = dao.getWebAppById(newer)!!
        assertThat(afterOlder.homeSortIndex).isEqualTo(0)
        assertThat(afterNewer.homeSortIndex).isEqualTo(1)
        assertThat(afterOlder.updatedAt).isEqualTo(beforeOlder.updatedAt)
        assertThat(afterNewer.updatedAt).isEqualTo(beforeNewer.updatedAt)
        assertThat(afterOlder.createdAt).isEqualTo(beforeOlder.createdAt)
        assertThat(afterNewer.createdAt).isEqualTo(beforeNewer.createdAt)

        val summaries = dao.getAllWebAppSummaries().first().associateBy { it.id }
        assertThat(summaries.getValue(older).homeSortIndex).isEqualTo(0)
        assertThat(summaries.getValue(newer).homeSortIndex).isEqualTo(1)
        assertThat(summaries.getValue(older).createdAt).isEqualTo(10L)
    }

    @Test
    fun `migration backfill numbers rows by updatedAt then id`() {
        context.deleteDatabase(MIGRATION_DB)
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(MIGRATION_DB)
                .callback(object : SupportSQLiteOpenHelper.Callback(45) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL(
                            """
                            CREATE TABLE web_apps (
                                id INTEGER PRIMARY KEY NOT NULL,
                                updatedAt INTEGER NOT NULL
                            )
                            """.trimIndent()
                        )
                    }

                    override fun onUpgrade(
                        db: SupportSQLiteDatabase,
                        oldVersion: Int,
                        newVersion: Int,
                    ) = Unit
                })
                .build()
        )
        try {
            val db = helper.writableDatabase
            db.execSQL("INSERT INTO web_apps (id, updatedAt) VALUES (1, 50)")
            db.execSQL("INSERT INTO web_apps (id, updatedAt) VALUES (2, 80)")
            db.execSQL("INSERT INTO web_apps (id, updatedAt) VALUES (3, 80)")
            db.addHomeSortIndex()

            val cursor = db.query(
                "SELECT id, homeSortIndex, updatedAt FROM web_apps ORDER BY homeSortIndex ASC"
            )
            val rows = mutableListOf<Triple<Long, Int, Long>>()
            try {
                while (cursor.moveToNext()) {
                    rows += Triple(cursor.getLong(0), cursor.getInt(1), cursor.getLong(2))
                }
            } finally {
                cursor.close()
            }
            assertThat(rows).containsExactly(
                Triple(3L, 0, 80L),
                Triple(2L, 1, 80L),
                Triple(1L, 2, 50L),
            ).inOrder()
        } finally {
            helper.close()
            context.deleteDatabase(MIGRATION_DB)
        }
    }

    private companion object {
        const val MIGRATION_DB = "home-sort-migration"
    }
}
