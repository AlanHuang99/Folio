package com.folio.reader.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingActionDao {

    @Query("SELECT * FROM pending_actions ORDER BY createdAt ASC")
    suspend fun getAll(): List<PendingAction>

    @Insert
    suspend fun insert(action: PendingAction)

    @Query("DELETE FROM pending_actions WHERE id = :id")
    suspend fun deleteById(id: Long)

    // Coalesce: a newer change to the same item+tag supersedes an older queued one.
    @Query("DELETE FROM pending_actions WHERE itemId = :itemId AND tag = :tag")
    suspend fun deleteByItemAndTag(itemId: String, tag: String)

    @Query("SELECT COUNT(*) FROM pending_actions")
    fun count(): Flow<Int>
}
