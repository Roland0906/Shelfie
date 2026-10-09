package com.rolandlin.shelfie.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.rolandlin.shelfie.core.designsystem.R
import com.rolandlin.shelfie.core.model.ShelfStatus

/** One wording per status, shared by the detail screen and the shelf. */
@Composable
fun ShelfStatus.label(): String = stringResource(
    when (this) {
        ShelfStatus.WantToRead -> R.string.shelf_status_want_to_read
        ShelfStatus.Reading -> R.string.shelf_status_reading
        ShelfStatus.Finished -> R.string.shelf_status_finished
    },
)
