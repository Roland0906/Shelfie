package com.rolandlin.shelfie.core.contract

/** Versioned so a breaking change can live next to the old routes while old apps are still in use. */
object ApiPaths {
    const val GOOGLE_SIGN_IN = "/v1/auth/google"
    const val SHELF_CHANGES = "/v1/shelf/changes"

    /** Query parameter of `GET` [SHELF_CHANGES]; omitted on the first pull. */
    const val SINCE = "since"
}
