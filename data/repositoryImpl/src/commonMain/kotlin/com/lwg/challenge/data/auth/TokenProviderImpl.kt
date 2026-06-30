package com.lwg.challenge.data.auth

import com.lwg.challenge.local.datastore.datasource.TokenLocalDataSource
import com.lwg.challenge.remote.network.auth.TokenProvider
import org.koin.core.annotation.Single

@Single(binds = [TokenProvider::class])
class TokenProviderImpl(
    private val tokenLocalDataSource: TokenLocalDataSource,
) : TokenProvider {

    override suspend fun getAccessToken(): String =
        tokenLocalDataSource.getAccessToken()

    override suspend fun getRefreshToken(): String =
        tokenLocalDataSource.getRefreshToken()

    override suspend fun updateTokens(accessToken: String, refreshToken: String) {
        tokenLocalDataSource.updateTokens(accessToken, refreshToken)
    }

    override suspend fun clearTokens() {
        tokenLocalDataSource.clearTokens()
    }
}
