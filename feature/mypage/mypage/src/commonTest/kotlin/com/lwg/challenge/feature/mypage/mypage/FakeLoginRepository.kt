package com.lwg.challenge.feature.mypage.mypage

import com.lwg.challenge.domain.model.AuthTokens
import com.lwg.challenge.domain.model.LoginResult
import com.lwg.challenge.domain.repository.LoginRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/** WithdrawUseCase 가 조립하는 LogoutUseCase 용 최소 Fake. */
class FakeLoginRepository : LoginRepository {

    var logoutCalled = false
        private set

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

    override suspend fun logout() {
        logoutCalled = true
    }

    override suspend fun clearTokens() {
        clearTokensCalled = true
    }
}
