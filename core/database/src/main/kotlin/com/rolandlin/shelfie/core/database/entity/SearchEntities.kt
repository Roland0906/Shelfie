package com.rolandlin.shelfie.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Cached search results keyed by normalized query + position. A book that appears under
 * several queries is stored once per query, which keeps each query's order and paging
 * independent and lets one query be evicted without touching the others.
 */
@Entity(
    tableName = "search_results",
    primaryKeys = ["query", "position"],
)
data class SearchResultEntity(
    val query: String,
    val position: Int,
    val workId: String,
    val title: String,
    val authorNames: List<String>,
    val coverId: Long?,
    val firstPublishYear: Int?,
)

/** Paging progress and freshness per query (the RemoteMediator's remote key). */
@Entity(tableName = "search_queries")
data class SearchQueryEntity(
    @PrimaryKey val query: String,
    /** Next page to load; null once the end has been reached. */
    val nextPage: Int?,
    /** When the first page was fetched; decides whether the whole query is stale. */
    val fetchedAt: Long,
)
