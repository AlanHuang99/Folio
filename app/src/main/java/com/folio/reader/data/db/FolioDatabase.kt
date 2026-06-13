package com.folio.reader.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [PendingAction::class], version = 1, exportSchema = false)
abstract class FolioDatabase : RoomDatabase() {
    abstract fun pendingActionDao(): PendingActionDao
}
