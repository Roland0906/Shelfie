package com.rolandlin.shelfie.feature.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.rolandlin.shelfie.core.data.repository.BookRepo
import com.rolandlin.shelfie.core.model.BookSummary
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class SearchViewModel(
    private val bookRepo: BookRepo,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    /** Kept in SavedStateHandle so the query survives process death. */
    val query: StateFlow<String> = savedStateHandle.getStateFlow(KEY_QUERY, "")

    /**
     * - debounce: search only after typing pauses for 300ms
     * - distinctUntilChanged: "dune" -> "dune " does not search again
     * - flatMapLatest: a new query cancels paging of the previous one
     * - cachedIn: rotation does not reload
     */
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val results: Flow<PagingData<BookSummary>> = query
        .debounce(DEBOUNCE_MILLIS)
        .map { it.trim() }
        .distinctUntilChanged()
        .flatMapLatest { if (it.isEmpty()) flowOf(PagingData.empty()) else bookRepo.searchBooks(it) }
        .cachedIn(viewModelScope)

    fun onQueryChange(query: String) {
        savedStateHandle[KEY_QUERY] = query
    }

    internal companion object {
        const val DEBOUNCE_MILLIS = 300L
        private const val KEY_QUERY = "query"
    }
}
