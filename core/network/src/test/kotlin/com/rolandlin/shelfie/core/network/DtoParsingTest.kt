package com.rolandlin.shelfie.core.network

import com.rolandlin.shelfie.core.network.dto.WorkDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DtoParsingTest {

    @Test
    fun `description as plain string`() {
        val work = OpenLibraryJson.decodeFromString<WorkDto>(
            """{"key":"/works/OL1W","title":"Dune","description":"Spice."}""",
        )

        assertEquals("Spice.", work.description)
    }

    @Test
    fun `description as typed text object`() {
        val work = OpenLibraryJson.decodeFromString<WorkDto>(
            """{"key":"/works/OL1W","title":"Dune","description":{"type":"/type/text","value":"Spice."}}""",
        )

        assertEquals("Spice.", work.description)
    }

    @Test
    fun `missing optional fields and unknown keys are tolerated`() {
        val work = OpenLibraryJson.decodeFromString<WorkDto>(
            """{"key":"/works/OL1W","title":"Dune","revision":12,"latest_revision":12}""",
        )

        assertNull(work.description)
        assertEquals(emptyList(), work.authors)
    }
}
