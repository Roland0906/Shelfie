package com.rolandlin.shelfie.core.data.internal

import com.rolandlin.shelfie.core.database.entity.AuthorEntity
import com.rolandlin.shelfie.core.database.entity.BookEntity
import com.rolandlin.shelfie.core.database.entity.SearchResultEntity
import com.rolandlin.shelfie.core.database.entity.ShelfEntryEntity
import com.rolandlin.shelfie.core.model.Author
import com.rolandlin.shelfie.core.model.AuthorId
import com.rolandlin.shelfie.core.model.Book
import com.rolandlin.shelfie.core.model.BookSummary
import com.rolandlin.shelfie.core.model.ShelfEntry
import com.rolandlin.shelfie.core.model.WorkId
import com.rolandlin.shelfie.core.network.dto.AuthorDto
import com.rolandlin.shelfie.core.network.dto.SearchDocDto
import com.rolandlin.shelfie.core.network.dto.WorkDto

// DTO -> Entity: the only place that knows the network format

internal fun WorkDto.toEntity(fetchedAt: Long, fallbackYear: Int?): BookEntity = BookEntity(
    workId = WorkId.fromKey(key).value,
    title = title,
    description = description?.trim()?.takeIf { it.isNotEmpty() },
    // Open Library uses -1 for "no cover"
    coverId = covers.firstOrNull { it > 0 },
    subjects = subjects.take(MAX_SUBJECTS),
    firstPublishYear = firstPublishDate?.let(::parseYear) ?: fallbackYear,
    fetchedAt = fetchedAt,
)

internal fun AuthorDto.toEntity(): AuthorEntity = AuthorEntity(
    authorId = AuthorId.fromKey(key).value,
    name = name.ifBlank { personalName.orEmpty() },
)

internal fun SearchDocDto.toEntity(query: String, position: Int): SearchResultEntity = SearchResultEntity(
    query = query,
    position = position,
    workId = WorkId.fromKey(key).value,
    title = title,
    authorNames = authorNames,
    coverId = coverId?.takeIf { it > 0 },
    firstPublishYear = firstPublishYear,
)

/** Extracts 1965 from "1965", "June 1, 1965" or "1965-06". */
internal fun parseYear(date: String): Int? = YEAR.find(date)?.value?.toIntOrNull()

private val YEAR = Regex("""\b(\d{4})\b""")
private const val MAX_SUBJECTS = 10

// Entity → Model

internal fun BookEntity.toModel(authors: List<AuthorEntity>): Book = Book(
    workId = WorkId(workId),
    title = title,
    description = description,
    authors = authors.map { Author(AuthorId(it.authorId), it.name) },
    coverId = coverId,
    subjects = subjects,
    firstPublishYear = firstPublishYear,
    fetchedAt = fetchedAt,
)

internal fun SearchResultEntity.toSummary(): BookSummary = BookSummary(
    workId = WorkId(workId),
    title = title,
    authorNames = authorNames,
    coverId = coverId,
    firstPublishYear = firstPublishYear,
)

internal fun ShelfEntryEntity.toModel(): ShelfEntry = ShelfEntry(
    workId = WorkId(workId),
    title = title,
    authorNames = authorNames,
    coverId = coverId,
    status = status,
    progressPercent = progressPercent,
    note = note,
    addedAt = addedAt,
    updatedAt = updatedAt,
)

internal fun ShelfEntry.toEntity(): ShelfEntryEntity = ShelfEntryEntity(
    workId = workId.value,
    title = title,
    authorNames = authorNames,
    coverId = coverId,
    status = status,
    progressPercent = progressPercent,
    note = note,
    addedAt = addedAt,
    updatedAt = updatedAt,
)
