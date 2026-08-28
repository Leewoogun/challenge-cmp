package com.lwg.challenge.domain.repository

interface WithdrawRepository {

    /**
     * 계정을 탈퇴시킨다. 이미 탈퇴한 계정으로 다시 불러도 성공한다(서버가 멱등하다).
     *
     * 로그아웃과 달리 실패를 삼키지 않는다 — 카카오 연결 해제가 이행되지 않았는데 성공이라고
     * 답하면 안 되고, 그 실패는 재시도로 풀린다. 그래서 [onSuccess] 와 [onError] 가 갈린다.
     *
     * 로컬 토큰·캐시는 건드리지 않는다 — 정리 책임은 호출자(WithdrawUseCase)에 있다.
     */
    suspend fun withdraw(
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    )
}
