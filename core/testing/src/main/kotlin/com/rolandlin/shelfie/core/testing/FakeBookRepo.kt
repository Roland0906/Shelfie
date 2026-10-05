package com.rolandlin.shelfie.core.testing

import androidx.paging.PagingData
import com.rolandlin.shelfie.core.common.Outcome
import com.rolandlin.shelfie.core.data.repository.BookRepo
import com.rolandlin.shelfie.core.model.Book
import com.rolandlin.shelfie.core.model.BookSummary
import com.rolandlin.shelfie.core.model.WorkId
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeBookRepo : BookRepo {
    private val books = MutableStateFlow<Map<WorkId, Book>>(emptyMap())

    /** Result of the next refresh; on success, [refreshedBook] (if set) is written to the "database". */
    var refreshOutcome: Outcome<Unit> = Outcome.Success(Unit)
    var refreshedBook: Book? = null

    /** When set, refresh suspends until it completes, so tests can observe the refreshing state. */
    var refreshGate: CompletableDeferred<Unit>? = null

    val refreshCalls = mutableListOf<Pair<WorkId, Boolean>>()
    val searchResults = mutableMapOf<String, List<BookSummary>>()
    val searchQueries = mutableListOf<String>()

    fun setBook(book: Book) = books.update { it + (book.workId to book) }

    override fun observeBook(workId: WorkId): Flow<Book?> = books.map { it[workId] }

    override suspend fun refreshBook(workId: WorkId, force: Boolean): Outcome<Unit> {
        refreshCalls += workId to force
        refreshGate?.await()
        val outcome = refreshOutcome
        if (outcome is Outcome.Success) refreshedBook?.let(::setBook)
        return outcome
    }

    override fun searchBooks(query: String): Flow<PagingData<BookSummary>> {
        searchQueries += query
        return flowOf(PagingData.from(searchResults[query].orEmpty()))
    }
}
