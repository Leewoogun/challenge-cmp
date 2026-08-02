package com.lwg.challenge.domain.usecase

import com.lwg.challenge.domain.model.LoginResult
import com.lwg.challenge.domain.repository.LoginRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

/**
 * 테스트 계정(1~3)으로 로그인한다 (debug 전용 개발 도구).
 *
 * 계정 전환 시 [LogoutUseCase] 로 이전 계정의 토큰과 `UserInfo` 캐시를 먼저 비운다.
 * `UserInfoRepository` 가 CACHE_FIRST 라 캐시를 안 비우면 토큰만 바뀌고
 * 홈이 이전 테스터의 닉네임을 계속 보여준다.
 * `LoginRepository.clearTokens()` 만으로는 부족하다 — 그건 토큰과 Ktor Auth 캐시만 비우고
 * `UserInfo` 캐시는 건드리지 않는다. 둘을 함께 정리하는 것은 [LogoutUseCase] 다.
 */
class LoginWithTestAccountUseCase(
    private val logoutUseCase: LogoutUseCase,
    private val loginRepository: LoginRepository,
) {

    operator fun invoke(
        testUserNo: Int,
        onError: (String) -> Unit,
        onUnsupported: () -> Unit,
    ): Flow<LoginResult> = flow {
        logoutUseCase()
        emitAll(
            loginRepository.loginWithTestAccount(
                testUserNo = testUserNo,
                onError = onError,
                onUnsupported = onUnsupported,
            ),
        )
    }
}
