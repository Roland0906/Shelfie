package com.rolandlin.shelfie.feature.search

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val searchModule = module {
    viewModelOf(::SearchViewModel)
}
