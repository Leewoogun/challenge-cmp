package com.lwg.challenge.local.datastore.model

import kotlinx.serialization.Serializable

@Serializable
internal data class TokenPrefs(
    val accessToken: String = "",
    val refreshToken: String = "",
)
