package com.lwg.challenge.remote.network.auth

interface TokenProvider {
    suspend fun getAccessToken(): String
    suspend fun getRefreshToken(): String
    suspend fun updateTokens(accessToken: String, refreshToken: String)
    suspend fun clearTokens()
}
