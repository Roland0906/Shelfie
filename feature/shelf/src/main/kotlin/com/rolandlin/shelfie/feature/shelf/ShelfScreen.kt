package com.rolandlin.shelfie.feature.shelf

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rolandlin.shelfie.core.designsystem.component.BookCover
import com.rolandlin.shelfie.core.designsystem.component.CoverSize
import com.rolandlin.shelfie.core.designsystem.component.LoadingState
import com.rolandlin.shelfie.core.designsystem.component.MessageState
import com.rolandlin.shelfie.core.designsystem.component.label
import com.rolandlin.shelfie.core.model.ShelfEntry
import com.rolandlin.shelfie.core.model.ShelfStatus
import com.rolandlin.shelfie.core.model.WorkId
import org.koin.compose.viewmodel.koinViewModel

/** The feature's only public entry point; navigation is passed in as callbacks. */
@Composable
fun ShelfRoute(
    onBookClick: (WorkId) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ShelfViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ShelfScreen(
        uiState = uiState,
        onFilterChange = viewModel::onFilterChange,
        onBookClick = onBookClick,
        modifier = modifier,
    )
}

@Composable
internal fun ShelfScreen(
    uiState: ShelfUiState,
    onFilterChange: (ShelfStatus?) -> Unit,
    onBookClick: (WorkId) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize()) {
        when (uiState) {
            ShelfUiState.Loading -> LoadingState()
            is ShelfUiState.Content -> {
                FilterRow(uiState.filter, onFilterChange)
                if (uiState.entries.isEmpty()) {
                    EmptyShelf(uiState.filter)
                } else {
                    LazyColumn(Modifier.fillMaxSize()) {
                        items(uiState.entries, key = { it.workId.value }) { entry ->
                            ShelfRow(entry, onClick = { onBookClick(entry.workId) })
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterRow(selected: ShelfStatus?, onFilterChange: (ShelfStatus?) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = selected == null,
            onClick = { onFilterChange(null) },
            label = { Text(stringResource(R.string.shelf_filter_all)) },
        )
        ShelfStatus.entries.forEach { status ->
            FilterChip(
                selected = selected == status,
                onClick = { onFilterChange(status) },
                label = { Text(status.label()) },
            )
        }
    }
}

@Composable
private fun EmptyShelf(filter: ShelfStatus?) {
    if (filter == null) {
        MessageState(
            title = stringResource(R.string.shelf_empty_title),
            body = stringResource(R.string.shelf_empty_body),
        )
    } else {
        MessageState(
            title = stringResource(R.string.shelf_empty_filtered_title),
            body = stringResource(R.string.shelf_empty_filtered_body, filter.label()),
        )
    }
}

@Composable
private fun ShelfRow(entry: ShelfEntry, onClick: () -> Unit) {
    // A book shelved before its details were cached has no title until a refresh fills it in
    val title = entry.title ?: stringResource(R.string.shelf_untitled)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BookCover(entry.coverId, title, Modifier.width(48.dp), size = CoverSize.Small)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (entry.authorNames.isNotEmpty()) {
                Text(
                    entry.authorNames.joinToString(),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                entry.status.label(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            // Want-to-read books are always at 0%, so the bar would carry no information
            if (entry.status != ShelfStatus.WantToRead) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LinearProgressIndicator(
                        progress = { entry.progressPercent / 100f },
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        stringResource(R.string.shelf_progress, entry.progressPercent),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}
