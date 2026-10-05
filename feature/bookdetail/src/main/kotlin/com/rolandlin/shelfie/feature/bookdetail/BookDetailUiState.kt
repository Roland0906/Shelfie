package com.rolandlin.shelfie.feature.bookdetail

import com.rolandlin.shelfie.core.common.DataError
import com.rolandlin.shelfie.core.model.Book
import com.rolandlin.shelfie.core.model.ShelfEntry

sealed interface BookDetailUiState {
    /** Nothing cached yet and the first fetch is still running. */
    data object Loading : BookDetailUiState

    /** Nothing cached and the fetch failed, so there is nothing to show. */
    data class Error(val error: DataError) : BookDetailUiState

    /**
     * A book is shown. A failed refresh only adds [refreshError]; it never replaces
     * content that is already on screen.
     */
    data class Content(
        val book: Book,
        val shelfEntry: ShelfEntry?,
        val isRefreshing: Boolean,
        val refreshError: DataError?,
    ) : BookDetailUiState

    companion object {
        internal fun from(book: Book?, shelfEntry: ShelfEntry?, refresh: RefreshState): BookDetailUiState = when {
            book != null -> Content(
                book = book,
                shelfEntry = shelfEntry,
                isRefreshing = refresh is RefreshState.Refreshing,
                refreshError = (refresh as? RefreshState.Failed)?.error,
            )
            refresh is RefreshState.Failed -> Error(refresh.error)
            else -> Loading
        }
    }
}

internal sealed interface RefreshState {
    data object Idle : RefreshState
    data object Refreshing : RefreshState
    data class Failed(val error: DataError) : RefreshState
}
