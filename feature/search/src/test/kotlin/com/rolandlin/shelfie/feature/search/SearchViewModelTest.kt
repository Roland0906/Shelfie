package com.rolandlin.shelfie.feature.search

import androidx.lifecycle.SavedStateHandle
import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import com.rolandlin.shelfie.core.model.BookSummary
import com.rolandlin.shelfie.core.model.WorkId
import com.rolandlin.shelfie.core.testing.FakeBookRepo
import com.rolandlin.shelfie.core.testing.MainDispatcherRule
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals

class SearchViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule(StandardTestDispatcher())

    private val repo = FakeBookRepo()

    // Lazy so it is created after MainDispatcherRule installs the test Main dispatcher,
    // which viewModelScope then runs on.
    private val viewModel by lazy { SearchViewModel(repo, SavedStateHandle()) }

    @Test
    fun `typing quickly sends a single request 300ms after the last keystroke`() = runTest(mainDispatcher.dispatcher) {
        backgroundScope.launch { viewModel.results.collect {} }

        for (partial in listOf("d", "du", "dun", "dune")) {
            viewModel.onQueryChange(partial)
            advanceTimeBy(100)
        }
        assertEquals(emptyList(), repo.searchQueries)

        advanceTimeBy(SearchViewModel.DEBOUNCE_MILLIS)
        runCurrent()
        assertEquals(listOf("dune"), repo.searchQueries)
    }

    @Test
    fun `blank and whitespace-only changes do not hit the repository`() = runTest(mainDispatcher.dispatcher) {
        backgroundScope.launch { viewModel.results.collect {} }

        viewModel.onQueryChange("   ")
        advanceTimeBy(1_000)
        viewModel.onQueryChange("dune")
        advanceTimeBy(1_000)
        viewModel.onQueryChange("dune  ")
        advanceTimeBy(1_000)

        assertEquals(listOf("dune"), repo.searchQueries)
    }

    @Test
    fun `results come from the repository`() = runTest(mainDispatcher.dispatcher) {
        val dune = BookSummary(WorkId("OL1W"), "Dune", listOf("Frank Herbert"), null, 1965)
        repo.searchResults["dune"] = listOf(dune)
        val emitted = mutableListOf<PagingData<BookSummary>>()
        backgroundScope.launch { viewModel.results.collect { emitted += it } }

        viewModel.onQueryChange("dune")
        advanceTimeBy(SearchViewModel.DEBOUNCE_MILLIS + 1)
        runCurrent()

        // Snapshot the emitted page directly: re-subscribing to the shared cachedIn flow
        // with asSnapshot() never goes idle under the test dispatcher.
        assertEquals(listOf(dune), flowOf(emitted.last()).asSnapshot())
    }

    @Test
    fun `query survives process death through SavedStateHandle`() {
        val handle = SavedStateHandle()
        SearchViewModel(repo, handle).onQueryChange("dune")

        assertEquals("dune", SearchViewModel(repo, handle).query.value)
    }
}
