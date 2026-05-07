package com.lwg.challenge.feature.login.kakao

import android.content.Context
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Android 카카오 OAuth 진입점.
 *
 * 흐름 (카카오 SDK 가 1~4 단계를 모두 캡슐화):
 * - 카카오톡이 설치되어 있으면 카카오톡 앱 ↔ 우리 앱 간 인증 (App-to-App, 자동 redirect intent 처리)
 * - 미설치 시 카카오 계정 웹 로그인 (Custom Tab) 으로 fallback
 *
 * AndroidManifest 에 `AuthCodeHandlerActivity` (`kakao{NATIVE_APP_KEY}://oauth`) 가
 * 등록되어 있어야 SDK 가 redirect 를 가로챌 수 있다.
 *
 * SDK 초기화 (`KakaoSdk.init`) 는 `ChallengeApplication.onCreate` 에서 수행.
 */
internal class KakaoAuthClientAndroid(
    private val context: Context,
) : KakaoAuthClient {

    override suspend fun requestAccessToken(): KakaoAuthResult =
        suspendCancellableCoroutine { continuation ->
            val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
                if (continuation.isActive) {
                    continuation.resume(mapResult(token, error))
                }
            }

            val client = UserApiClient.instance
            if (client.isKakaoTalkLoginAvailable(context)) {
                client.loginWithKakaoTalk(context) { token, error ->
                    // 카카오톡 로그인 실패 시 카카오 계정 웹 로그인으로 fallback.
                    if (error != null && shouldFallbackToAccount(error)) {
                        client.loginWithKakaoAccount(context, callback = callback)
                    } else {
                        callback(token, error)
                    }
                }
            } else {
                client.loginWithKakaoAccount(context, callback = callback)
            }
        }

    private fun mapResult(token: OAuthToken?, error: Throwable?): KakaoAuthResult {
        if (token != null) {
            return KakaoAuthResult.Success(accessToken = token.accessToken)
        }
        return when {
            error == null -> KakaoAuthResult.Failure(message = ERROR_UNKNOWN)
            error is ClientError && error.reason == ClientErrorCause.Cancelled -> KakaoAuthResult.Cancelled
            else -> KakaoAuthResult.Failure(message = error.message ?: ERROR_UNKNOWN)
        }
    }

    private fun shouldFallbackToAccount(error: Throwable): Boolean {
        // 사용자가 카카오톡 동의 화면에서 직접 취소한 경우는 fallback 하지 않고 그대로 Cancelled.
        if (error is ClientError && error.reason == ClientErrorCause.Cancelled) return false
        return true
    }

    private companion object {
        const val ERROR_UNKNOWN = "카카오 로그인에 실패했습니다. 잠시 후 다시 시도해주세요."
    }
}
