package com.rolandlin.shelfie.core.network

import io.ktor.client.engine.okhttp.OkHttp
import org.koin.dsl.module
import kotlin.time.Duration.Companion.milliseconds

/** Requires an [OpenLibraryConfig] from the app, which owns the User-Agent and its version. */
val networkModule = module {
    // Identified clients may send 3 requests per second; 350ms leaves some headroom
    single<RateLimiter> { SpacingRateLimiter(minInterval = 350.milliseconds) }
    single { createOpenLibraryClient(OkHttp.create(), get(), get()) }
    single<OpenLibraryApi> { KtorOpenLibraryApi(get()) }
}
