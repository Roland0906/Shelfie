package com.rolandlin.shelfie.core.contract

import com.rolandlin.shelfie.core.model.ShelfEntry
import com.rolandlin.shelfie.core.model.ShelfStatus
import com.rolandlin.shelfie.core.model.WorkId

fun ShelfEntry.toDto(version: Long): ShelfEntryDto = ShelfEntryDto(
    workId = workId.value,
    title = title,
    authorNames = authorNames,
    coverId = coverId,
    status = status.toDto(),
    progressPercent = progressPercent,
    note = note,
    addedAt = addedAt,
    updatedAt = updatedAt,
    version = version,
)

/** Throws [IllegalArgumentException] for a malformed work id, which the server reports as a bad request. */
fun ShelfEntryDto.toModel(): ShelfEntry = ShelfEntry(
    workId = WorkId(workId),
    title = title,
    authorNames = authorNames,
    coverId = coverId,
    status = status.toModel(),
    progressPercent = progressPercent,
    note = note,
    addedAt = addedAt,
    updatedAt = updatedAt,
)

fun ShelfStatus.toDto(): ShelfStatusDto = when (this) {
    ShelfStatus.WantToRead -> ShelfStatusDto.WantToRead
    ShelfStatus.Reading -> ShelfStatusDto.Reading
    ShelfStatus.Finished -> ShelfStatusDto.Finished
}

fun ShelfStatusDto.toModel(): ShelfStatus = when (this) {
    ShelfStatusDto.WantToRead -> ShelfStatus.WantToRead
    ShelfStatusDto.Reading -> ShelfStatus.Reading
    ShelfStatusDto.Finished -> ShelfStatus.Finished
}
