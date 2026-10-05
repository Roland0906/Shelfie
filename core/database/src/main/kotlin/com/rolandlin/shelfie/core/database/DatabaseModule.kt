package com.rolandlin.shelfie.core.database

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder<ShelfieDatabase>(androidContext(), "shelfie.db")
            // Bundled SQLite: the same SQLite version on every device, and the driver a future iOS target would share
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }
    single { get<ShelfieDatabase>().bookDao() }
    single { get<ShelfieDatabase>().searchDao() }
    single { get<ShelfieDatabase>().shelfDao() }
}
