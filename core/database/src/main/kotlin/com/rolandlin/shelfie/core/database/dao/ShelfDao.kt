package com.rolandlin.shelfie.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.rolandlin.shelfie.core.database.entity.ShelfEntryEntity
import com.rolandlin.shelfie.core.model.ShelfStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ShelfDao {
    @Query("SELECT * FROM shelf_entries ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<ShelfEntryEntity>>

    @Query("SELECT * FROM shelf_entries WHERE status = :status ORDER BY updatedAt DESC")
    fun observeByStatus(status: ShelfStatus): Flow<List<ShelfEntryEntity>>

    @Query("SELECT * FROM shelf_entries WHERE workId = :workId")
    fun observe(workId: String): Flow<ShelfEntryEntity?>

    @Query("SELECT * FROM shelf_entries WHERE workId = :workId")
    suspend fun get(workId: String): ShelfEntryEntity?

    @Upsert
    suspend fun upsert(entry: ShelfEntryEntity)

    @Query("DELETE FROM shelf_entries WHERE workId = :workId")
    suspend fun delete(workId: String)
}
