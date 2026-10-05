package com.rolandlin.shelfie.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.rolandlin.shelfie.core.common.DataError
import com.rolandlin.shelfie.core.designsystem.R

/** One wording per error, shared by every screen. */
@Composable
fun DataError.message(): String = stringResource(
    when (this) {
        DataError.Offline -> R.string.error_offline
        DataError.NotFound -> R.string.error_not_found
        DataError.RateLimited -> R.string.error_rate_limited
        is DataError.Server -> R.string.error_server
        is DataError.Unknown -> R.string.error_unknown
    },
)
