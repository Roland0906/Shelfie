package com.rolandlin.shelfie.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil3.compose.SubcomposeAsyncImage

enum class CoverSize(internal val suffix: String) { Small("S"), Medium("M"), Large("L") }

/** Open Library cover; shows the title's initial when there is no cover or it fails to load. */
@Composable
fun BookCover(
    coverId: Long?,
    title: String?,
    modifier: Modifier = Modifier,
    size: CoverSize = CoverSize.Medium,
) {
    val shape = RoundedCornerShape(4)
    val coverModifier = modifier.aspectRatio(2f / 3f).clip(shape)
    if (coverId == null) {
        CoverPlaceholder(title, coverModifier)
        return
    }
    SubcomposeAsyncImage(
        model = "https://covers.openlibrary.org/b/id/$coverId-${size.suffix}.jpg",
        contentDescription = title,
        contentScale = ContentScale.Crop,
        modifier = coverModifier,
        loading = { CoverPlaceholder(title, Modifier) },
        error = { CoverPlaceholder(title, Modifier) },
    )
}

@Composable
private fun CoverPlaceholder(title: String?, modifier: Modifier) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title?.firstOrNull()?.uppercase() ?: "?",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}
