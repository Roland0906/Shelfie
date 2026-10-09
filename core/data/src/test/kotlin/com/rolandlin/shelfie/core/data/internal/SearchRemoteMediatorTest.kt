package com.rolandlin.shelfie.core.data.internal

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingState
import androidx.paging.RemoteMediator.InitializeAction
import androidx.paging.RemoteMediator.MediatorResult
import com.rolandlin.shelfie.core.common.DataError
import com.rolandlin.shelfie.core.common.asDataError
import com.rolandlin.shelfie.core.data.fake.FakeClock
import com.rolandlin.shelfie.core.data.fake.FakeOpenLibraryApi
import com.rolandlin.shelfie.core.data.fake.FakeOpenLibraryApi.Companion.doc
import com.rolandlin.shelfie.core.data.fake.FakeSearchDao
import com.rolandlin.shelfie.core.database.entity.SearchQueryEntity
import com.rolandlin.shelfie.core.database.entity.SearchResultEntity
import kotlinx.coroutines.test.runTest
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalPagingApi::class)
class SearchRemoteMediatorTest {

    private val api = FakeOpenLibraryApi()
    private val dao = FakeSearchDao()
    private val clock = FakeClock()
    private val policy = CachePolicy(searchPageSize = 2, maxCachedQueries = 2)

    private fun mediator(query: String = "dune") = SearchRemoteMediator(query, api, dao, clock, policy)

    private val emptyState = PagingState<Int, SearchResultEntity>(
        pages = emptyList(),
        anchorPosition = null,
        config = PagingConfig(pageSize = 2),
        leadingPlaceholderCount = 0,
    )

    init {
        api.searchResults["dune"] = listOf(doc("W1", "Dune"), doc("W2", "Dune Messiah"), doc("W3", "Children of Dune"))
    }

    @Test
    fun `fresh cache skips initial refresh`() = runTest {
        dao.upsertQuery(SearchQueryEntity("dune", nextPage = 2, fetchedAt = clock.now))
        clock.now += policy.searchTtl.inWholeMilliseconds - 1

        assertEquals(InitializeAction.SKIP_INITIAL_REFRESH, mediator().initialize())
    }

    @Test
    fun `expired or missing cache launches refresh`() = runTest {
        assertEquals(InitializeAction.LAUNCH_INITIAL_REFRESH, mediator().initialize())

        dao.upsertQuery(SearchQueryEntity("dune", nextPage = 2, fetchedAt = clock.now))
        clock.now += policy.searchTtl.inWholeMilliseconds
        assertEquals(InitializeAction.LAUNCH_INITIAL_REFRESH, mediator().initialize())
    }

    @Test
    fun `refresh then append pages through results with continuous positions`() = runTest {
        val mediator = mediator()

        val first = mediator.load(LoadType.REFRESH, emptyState)
        assertFalse(assertIs<MediatorResult.Success>(first).endOfPaginationReached)
        assertEquals(2, dao.getQuery("dune")!!.nextPage)

        val second = mediator.load(LoadType.APPEND, emptyState)
        assertTrue(assertIs<MediatorResult.Success>(second).endOfPaginationReached)
        assertNull(dao.getQuery("dune")!!.nextPage)

        assertEquals(listOf(0 to "W1", 1 to "W2", 2 to "W3"), dao.resultsFor("dune").map { it.position to it.workId })
    }

    @Test
    fun `a book repeated on a later page is listed once`() = runTest {
        val mediator = mediator()
        mediator.load(LoadType.REFRESH, emptyState)
        // The ranking shifts between requests, so page 2 repeats W2 from page 1
        api.searchResults["dune"] = listOf(doc("W1", "Dune"), doc("W0", "New"), doc("W2", "Dune Messiah"), doc("W3", "Children of Dune"))

        mediator.load(LoadType.APPEND, emptyState)

        assertEquals(listOf("W1", "W2", "W3"), dao.resultsFor("dune").map { it.workId })
    }

    @Test
    fun `refresh replaces stale results for the query`() = runTest {
        val mediator = mediator()
        mediator.load(LoadType.REFRESH, emptyState)
        api.searchResults["dune"] = listOf(doc("W9", "Dune (new edition)"))

        mediator.load(LoadType.REFRESH, emptyState)

        assertEquals(listOf("W9"), dao.resultsFor("dune").map { it.workId })
    }

    @Test
    fun `appending does not extend the freshness of the query`() = runTest {
        val mediator = mediator()
        mediator.load(LoadType.REFRESH, emptyState)
        val refreshedAt = clock.now

        clock.now += 60_000
        mediator.load(LoadType.APPEND, emptyState)

        assertEquals(refreshedAt, dao.getQuery("dune")!!.fetchedAt)
    }

    @Test
    fun `network error keeps cached results and surfaces a typed error`() = runTest {
        val mediator = mediator()
        mediator.load(LoadType.REFRESH, emptyState)
        api.failWith = IOException("offline")

        val result = mediator.load(LoadType.REFRESH, emptyState)

        assertEquals(DataError.Offline, assertIs<MediatorResult.Error>(result).throwable.asDataError())
        assertEquals(2, dao.resultsFor("dune").size)
    }

    @Test
    fun `empty result ends pagination immediately`() = runTest {
        val result = mediator("zzzz").load(LoadType.REFRESH, emptyState)

        assertTrue(assertIs<MediatorResult.Success>(result).endOfPaginationReached)
    }

    @Test
    fun `only the most recent queries are kept after refresh`() = runTest {
        api.searchResults["a"] = listOf(doc("A", "A"))
        api.searchResults["b"] = listOf(doc("B", "B"))
        api.searchResults["c"] = listOf(doc("C", "C"))

        for (query in listOf("a", "b", "c")) {
            clock.now += 1_000
            mediator(query).load(LoadType.REFRESH, emptyState)
        }

        assertEquals(setOf("b", "c"), dao.queries.value.keys)
        assertEquals(emptyList(), dao.resultsFor("a"))
    }

    @Test
    fun `query key normalization`() {
        assertEquals("dune messiah", SearchQueryKey.normalize("  Dune   MESSIAH "))
    }
}
