package com.lwg.challenge.remote.mapper

import com.lwg.challenge.domain.model.AuthTokens
import com.lwg.challenge.domain.model.LoginResult
import com.lwg.challenge.domain.model.UserProfile
import com.lwg.challenge.remote.model.auth.LoginResponse

fun LoginResponse.toLoginResult(): LoginResult {
    return LoginResult(
        tokens = AuthTokens(
            accessToken = data.accessToken,
            refreshToken = data.refreshToken,
        ),
        userProfile = UserProfile(
            userId = data.userId,
            isNewUser = data.isNewUser,
        ),
    )
}
