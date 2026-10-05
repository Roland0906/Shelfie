package com.rolandlin.shelfie.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.rolandlin.shelfie.core.model.ShelfStatus

/**
 * Shelf entries live only on the device and intentionally have no foreign key to books:
 * the book cache may be cleared, but the shelf must survive it (title etc. are snapshots).
 */
@Entity(
    tableName = "shelf_entries",
    indices = [Index("status")],
)
data class ShelfEntryEntity(
    @PrimaryKey val workId: String,
    val title: String?,
    val authorNames: List<String>,
    val coverId: Long?,
    val status: ShelfStatus,
    val progressPercent: Int,
    val note: String,
    val addedAt: Long,
    val updatedAt: Long,
)
