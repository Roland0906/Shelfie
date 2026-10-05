package com.rolandlin.shelfie.core.data.fake

import com.rolandlin.shelfie.core.network.OpenLibraryApi
import com.rolandlin.shelfie.core.network.dto.AuthorDto
import com.rolandlin.shelfie.core.network.dto.KeyRefDto
import com.rolandlin.shelfie.core.network.dto.SearchDocDto
import com.rolandlin.shelfie.core.network.dto.SearchResponseDto
import com.rolandlin.shelfie.core.network.dto.WorkAuthorDto
import com.rolandlin.shelfie.core.network.dto.WorkDto
import java.io.IOException

/**
 * Scriptable fake API: preload fixtures, or set [failWith] to simulate going offline.
 * Records every call so tests can assert that no request was made.
 */
class FakeOpenLibraryApi : OpenLibraryApi {
    val works = mutableMapOf<String, WorkDto>()
    val authors = mutableMapOf<String, AuthorDto>()
    /** query -> all results; sliced by page/limit like the real API. */
    val searchResults = mutableMapOf<String, List<SearchDocDto>>()

    var failWith: Throwable? = null
    val failingAuthorIds = mutableSetOf<String>()

    val calls = mutableListOf<String>()

    override suspend fun search(query: String, page: Int, limit: Int): SearchResponseDto {
        calls += "search:$query:$page"
        failWith?.let { throw it }
        val all = searchResults[query].orEmpty()
        val start = (page - 1) * limit
        return SearchResponseDto(numFound = all.size, start = start, docs = all.drop(start).take(limit))
    }

    override suspend fun getWork(workId: String): WorkDto {
        calls += "work:$workId"
        failWith?.let { throw it }
        return works[workId] ?: throw IOException("no fixture for $workId")
    }

    override suspend fun getAuthor(authorId: String): AuthorDto {
        calls += "author:$authorId"
        failWith?.let { throw it }
        if (authorId in failingAuthorIds) throw IOException("author $authorId down")
        return authors[authorId] ?: throw IOException("no fixture for $authorId")
    }

    companion object {
        fun work(id: String, title: String, description: String? = null, authorIds: List<String> = emptyList()) = WorkDto(
            key = "/works/$id",
            title = title,
            description = description,
            authors = authorIds.map { WorkAuthorDto(KeyRefDto("/authors/$it")) },
        )

        fun author(id: String, name: String) = AuthorDto(key = "/authors/$id", name = name)

        fun doc(id: String, title: String) = SearchDocDto(key = "/works/$id", title = title, authorNames = listOf("Author of $title"))
    }
}

class FakeClock(var now: Long = 1_000_000L) : com.rolandlin.shelfie.core.common.Clock {
    override fun nowMillis(): Long = now
}
