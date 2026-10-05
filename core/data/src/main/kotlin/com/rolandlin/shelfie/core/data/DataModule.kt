package com.rolandlin.shelfie.core.data

import com.rolandlin.shelfie.core.common.Clock
import com.rolandlin.shelfie.core.data.internal.CachePolicy
import com.rolandlin.shelfie.core.data.internal.LocalShelfRepo
import com.rolandlin.shelfie.core.data.internal.OfflineFirstBookRepo
import com.rolandlin.shelfie.core.data.repository.BookRepo
import com.rolandlin.shelfie.core.data.repository.ShelfRepo
import com.rolandlin.shelfie.core.database.databaseModule
import com.rolandlin.shelfie.core.network.networkModule
import org.koin.dsl.module

/**
 * The only module the app needs to load, plus an OpenLibraryConfig.
 * DAOs, the HTTP client and other implementation details stay inside.
 */
val dataModule = module {
    includes(networkModule, databaseModule)

    single { Clock.System }
    single { CachePolicy() }

    single<BookRepo> { OfflineFirstBookRepo(get(), get(), get(), get(), get()) }
    single<ShelfRepo> { LocalShelfRepo(get(), get(), get(), get()) }
}
