package com.lwg.challenge.domain.repository

import com.lwg.challenge.domain.model.AuthTokens
import com.lwg.challenge.domain.model.LoginResult

/**
 * 인증 레이어 Repository. 서버 API + 플랫폼 보안 저장소에 접근한다.
 *
 * 에러 처리 정책:
 * - 서버가 항상 HTTP 200 + body.code 로 에러를 반환하므로 (ADR-0002),
 *   실패 시 onError(errorCode, message) 를 호출한다.
 *   - code=700 → 스낵바
 *   - code=701 → 다이얼로그 (카카오 토큰 만료 등)
 *   - code=703 → 전체화면 에러
 *   - code=401 → refresh 실패, 로그인 화면으로
 *   - 기타 네트워크/파싱 에러도 동일 콜백으로 전달 (code=0 또는 -1).
 */
interface LoginRepository {

    /**
     * Kakao access token 으로 서버 로그인.
     * 성공 시 반환한 토큰은 자동으로 [SecureTokenStorage]-like 저장소에 저장된다.
     */
    suspend fun loginWithKakao(
        kakaoAccessToken: String,
        onError: (code: Int, message: String) -> Unit,
    ): LoginResult?

    /**
     * 저장된 refresh token 으로 access token 재발급.
     * 성공 시 access token 갱신 후 [AuthTokens] 반환.
     * 저장된 refresh 가 없거나 서버가 code=401 반환 시 null.
     */
    suspend fun refreshAccessToken(
        onError: (code: Int, message: String) -> Unit,
    ): AuthTokens?

    /**
     * 현재 저장된 토큰 조회. 비어있으면 [AuthTokens.Empty].
     */
    suspend fun getStoredTokens(): AuthTokens

    /**
     * 로컬 저장소에서 토큰 삭제 (로그아웃/refresh 만료 시).
     */
    suspend fun clearTokens()
}
