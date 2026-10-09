package com.rolandlin.shelfie

import android.app.Application
import com.rolandlin.shelfie.core.data.dataModule
import com.rolandlin.shelfie.core.network.OpenLibraryConfig
import com.rolandlin.shelfie.feature.bookdetail.bookDetailModule
import com.rolandlin.shelfie.feature.search.searchModule
import com.rolandlin.shelfie.feature.shelf.shelfModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class ShelfieApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@ShelfieApplication)
            modules(appModule, dataModule, searchModule, bookDetailModule, shelfModule)
        }
    }
}

private val appModule = module {
    // Open Library asks clients to identify themselves; the repo URL is the public contact point
    single { OpenLibraryConfig(userAgent = "Shelfie/${BuildConfig.VERSION_NAME} (+https://github.com/Roland0906/Shelfie)") }
}
