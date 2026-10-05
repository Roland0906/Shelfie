package com.rolandlin.shelfie.core.data.internal

import app.cash.turbine.test
import com.rolandlin.shelfie.core.data.fake.FakeBookDao
import com.rolandlin.shelfie.core.data.fake.FakeClock
import com.rolandlin.shelfie.core.data.fake.FakeSearchDao
import com.rolandlin.shelfie.core.data.fake.FakeShelfDao
import com.rolandlin.shelfie.core.database.entity.BookEntity
import com.rolandlin.shelfie.core.database.entity.SearchResultEntity
import com.rolandlin.shelfie.core.model.ShelfStatus
import com.rolandlin.shelfie.core.model.WorkId
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class LocalShelfRepoTest {

    private val bookDao = FakeBookDao()
    private val searchDao = FakeSearchDao()
    private val clock = FakeClock()
    private val repo = LocalShelfRepo(FakeShelfDao(), bookDao, searchDao, clock)

    private val dune = WorkId("OL1W")

    @Test
    fun `status changes are reflected in the observed list`() = runTest {
        repo.observeShelf(ShelfStatus.Reading).test {
            assertEquals(emptyList(), awaitItem())

            repo.upsert(dune, ShelfStatus.Reading)
            assertEquals(listOf(dune), awaitItem().map { it.workId })

            repo.upsert(dune, ShelfStatus.Finished)
            assertEquals(emptyList(), awaitItem())
        }
    }

    @Test
    fun `progress 100 moves the entry to finished`() = runTest {
        repo.upsert(dune, ShelfStatus.Reading)

        repo.updateProgress(dune, 100)

        repo.observeEntry(dune).test {
            assertEquals(ShelfStatus.Finished, awaitItem()!!.status)
        }
    }

    @Test
    fun `new entry takes its snapshot from the cached book`() = runTest {
        bookDao.upsertBook(BookEntity("OL1W", "Dune", null, coverId = 42, emptyList(), 1965, fetchedAt = 0))

        repo.upsert(dune, ShelfStatus.WantToRead)

        repo.observeEntry(dune).test {
            val entry = awaitItem()!!
            assertEquals("Dune", entry.title)
            assertEquals(42L, entry.coverId)
        }
    }

    @Test
    fun `new entry falls back to search cache, then to an empty snapshot`() = runTest {
        searchDao.insertResults(listOf(SearchResultEntity("dune", 0, "OL1W", "Dune", listOf("Frank Herbert"), null, null)))

        repo.upsert(dune, ShelfStatus.WantToRead)
        repo.upsert(WorkId("OL2W"), ShelfStatus.WantToRead)

        repo.observeEntry(dune).test { assertEquals(listOf("Frank Herbert"), awaitItem()!!.authorNames) }
        repo.observeEntry(WorkId("OL2W")).test { assertNull(awaitItem()!!.title) }
    }

    @Test
    fun `re-adding keeps note and addedAt`() = runTest {
        repo.upsert(dune, ShelfStatus.WantToRead)
        repo.updateNote(dune, "recommended by a friend")
        clock.now += 1_000

        repo.upsert(dune, ShelfStatus.Reading)

        repo.observeEntry(dune).test {
            val entry = awaitItem()!!
            assertEquals("recommended by a friend", entry.note)
            assertEquals(1_000_000L, entry.addedAt)
        }
    }

    @Test
    fun `updating a book that is not on the shelf is a no-op`() = runTest {
        assertFalse(repo.updateProgress(dune, 50))
        assertFalse(repo.updateNote(dune, "x"))
    }
}
