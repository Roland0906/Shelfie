package com.rolandlin.shelfie.core.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.rolandlin.shelfie.core.database.entity.SearchQueryEntity
import com.rolandlin.shelfie.core.database.entity.SearchResultEntity

@Dao
interface SearchDao {
    @Query("SELECT * FROM search_results WHERE `query` = :query ORDER BY position")
    fun pagingSource(query: String): PagingSource<Int, SearchResultEntity>

    @Query("SELECT * FROM search_queries WHERE `query` = :query")
    suspend fun getQuery(query: String): SearchQueryEntity?

    /** Snapshot source when a book is shelved before its details were ever loaded. */
    @Query("SELECT * FROM search_results WHERE workId = :workId LIMIT 1")
    suspend fun findAnyResult(workId: String): SearchResultEntity?

    @Query("SELECT MAX(position) FROM search_results WHERE `query` = :query")
    suspend fun maxPosition(query: String): Int?

    @Upsert
    suspend fun upsertQuery(query: SearchQueryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResults(results: List<SearchResultEntity>)

    @Query("DELETE FROM search_results WHERE `query` = :query")
    suspend fun deleteResults(query: String)

    /** First page: replaces the query's old results in one transaction, so the PagingSource invalidates once. */
    @Transaction
    suspend fun replaceWithFirstPage(query: SearchQueryEntity, results: List<SearchResultEntity>) {
        deleteResults(query.query)
        insertResults(results)
        upsertQuery(query)
    }

    @Transaction
    suspend fun appendPage(query: SearchQueryEntity, results: List<SearchResultEntity>) {
        insertResults(results)
        upsertQuery(query)
    }

    /** Keeps the [keep] most recently fetched queries and deletes the rest with their results. */
    @Transaction
    suspend fun trimTo(keep: Int) {
        deleteResultsOutsideRecent(keep)
        deleteQueriesOutsideRecent(keep)
    }

    @Query(
        """
        DELETE FROM search_results WHERE `query` NOT IN (
            SELECT `query` FROM search_queries ORDER BY fetchedAt DESC LIMIT :keep
        )
        """,
    )
    suspend fun deleteResultsOutsideRecent(keep: Int)

    @Query(
        """
        DELETE FROM search_queries WHERE `query` NOT IN (
            SELECT `query` FROM search_queries ORDER BY fetchedAt DESC LIMIT :keep
        )
        """,
    )
    suspend fun deleteQueriesOutsideRecent(keep: Int)
}
