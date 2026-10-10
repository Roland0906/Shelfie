package com.rolandlin.shelfie.core.contract

import kotlinx.serialization.Serializable

@Serializable
data class GoogleSignInRequest(val idToken: String)

@Serializable
data class TokenResponse(
    val accessToken: String,
    val expiresInSeconds: Long,
)

@Serializable
data class ErrorResponse(
    val code: String,
    val message: String,
)
