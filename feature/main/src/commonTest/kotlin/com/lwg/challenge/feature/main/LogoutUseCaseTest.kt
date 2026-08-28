package com.lwg.challenge.feature.main

import com.lwg.challenge.domain.model.LogoutReason
import com.lwg.challenge.domain.usecase.LogoutUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * `:domain:usecase` 에는 commonTest 소스셋이 없어 iOS 러너가 도는 이 모듈에 둔다.
 * [LogoutUseCase] 를 이미 쓰는 곳이고 필요한 Fake 도 여기 있다.
 */
class LogoutUseCaseTest {

    private val loginRepository = FakeLoginRepository()
    private val userInfoRepository = FakeUserInfoRepository()
    private val logoutUseCase = LogoutUseCase(loginRepository, userInfoRepository)

    // 토큰을 먼저 지우면 서버 호출에 Bearer 가 실리지 않아 401 이 나고, 그 401 이 갱신 실패로 이어져
    // 정상적인 계정 전환이 '세션 만료'로 반응한다.
    @Test
    fun `USER 면 서버 logout 을 clearTokens 보다 먼저 부른다`() = runTest {
        logoutUseCase(LogoutReason.USER)

        assertEquals(listOf("logout", "clearTokens"), loginRepository.callLog)
        assertTrue(userInfoRepository.clearUserInfoCacheCalled)
    }

    // 서버가 죽었다고 사용자를 로그인 상태에 가둘 수는 없다.
    @Test
    fun `서버 logout 이 실패해도 로컬 정리는 모두 수행된다`() = runTest {
        loginRepository.logoutFails = true

        logoutUseCase(LogoutReason.USER)

        assertTrue(loginRepository.logoutCalled)
        assertTrue(loginRepository.clearTokensCalled)
        assertTrue(userInfoRepository.clearUserInfoCacheCalled)
    }

    // 갱신이 이미 실패해 서버가 세션을 끊은 뒤다. 부르면 401 → 갱신 재시도 → 만료 이벤트로 재진입한다.
    @Test
    fun `SESSION_EXPIRED 면 서버를 부르지 않고 로컬만 정리한다`() = runTest {
        logoutUseCase(LogoutReason.SESSION_EXPIRED)

        assertFalse(loginRepository.logoutCalled)
        assertEquals(listOf("clearTokens"), loginRepository.callLog)
        assertTrue(userInfoRepository.clearUserInfoCacheCalled)
    }

    // 계정 자체가 사라져 끊을 세션이 없다. 부르면 이미 무효한 토큰으로 401 을 받는다.
    @Test
    fun `WITHDRAWN 이면 서버를 부르지 않고 로컬만 정리한다`() = runTest {
        logoutUseCase(LogoutReason.WITHDRAWN)

        assertFalse(loginRepository.logoutCalled)
        assertEquals(listOf("clearTokens"), loginRepository.callLog)
        assertTrue(userInfoRepository.clearUserInfoCacheCalled)
    }
}
