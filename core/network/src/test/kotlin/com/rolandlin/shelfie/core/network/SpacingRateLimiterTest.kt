package com.rolandlin.shelfie.core.network

import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds

class SpacingRateLimiterTest {

    @Test
    fun `requests are spaced by the minimum interval`() = runTest {
        val limiter = SpacingRateLimiter(500.milliseconds, testScheduler.timeSource)
        val grantedAt = mutableListOf<Long>()

        repeat(3) {
            limiter.acquire()
            grantedAt += testScheduler.currentTime
        }

        assertEquals(listOf(0L, 500L, 1000L), grantedAt)
    }

    @Test
    fun `concurrent callers are serialized`() = runTest {
        val limiter = SpacingRateLimiter(500.milliseconds, testScheduler.timeSource)
        val grantedAt = mutableListOf<Long>()

        repeat(3) {
            launch {
                limiter.acquire()
                grantedAt += testScheduler.currentTime
            }
        }
        testScheduler.advanceUntilIdle()

        assertEquals(listOf(0L, 500L, 1000L), grantedAt)
    }

    @Test
    fun `no wait after an idle period`() = runTest {
        val limiter = SpacingRateLimiter(500.milliseconds, testScheduler.timeSource)

        limiter.acquire()
        testScheduler.advanceTimeBy(2_000)
        val before = testScheduler.currentTime
        limiter.acquire()

        assertEquals(before, testScheduler.currentTime)
    }
}
