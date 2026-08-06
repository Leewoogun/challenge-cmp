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
}
