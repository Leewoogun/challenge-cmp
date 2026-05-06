package com.lwg.challenge.remote.mapper

import com.lwg.challenge.domain.model.AuthTokens
import com.lwg.challenge.remote.model.auth.RefreshResponse

fun RefreshResponse.toAuthTokens(refreshToken: String): AuthTokens {
    return AuthTokens(
        accessToken = data.accessToken,
        refreshToken = refreshToken,
    )
}
