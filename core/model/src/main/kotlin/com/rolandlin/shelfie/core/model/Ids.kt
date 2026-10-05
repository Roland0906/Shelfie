package com.rolandlin.shelfie.core.model

/** An Open Library work key without its path prefix, e.g. `OL45804W`. */
@JvmInline
value class WorkId(val value: String) {
    init {
        require(value.isNotBlank() && '/' !in value) { "WorkId must not contain a path: $value" }
    }

    override fun toString(): String = value

    companion object {
        /** Accepts both `/works/OL45804W` and `OL45804W`. */
        fun fromKey(key: String): WorkId = WorkId(key.substringAfterLast('/'))
    }
}

/** An Open Library author key without its path prefix, e.g. `OL23919A`. */
@JvmInline
value class AuthorId(val value: String) {
    init {
        require(value.isNotBlank() && '/' !in value) { "AuthorId must not contain a path: $value" }
    }

    override fun toString(): String = value

    companion object {
        fun fromKey(key: String): AuthorId = AuthorId(key.substringAfterLast('/'))
    }
}
