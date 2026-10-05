package com.rolandlin.shelfie.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.io.IOException

data class OpenLibraryConfig(
    val baseUrl: String = "https://openlibrary.org/",
    /** Open Library asks for an identifying User-Agent (app name + contact); identified clients get a higher rate limit. */
    val userAgent: String,
    val maxRetries: Int = 3,
)

internal val OpenLibraryJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    coerceInputValues = true
}

internal fun createOpenLibraryClient(
    engine: HttpClientEngine,
    config: OpenLibraryConfig,
    rateLimiter: RateLimiter,
): HttpClient = HttpClient(engine) {
    // Any non-2xx throws ResponseException, which toDataError() classifies
    expectSuccess = true

    install(ContentNegotiation) { json(OpenLibraryJson) }
    install(HttpTimeout) {
        connectTimeoutMillis = 10_000
        requestTimeoutMillis = 20_000
    }
    // Retry only what may succeed later: 429, 5xx and I/O errors. Retrying other 4xx is pointless.
    install(HttpRequestRetry) {
        maxRetries = config.maxRetries
        retryIf { _, response -> response.status.value == 429 || response.status.value in 500..599 }
        retryOnExceptionIf { _, cause -> cause is IOException }
        exponentialDelay(respectRetryAfterHeader = true)
    }
    // Installed after HttpRequestRetry so retries are rate limited too
    install(RateLimit) { limiter = rateLimiter }

    defaultRequest {
        url(config.baseUrl)
        header(HttpHeaders.UserAgent, config.userAgent)
    }
}
