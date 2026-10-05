package com.rolandlin.shelfie.core.network

import com.rolandlin.shelfie.core.common.DataError
import io.ktor.client.plugins.ResponseException
import java.io.IOException

/** Maps network exceptions to [DataError] so Ktor types never leak past :core:data. */
fun Throwable.toDataError(): DataError = when (this) {
    is ResponseException -> when (val code = response.status.value) {
        404 -> DataError.NotFound
        429 -> DataError.RateLimited
        else -> DataError.Server(code)
    }
    // UnknownHostException, connect timeouts and HttpRequestTimeoutException are all IOExceptions
    is IOException -> DataError.Offline
    else -> DataError.Unknown(this)
}
