package com.rolandlin.shelfie.core.data.internal

import app.cash.turbine.test
import com.rolandlin.shelfie.core.common.DataError
import com.rolandlin.shelfie.core.common.Outcome
import com.rolandlin.shelfie.core.data.fake.FakeBookDao
import com.rolandlin.shelfie.core.data.fake.FakeClock
import com.rolandlin.shelfie.core.data.fake.FakeOpenLibraryApi
import com.rolandlin.shelfie.core.data.fake.FakeOpenLibraryApi.Companion.author
import com.rolandlin.shelfie.core.data.fake.FakeOpenLibraryApi.Companion.work
import com.rolandlin.shelfie.core.data.fake.FakeSearchDao
import com.rolandlin.shelfie.core.model.WorkId
import kotlinx.coroutines.test.runTest
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class OfflineFirstBookRepoTest {

    private val api = FakeOpenLibraryApi()
    private val bookDao = FakeBookDao()
    private val clock = FakeClock()
    private val policy = CachePolicy()
    private val repo = OfflineFirstBookRepo(api, bookDao, FakeSearchDao(), clock, policy)

    private val dune = WorkId("OL1W")

    init {
        api.works["OL1W"] = work("OL1W", "Dune", "Spice.", authorIds = listOf("A1", "A2"))
        api.authors["A1"] = author("A1", "Frank Herbert")
        api.authors["A2"] = author("A2", "Brian Herbert")
    }

    @Test
    fun `refresh writes book and ordered authors into the db`() = runTest {
        val outcome = repo.refreshBook(dune)

        assertEquals(Outcome.Success(Unit), outcome)
        repo.observeBook(dune).test {
            val book = awaitItem()!!
            assertEquals("Dune", book.title)
            assertEquals(listOf("Frank Herbert", "Brian Herbert"), book.authors.map { it.name })
        }
    }

    @Test
    fun `fresh cache skips the network`() = runTest {
        repo.refreshBook(dune)
        api.calls.clear()

        clock.now += policy.bookTtl.inWholeMilliseconds - 1
        repo.refreshBook(dune)

        assertEquals(emptyList(), api.calls)
    }

    @Test
    fun `cached book is emitted first, then the revalidated one`() = runTest {
        repo.refreshBook(dune)
        clock.now += policy.bookTtl.inWholeMilliseconds
        api.works["OL1W"] = work("OL1W", "Dune (revised)", authorIds = listOf("A1"))

        repo.observeBook(dune).test {
            assertEquals("Dune", awaitItem()!!.title)
            repo.refreshBook(dune)
            assertEquals("Dune (revised)", expectMostRecentItem()!!.title)
        }
    }

    @Test
    fun `force refresh ignores ttl`() = runTest {
        repo.refreshBook(dune)
        api.calls.clear()

        repo.refreshBook(dune, force = true)

        assertEquals("work:OL1W", api.calls.first())
    }

    @Test
    fun `failed refresh keeps cached data and reports offline`() = runTest {
        repo.refreshBook(dune)
        clock.now += policy.bookTtl.inWholeMilliseconds
        api.failWith = IOException("airplane mode")

        val outcome = repo.refreshBook(dune)

        assertEquals(Outcome.Failure(DataError.Offline), outcome)
        repo.observeBook(dune).test {
            assertEquals("Dune", awaitItem()!!.title)
        }
    }

    @Test
    fun `no cache and failed refresh leaves book absent`() = runTest {
        api.failWith = IOException("airplane mode")

        repo.refreshBook(dune)

        repo.observeBook(dune).test { assertNull(awaitItem()) }
    }

    @Test
    fun `one failing author falls back to cached name instead of failing the book`() = runTest {
        repo.refreshBook(dune)
        clock.now += policy.bookTtl.inWholeMilliseconds
        api.failingAuthorIds += "A2"

        val outcome = repo.refreshBook(dune)

        assertEquals(Outcome.Success(Unit), outcome)
        repo.observeBook(dune).test {
            assertEquals(listOf("Frank Herbert", "Brian Herbert"), awaitItem()!!.authors.map { it.name })
        }
    }

    @Test
    fun `failing author without cache is dropped`() = runTest {
        api.failingAuthorIds += "A2"

        repo.refreshBook(dune)

        repo.observeBook(dune).test {
            assertEquals(listOf("Frank Herbert"), awaitItem()!!.authors.map { it.name })
        }
    }

    @Test
    fun `parses year from free-form first publish date`() {
        assertEquals(1965, parseYear("June 1, 1965"))
        assertEquals(1965, parseYear("1965-06"))
        assertNull(parseYear("unknown"))
    }
}
