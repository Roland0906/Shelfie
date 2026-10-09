package com.rolandlin.shelfie.feature.shelf

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val shelfModule = module {
    viewModelOf(::ShelfViewModel)
}
