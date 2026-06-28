package com.example.recipebook.IntegrationTests.worker

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.example.recipebook.data.local.RecipeBookDatabase
import com.example.recipebook.data.local.datastore.SettingsDataStore
import com.example.recipebook.data.local.searchResults.SearchMealDao
import com.example.recipebook.data.local.searchResults.SearchMealEntity
import com.example.recipebook.worker.CacheCleanupWorker
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CacheCleanupWorkerTest {

    private lateinit var context: Context
    private lateinit var database: RecipeBookDatabase
    private lateinit var dao: SearchMealDao
    private lateinit var settingsDataStore: SettingsDataStore

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, RecipeBookDatabase::class.java)
            .build()
        dao = database.searchMealDao()
        settingsDataStore = SettingsDataStore(context)
        runBlocking { settingsDataStore.updateCacheTTL(1) }
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testCleanupRemovesOldEntries() = runBlocking {
        val oldEntity =
            SearchMealEntity(1, "old", "img", System.currentTimeMillis() - 2 * 3600 * 1000L)
        dao.insertAll(listOf(oldEntity))

        val workerFactory = object : WorkerFactory() {
            override fun createWorker(
                appContext: Context,
                workerClassName: String,
                workerParameters: WorkerParameters
            ): ListenableWorker? {
                return if (workerClassName == CacheCleanupWorker::class.java.name) {
                    CacheCleanupWorker(appContext, workerParameters, dao, settingsDataStore)
                } else {
                    null
                }
            }
        }

        val worker = TestListenableWorkerBuilder<CacheCleanupWorker>(context)
            .setWorkerFactory(workerFactory)
            .build()

        val result = worker.doWork()
        assertEquals(ListenableWorker.Result.success(), result)

        val all = dao.getAll()
        assertTrue(all.isEmpty())
    }
}