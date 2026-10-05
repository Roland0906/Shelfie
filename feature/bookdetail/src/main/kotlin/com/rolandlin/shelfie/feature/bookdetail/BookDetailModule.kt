package com.rolandlin.shelfie.feature.bookdetail

import com.rolandlin.shelfie.core.model.WorkId
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val bookDetailModule = module {
    // The id is passed as a String: value classes are boxed by Koin parameters anyway
    viewModel { (workId: String) -> BookDetailViewModel(WorkId(workId), get(), get()) }
}
