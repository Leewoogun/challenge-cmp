package com.lwg.challenge.remote.datasource

import com.lwg.challenge.domain.model.AuthTokens
import com.lwg.challenge.domain.model.LoginResult
import com.lwg.challenge.remote.api.LoginApi
import com.lwg.challenge.remote.model.auth.KakaoLoginRequest
import com.lwg.challenge.remote.model.auth.LoginData
import com.lwg.challenge.remote.model.auth.LoginResponse
import com.lwg.challenge.remote.model.auth.LogoutResponse
import com.lwg.challenge.remote.model.auth.TestLoginRequest
import com.lwg.challenge.remote.network.util.ApiResult
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LoginRemoteDataSourceImplTest {

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

    @Test
    fun `loginWithTestAccount 성공 - LoginResult emit, 요청 body 일치`() = runTest {
        val api = FakeLoginApi(testResult = successResponse)
        val dataSource = LoginRemoteDataSourceImpl(api)

        val errors = mutableListOf<String>()
        var unsupportedCallCount = 0
        val emitted = dataSource.loginWithTestAccount(
            testUserNo = 2,
            onError = { errors.add(it) },
            onUnsupported = { unsupportedCallCount++ },
        ).toList()

        assertEquals(listOf(expectedResult), emitted)
        assertEquals(TestLoginRequest(testUserNo = 2), api.lastTestRequest)
        assertEquals(emptyList(), errors)
        assertEquals(0, unsupportedCallCount)
    }

    @Test
    fun `loginWithTestAccount 404 - onUnsupported 1회, onError 0회, emit 0건`() = runTest {
        val api = FakeLoginApi(
            testResult = ApiResult.Failure.HttpError(code = 404, message = "Not Found", body = ""),
        )
        val dataSource = LoginRemoteDataSourceImpl(api)

        val errors = mutableListOf<String>()
        var unsupportedCallCount = 0
        val emitted = dataSource.loginWithTestAccount(
            testUserNo = 1,
            onError = { errors.add(it) },
            onUnsupported = { unsupportedCallCount++ },
        ).toList()

        assertEquals(emptyList(), emitted)
        assertEquals(1, unsupportedCallCount)
        assertEquals(emptyList(), errors)
    }

    @Test
    fun `loginWithTestAccount 401 - onUnsupported 1회, onError 0회, emit 0건`() = runTest {
        val api = FakeLoginApi(
            testResult = ApiResult.Failure.HttpError(code = 401, message = "Unauthorized", body = ""),
        )
        val dataSource = LoginRemoteDataSourceImpl(api)

        val errors = mutableListOf<String>()
        var unsupportedCallCount = 0
        val emitted = dataSource.loginWithTestAccount(
            testUserNo = 1,
            onError = { errors.add(it) },
            onUnsupported = { unsupportedCallCount++ },
        ).toList()

        assertEquals(emptyList(), emitted)
        assertEquals(1, unsupportedCallCount)
        assertEquals(emptyList(), errors)
    }

    @Test
    fun `loginWithTestAccount 비즈니스 에러(CustomError 700) - onError 1회, onUnsupported 0회`() = runTest {
        val api = FakeLoginApi(
            testResult = ApiResult.Failure.CustomError(code = 700, message = "테스트 계정 번호는 1~3이에요"),
        )
        val dataSource = LoginRemoteDataSourceImpl(api)

        val errors = mutableListOf<String>()
        var unsupportedCallCount = 0
        val emitted = dataSource.loginWithTestAccount(
            testUserNo = 4,
            onError = { errors.add(it) },
            onUnsupported = { unsupportedCallCount++ },
        ).toList()

        assertEquals(emptyList(), emitted)
        assertEquals(listOf("테스트 계정 번호는 1~3이에요"), errors)
        assertEquals(0, unsupportedCallCount)
    }

    @Test
    fun `loginWithTestAccount 그 외 HttpError(500) - onUnsupported 아닌 onError 로 간다`() = runTest {
        val api = FakeLoginApi(
            testResult = ApiResult.Failure.HttpError(code = 500, message = "Internal Server Error", body = ""),
        )
        val dataSource = LoginRemoteDataSourceImpl(api)

        val errors = mutableListOf<String>()
        var unsupportedCallCount = 0
        val emitted = dataSource.loginWithTestAccount(
            testUserNo = 1,
            onError = { errors.add(it) },
            onUnsupported = { unsupportedCallCount++ },
        ).toList()

        assertEquals(emptyList(), emitted)
        assertEquals(listOf("Internal Server Error"), errors)
        assertEquals(0, unsupportedCallCount)
    }

    @Test
    fun `loginWithKakao 성공 - LoginResult emit, 요청 body 일치`() = runTest {
        val api = FakeLoginApi(kakaoResult = successResponse)
        val dataSource = LoginRemoteDataSourceImpl(api)

        val errors = mutableListOf<String>()
        val emitted = dataSource.loginWithKakao(
            kakaoAccessToken = "kakao-token",
            onError = { errors.add(it) },
        ).toList()

        assertEquals(listOf(expectedResult), emitted)
        assertEquals(KakaoLoginRequest("kakao-token"), api.lastKakaoRequest)
        assertEquals(emptyList(), errors)
    }

    @Test
    fun `logout 성공 - Api 를 1회 호출한다`() = runTest {
        val api = FakeLoginApi()
        val dataSource = LoginRemoteDataSourceImpl(api)

        dataSource.logout()

        assertEquals(1, api.logoutCallCount)
    }

    // 로그아웃 실패를 예외로 올리면 호출자(LogoutUseCase)의 로컬 정리가 끊긴다.
    @Test
    fun `logout 실패 - 예외를 던지지 않는다`() = runTest {
        val api = FakeLoginApi(logoutResult = ApiResult.Failure.NetworkError("offline"))
        val dataSource = LoginRemoteDataSourceImpl(api)

        dataSource.logout()

        assertEquals(1, api.logoutCallCount)
    }
}

private class FakeLoginApi(
    private val kakaoResult: ApiResult<LoginResponse> = ApiResult.Failure.NetworkError("호출되면 안 되는 경로"),
    private val testResult: ApiResult<LoginResponse> = ApiResult.Failure.NetworkError("호출되면 안 되는 경로"),
    private val logoutResult: ApiResult<LogoutResponse> = ApiResult.Success(LogoutResponse()),
) : LoginApi {

    var lastKakaoRequest: KakaoLoginRequest? = null
        private set
    var lastTestRequest: TestLoginRequest? = null
        private set
    var logoutCallCount = 0
        private set

    override suspend fun kakaoLogin(request: KakaoLoginRequest): ApiResult<LoginResponse> {
        lastKakaoRequest = request
        return kakaoResult
    }

    override suspend fun testLogin(request: TestLoginRequest): ApiResult<LoginResponse> {
        lastTestRequest = request
        return testResult
    }

    override suspend fun logout(): ApiResult<LogoutResponse> {
        logoutCallCount++
        return logoutResult
    }
}
