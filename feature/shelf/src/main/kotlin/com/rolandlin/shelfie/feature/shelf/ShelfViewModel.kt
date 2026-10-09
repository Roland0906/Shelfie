package com.rolandlin.shelfie.feature.shelf

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rolandlin.shelfie.core.data.repository.ShelfRepo
import com.rolandlin.shelfie.core.model.ShelfStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** The shelf is local only, so this screen has no loading from the network and no error state. */
class ShelfViewModel(
    private val shelfRepo: ShelfRepo,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ShelfUiState> = savedStateHandle.getStateFlow<String?>(KEY_FILTER, null)
        .map(::parseFilter)
        .flatMapLatest { filter -> shelfRepo.observeShelf(filter).map { ShelfUiState.Content(filter, it) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ShelfUiState.Loading)

    fun onFilterChange(filter: ShelfStatus?) {
        // Saved by name so the filter survives process death
        savedStateHandle[KEY_FILTER] = filter?.name
    }

    internal companion object {
        const val KEY_FILTER = "filter"

        /** A name saved by an older version that no longer exists falls back to every status. */
        fun parseFilter(name: String?): ShelfStatus? = ShelfStatus.entries.firstOrNull { it.name == name }
    }
}
