package com.folio.reader.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.folio.reader.data.repository.ArticleRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/**
 * Flushes the queued read/star actions. Uses a Hilt EntryPoint to obtain the
 * repository, so the default WorkManager factory can instantiate it (no hilt-work
 * dependency needed).
 */
class SyncWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface SyncWorkerEntryPoint {
        fun articleRepository(): ArticleRepository
    }

    override suspend fun doWork(): Result = try {
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            SyncWorkerEntryPoint::class.java,
        )
        if (entryPoint.articleRepository().flush()) Result.success() else Result.retry()
    } catch (e: Exception) {
        Result.retry()
    }
}
