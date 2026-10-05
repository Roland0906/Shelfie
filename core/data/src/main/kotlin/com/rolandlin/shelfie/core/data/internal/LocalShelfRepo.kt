package com.rolandlin.shelfie.core.data.internal

import com.rolandlin.shelfie.core.common.Clock
import com.rolandlin.shelfie.core.data.repository.ShelfRepo
import com.rolandlin.shelfie.core.database.dao.BookDao
import com.rolandlin.shelfie.core.database.dao.SearchDao
import com.rolandlin.shelfie.core.database.dao.ShelfDao
import com.rolandlin.shelfie.core.model.ShelfEntry
import com.rolandlin.shelfie.core.model.ShelfStatus
import com.rolandlin.shelfie.core.model.WorkId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Takes no API client: working offline is guaranteed by construction, not by try/catch.
 */
internal class LocalShelfRepo(
    private val shelfDao: ShelfDao,
    private val bookDao: BookDao,
    private val searchDao: SearchDao,
    private val clock: Clock,
) : ShelfRepo {

    /** Read-modify-write must be atomic so two quick taps on a status button cannot overwrite each other. */
    private val writeLock = Mutex()

    override fun observeShelf(status: ShelfStatus?): Flow<List<ShelfEntry>> =
        (if (status == null) shelfDao.observeAll() else shelfDao.observeByStatus(status))
            .map { entries -> entries.map { it.toModel() } }
            .distinctUntilChanged()

    override fun observeEntry(workId: WorkId): Flow<ShelfEntry?> =
        shelfDao.observe(workId.value)
            .map { it?.toModel() }
            .distinctUntilChanged()

    override suspend fun upsert(workId: WorkId, status: ShelfStatus) = writeLock.withLock {
        val now = clock.nowMillis()
        val existing = shelfDao.get(workId.value)?.toModel()
        val updated = existing?.withStatus(status, now) ?: newEntry(workId, now).withStatus(status, now)
        shelfDao.upsert(updated.toEntity())
    }

    override suspend fun updateProgress(workId: WorkId, percent: Int): Boolean = update(workId) {
        it.withProgress(percent, clock.nowMillis())
    }

    override suspend fun updateNote(workId: WorkId, note: String): Boolean = update(workId) {
        it.copy(note = note, updatedAt = clock.nowMillis())
    }

    override suspend fun remove(workId: WorkId) = writeLock.withLock {
        shelfDao.delete(workId.value)
    }

    private suspend fun update(workId: WorkId, transform: (ShelfEntry) -> ShelfEntry): Boolean = writeLock.withLock {
        val existing = shelfDao.get(workId.value)?.toModel() ?: return@withLock false
        shelfDao.upsert(transform(existing).toEntity())
        true
    }

    /** Snapshot sources in order: cached details, cached search result, then bare workId (filled in later). */
    private suspend fun newEntry(workId: WorkId, now: Long): ShelfEntry {
        val book = bookDao.getBook(workId.value)
        val searchHit = if (book == null) searchDao.findAnyResult(workId.value) else null
        return ShelfEntry(
            workId = workId,
            title = book?.title ?: searchHit?.title,
            authorNames = book?.let { bookDao.getAuthors(workId.value).map { it.name } } ?: searchHit?.authorNames.orEmpty(),
            coverId = book?.coverId ?: searchHit?.coverId,
            status = ShelfStatus.WantToRead,
            progressPercent = 0,
            note = "",
            addedAt = now,
            updatedAt = now,
        )
    }
}
