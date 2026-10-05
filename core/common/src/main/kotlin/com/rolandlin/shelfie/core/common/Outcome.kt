package com.rolandlin.shelfie.core.common

import kotlin.coroutines.cancellation.CancellationException

/**
 * Result of a one-shot repository operation such as a refresh.
 *
 * Preferred over kotlin.Result because the error type is fixed to [DataError], so callers
 * never inspect exception types, and because `runCatching` swallows CancellationException
 * (see [suspendRunCatching]).
 */
sealed interface Outcome<out T> {
    data class Success<out T>(val value: T) : Outcome<T>
    data class Failure(val error: DataError) : Outcome<Nothing>
}

sealed interface DataError {
    /** No connection. The UI shows an offline hint rather than a generic error. */
    data object Offline : DataError
    data object NotFound : DataError
    /** Rate limited by Open Library (HTTP 429) and still failing after retries. */
    data object RateLimited : DataError
    data class Server(val code: Int) : DataError
    data class Unknown(val cause: Throwable) : DataError
}

/**
 * Like `runCatching`, but rethrows [CancellationException]. Otherwise a refresh that is
 * cancelled because its ViewModel was cleared would be reported as a failure, and the
 * cancellation would stop propagating to the parent coroutine.
 */
inline fun <T> suspendRunCatching(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.failure(e)
    }
