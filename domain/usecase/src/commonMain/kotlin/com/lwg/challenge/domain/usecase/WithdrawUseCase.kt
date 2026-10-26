package com.lwg.challenge.domain.usecase

import com.lwg.challenge.domain.model.LogoutReason
import com.lwg.challenge.domain.repository.WithdrawRepository

class WithdrawUseCase(
    private val withdrawRepository: WithdrawRepository,
    private val logoutUseCase: LogoutUseCase,
) {
    /**
     * 탈퇴가 성공했을 때만 로컬을 정리하고 [onSuccess] 를 부른다.
     *
     * 실패 시 로컬을 남기는 것이 의도다 — 계정이 그대로 살아 있으므로 재시도하려면 세션이 필요하다.
     */
    suspend operator fun invoke(
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        var withdrawn = false
        withdrawRepository.withdraw(
            // onSuccess 가 suspend 가 아니라 정리를 여기서 못 한다. 플래그로 받아 호출 밖에서 잇는다.
            onSuccess = { withdrawn = true },
            onError = onError,
        )
        if (!withdrawn) return

        logoutUseCase(LogoutReason.WITHDRAWN)
        onSuccess()
    }
}
