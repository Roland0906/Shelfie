package com.rolandlin.shelfie.core.data.internal

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.rolandlin.shelfie.core.common.Clock
import com.rolandlin.shelfie.core.common.Outcome
import com.rolandlin.shelfie.core.common.suspendRunCatching
import com.rolandlin.shelfie.core.data.repository.BookRepo
import com.rolandlin.shelfie.core.database.dao.BookDao
import com.rolandlin.shelfie.core.database.dao.SearchDao
import com.rolandlin.shelfie.core.database.entity.AuthorEntity
import com.rolandlin.shelfie.core.model.AuthorId
import com.rolandlin.shelfie.core.model.Book
import com.rolandlin.shelfie.core.model.BookSummary
import com.rolandlin.shelfie.core.model.WorkId
import com.rolandlin.shelfie.core.network.OpenLibraryApi
import com.rolandlin.shelfie.core.network.toDataError
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

internal class OfflineFirstBookRepo(
    private val api: OpenLibraryApi,
    private val bookDao: BookDao,
    private val searchDao: SearchDao,
    private val clock: Clock,
    private val policy: CachePolicy,
) : BookRepo {

    override fun observeBook(workId: WorkId): Flow<Book?> =
        combine(
            bookDao.observeBook(workId.value),
            bookDao.observeAuthors(workId.value),
        ) { book, authors -> book?.toModel(authors) }
            .distinctUntilChanged()

    override suspend fun refreshBook(workId: WorkId, force: Boolean): Outcome<Unit> {
        val cached = bookDao.getBook(workId.value)
        if (!force && cached != null && clock.nowMillis() - cached.fetchedAt < policy.bookTtl.inWholeMilliseconds) {
            return Outcome.Success(Unit)
        }

        return suspendRunCatching {
            val work = api.getWork(workId.value)
            val authorIds = work.authors
                .mapNotNull { it.author?.key }
                .map { AuthorId.fromKey(it).value }
                .distinct()
                .take(policy.maxAuthorsPerBook)
            val authors = fetchAuthors(authorIds)
            // The work API often lacks a publish year that search results have; keep the known one
            val fallbackYear = cached?.firstPublishYear ?: searchDao.findAnyResult(workId.value)?.firstPublishYear
            bookDao.replaceBook(work.toEntity(clock.nowMillis(), fallbackYear), authors)
        }.fold(
            onSuccess = { Outcome.Success(Unit) },
            onFailure = { Outcome.Failure(it.toDataError()) },
        )
    }

    /**
     * Authors are secondary: a failed author request falls back to the cached author and is
     * dropped only if there is none, so one author's 5xx never fails the whole book.
     */
    private suspend fun fetchAuthors(authorIds: List<String>): List<AuthorEntity> {
        if (authorIds.isEmpty()) return emptyList()
        val cachedById = bookDao.getAuthorsByIds(authorIds).associateBy { it.authorId }
        return coroutineScope {
            authorIds.map { id ->
                async {
                    suspendRunCatching { api.getAuthor(id).toEntity() }.getOrNull() ?: cachedById[id]
                }
            }.awaitAll()
        }.filterNotNull()
    }

    @OptIn(ExperimentalPagingApi::class)
    override fun searchBooks(query: String): Flow<PagingData<BookSummary>> {
        val key = SearchQueryKey.normalize(query)
        if (key.isEmpty()) return flowOf(PagingData.empty())

        return Pager(
            config = PagingConfig(
                pageSize = policy.searchPageSize,
                initialLoadSize = policy.searchPageSize,
                enablePlaceholders = false,
            ),
            remoteMediator = SearchRemoteMediator(key, api, searchDao, clock, policy),
            pagingSourceFactory = { searchDao.pagingSource(key) },
        ).flow.map { page -> page.map { it.toSummary() } }
    }
}
