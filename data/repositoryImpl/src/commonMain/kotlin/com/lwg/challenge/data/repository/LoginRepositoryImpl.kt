package com.lwg.challenge.data.repository

import com.lwg.challenge.domain.model.AuthTokens
import com.lwg.challenge.domain.model.LoginResult
import com.lwg.challenge.domain.repository.LoginRepository
import com.lwg.challenge.local.datastore.datasource.TokenLocalDataSource
import com.lwg.challenge.remote.api.LoginApi
import com.lwg.challenge.remote.mapper.toLoginResult
import com.lwg.challenge.remote.model.auth.KakaoLoginRequest
import com.lwg.challenge.remote.network.util.suspendOnFailureWithErrorHandling
import com.lwg.challenge.remote.network.util.suspendOnSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Single

@Single(binds = [LoginRepository::class])
class LoginRepositoryImpl(
    private val loginApi: LoginApi,
    private val tokenLocalDataSource: TokenLocalDataSource,
) : LoginRepository {

    override fun loginWithKakao(
        kakaoAccessToken: String,
        onError: (String) -> Unit,
    ): Flow<LoginResult> = flow {
        loginApi.kakaoLogin(KakaoLoginRequest(kakaoAccessToken))
            .suspendOnSuccess {
                val result = response.toLoginResult()
                tokenLocalDataSource.updateTokens(
                    accessToken = result.tokens.accessToken,
                    refreshToken = result.tokens.refreshToken,
                )
                emit(result)
            }
            .suspendOnFailureWithErrorHandling(onError)
    }

    override suspend fun getStoredTokens(): AuthTokens {
        val access = tokenLocalDataSource.getAccessToken()
        val refresh = tokenLocalDataSource.getRefreshToken()
        return if (access.isBlank() && refresh.isBlank()) {
            AuthTokens.Empty
        } else {
            AuthTokens(accessToken = access, refreshToken = refresh)
        }
    }

    override suspend fun clearTokens() {
        tokenLocalDataSource.clearTokens()
    }
}
