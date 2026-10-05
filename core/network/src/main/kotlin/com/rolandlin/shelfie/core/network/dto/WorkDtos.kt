package com.rolandlin.shelfie.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WorkDto(
    val key: String,
    val title: String,
    @Serializable(with = TextValueSerializer::class)
    val description: String? = null,
    val covers: List<Long> = emptyList(),
    val authors: List<WorkAuthorDto> = emptyList(),
    val subjects: List<String> = emptyList(),
    @SerialName("first_publish_date") val firstPublishDate: String? = null,
)

@Serializable
data class WorkAuthorDto(
    val author: KeyRefDto? = null,
)

@Serializable
data class KeyRefDto(val key: String)

@Serializable
data class AuthorDto(
    val key: String,
    val name: String = "",
    @SerialName("personal_name") val personalName: String? = null,
)
