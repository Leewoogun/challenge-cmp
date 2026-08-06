package com.lwg.challenge.data.datasource.local

import kotlinx.coroutines.flow.Flow

interface TokenLocalDataSource {

    fun observeAccessToken(): Flow<String>

    suspend fun getAccessToken(): String

    suspend fun getRefreshToken(): String

    suspend fun saveAccessToken(accessToken: String)

    suspend fun updateTokens(accessToken: String, refreshToken: String)

    suspend fun clearTokens()
}
