package com.rolandlin.shelfie.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchResponseDto(
    val numFound: Int = 0,
    val start: Int = 0,
    val docs: List<SearchDocDto> = emptyList(),
)

@Serializable
data class SearchDocDto(
    /** `/works/OL45804W` */
    val key: String,
    val title: String,
    @SerialName("author_name") val authorNames: List<String> = emptyList(),
    @SerialName("cover_i") val coverId: Long? = null,
    @SerialName("first_publish_year") val firstPublishYear: Int? = null,
)
