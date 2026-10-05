package com.rolandlin.shelfie.core.data.internal

import java.util.Locale

/**
 * Search cache key: trimmed, inner whitespace collapsed, lowercased. "Dune", " dune " and
 * "DUNE" share one cache entry; Open Library search is case-insensitive, so results match.
 */
internal object SearchQueryKey {
    private val WHITESPACE = Regex("""\s+""")

    fun normalize(query: String): String = query.trim().replace(WHITESPACE, " ").lowercase(Locale.ROOT)
}
