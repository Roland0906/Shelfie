package com.rolandlin.shelfie.core.data.internal

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.rolandlin.shelfie.core.common.Clock
import com.rolandlin.shelfie.core.common.DataException
import com.rolandlin.shelfie.core.common.suspendRunCatching
import com.rolandlin.shelfie.core.database.dao.SearchDao
import com.rolandlin.shelfie.core.database.entity.SearchQueryEntity
import com.rolandlin.shelfie.core.database.entity.SearchResultEntity
import com.rolandlin.shelfie.core.network.OpenLibraryApi
import com.rolandlin.shelfie.core.network.toDataError

/**
 * Network -> database; the UI always reads the database PagingSource.
 * A network failure leaves the database untouched, so the last results stay visible.
 */
@OptIn(ExperimentalPagingApi::class)
internal class SearchRemoteMediator(
    private val query: String,
    private val api: OpenLibraryApi,
    private val searchDao: SearchDao,
    private val clock: Clock,
    private val policy: CachePolicy,
) : RemoteMediator<Int, SearchResultEntity>() {

    /** A query with fresh cached results is not requested again. */
    override suspend fun initialize(): InitializeAction {
        val cached = searchDao.getQuery(query) ?: return InitializeAction.LAUNCH_INITIAL_REFRESH
        val age = clock.nowMillis() - cached.fetchedAt
        return if (age < policy.searchTtl.inWholeMilliseconds) {
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }

    override suspend fun load(loadType: LoadType, state: PagingState<Int, SearchResultEntity>): MediatorResult {
        val cachedQuery = searchDao.getQuery(query)
        val page = when (loadType) {
            LoadType.REFRESH -> 1
            // Results only grow at the end
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            // null nextPage = end reached; no query row = first page never succeeded, retry will REFRESH
            LoadType.APPEND -> cachedQuery?.nextPage ?: return MediatorResult.Success(endOfPaginationReached = true)
        }

        return suspendRunCatching {
            val response = api.search(query, page, policy.searchPageSize)
            val startPosition = if (loadType == LoadType.REFRESH) 0 else (searchDao.maxPosition(query) ?: -1) + 1
            val results = response.docs.mapIndexed { index, doc -> doc.toEntity(query, startPosition + index) }
            val endReached = response.docs.isEmpty() || response.start + response.docs.size >= response.numFound
            val key = SearchQueryEntity(
                query = query,
                nextPage = if (endReached) null else page + 1,
                // Freshness follows the first page; loading more does not extend the query's lifetime
                fetchedAt = if (loadType == LoadType.REFRESH) clock.nowMillis() else cachedQuery?.fetchedAt ?: clock.nowMillis(),
            )
            if (loadType == LoadType.REFRESH) {
                searchDao.replaceWithFirstPage(key, results)
                searchDao.trimTo(policy.maxCachedQueries)
            } else {
                searchDao.appendPage(key, results)
            }
            endReached
        }.fold(
            onSuccess = { MediatorResult.Success(endOfPaginationReached = it) },
            onFailure = { MediatorResult.Error(DataException(it.toDataError())) },
        )
    }
}
