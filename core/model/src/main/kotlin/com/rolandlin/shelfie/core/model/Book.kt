package com.rolandlin.shelfie.core.model

data class Author(
    val id: AuthorId,
    val name: String,
)

data class Book(
    val workId: WorkId,
    val title: String,
    val description: String?,
    val authors: List<Author>,
    val coverId: Long?,
    val subjects: List<String>,
    val firstPublishYear: Int?,
    /** Last successful fetch from Open Library (epoch millis); drives cache expiry. */
    val fetchedAt: Long,
)

/** Lightweight version for lists (search, discover). */
data class BookSummary(
    val workId: WorkId,
    val title: String,
    val authorNames: List<String>,
    val coverId: Long?,
    val firstPublishYear: Int?,
)
