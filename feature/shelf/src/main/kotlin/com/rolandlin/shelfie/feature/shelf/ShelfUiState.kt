package com.rolandlin.shelfie.feature.shelf

import com.rolandlin.shelfie.core.model.ShelfEntry
import com.rolandlin.shelfie.core.model.ShelfStatus

sealed interface ShelfUiState {
    data object Loading : ShelfUiState

    /**
     * [filter] is the one [entries] were loaded for, so the selected chip and the list
     * never disagree while a new filter is loading. A null filter means every status.
     */
    data class Content(
        val filter: ShelfStatus?,
        val entries: List<ShelfEntry>,
    ) : ShelfUiState
}
