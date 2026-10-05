package com.rolandlin.shelfie.core.data.repository

import com.rolandlin.shelfie.core.model.ShelfEntry
import com.rolandlin.shelfie.core.model.ShelfStatus
import com.rolandlin.shelfie.core.model.WorkId
import kotlinx.coroutines.flow.Flow

/** The shelf is local only, so every operation works offline. */
interface ShelfRepo {
    fun observeShelf(status: ShelfStatus? = null): Flow<List<ShelfEntry>>
    fun observeEntry(workId: WorkId): Flow<ShelfEntry?>

    /** Adds the book or changes its status; new entries snapshot title etc. from local caches. */
    suspend fun upsert(workId: WorkId, status: ShelfStatus)

    /** Returns false when the book is not on the shelf. */
    suspend fun updateProgress(workId: WorkId, percent: Int): Boolean
    suspend fun updateNote(workId: WorkId, note: String): Boolean
    suspend fun remove(workId: WorkId)
}
