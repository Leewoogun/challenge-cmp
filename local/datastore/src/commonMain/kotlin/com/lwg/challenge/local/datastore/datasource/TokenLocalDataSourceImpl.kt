package com.lwg.challenge.local.datastore.datasource

import com.lwg.challenge.local.datastore.createDataStore
import com.lwg.challenge.local.datastore.model.TokenPrefs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single(binds = [TokenLocalDataSource::class])
class TokenLocalDataSourceImpl : TokenLocalDataSource {

    private val dataStore = createDataStore(
        serializer = TokenPrefs.serializer(),
        defaultValue = TokenPrefs(),
        fileName = "token",
    )

    override fun observeAccessToken(): Flow<String> {
        return dataStore.data.map { it.accessToken }
    }

    override suspend fun getAccessToken(): String {
        return dataStore.data.first().accessToken
    }

    override suspend fun getRefreshToken(): String {
        return dataStore.data.first().refreshToken
    }

    override suspend fun saveAccessToken(accessToken: String) {
        dataStore.updateData { it.copy(accessToken = accessToken) }
    }

    override suspend fun updateTokens(accessToken: String, refreshToken: String) {
        dataStore.updateData {
            it.copy(accessToken = accessToken, refreshToken = refreshToken)
        }
    }

    override suspend fun clearTokens() {
        dataStore.updateData { TokenPrefs() }
    }
}
