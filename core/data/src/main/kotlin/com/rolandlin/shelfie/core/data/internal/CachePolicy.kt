package com.rolandlin.shelfie.core.data.internal

import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

/** Cache freshness in one place, so code and tests share the same numbers. */
internal data class CachePolicy(
    /** Book details rarely change; stale ones are refreshed in the background. */
    val bookTtl: Duration = 1.days,
    /** Rankings drift, but repeating a search within hours should not hit the API. */
    val searchTtl: Duration = 6.hours,
    /** How many queries keep their cached results. */
    val maxCachedQueries: Int = 30,
    val searchPageSize: Int = 20,
    /** Each author is one more rate-limited request, so only the first few are fetched. */
    val maxAuthorsPerBook: Int = 5,
)
