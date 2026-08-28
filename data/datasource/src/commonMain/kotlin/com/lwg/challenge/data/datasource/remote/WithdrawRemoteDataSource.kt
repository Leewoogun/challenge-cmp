package com.lwg.challenge.data.datasource.remote

interface WithdrawRemoteDataSource {

    /**
     * 계정을 탈퇴시킨다. 멱등하다.
     *
     * 로그아웃과 달리 `onError` 를 받는다 — 카카오 연결 해제 실패(`code 701`)는 재시도로 풀리는
     * 일시적 실패라 사용자에게 알려야 하고, 성공/실패를 호출부가 구분할 수 있어야 한다.
     */
    suspend fun withdraw(
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    )
}
