package com.lwg.challenge.feature.login.kakao

interface KakaoAuthClient {
    suspend fun requestAccessToken(): KakaoAuthResult
}

/**
 * 카카오 OAuth 흐름의 결과.
 */
sealed interface KakaoAuthResult {

    /** access_token 획득 성공. */
    data class Success(val accessToken: String) : KakaoAuthResult

    /** 사용자가 동의 화면에서 취소 / 뒤로가기. UI 는 메시지 노출 없이 Idle 복귀. */
    data object Cancelled : KakaoAuthResult

    /**
     * SDK / 네트워크 오류 등.
     * @param message 사용자 친화적 메시지 (스낵바 노출용).
     */
    data class Failure(val message: String) : KakaoAuthResult
}
