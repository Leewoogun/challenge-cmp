package com.lwg.challenge.local.datastore.datasource

import com.lwg.challenge.local.datastore.createDataStore
import com.lwg.challenge.local.datastore.model.TokenPrefs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single
class TokenLocalDataSource {

    private val dataStore = createDataStore(
        serializer = TokenPrefs.serializer(),
        defaultValue = TokenPrefs(),
        fileName = "token",
    )

    fun observeAccessToken(): Flow<String> {
        return dataStore.data.map { it.accessToken }
    }

    suspend fun getAccessToken(): String {
        return dataStore.data.first().accessToken
    }

    suspend fun getRefreshToken(): String {
        return dataStore.data.first().refreshToken
    }

    suspend fun saveAccessToken(accessToken: String) {
        dataStore.updateData { it.copy(accessToken = accessToken) }
    }

    suspend fun updateTokens(accessToken: String, refreshToken: String) {
        dataStore.updateData {
            it.copy(accessToken = accessToken, refreshToken = refreshToken)
        }
    }

    suspend fun clearTokens() {
        dataStore.updateData { TokenPrefs() }
    }
}
