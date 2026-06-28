package com.example.recipebook.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.recipebook.api.MealRepository
import com.example.recipebook.data.local.favourite.FavouriteDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class FavouritePreloadWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val favouriteDao: FavouriteDao,
    private val mealRepository: MealRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val favourites = favouriteDao.observeAll().first()

            favourites.forEach { entity ->
                try {
                    mealRepository.getRecipe(entity.id)
                } catch (e: Exception) {

                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}