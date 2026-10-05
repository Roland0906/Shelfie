package com.rolandlin.shelfie.core.common

import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SuspendRunCatchingTest {

    @Test
    fun `regular exceptions become failures`() {
        val result = suspendRunCatching { error("boom") }

        assertTrue(result.isFailure)
    }

    @Test
    fun `cancellation is rethrown instead of swallowed`() {
        assertFailsWith<CancellationException> {
            suspendRunCatching { throw CancellationException("scope cancelled") }
        }
    }
}
