package com.rolandlin.shelfie.core.data.repository

import androidx.paging.PagingData
import com.rolandlin.shelfie.core.common.Outcome
import com.rolandlin.shelfie.core.model.Book
import com.rolandlin.shelfie.core.model.BookSummary
import com.rolandlin.shelfie.core.model.WorkId
import kotlinx.coroutines.flow.Flow

/**
 * Reads are always observed from the database, the single source of truth; network
 * results only reach callers by being written there first.
 */
interface BookRepo {
    /** Emits null while the book has never been cached. */
    fun observeBook(workId: WorkId): Flow<Book?>

    /**
     * Skips the network while the cache is fresh, unless [force] is set.
     * A failed refresh never clears cached data.
     */
    suspend fun refreshBook(workId: WorkId, force: Boolean = false): Outcome<Unit>

    /** A blank query yields empty results without a request. */
    fun searchBooks(query: String): Flow<PagingData<BookSummary>>
}
