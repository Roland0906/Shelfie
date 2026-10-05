package com.rolandlin.shelfie.core.network

import com.rolandlin.shelfie.core.network.dto.AuthorDto
import com.rolandlin.shelfie.core.network.dto.SearchResponseDto
import com.rolandlin.shelfie.core.network.dto.WorkDto

/**
 * Access to Open Library. :core:data depends on this interface and tests replace it with a fake.
 * Failures are thrown as exceptions; [toDataError] classifies them.
 */
interface OpenLibraryApi {
    /** [page] is 1-based. */
    suspend fun search(query: String, page: Int, limit: Int): SearchResponseDto
    suspend fun getWork(workId: String): WorkDto
    suspend fun getAuthor(authorId: String): AuthorDto
}
