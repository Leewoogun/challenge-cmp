package com.lwg.challenge.data.repository

import com.lwg.challenge.domain.model.AuthTokens
import com.lwg.challenge.domain.model.LoginResult
import com.lwg.challenge.local.datastore.datasource.TokenLocalDataSource
import com.lwg.challenge.remote.api.LoginApi
import com.lwg.challenge.remote.model.auth.KakaoLoginRequest
import com.lwg.challenge.remote.model.auth.LoginData
import com.lwg.challenge.remote.model.auth.LoginResponse
import com.lwg.challenge.remote.model.auth.TestLoginRequest
import com.lwg.challenge.remote.network.auth.AuthTokenCacheInvalidator
import com.lwg.challenge.remote.network.util.ApiResult
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LoginRepositoryImplTest {

    private val successResponse = ApiResult.Success(
        LoginResponse(
            data = LoginData(
                accessToken = "access-1",
                refreshToken = "refresh-1",
                userId = 101L,
                isNewUser = false,
            ),
        ),
    )

    private val expectedResult = LoginResult(
        tokens = AuthTokens(accessToken = "access-1", refreshToken = "refresh-1"),
        userId = 101L,
        isNewUser = false,
    )

    private fun repository(api: LoginApi, local: TokenLocalDataSource) = LoginRepositoryImpl(
        loginApi = api,
        tokenLocalDataSource = local,
        // Auth 플러그인이 없는 클라이언트라 invalidate() 는 no-op — 실물 생성이 안전하다
        authTokenCacheInvalidator = AuthTokenCacheInvalidator(HttpClient(MockEngine { respondOk() })),
    )

    @Test
    fun `loginWithTestAccount 성공 - LoginResult emit, 토큰 저장, 요청 body 일치`() = runTest {
        val api = FakeLoginApi(testResult = successResponse)
        val local = FakeTokenLocalDataSource()
        val repository = repository(api, local)

        val errors = mutableListOf<String>()
        var unsupportedCallCount = 0
        val emitted = repository.loginWithTestAccount(
            testUserNo = 2,
            onError = { errors.add(it) },
            onUnsupported = { unsupportedCallCount++ },
        ).toList()

        assertEquals(listOf(expectedResult), emitted)
        assertEquals("access-1", local.accessToken)
        assertEquals("refresh-1", local.refreshToken)
        assertEquals(TestLoginRequest(testUserNo = 2), api.lastTestRequest)
        assertEquals(emptyList(), errors)
        assertEquals(0, unsupportedCallCount)
    }

    @Test
    fun `loginWithTestAccount 404 - onUnsupported 1회, onError 0회, emit 0건`() = runTest {
        val api = FakeLoginApi(
            testResult = ApiResult.Failure.HttpError(code = 404, message = "Not Found", body = ""),
        )
        val local = FakeTokenLocalDataSource()
        val repository = repository(api, local)

        val errors = mutableListOf<String>()
        var unsupportedCallCount = 0
        val emitted = repository.loginWithTestAccount(
            testUserNo = 1,
            onError = { errors.add(it) },
            onUnsupported = { unsupportedCallCount++ },
        ).toList()

        assertEquals(emptyList(), emitted)
        assertEquals(1, unsupportedCallCount)
        assertEquals(emptyList(), errors)
        assertEquals(0, local.updateTokensCallCount)
    }

    @Test
    fun `loginWithTestAccount 401 - onUnsupported 1회, onError 0회, emit 0건`() = runTest {
        val api = FakeLoginApi(
            testResult = ApiResult.Failure.HttpError(code = 401, message = "Unauthorized", body = ""),
        )
        val local = FakeTokenLocalDataSource()
        val repository = repository(api, local)

        val errors = mutableListOf<String>()
        var unsupportedCallCount = 0
        val emitted = repository.loginWithTestAccount(
            testUserNo = 1,
            onError = { errors.add(it) },
            onUnsupported = { unsupportedCallCount++ },
        ).toList()

        assertEquals(emptyList(), emitted)
        assertEquals(1, unsupportedCallCount)
        assertEquals(emptyList(), errors)
        assertEquals(0, local.updateTokensCallCount)
    }

    @Test
    fun `loginWithTestAccount 비즈니스 에러(CustomError 700) - onError 1회, onUnsupported 0회`() = runTest {
        val api = FakeLoginApi(
            testResult = ApiResult.Failure.CustomError(code = 700, message = "테스트 계정 번호는 1~3이에요"),
        )
        val local = FakeTokenLocalDataSource()
        val repository = repository(api, local)

        val errors = mutableListOf<String>()
        var unsupportedCallCount = 0
        val emitted = repository.loginWithTestAccount(
            testUserNo = 4,
            onError = { errors.add(it) },
            onUnsupported = { unsupportedCallCount++ },
        ).toList()

        assertEquals(emptyList(), emitted)
        assertEquals(listOf("테스트 계정 번호는 1~3이에요"), errors)
        assertEquals(0, unsupportedCallCount)
        assertEquals(0, local.updateTokensCallCount)
    }

    @Test
    fun `loginWithTestAccount 그 외 HttpError(500) - onUnsupported 아닌 onError 로 간다`() = runTest {
        val api = FakeLoginApi(
            testResult = ApiResult.Failure.HttpError(code = 500, message = "Internal Server Error", body = ""),
        )
        val local = FakeTokenLocalDataSource()
        val repository = repository(api, local)

        val errors = mutableListOf<String>()
        var unsupportedCallCount = 0
        val emitted = repository.loginWithTestAccount(
            testUserNo = 1,
            onError = { errors.add(it) },
            onUnsupported = { unsupportedCallCount++ },
        ).toList()

        assertEquals(emptyList(), emitted)
        assertEquals(listOf("Internal Server Error"), errors)
        assertEquals(0, unsupportedCallCount)
    }

    @Test
    fun `loginWithKakao 성공 - 테스트 로그인과 동일한 후처리로 토큰 저장 후 emit`() = runTest {
        val api = FakeLoginApi(kakaoResult = successResponse)
        val local = FakeTokenLocalDataSource()
        val repository = repository(api, local)

        val errors = mutableListOf<String>()
        val emitted = repository.loginWithKakao(
            kakaoAccessToken = "kakao-token",
            onError = { errors.add(it) },
        ).toList()

        assertEquals(listOf(expectedResult), emitted)
        assertEquals("access-1", local.accessToken)
        assertEquals("refresh-1", local.refreshToken)
        assertEquals(KakaoLoginRequest("kakao-token"), api.lastKakaoRequest)
        assertEquals(emptyList(), errors)
    }
}

private class FakeLoginApi(
    private val kakaoResult: ApiResult<LoginResponse> = ApiResult.Failure.NetworkError("호출되면 안 되는 경로"),
    private val testResult: ApiResult<LoginResponse> = ApiResult.Failure.NetworkError("호출되면 안 되는 경로"),
) : LoginApi {

    var lastKakaoRequest: KakaoLoginRequest? = null
        private set
    var lastTestRequest: TestLoginRequest? = null
        private set

    override suspend fun kakaoLogin(request: KakaoLoginRequest): ApiResult<LoginResponse> {
        lastKakaoRequest = request
        return kakaoResult
    }

    override suspend fun testLogin(request: TestLoginRequest): ApiResult<LoginResponse> {
        lastTestRequest = request
        return testResult
    }
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
