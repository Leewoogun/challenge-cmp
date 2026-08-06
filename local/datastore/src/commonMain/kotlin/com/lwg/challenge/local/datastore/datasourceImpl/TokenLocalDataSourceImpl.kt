package com.lwg.challenge.local.datastore.datasourceImpl

import com.lwg.challenge.data.datasource.local.TokenLocalDataSource
import com.lwg.challenge.local.datastore.datastore.TokenDataStore
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single(binds = [TokenLocalDataSource::class])
internal class TokenLocalDataSourceImpl(
    private val tokenDataStore: TokenDataStore,
) : TokenLocalDataSource {

    override fun observeAccessToken(): Flow<String> = tokenDataStore.observeAccessToken()

    override suspend fun getAccessToken(): String = tokenDataStore.getAccessToken()

    override suspend fun getRefreshToken(): String = tokenDataStore.getRefreshToken()

    override suspend fun saveAccessToken(accessToken: String) {
        tokenDataStore.saveAccessToken(accessToken)
    }

    override suspend fun updateTokens(accessToken: String, refreshToken: String) {
        tokenDataStore.updateTokens(accessToken, refreshToken)
    }

    override suspend fun clearTokens() {
        tokenDataStore.clearTokens()
    }
}
