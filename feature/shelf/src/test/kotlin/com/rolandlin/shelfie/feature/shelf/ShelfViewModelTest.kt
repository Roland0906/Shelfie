package com.rolandlin.shelfie.feature.shelf

import androidx.lifecycle.SavedStateHandle
import com.rolandlin.shelfie.core.model.ShelfStatus
import com.rolandlin.shelfie.core.model.WorkId
import com.rolandlin.shelfie.core.testing.FakeShelfRepo
import com.rolandlin.shelfie.core.testing.MainDispatcherRule
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class ShelfViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private val repo = FakeShelfRepo()
    private val dune = WorkId("OL1W")
    private val emma = WorkId("OL2W")

    /** uiState is shared WhileSubscribed, so tests keep one subscriber alive. */
    private fun TestScope.viewModel(savedState: SavedStateHandle = SavedStateHandle()): ShelfViewModel {
        val viewModel = ShelfViewModel(repo, savedState)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        return viewModel
    }

    private fun ShelfViewModel.content() = assertIs<ShelfUiState.Content>(uiState.value)

    @Test
    fun `shows every status by default`() = runTest {
        repo.upsert(dune, ShelfStatus.Reading)
        repo.upsert(emma, ShelfStatus.Finished)

        val content = viewModel().content()

        assertNull(content.filter)
        assertEquals(setOf(dune, emma), content.entries.map { it.workId }.toSet())
    }

    @Test
    fun `a filter narrows the list to one status`() = runTest {
        repo.upsert(dune, ShelfStatus.Reading)
        repo.upsert(emma, ShelfStatus.Finished)
        val viewModel = viewModel()

        viewModel.onFilterChange(ShelfStatus.Finished)

        assertEquals(ShelfStatus.Finished, viewModel.content().filter)
        assertEquals(listOf(emma), viewModel.content().entries.map { it.workId })
    }

    @Test
    fun `a status change moves the book out of the filtered list`() = runTest {
        repo.upsert(dune, ShelfStatus.Reading)
        val viewModel = viewModel()
        viewModel.onFilterChange(ShelfStatus.Reading)

        repo.updateProgress(dune, 100)

        assertEquals(emptyList(), viewModel.content().entries)
    }

    @Test
    fun `the filter is restored from saved state`() = runTest {
        repo.upsert(dune, ShelfStatus.Reading)
        repo.upsert(emma, ShelfStatus.Finished)

        val viewModel = viewModel(SavedStateHandle(mapOf(ShelfViewModel.KEY_FILTER to "Reading")))

        assertEquals(ShelfStatus.Reading, viewModel.content().filter)
        assertEquals(listOf(dune), viewModel.content().entries.map { it.workId })
    }

    @Test
    fun `an unknown saved filter falls back to every status`() {
        assertNull(ShelfViewModel.parseFilter("Abandoned"))
        assertNull(ShelfViewModel.parseFilter(null))
        assertEquals(ShelfStatus.Reading, ShelfViewModel.parseFilter("Reading"))
    }
}
