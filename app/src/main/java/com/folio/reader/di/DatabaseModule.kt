package com.folio.reader.di

import android.content.Context
import androidx.room.Room
import com.folio.reader.data.db.FolioDatabase
import com.folio.reader.data.db.PendingActionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FolioDatabase =
        Room.databaseBuilder(context, FolioDatabase::class.java, "folio.db").build()

    @Provides
    fun providePendingActionDao(database: FolioDatabase): PendingActionDao = database.pendingActionDao()
}
