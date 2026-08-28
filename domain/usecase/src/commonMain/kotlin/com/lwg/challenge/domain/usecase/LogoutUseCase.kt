package com.lwg.challenge.domain.usecase

import com.lwg.challenge.domain.model.LogoutReason
import com.lwg.challenge.domain.repository.LoginRepository
import com.lwg.challenge.domain.repository.UserInfoRepository
import kotlin.coroutines.cancellation.CancellationException

class LogoutUseCase(
    private val loginRepository: LoginRepository,
    private val userInfoRepository: UserInfoRepository,
) {
    suspend operator fun invoke(reason: LogoutReason) {
        // SESSION_EXPIRED 는 갱신이 이미 실패한 상태라 서버가 세션을 끊어 둔 뒤다. 여기서 호출하면
        // 401 → 갱신 재시도 → 실패 → emitSessionExpired() 로 같은 로그아웃 흐름에 재진입한다.
        if (reason == LogoutReason.USER) {
            // 토큰이 살아 있어야 Bearer 가 실린다 — 반드시 clearTokens() 앞이다.
            try {
                loginRepository.logout()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // 서버가 죽었다고 사용자를 로그인 상태에 가둘 수는 없다. 삼키고 로컬 정리를 계속한다.
                // 실패 내용은 RemoteDataSource 의 에러 핸들러가 이미 로그로 남긴다.
            }
        }

        loginRepository.clearTokens()
        userInfoRepository.clearUserInfoCache()
    }
}
