package com.rolandlin.shelfie.feature.search

import androidx.paging.LoadState
import com.rolandlin.shelfie.core.common.DataError
import com.rolandlin.shelfie.core.common.DataException
import kotlin.test.Test
import kotlin.test.assertEquals

class SearchListStateTest {

    private val done = LoadState.NotLoading(endOfPaginationReached = true)
    private val more = LoadState.NotLoading(endOfPaginationReached = false)
    private val offline = LoadState.Error(DataException(DataError.Offline))

    @Test
    fun `blank query is idle`() {
        assertEquals(SearchListState.Idle, SearchListState.from(" ", LoadState.Loading, more, 0))
    }

    @Test
    fun `no results after loading completes is empty`() {
        assertEquals(SearchListState.Empty, SearchListState.from("zzzz", done, done, 0))
    }

    @Test
    fun `no items yet and more to load is still loading, not empty`() {
        assertEquals(SearchListState.Loading, SearchListState.from("dune", more, more, 0))
    }

    @Test
    fun `error without cache shows full-screen error`() {
        assertEquals(SearchListState.Error(DataError.Offline), SearchListState.from("dune", offline, more, 0))
    }

    @Test
    fun `error with cached items keeps the list and flags it as stale`() {
        assertEquals(SearchListState.Results(DataError.Offline), SearchListState.from("dune", offline, more, 3))
    }
}
