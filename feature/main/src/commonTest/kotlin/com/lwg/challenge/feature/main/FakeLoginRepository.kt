package com.lwg.challenge.feature.main

import com.lwg.challenge.domain.model.AuthTokens
import com.lwg.challenge.domain.model.LoginResult
import com.lwg.challenge.domain.repository.LoginRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/** LogoutUseCase 조립용 최소 Fake. */
class FakeLoginRepository : LoginRepository {

    /** 서버 logout 이 clearTokens 보다 먼저 불렸는지 보기 위해 순서를 그대로 기록한다. */
    val callLog = mutableListOf<String>()

    val logoutCalled: Boolean get() = callLog.contains("logout")
    val clearTokensCalled: Boolean get() = callLog.contains("clearTokens")

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
        callLog += "logout"
    }

    override suspend fun clearTokens() {
        callLog += "clearTokens"
    }
}
