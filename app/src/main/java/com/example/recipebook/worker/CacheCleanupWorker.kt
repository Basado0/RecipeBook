package com.example.recipebook.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.recipebook.data.local.datastore.SettingsDataStore
import com.example.recipebook.data.local.searchResults.SearchMealDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class CacheCleanupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val searchMealDao: SearchMealDao,
    private val settingsDataStore: SettingsDataStore
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val settings = settingsDataStore.settingsFlow.first()
            val cacheTTLHours = settings.cacheTTLHours
            val expirationTime = System.currentTimeMillis() - (cacheTTLHours * 60 * 60 * 1000L)

            searchMealDao.deleteOlderThan(expirationTime)

            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}