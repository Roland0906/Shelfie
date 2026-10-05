package com.rolandlin.shelfie.core.testing

import com.rolandlin.shelfie.core.data.repository.ShelfRepo
import com.rolandlin.shelfie.core.model.ShelfEntry
import com.rolandlin.shelfie.core.model.ShelfStatus
import com.rolandlin.shelfie.core.model.WorkId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** Applies the same ShelfEntry rules as the real repository, kept in memory. */
class FakeShelfRepo : ShelfRepo {
    private val entries = MutableStateFlow<Map<WorkId, ShelfEntry>>(emptyMap())

    override fun observeShelf(status: ShelfStatus?): Flow<List<ShelfEntry>> =
        entries.map { map -> map.values.filter { status == null || it.status == status } }

    override fun observeEntry(workId: WorkId): Flow<ShelfEntry?> = entries.map { it[workId] }

    override suspend fun upsert(workId: WorkId, status: ShelfStatus) = entries.update { map ->
        val entry = map[workId] ?: ShelfEntry(workId, null, emptyList(), null, ShelfStatus.WantToRead, 0, "", 0, 0)
        map + (workId to entry.withStatus(status, now = 0))
    }

    override suspend fun updateProgress(workId: WorkId, percent: Int): Boolean = update(workId) {
        it.withProgress(percent, now = 0)
    }

    override suspend fun updateNote(workId: WorkId, note: String): Boolean = update(workId) { it.copy(note = note) }

    override suspend fun remove(workId: WorkId) = entries.update { it - workId }

    private fun update(workId: WorkId, transform: (ShelfEntry) -> ShelfEntry): Boolean {
        val existing = entries.value[workId] ?: return false
        entries.update { it + (workId to transform(existing)) }
        return true
    }
}
