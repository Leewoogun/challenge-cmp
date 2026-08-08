package com.lwg.challenge.feature.main

import com.lwg.challenge.domain.model.AuthTokens
import com.lwg.challenge.domain.model.LoginResult
import com.lwg.challenge.domain.repository.LoginRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/** LogoutUseCase 조립용 최소 Fake. */
class FakeLoginRepository : LoginRepository {

    var clearTokensCalled = false
        private set

    override fun loginWithKakao(
        kakaoAccessToken: String,
        onError: (String) -> Unit,
    ): Flow<LoginResult> = emptyFlow()

    override fun loginWithTestAccount(
        testUserNo: Int,
        onError: (String) -> Unit,
        onUnsupported: () -> Unit,
    ): Flow<LoginResult> = emptyFlow()

    override suspend fun getStoredTokens(): AuthTokens = AuthTokens.Empty

    override suspend fun clearTokens() {
        clearTokensCalled = true
    }
}
