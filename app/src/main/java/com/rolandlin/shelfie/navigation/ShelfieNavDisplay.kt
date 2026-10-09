package com.rolandlin.shelfie.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.rolandlin.shelfie.R
import com.rolandlin.shelfie.core.model.WorkId
import com.rolandlin.shelfie.feature.bookdetail.BookDetailRoute
import com.rolandlin.shelfie.feature.search.SearchRoute
import com.rolandlin.shelfie.feature.shelf.ShelfRoute
import kotlinx.serialization.Serializable

@Serializable
data object SearchKey : NavKey

@Serializable
data object ShelfKey : NavKey

@Serializable
data class BookDetailKey(val workId: String) : NavKey

private enum class TopLevelDestination(val key: NavKey, val icon: ImageVector, @StringRes val label: Int) {
    Search(SearchKey, Icons.Default.Search, R.string.nav_search),
    Shelf(ShelfKey, Icons.AutoMirrored.Filled.List, R.string.nav_shelf),
}

/**
 * Features never reference each other; they expose callbacks and the app turns those
 * into back stack changes here.
 *
 * One back stack that always starts at Search, with Shelf placed right above it when
 * selected. Search therefore stays alive with its query and results while Shelf is shown,
 * and back from Shelf returns to Search. Switching tabs drops detail screens opened in
 * the other tab.
 */
@Composable
fun ShelfieNavDisplay(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(SearchKey)
    val currentTab = if (ShelfKey in backStack) TopLevelDestination.Shelf else TopLevelDestination.Search
    val openBook: (WorkId) -> Unit = { backStack.add(BookDetailKey(it.value)) }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar {
                TopLevelDestination.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = tab == currentTab,
                        onClick = { backStack.selectTab(tab) },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(stringResource(tab.label)) },
                    )
                }
            }
        },
    ) { padding ->
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            // Consumed so screens with their own top bar do not pad for the status bar twice
            modifier = Modifier.padding(padding).consumeWindowInsets(padding),
            // Each entry gets its own saved state and ViewModelStore, cleared when it is popped
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider {
                entry<SearchKey> {
                    SearchRoute(onBookClick = openBook, modifier = Modifier.safeDrawingPadding())
                }
                entry<ShelfKey> {
                    ShelfRoute(onBookClick = openBook, modifier = Modifier.safeDrawingPadding())
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
}

private fun MutableList<NavKey>.selectTab(tab: TopLevelDestination) {
    val rootIndex = indexOf(tab.key)
    if (rootIndex >= 0) {
        // Re-selecting a tab pops back to its root, as Material navigation bars do
        while (lastIndex > rootIndex) removeAt(lastIndex)
    } else {
        while (size > 1) removeAt(lastIndex)
        add(tab.key)
    }
}
