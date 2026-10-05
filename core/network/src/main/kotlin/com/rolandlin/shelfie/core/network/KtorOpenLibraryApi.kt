package com.rolandlin.shelfie.core.network

import com.rolandlin.shelfie.core.network.dto.AuthorDto
import com.rolandlin.shelfie.core.network.dto.SearchResponseDto
import com.rolandlin.shelfie.core.network.dto.WorkDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

internal class KtorOpenLibraryApi(
    private val client: HttpClient,
) : OpenLibraryApi {

    override suspend fun search(query: String, page: Int, limit: Int): SearchResponseDto =
        client.get("search.json") {
            parameter("q", query)
            parameter("page", page)
            parameter("limit", limit)
            // Only the fields a list row needs: shrinks the response from hundreds of KB to a few
            parameter("fields", SEARCH_FIELDS)
        }.body()

    override suspend fun getWork(workId: String): WorkDto =
        client.get("works/$workId.json").body()

    override suspend fun getAuthor(authorId: String): AuthorDto =
        client.get("authors/$authorId.json").body()

    private companion object {
        const val SEARCH_FIELDS = "key,title,author_name,cover_i,first_publish_year"
    }
}
