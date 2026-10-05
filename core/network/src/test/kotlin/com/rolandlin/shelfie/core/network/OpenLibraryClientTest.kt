package com.rolandlin.shelfie.core.network

import com.rolandlin.shelfie.core.common.DataError
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class OpenLibraryClientTest {

    private val requests = mutableListOf<HttpRequestData>()

    private fun api(vararg responses: MockRequestHandleScope.() -> io.ktor.client.request.HttpResponseData): OpenLibraryApi {
        var index = 0
        val engine = MockEngine { request ->
            requests += request
            responses[index++.coerceAtMost(responses.lastIndex)]()
        }
        val client = createOpenLibraryClient(
            engine = engine,
            config = OpenLibraryConfig(userAgent = "Shelfie/test (test@example.com)", maxRetries = 2),
            rateLimiter = RateLimiter { },
        )
        return KtorOpenLibraryApi(client)
    }

    private val workJson: MockRequestHandleScope.() -> io.ktor.client.request.HttpResponseData = {
        respond(
            content = """{"key":"/works/OL1W","title":"Dune"}""",
            headers = headersOf(HttpHeaders.ContentType, "application/json"),
        )
    }

    @Test
    fun `every request carries the identifying user agent`() = runTest {
        api(workJson).getWork("OL1W")

        assertEquals("Shelfie/test (test@example.com)", requests.single().headers[HttpHeaders.UserAgent])
        assertEquals("/works/OL1W.json", requests.single().url.encodedPath)
    }

    @Test
    fun `429 is retried and then succeeds`() = runTest {
        val work = api({ respondError(HttpStatusCode.TooManyRequests) }, workJson).getWork("OL1W")

        assertEquals("Dune", work.title)
        assertEquals(2, requests.size)
    }

    @Test
    fun `404 is not retried and maps to NotFound`() = runTest {
        try {
            api({ respondError(HttpStatusCode.NotFound) }).getWork("OL1W")
            fail("expected exception")
        } catch (e: Exception) {
            assertEquals(DataError.NotFound, e.toDataError())
        }
        assertEquals(1, requests.size)
    }

    @Test
    fun `persistent 429 maps to RateLimited after retries are exhausted`() = runTest {
        try {
            api({ respondError(HttpStatusCode.TooManyRequests) }).getWork("OL1W")
            fail("expected exception")
        } catch (e: Exception) {
            assertEquals(DataError.RateLimited, e.toDataError())
        }
        assertTrue(requests.size == 3, "1 request + 2 retries, got ${requests.size}")
    }
}
