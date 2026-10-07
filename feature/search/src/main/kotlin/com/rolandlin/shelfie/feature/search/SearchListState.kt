package com.rolandlin.shelfie.feature.search

import androidx.paging.LoadState
import com.rolandlin.shelfie.core.common.DataError
import com.rolandlin.shelfie.core.common.asDataError

/** What the result list should show, derived from Paging LoadStates as a pure function so it can be tested. */
internal sealed interface SearchListState {
    data object Idle : SearchListState
    data object Loading : SearchListState
    /** Loading finished with no results. */
    data object Empty : SearchListState
    /** An error with no cached results to fall back on. */
    data class Error(val error: DataError) : SearchListState
    /** Results are shown; a non-null [staleError] means the refresh failed and these are cached. */
    data class Results(val staleError: DataError?) : SearchListState

    companion object {
        fun from(query: String, refresh: LoadState, append: LoadState, itemCount: Int): SearchListState {
            if (query.isBlank()) return Idle
            if (itemCount > 0) return Results(staleError = (refresh as? LoadState.Error)?.error?.toDataError())
            return when (refresh) {
                is LoadState.Loading -> Loading
                is LoadState.Error -> Error(refresh.error.toDataError())
                is LoadState.NotLoading -> if (append.endOfPaginationReached) Empty else Loading
            }
        }

        private fun Throwable.toDataError(): DataError = asDataError() ?: DataError.Unknown(this)
    }
}
