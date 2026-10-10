package com.rolandlin.shelfie.core.contract

import com.rolandlin.shelfie.core.model.ShelfEntry
import com.rolandlin.shelfie.core.model.ShelfStatus
import com.rolandlin.shelfie.core.model.WorkId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ShelfMappersTest {

    private val entry = ShelfEntry(
        workId = WorkId("OL1W"),
        title = null,
        authorNames = emptyList(),
        coverId = null,
        status = ShelfStatus.Reading,
        progressPercent = 40,
        note = "halfway",
        addedAt = 1,
        updatedAt = 2,
    )

    @Test
    fun `entry survives a round trip`() {
        val dto = entry.toDto(version = 7)

        assertEquals(7, dto.version)
        assertEquals(entry, dto.toModel())
    }

    @Test
    fun `every status maps both ways`() {
        ShelfStatus.entries.forEach { assertEquals(it, it.toDto().toModel()) }
    }

    @Test
    fun `malformed work id is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            entry.toDto(version = 1).copy(workId = "/works/OL1W").toModel()
        }
    }
}
