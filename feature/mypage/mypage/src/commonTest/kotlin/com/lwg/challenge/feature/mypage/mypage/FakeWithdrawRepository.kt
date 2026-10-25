package com.lwg.challenge.feature.mypage.mypage

import com.lwg.challenge.domain.repository.WithdrawRepository
import kotlinx.coroutines.CompletableDeferred

class FakeWithdrawRepository : WithdrawRepository {

    var error: String? = null

    /** 설정하면 [withdraw] 가 완료될 때까지 멈춘다 — '진행 중' 상태를 붙잡아 두기 위한 스위치. */
    var gate: CompletableDeferred<Unit>? = null

    var callCount: Int = 0
        private set

    override suspend fun withdraw(
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        callCount += 1
        gate?.await()

        val currentError = error
        if (currentError != null) onError(currentError) else onSuccess()
    }
}
