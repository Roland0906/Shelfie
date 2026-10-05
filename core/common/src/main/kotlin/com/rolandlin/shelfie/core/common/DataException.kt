package com.rolandlin.shelfie.core.common

/**
 * Carries a [DataError] where an API only accepts a Throwable (e.g. Paging's LoadState.Error),
 * so the UI can unwrap it without knowing about Ktor or IOException.
 */
class DataException(val error: DataError) : Exception(error.toString())

fun Throwable.asDataError(): DataError? = (this as? DataException)?.error
