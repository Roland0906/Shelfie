package com.rolandlin.shelfie.feature.bookdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rolandlin.shelfie.core.designsystem.component.BookCover
import com.rolandlin.shelfie.core.designsystem.component.CoverSize
import com.rolandlin.shelfie.core.designsystem.component.LoadingState
import com.rolandlin.shelfie.core.designsystem.component.MessageState
import com.rolandlin.shelfie.core.designsystem.component.StatusBanner
import com.rolandlin.shelfie.core.designsystem.component.message
import com.rolandlin.shelfie.core.model.Book
import com.rolandlin.shelfie.core.model.ShelfEntry
import com.rolandlin.shelfie.core.model.ShelfStatus
import com.rolandlin.shelfie.core.model.WorkId
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** The feature's only public entry point; navigation is passed in as callbacks. */
@Composable
fun BookDetailRoute(
    workId: WorkId,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BookDetailViewModel = koinViewModel(key = workId.value) { parametersOf(workId.value) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BookDetailScreen(
        uiState = uiState,
        onBack = onBack,
        onRefresh = viewModel::refresh,
        onStatusSelected = viewModel::setShelfStatus,
        onRemoveFromShelf = viewModel::removeFromShelf,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BookDetailScreen(
    uiState: BookDetailUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onStatusSelected: (ShelfStatus) -> Unit,
    onRemoveFromShelf: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.detail_back))
                    }
                },
                actions = {
                    if (uiState is BookDetailUiState.Content) {
                        IconButton(onClick = onRefresh, enabled = !uiState.isRefreshing) {
                            Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.detail_refresh))
                        }
                    }
                },
            )
        },
    ) { padding ->
        val contentModifier = Modifier.padding(padding)
        when (uiState) {
            BookDetailUiState.Loading -> LoadingState(contentModifier)
            is BookDetailUiState.Error -> MessageState(
                title = uiState.error.message(),
                actionLabel = stringResource(R.string.detail_retry),
                onAction = onRefresh,
                modifier = contentModifier,
            )
            is BookDetailUiState.Content -> Column(contentModifier.fillMaxSize()) {
                if (uiState.isRefreshing) LinearProgressIndicator(Modifier.fillMaxWidth())
                uiState.refreshError?.let {
                    StatusBanner(
                        message = it.message(),
                        actionLabel = stringResource(R.string.detail_retry),
                        onAction = onRefresh,
                    )
                }
                BookContent(uiState.book, uiState.shelfEntry, onStatusSelected, onRemoveFromShelf)
            }
        }
    }
}

@Composable
private fun BookContent(
    book: Book,
    shelfEntry: ShelfEntry?,
    onStatusSelected: (ShelfStatus) -> Unit,
    onRemoveFromShelf: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            BookCover(book.coverId, book.title, Modifier.width(120.dp), size = CoverSize.Large)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(book.title, style = MaterialTheme.typography.headlineSmall)
                if (book.authors.isNotEmpty()) {
                    Text(book.authors.joinToString { it.name }, style = MaterialTheme.typography.titleMedium)
                }
                book.firstPublishYear?.let {
                    Text(it.toString(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        ShelfSection(shelfEntry, onStatusSelected, onRemoveFromShelf)

        Text(
            text = book.description ?: stringResource(R.string.detail_no_description),
            style = MaterialTheme.typography.bodyLarge,
        )
        if (book.subjects.isNotEmpty()) {
            Text(
                text = book.subjects.joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ShelfSection(
    shelfEntry: ShelfEntry?,
    onStatusSelected: (ShelfStatus) -> Unit,
    onRemoveFromShelf: () -> Unit,
) {
    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ShelfStatus.entries.forEach { status ->
                FilterChip(
                    selected = shelfEntry?.status == status,
                    onClick = { onStatusSelected(status) },
                    label = { Text(stringResource(status.labelRes)) },
                )
            }
        }
        if (shelfEntry != null) {
            TextButton(onClick = onRemoveFromShelf) { Text(stringResource(R.string.detail_remove_from_shelf)) }
        }
    }
}

private val ShelfStatus.labelRes: Int
    get() = when (this) {
        ShelfStatus.WantToRead -> R.string.shelf_status_want_to_read
        ShelfStatus.Reading -> R.string.shelf_status_reading
        ShelfStatus.Finished -> R.string.shelf_status_finished
    }
