package com.lwg.challenge.data.repository

import com.lwg.challenge.data.datasource.local.TokenLocalDataSource
import com.lwg.challenge.data.datasource.remote.LoginRemoteDataSource
import com.lwg.challenge.domain.model.AuthTokens
import com.lwg.challenge.domain.model.LoginResult
import com.lwg.challenge.remote.network.auth.AuthTokenCacheInvalidator
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Api 응답 해석(성공 매핑 / 404·401 미지원 판별)은 `LoginRemoteDataSourceImplTest` 가 검증한다.
 * 여기서는 Repository 의 책임인 **세션 영속화**만 본다.
 */
class LoginRepositoryImplTest {

    private val loginResult = LoginResult(
        tokens = AuthTokens(accessToken = "access-1", refreshToken = "refresh-1"),
        userId = 101L,
        isNewUser = false,
    )

    private fun repository(remote: LoginRemoteDataSource, local: TokenLocalDataSource) = LoginRepositoryImpl(
        loginRemoteDataSource = remote,
        tokenLocalDataSource = local,
        // Auth 플러그인이 없는 클라이언트라 invalidate() 는 no-op — 실물 생성이 안전하다
        authTokenCacheInvalidator = AuthTokenCacheInvalidator(HttpClient(MockEngine { respondOk() })),
    )

    @Test
    fun `loginWithKakao 성공 - 토큰 저장 후 emit`() = runTest {
        val local = FakeTokenLocalDataSource()
        val repository = repository(FakeLoginRemoteDataSource(kakaoResult = flowOf(loginResult)), local)

        val emitted = repository.loginWithKakao(kakaoAccessToken = "kakao-token", onError = {}).toList()

        assertEquals(listOf(loginResult), emitted)
        assertEquals("access-1", local.accessToken)
        assertEquals("refresh-1", local.refreshToken)
    }

    @Test
    fun `loginWithTestAccount 성공 - 카카오와 동일한 후처리로 토큰 저장`() = runTest {
        val local = FakeTokenLocalDataSource()
        val repository = repository(FakeLoginRemoteDataSource(testResult = flowOf(loginResult)), local)

        val emitted = repository.loginWithTestAccount(
            testUserNo = 2,
            onError = {},
            onUnsupported = {},
        ).toList()

        assertEquals(listOf(loginResult), emitted)
        assertEquals(1, local.updateTokensCallCount)
        assertEquals("access-1", local.accessToken)
        assertEquals("refresh-1", local.refreshToken)
    }

    @Test
    fun `로그인 실패로 emit 이 없으면 토큰을 건드리지 않는다`() = runTest {
        val local = FakeTokenLocalDataSource()
        val repository = repository(FakeLoginRemoteDataSource(), local)

        val emitted = repository.loginWithTestAccount(
            testUserNo = 1,
            onError = {},
            onUnsupported = {},
        ).toList()

        assertEquals(emptyList(), emitted)
        assertEquals(0, local.updateTokensCallCount)
    }

    @Test
    fun `getStoredTokens - 저장된 값이 없으면 Empty`() = runTest {
        val repository = repository(FakeLoginRemoteDataSource(), FakeTokenLocalDataSource())

        assertEquals(AuthTokens.Empty, repository.getStoredTokens())
    }

    @Test
    fun `getStoredTokens - 저장된 값이 있으면 그대로 반환`() = runTest {
        val local = FakeTokenLocalDataSource()
        val repository = repository(FakeLoginRemoteDataSource(kakaoResult = flowOf(loginResult)), local)
        repository.loginWithKakao(kakaoAccessToken = "kakao-token", onError = {}).toList()

        assertEquals(loginResult.tokens, repository.getStoredTokens())
    }

    @Test
    fun `clearTokens - 저장소를 비운다`() = runTest {
        val local = FakeTokenLocalDataSource()
        val repository = repository(FakeLoginRemoteDataSource(kakaoResult = flowOf(loginResult)), local)
        repository.loginWithKakao(kakaoAccessToken = "kakao-token", onError = {}).toList()

        repository.clearTokens()

        assertEquals(AuthTokens.Empty, repository.getStoredTokens())
    }
}

private class FakeLoginRemoteDataSource(
    private val kakaoResult: Flow<LoginResult> = emptyFlow(),
    private val testResult: Flow<LoginResult> = emptyFlow(),
) : LoginRemoteDataSource {

    override fun loginWithKakao(
        kakaoAccessToken: String,
        onError: (String) -> Unit,
    ): Flow<LoginResult> = kakaoResult

    override fun loginWithTestAccount(
        testUserNo: Int,
        onError: (String) -> Unit,
        onUnsupported: () -> Unit,
    ): Flow<LoginResult> = testResult
}

private class FakeTokenLocalDataSource : TokenLocalDataSource {

    var accessToken = ""
        private set
    var refreshToken = ""
        private set
    var updateTokensCallCount = 0
        private set

    override fun observeAccessToken(): Flow<String> = flowOf(accessToken)

    override suspend fun getAccessToken(): String = accessToken

    override suspend fun getRefreshToken(): String = refreshToken

    override suspend fun saveAccessToken(accessToken: String) {
        this.accessToken = accessToken
    }

    override suspend fun updateTokens(accessToken: String, refreshToken: String) {
        updateTokensCallCount++
        this.accessToken = accessToken
        this.refreshToken = refreshToken
    }

    override suspend fun clearTokens() {
        accessToken = ""
        refreshToken = ""
    }
}
