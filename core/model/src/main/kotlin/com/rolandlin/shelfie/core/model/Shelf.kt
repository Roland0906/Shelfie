package com.rolandlin.shelfie.core.model

enum class ShelfStatus { WantToRead, Reading, Finished }

/**
 * Shelf entries live only on the device. Title, authors and cover are a snapshot taken
 * when the book is added, so the shelf renders offline. A book added by id alone
 * (never opened before) has a null title until a background refresh fills it in.
 */
data class ShelfEntry(
    val workId: WorkId,
    val title: String?,
    val authorNames: List<String>,
    val coverId: Long?,
    val status: ShelfStatus,
    val progressPercent: Int,
    val note: String,
    val addedAt: Long,
    val updatedAt: Long,
) {
    /**
     * The single place that ties progress to status:
     * - reaching 100% marks the book as finished
     * - lowering progress of a finished book moves it back to reading
     * - any progress on a want-to-read book starts reading it
     */
    fun withProgress(percent: Int, now: Long): ShelfEntry {
        val clamped = percent.coerceIn(0, 100)
        val newStatus = when {
            clamped == 100 -> ShelfStatus.Finished
            status == ShelfStatus.Finished -> ShelfStatus.Reading
            status == ShelfStatus.WantToRead && clamped > 0 -> ShelfStatus.Reading
            else -> status
        }
        return copy(progressPercent = clamped, status = newStatus, updatedAt = now)
    }

    /** Manual status changes realign progress, so a book is never "finished at 30%". */
    fun withStatus(newStatus: ShelfStatus, now: Long): ShelfEntry {
        val progress = when (newStatus) {
            ShelfStatus.Finished -> 100
            ShelfStatus.WantToRead -> 0
            ShelfStatus.Reading -> if (progressPercent == 100) 0 else progressPercent
        }
        return copy(status = newStatus, progressPercent = progress, updatedAt = now)
    }
}
