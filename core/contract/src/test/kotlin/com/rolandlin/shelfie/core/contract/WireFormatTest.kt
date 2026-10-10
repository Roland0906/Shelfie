package com.rolandlin.shelfie.core.contract

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Pins the JSON that installed apps already depend on. If one of these fails,
 * the change breaks old clients and needs a new route version instead.
 */
class WireFormatTest {

    private val entry = ShelfEntryDto(
        workId = "OL1W",
        title = "Dune",
        authorNames = listOf("Frank Herbert"),
        coverId = 42,
        status = ShelfStatusDto.WantToRead,
        progressPercent = 0,
        note = "",
        addedAt = 1,
        updatedAt = 2,
        version = 3,
    )

    private val entryJson =
        """{"workId":"OL1W","title":"Dune","authorNames":["Frank Herbert"],"coverId":42,""" +
            """"status":"want_to_read","progressPercent":0,"note":"","addedAt":1,"updatedAt":2,"version":3}"""

    @Test
    fun `shelf entry`() {
        assertEquals(entryJson, ContractJson.encodeToString(entry))
        assertEquals(entry, ContractJson.decodeFromString<ShelfEntryDto>(entryJson))
    }

    @Test
    fun `push request tags each change with its type`() {
        val request = PushShelfChangesRequest(
            changes = listOf(
                ShelfChangeDto.Upsert(entry, baseVersion = null),
                ShelfChangeDto.Delete(workId = "OL2W", baseVersion = 5),
            ),
        )
        val json =
            """{"changes":[{"type":"upsert","entry":$entryJson,"baseVersion":null},""" +
                """{"type":"delete","workId":"OL2W","baseVersion":5}]}"""

        assertEquals(json, ContractJson.encodeToString(request))
        assertEquals(request, ContractJson.decodeFromString<PushShelfChangesRequest>(json))
    }

    @Test
    fun `conflict with a deleted server copy`() {
        val response = PushShelfChangesResponse(
            accepted = listOf(AcceptedChangeDto(workId = "OL1W", version = 4)),
            conflicts = listOf(ConflictDto(workId = "OL2W", current = null, currentVersion = 6)),
        )
        val json =
            """{"accepted":[{"workId":"OL1W","version":4}],""" +
                """"conflicts":[{"workId":"OL2W","current":null,"currentVersion":6}]}"""

        assertEquals(json, ContractJson.encodeToString(response))
        assertEquals(response, ContractJson.decodeFromString<PushShelfChangesResponse>(json))
    }

    @Test
    fun `fields added by a newer server are ignored`() {
        val json = """{"entries":[],"tombstones":[],"nextCursor":"c1","serverTime":99}"""

        assertEquals(
            ShelfChangesResponse(entries = emptyList(), tombstones = emptyList(), nextCursor = "c1"),
            ContractJson.decodeFromString<ShelfChangesResponse>(json),
        )
    }
}
