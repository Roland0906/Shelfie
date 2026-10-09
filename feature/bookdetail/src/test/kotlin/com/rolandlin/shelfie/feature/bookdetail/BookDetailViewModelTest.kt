package com.rolandlin.shelfie.feature.bookdetail

import com.rolandlin.shelfie.core.common.DataError
import com.rolandlin.shelfie.core.common.Outcome
import com.rolandlin.shelfie.core.model.Book
import com.rolandlin.shelfie.core.model.ShelfStatus
import com.rolandlin.shelfie.core.model.WorkId
import com.rolandlin.shelfie.core.testing.FakeBookRepo
import com.rolandlin.shelfie.core.testing.FakeShelfRepo
import com.rolandlin.shelfie.core.testing.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class BookDetailViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private val bookRepo = FakeBookRepo()
    private val shelfRepo = FakeShelfRepo()

    private val dune = Book(
        workId = WorkId("OL1W"),
        title = "Dune",
        description = "Spice.",
        authors = emptyList(),
        coverId = null,
        subjects = emptyList(),
        firstPublishYear = 1965,
        fetchedAt = 0,
    )

    /** uiState is shared WhileSubscribed, so tests keep one subscriber alive. */
    private fun TestScope.viewModel(): BookDetailViewModel {
        val viewModel = BookDetailViewModel(dune.workId, bookRepo, shelfRepo)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        return viewModel
    }

    @Test
    fun `cached book is shown and revalidated without forcing`() = runTest {
        bookRepo.setBook(dune)

        val viewModel = viewModel()

        val content = assertIs<BookDetailUiState.Content>(viewModel.uiState.value)
        assertEquals(dune, content.book)
        assertEquals(listOf(dune.workId to false), bookRepo.refreshCalls)
    }

    @Test
    fun `failed refresh keeps the cached book and adds an error`() = runTest {
        bookRepo.setBook(dune)
        bookRepo.refreshOutcome = Outcome.Failure(DataError.Offline)

        val viewModel = viewModel()

        val content = assertIs<BookDetailUiState.Content>(viewModel.uiState.value)
        assertEquals(dune, content.book)
        assertEquals(DataError.Offline, content.refreshError)
    }

    @Test
    fun `no cache and failed fetch shows an error screen`() = runTest {
        bookRepo.refreshOutcome = Outcome.Failure(DataError.NotFound)

        val viewModel = viewModel()

        assertEquals(BookDetailUiState.Error(DataError.NotFound), viewModel.uiState.value)
    }

    @Test
    fun `no cache shows loading until the first fetch lands`() = runTest {
        val gate = CompletableDeferred<Unit>()
        bookRepo.refreshGate = gate
        bookRepo.refreshedBook = dune

        val viewModel = viewModel()
        assertEquals(BookDetailUiState.Loading, viewModel.uiState.value)

        gate.complete(Unit)
        assertEquals(dune, assertIs<BookDetailUiState.Content>(viewModel.uiState.value).book)
    }

    @Test
    fun `manual refresh forces a fetch and clears the previous error`() = runTest {
        bookRepo.setBook(dune)
        bookRepo.refreshOutcome = Outcome.Failure(DataError.Offline)
        val viewModel = viewModel()

        bookRepo.refreshOutcome = Outcome.Success(Unit)
        viewModel.refresh()

        assertEquals(dune.workId to true, bookRepo.refreshCalls.last())
        assertNull(assertIs<BookDetailUiState.Content>(viewModel.uiState.value).refreshError)
    }

    @Test
    fun `refresh requests are ignored while one is running`() = runTest {
        bookRepo.setBook(dune)
        bookRepo.refreshGate = CompletableDeferred()
        val viewModel = viewModel()

        viewModel.refresh()
        viewModel.refresh()

        assertEquals(1, bookRepo.refreshCalls.size)
        assertEquals(true, assertIs<BookDetailUiState.Content>(viewModel.uiState.value).isRefreshing)
    }

    @Test
    fun `choosing a status shelves the book and removing takes it off`() = runTest {
        bookRepo.setBook(dune)
        val viewModel = viewModel()

        viewModel.setShelfStatus(ShelfStatus.Reading)
        assertEquals(ShelfStatus.Reading, assertIs<BookDetailUiState.Content>(viewModel.uiState.value).shelfEntry?.status)

        viewModel.removeFromShelf()
        assertNull(assertIs<BookDetailUiState.Content>(viewModel.uiState.value).shelfEntry)
    }

    @Test
    fun `progress updates the entry and reaching 100 finishes the book`() = runTest {
        bookRepo.setBook(dune)
        val viewModel = viewModel()
        viewModel.setShelfStatus(ShelfStatus.WantToRead)

        viewModel.setProgress(40)
        val reading = assertIs<BookDetailUiState.Content>(viewModel.uiState.value).shelfEntry
        assertEquals(40, reading?.progressPercent)
        assertEquals(ShelfStatus.Reading, reading?.status)

        viewModel.setProgress(100)
        assertEquals(ShelfStatus.Finished, assertIs<BookDetailUiState.Content>(viewModel.uiState.value).shelfEntry?.status)
    }
}
