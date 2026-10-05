package com.rolandlin.shelfie.core.common

/** All TTL checks and timestamps go through this, so tests control time. */
fun interface Clock {
    fun nowMillis(): Long

    companion object {
        val System: Clock = Clock { java.lang.System.currentTimeMillis() }
    }
}
