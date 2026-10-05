package com.rolandlin.shelfie.core.model

import kotlin.test.Test
import kotlin.test.assertEquals

class ShelfEntryTest {

    private val entry = ShelfEntry(
        workId = WorkId("OL1W"),
        title = "Dune",
        authorNames = listOf("Frank Herbert"),
        coverId = null,
        status = ShelfStatus.Reading,
        progressPercent = 40,
        note = "",
        addedAt = 0,
        updatedAt = 0,
    )

    @Test
    fun `reaching 100 percent marks the book as finished`() {
        val updated = entry.withProgress(100, now = 10)

        assertEquals(ShelfStatus.Finished, updated.status)
        assertEquals(100, updated.progressPercent)
        assertEquals(10, updated.updatedAt)
    }

    @Test
    fun `progress is clamped to 0-100`() {
        assertEquals(100, entry.withProgress(150, now = 0).progressPercent)
        assertEquals(0, entry.withProgress(-5, now = 0).progressPercent)
    }

    @Test
    fun `lowering progress of a finished book moves it back to reading`() {
        val finished = entry.withProgress(100, now = 0)

        assertEquals(ShelfStatus.Reading, finished.withProgress(80, now = 0).status)
    }

    @Test
    fun `want-to-read starts reading once progress is above zero`() {
        val wantToRead = entry.copy(status = ShelfStatus.WantToRead, progressPercent = 0)

        assertEquals(ShelfStatus.WantToRead, wantToRead.withProgress(0, now = 0).status)
        assertEquals(ShelfStatus.Reading, wantToRead.withProgress(5, now = 0).status)
    }

    @Test
    fun `manual status change keeps progress consistent`() {
        assertEquals(100, entry.withStatus(ShelfStatus.Finished, now = 0).progressPercent)
        assertEquals(0, entry.withStatus(ShelfStatus.WantToRead, now = 0).progressPercent)
        assertEquals(40, entry.withStatus(ShelfStatus.Reading, now = 0).progressPercent)
    }
}
