package com.lwg.challenge.data.datasource.remote

import com.lwg.challenge.domain.model.LoginResult
import kotlinx.coroutines.flow.Flow

interface LoginRemoteDataSource {

    fun loginWithKakao(
        kakaoAccessToken: String,
        onError: (String) -> Unit,
    ): Flow<LoginResult>

    /**
     * @param onUnsupported 서버가 테스트 로그인을 지원하지 않을 때(라우팅 부재) 호출된다.
     *   [onError] 와 분리한 이유: 이것은 "실패"가 아니라 "이 서버에는 그 기능이 없다"는
     *   다른 종류의 신호이고, UI 가 버튼을 비활성화하는 근거로 쓰기 때문이다.
     */
    fun loginWithTestAccount(
        testUserNo: Int,
        onError: (String) -> Unit,
        onUnsupported: () -> Unit,
    ): Flow<LoginResult>

    /**
     * 서버 세션을 무효화한다. 멱등하다.
     *
     * 다른 함수들과 달리 `onError` 를 받지 않는다 — 로그아웃 실패는 사용자에게 보여줄 것이 없고,
     * 실패해도 로컬 정리는 그대로 진행돼야 하기 때문이다(로그인 상태에 가두지 않는다).
     */
    suspend fun logout()
}
