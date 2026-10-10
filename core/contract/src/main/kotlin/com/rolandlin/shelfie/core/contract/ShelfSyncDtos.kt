package com.rolandlin.shelfie.core.contract

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Wire names are fixed here so renaming a Kotlin enum constant never changes the JSON. */
@Serializable
enum class ShelfStatusDto {
    @SerialName("want_to_read") WantToRead,
    @SerialName("reading") Reading,
    @SerialName("finished") Finished,
}

/** A shelf entry as the server stores it; [version] is assigned by the server on every change. */
@Serializable
data class ShelfEntryDto(
    val workId: String,
    val title: String?,
    val authorNames: List<String>,
    val coverId: Long?,
    val status: ShelfStatusDto,
    val progressPercent: Int,
    val note: String,
    val addedAt: Long,
    val updatedAt: Long,
    val version: Long,
)

/** A deleted entry, kept so the deletion reaches other devices. */
@Serializable
data class TombstoneDto(
    val workId: String,
    val version: Long,
)

/** Response of `GET` [ApiPaths.SHELF_CHANGES]: everything changed after the `since` cursor. */
@Serializable
data class ShelfChangesResponse(
    val entries: List<ShelfEntryDto>,
    val tombstones: List<TombstoneDto>,
    val nextCursor: String,
)

/**
 * A local edit pushed to the server. [baseVersion] is the server version the edit started
 * from, or null for an entry the server has never seen.
 */
@Serializable
sealed interface ShelfChangeDto {
    val workId: String
    val baseVersion: Long?

    @Serializable
    @SerialName("upsert")
    data class Upsert(val entry: ShelfEntryDto, override val baseVersion: Long?) : ShelfChangeDto {
        override val workId: String get() = entry.workId
    }

    @Serializable
    @SerialName("delete")
    data class Delete(override val workId: String, override val baseVersion: Long) : ShelfChangeDto
}

@Serializable
data class PushShelfChangesRequest(val changes: List<ShelfChangeDto>)

@Serializable
data class AcceptedChangeDto(
    val workId: String,
    val version: Long,
)

/**
 * A change rejected because the entry moved on since [ShelfChangeDto.baseVersion].
 * [current] is the server's copy to merge with, or null if it was deleted at [currentVersion].
 */
@Serializable
data class ConflictDto(
    val workId: String,
    val current: ShelfEntryDto?,
    val currentVersion: Long,
)

@Serializable
data class PushShelfChangesResponse(
    val accepted: List<AcceptedChangeDto>,
    val conflicts: List<ConflictDto>,
)
