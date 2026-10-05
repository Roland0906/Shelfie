package com.rolandlin.shelfie.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.rolandlin.shelfie.core.database.entity.AuthorEntity
import com.rolandlin.shelfie.core.database.entity.BookAuthorCrossRef
import com.rolandlin.shelfie.core.database.entity.BookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Query("SELECT * FROM books WHERE workId = :workId")
    fun observeBook(workId: String): Flow<BookEntity?>

    @Query(
        """
        SELECT authors.* FROM authors
        INNER JOIN book_authors ON book_authors.authorId = authors.authorId
        WHERE book_authors.workId = :workId
        ORDER BY book_authors.position
        """,
    )
    fun observeAuthors(workId: String): Flow<List<AuthorEntity>>

    @Query("SELECT * FROM books WHERE workId = :workId")
    suspend fun getBook(workId: String): BookEntity?

    @Query(
        """
        SELECT authors.* FROM authors
        INNER JOIN book_authors ON book_authors.authorId = authors.authorId
        WHERE book_authors.workId = :workId
        ORDER BY book_authors.position
        """,
    )
    suspend fun getAuthors(workId: String): List<AuthorEntity>

    @Query("SELECT * FROM authors WHERE authorId IN (:authorIds)")
    suspend fun getAuthorsByIds(authorIds: List<String>): List<AuthorEntity>

    @Query("SELECT fetchedAt FROM books WHERE workId = :workId")
    suspend fun getFetchedAt(workId: String): Long?

    @Upsert
    suspend fun upsertBook(book: BookEntity)

    @Upsert
    suspend fun upsertAuthors(authors: List<AuthorEntity>)

    @Query("DELETE FROM book_authors WHERE workId = :workId")
    suspend fun deleteAuthorRefs(workId: String)

    @Upsert
    suspend fun upsertAuthorRefs(refs: List<BookAuthorCrossRef>)

    /** Replaces a book and its ordered authors atomically, so observers see a single complete update. */
    @Transaction
    suspend fun replaceBook(book: BookEntity, authors: List<AuthorEntity>) {
        upsertBook(book)
        upsertAuthors(authors)
        deleteAuthorRefs(book.workId)
        upsertAuthorRefs(authors.mapIndexed { index, author -> BookAuthorCrossRef(book.workId, author.authorId, index) })
    }
}
