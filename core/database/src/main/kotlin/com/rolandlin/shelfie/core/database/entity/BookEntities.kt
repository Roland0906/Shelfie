package com.rolandlin.shelfie.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey val workId: String,
    val title: String,
    val description: String?,
    val coverId: Long?,
    val subjects: List<String>,
    val firstPublishYear: Int?,
    val fetchedAt: Long,
)

@Entity(tableName = "authors")
data class AuthorEntity(
    @PrimaryKey val authorId: String,
    val name: String,
)

/** Many-to-many between books and authors; position keeps Open Library's author order (primary author first). */
@Entity(
    tableName = "book_authors",
    primaryKeys = ["workId", "authorId"],
    indices = [Index("authorId")],
)
data class BookAuthorCrossRef(
    val workId: String,
    val authorId: String,
    val position: Int,
)
