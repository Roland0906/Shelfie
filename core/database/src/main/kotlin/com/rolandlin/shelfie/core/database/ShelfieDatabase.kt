package com.rolandlin.shelfie.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rolandlin.shelfie.core.database.dao.BookDao
import com.rolandlin.shelfie.core.database.dao.SearchDao
import com.rolandlin.shelfie.core.database.dao.ShelfDao
import com.rolandlin.shelfie.core.database.entity.AuthorEntity
import com.rolandlin.shelfie.core.database.entity.BookAuthorCrossRef
import com.rolandlin.shelfie.core.database.entity.BookEntity
import com.rolandlin.shelfie.core.database.entity.SearchQueryEntity
import com.rolandlin.shelfie.core.database.entity.SearchResultEntity
import com.rolandlin.shelfie.core.database.entity.ShelfEntryEntity

@Database(
    entities = [
        BookEntity::class,
        AuthorEntity::class,
        BookAuthorCrossRef::class,
        SearchResultEntity::class,
        SearchQueryEntity::class,
        ShelfEntryEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class ShelfieDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun searchDao(): SearchDao
    abstract fun shelfDao(): ShelfDao
}
