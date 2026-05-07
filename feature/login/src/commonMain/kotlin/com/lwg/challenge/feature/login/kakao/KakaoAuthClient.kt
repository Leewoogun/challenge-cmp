package com.lwg.challenge.feature.login.kakao

/**
 * 플랫폼별 카카오 OAuth 인증 진입점.
 *
 * 단계:
 * 1. 앱이 이 메서드를 호출
 * 2. 플랫폼별 actual 구현체가 카카오 SDK / 카카오 계정 웹뷰로 OAuth 흐름을 시작
 *    - Android: `UserApiClient.loginWithKakaoTalk` → fallback `loginWithKakaoAccount`
 *    - iOS: `AuthApi.shared.loginWithKakaoTalk` → fallback `loginWithKakaoAccount`
 *    SDK 내부에서 인증 코드 획득 → access_token 교환까지 완료된다 (1~4 단계 캡슐화).
 * 3. 결과로 카카오 access_token 을 반환
 *    이 값을 그대로 [com.lwg.challenge.remote.api.LoginApi.kakaoLogin] 에 넘긴다 (api-contract.md 참고).
 *
 * 실패는 예외로 던지지 않고 [KakaoAuthResult] sealed 로 반환한다 (UI 분기 단순화).
 */
interface KakaoAuthClient {

    /**
     * Kakao OAuth 흐름을 시작하고, 성공 시 access_token 을 반환한다.
     *
     * Android 는 메인 스레드에서 호출되어야 한다 (Activity Context 필요).
     * iOS 는 메인 스레드에서 호출되어야 한다 (UIWindowScene 접근 필요).
     */
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
