package com.rolandlin.shelfie.core.network

import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration
import kotlin.time.TimeSource

/** Grants permission to send one request, suspending until it is allowed. */
fun interface RateLimiter {
    suspend fun acquire()
}

/**
 * Keeps at least [minInterval] between consecutive requests.
 *
 * Spacing rather than a token bucket: Open Library limits requests per second, and evenly
 * spread requests are less likely to hit 429 than bursts. It is also simple to reason about.
 */
class SpacingRateLimiter(
    private val minInterval: Duration,
    private val timeSource: TimeSource.WithComparableMarks = TimeSource.Monotonic,
) : RateLimiter {
    private val mutex = Mutex()
    private var nextSlot: ComparableTimeMark? = null

    override suspend fun acquire() = mutex.withLock {
        val now = timeSource.markNow()
        val slot = nextSlot?.takeIf { it > now } ?: now
        if (slot > now) delay(slot - now)
        nextSlot = slot + minInterval
    }
}

internal class RateLimitConfig {
    var limiter: RateLimiter = RateLimiter { }
}

/**
 * Hooks into Send rather than onRequest: every HttpRequestRetry attempt passes through Send
 * again, so retries are rate limited too. Must be installed after HttpRequestRetry.
 */
internal val RateLimit = createClientPlugin("OpenLibraryRateLimit", ::RateLimitConfig) {
    val limiter = pluginConfig.limiter
    on(Send) { request ->
        limiter.acquire()
        proceed(request)
    }
}
