package com.rolandlin.shelfie.feature.bookdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rolandlin.shelfie.core.common.Outcome
import com.rolandlin.shelfie.core.data.repository.BookRepo
import com.rolandlin.shelfie.core.data.repository.ShelfRepo
import com.rolandlin.shelfie.core.model.ShelfStatus
import com.rolandlin.shelfie.core.model.WorkId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Shows the cached book immediately and revalidates it in the background. The screen
 * state is derived from the database flows plus the refresh status, so a refresh can
 * only add an error banner, never blank out a book that is already shown.
 */
class BookDetailViewModel(
    private val workId: WorkId,
    private val bookRepo: BookRepo,
    private val shelfRepo: ShelfRepo,
) : ViewModel() {

    private val refreshState = MutableStateFlow<RefreshState>(RefreshState.Idle)

    val uiState: StateFlow<BookDetailUiState> = combine(
        bookRepo.observeBook(workId),
        shelfRepo.observeEntry(workId),
        refreshState,
        BookDetailUiState::from,
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BookDetailUiState.Loading)

    init {
        refresh(force = false)
    }

    /** User-initiated refreshes bypass the cache TTL; the automatic one on open does not. */
    fun refresh(force: Boolean = true) {
        if (refreshState.value == RefreshState.Refreshing) return
        refreshState.value = RefreshState.Refreshing
        viewModelScope.launch {
            refreshState.value = when (val outcome = bookRepo.refreshBook(workId, force)) {
                is Outcome.Success -> RefreshState.Idle
                is Outcome.Failure -> RefreshState.Failed(outcome.error)
            }
        }
    }

    fun setShelfStatus(status: ShelfStatus) {
        viewModelScope.launch { shelfRepo.upsert(workId, status) }
    }

    fun removeFromShelf() {
        viewModelScope.launch { shelfRepo.remove(workId) }
    }
}
