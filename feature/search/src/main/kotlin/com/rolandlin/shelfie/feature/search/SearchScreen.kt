package com.rolandlin.shelfie.feature.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.rolandlin.shelfie.core.designsystem.component.BookCover
import com.rolandlin.shelfie.core.designsystem.component.CoverSize
import com.rolandlin.shelfie.core.designsystem.component.LoadingState
import com.rolandlin.shelfie.core.designsystem.component.MessageState
import com.rolandlin.shelfie.core.designsystem.component.StatusBanner
import com.rolandlin.shelfie.core.designsystem.component.message
import com.rolandlin.shelfie.core.model.BookSummary
import com.rolandlin.shelfie.core.model.WorkId
import org.koin.compose.viewmodel.koinViewModel

/** The feature's only public entry point. Navigation is passed in as callbacks, so it knows no other feature. */
@Composable
fun SearchRoute(
    onBookClick: (WorkId) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = koinViewModel(),
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val results = viewModel.results.collectAsLazyPagingItems()
    SearchScreen(
        query = query,
        results = results,
        onQueryChange = viewModel::onQueryChange,
        onBookClick = onBookClick,
        modifier = modifier,
    )
}

@Composable
internal fun SearchScreen(
    query: String,
    results: LazyPagingItems<BookSummary>,
    onQueryChange: (String) -> Unit,
    onBookClick: (WorkId) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text(stringResource(R.string.search_hint)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        )

        val state = SearchListState.from(
            query = query,
            refresh = results.loadState.refresh,
            append = results.loadState.append,
            itemCount = results.itemCount,
        )
        when (state) {
            SearchListState.Idle -> MessageState(title = stringResource(R.string.search_idle))
            SearchListState.Loading -> LoadingState()
            SearchListState.Empty -> MessageState(
                title = stringResource(R.string.search_empty_title),
                body = stringResource(R.string.search_empty_body, query.trim()),
            )
            is SearchListState.Error -> MessageState(
                title = state.error.message(),
                actionLabel = stringResource(R.string.search_retry),
                onAction = results::retry,
            )
            is SearchListState.Results -> {
                state.staleError?.let {
                    StatusBanner(
                        message = it.message(),
                        actionLabel = stringResource(R.string.search_retry),
                        onAction = results::retry,
                    )
                }
                LazyColumn(Modifier.fillMaxSize()) {
                    resultItems(results, onBookClick)
                    appendFooter(results.loadState.append, onRetry = results::retry)
                }
            }
        }
    }
}

private fun LazyListScope.resultItems(results: LazyPagingItems<BookSummary>, onBookClick: (WorkId) -> Unit) {
    items(
        count = results.itemCount,
        key = results.itemKey { it.workId.value },
        contentType = { "book" },
    ) { index ->
        val book = results[index] ?: return@items
        BookRow(book, onClick = { onBookClick(book.workId) })
        HorizontalDivider()
    }
}

/** Footer item: progress while the next page loads, a retry on failure, instead of a full-screen error. */
private fun LazyListScope.appendFooter(append: LoadState, onRetry: () -> Unit) {
    when (append) {
        is LoadState.Loading -> item(contentType = "footer") {
            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is LoadState.Error -> item(contentType = "footer") {
            Box(Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                TextButton(onClick = onRetry) { Text(stringResource(R.string.search_load_more_failed)) }
            }
        }
        is LoadState.NotLoading -> Unit
    }
}

@Composable
private fun BookRow(book: BookSummary, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BookCover(book.coverId, book.title, Modifier.width(48.dp), size = CoverSize.Small)
        Column(Modifier.weight(1f)) {
            Text(book.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (book.authorNames.isNotEmpty()) {
                Text(
                    book.authorNames.joinToString(),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            book.firstPublishYear?.let {
                Text(it.toString(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
