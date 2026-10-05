package com.rolandlin.shelfie.core.data.fake

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.rolandlin.shelfie.core.database.dao.BookDao
import com.rolandlin.shelfie.core.database.dao.SearchDao
import com.rolandlin.shelfie.core.database.dao.ShelfDao
import com.rolandlin.shelfie.core.database.entity.AuthorEntity
import com.rolandlin.shelfie.core.database.entity.BookAuthorCrossRef
import com.rolandlin.shelfie.core.database.entity.BookEntity
import com.rolandlin.shelfie.core.database.entity.SearchQueryEntity
import com.rolandlin.shelfie.core.database.entity.SearchResultEntity
import com.rolandlin.shelfie.core.database.entity.ShelfEntryEntity
import com.rolandlin.shelfie.core.model.ShelfStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/*
 * In-memory DAOs.  default methods are inherited from the interfaces, so the
 * fakes only implement the basic queries and writes. The SQL itself (joins, ordering,
 * trimTo's subqueries) is not covered here; that needs instrumented Room tests.
 */

class FakeBookDao : BookDao {
    val books = MutableStateFlow<Map<String, BookEntity>>(emptyMap())
    val authors = MutableStateFlow<Map<String, AuthorEntity>>(emptyMap())
    val refs = MutableStateFlow<List<BookAuthorCrossRef>>(emptyList())

    private fun authorsOf(workId: String, authorMap: Map<String, AuthorEntity>, refList: List<BookAuthorCrossRef>) =
        refList.filter { it.workId == workId }.sortedBy { it.position }.mapNotNull { authorMap[it.authorId] }

    override fun observeBook(workId: String): Flow<BookEntity?> = books.map { it[workId] }
    override fun observeAuthors(workId: String): Flow<List<AuthorEntity>> =
        refs.map { authorsOf(workId, authors.value, it) }

    override suspend fun getBook(workId: String) = books.value[workId]
    override suspend fun getAuthors(workId: String) = authorsOf(workId, authors.value, refs.value)
    override suspend fun getAuthorsByIds(authorIds: List<String>) = authorIds.mapNotNull { authors.value[it] }
    override suspend fun getFetchedAt(workId: String) = books.value[workId]?.fetchedAt
    override suspend fun upsertBook(book: BookEntity) = books.update { it + (book.workId to book) }
    override suspend fun upsertAuthors(authors: List<AuthorEntity>) =
        this.authors.update { current -> current + authors.associateBy { it.authorId } }
    override suspend fun deleteAuthorRefs(workId: String) = refs.update { list -> list.filterNot { it.workId == workId } }
    override suspend fun upsertAuthorRefs(refs: List<BookAuthorCrossRef>) = this.refs.update { it + refs }
}

class FakeSearchDao : SearchDao {
    val queries = MutableStateFlow<Map<String, SearchQueryEntity>>(emptyMap())
    val results = MutableStateFlow<List<SearchResultEntity>>(emptyList())

    fun resultsFor(query: String) = results.value.filter { it.query == query }.sortedBy { it.position }

    override fun pagingSource(query: String): PagingSource<Int, SearchResultEntity> =
        object : PagingSource<Int, SearchResultEntity>() {
            override fun getRefreshKey(state: PagingState<Int, SearchResultEntity>): Int? = null
            override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SearchResultEntity> =
                LoadResult.Page(resultsFor(query), prevKey = null, nextKey = null)
        }

    override suspend fun getQuery(query: String) = queries.value[query]
    override suspend fun findAnyResult(workId: String) = results.value.firstOrNull { it.workId == workId }
    override suspend fun maxPosition(query: String) = resultsFor(query).maxOfOrNull { it.position }
    override suspend fun upsertQuery(query: SearchQueryEntity) = queries.update { it + (query.query to query) }
    override suspend fun insertResults(results: List<SearchResultEntity>) = this.results.update { current ->
        val keys = results.map { it.query to it.position }.toSet()
        current.filterNot { (it.query to it.position) in keys } + results
    }
    override suspend fun deleteResults(query: String) = results.update { list -> list.filterNot { it.query == query } }

    private fun recent(keep: Int) = queries.value.values.sortedByDescending { it.fetchedAt }.take(keep).map { it.query }.toSet()
    override suspend fun deleteResultsOutsideRecent(keep: Int) {
        val recent = recent(keep)
        results.update { list -> list.filter { it.query in recent } }
    }
    override suspend fun deleteQueriesOutsideRecent(keep: Int) {
        val recent = recent(keep)
        queries.update { map -> map.filterKeys { it in recent } }
    }
}

class FakeShelfDao : ShelfDao {
    val entries = MutableStateFlow<Map<String, ShelfEntryEntity>>(emptyMap())

    override fun observeAll() = entries.map { map -> map.values.sortedByDescending { it.updatedAt } }
    override fun observeByStatus(status: ShelfStatus) =
        entries.map { map -> map.values.filter { it.status == status }.sortedByDescending { it.updatedAt } }
    override fun observe(workId: String) = entries.map { it[workId] }
    override suspend fun get(workId: String) = entries.value[workId]
    override suspend fun upsert(entry: ShelfEntryEntity) = entries.update { it + (entry.workId to entry) }
    override suspend fun delete(workId: String) = entries.update { it - workId }
}
