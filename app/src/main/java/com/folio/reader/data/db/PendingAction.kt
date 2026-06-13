package com.folio.reader.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A read/star change awaiting sync to the server (so changes survive offline). */
@Entity(tableName = "pending_actions")
data class PendingAction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemId: String,
    val tag: String, // user/-/state/com.google/read or .../starred
    val add: Boolean, // true = add the tag, false = remove it
    val createdAt: Long,
)
