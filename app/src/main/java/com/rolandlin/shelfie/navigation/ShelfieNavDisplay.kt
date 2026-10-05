package com.rolandlin.shelfie.navigation

import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.rolandlin.shelfie.core.model.WorkId
import com.rolandlin.shelfie.feature.bookdetail.BookDetailRoute
import com.rolandlin.shelfie.feature.search.SearchRoute
import kotlinx.serialization.Serializable

@Serializable
data object SearchKey : NavKey

@Serializable
data class BookDetailKey(val workId: String) : NavKey

/**
 * Features never reference each other; they expose callbacks and the app turns those
 * into back stack changes here.
 */
@Composable
fun ShelfieNavDisplay(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(SearchKey)
    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        modifier = modifier,
        // Each entry gets its own saved state and ViewModelStore, cleared when it is popped
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<SearchKey> {
                SearchRoute(
                    onBookClick = { workId -> backStack.add(BookDetailKey(workId.value)) },
                    modifier = Modifier.safeDrawingPadding(),
                )
            }
            entry<BookDetailKey> { key ->
                BookDetailRoute(
                    workId = WorkId(key.workId),
                    onBack = { backStack.removeLastOrNull() },
                )
            }
        },
    )
}
