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
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rolandlin.shelfie.core.designsystem.component.BookCover
import com.rolandlin.shelfie.core.designsystem.component.CoverSize
import com.rolandlin.shelfie.core.designsystem.component.LoadingState
import com.rolandlin.shelfie.core.designsystem.component.MessageState
import com.rolandlin.shelfie.core.designsystem.component.StatusBanner
import com.rolandlin.shelfie.core.designsystem.component.label
import com.rolandlin.shelfie.core.designsystem.component.message
import com.rolandlin.shelfie.core.model.Book
import com.rolandlin.shelfie.core.model.ShelfEntry
import com.rolandlin.shelfie.core.model.ShelfStatus
import com.rolandlin.shelfie.core.model.WorkId
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.roundToInt

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
        onProgressChange = viewModel::setProgress,
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
    onProgressChange: (Int) -> Unit,
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
                BookContent(uiState.book, uiState.shelfEntry, onStatusSelected, onRemoveFromShelf, onProgressChange)
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
    onProgressChange: (Int) -> Unit,
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

        ShelfSection(shelfEntry, onStatusSelected, onRemoveFromShelf, onProgressChange)

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
    onProgressChange: (Int) -> Unit,
) {
    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ShelfStatus.entries.forEach { status ->
                FilterChip(
                    selected = shelfEntry?.status == status,
                    onClick = { onStatusSelected(status) },
                    label = { Text(status.label()) },
                )
            }
        }
        if (shelfEntry != null) {
            ProgressSlider(shelfEntry.progressPercent, onProgressChange)
            TextButton(onClick = onRemoveFromShelf) { Text(stringResource(R.string.detail_remove_from_shelf)) }
        }
    }
}

/**
 * Follows the finger locally and saves only on release, so a drag is one database write.
 * The saved value can differ from the dragged one: ShelfEntry may change the status too.
 */
@Composable
private fun ProgressSlider(savedPercent: Int, onProgressChange: (Int) -> Unit) {
    var dragPercent by remember { mutableStateOf<Int?>(null) }
    val shownPercent = dragPercent ?: savedPercent
    Column {
        Text(
            stringResource(R.string.detail_progress, shownPercent),
            style = MaterialTheme.typography.labelLarge,
        )
        Slider(
            value = shownPercent.toFloat(),
            onValueChange = { dragPercent = it.roundToInt() },
            onValueChangeFinished = {
                dragPercent?.let(onProgressChange)
                dragPercent = null
            },
            valueRange = 0f..100f,
            // 5% steps: fine enough for a book, coarse enough to hit 100 easily
            steps = 19,
        )
    }
}
