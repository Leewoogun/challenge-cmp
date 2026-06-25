package com.lwg.challenge.domain.model.friend

/**
 * 친구 API 의 비즈니스/HTTP 에러.
 *
 * [code] 는 BaseResponse.code (예: 700/702/703/705) 또는 HTTP status code.
 * [message] 는 서버가 내려준 사용자 노출용 메시지 — ViewModel 이 그대로 스낵바에 표시 가능.
 *
 * ## ViewModel 사용
 * ```kotlin
 * when (val t = result.exceptionOrNull()) {
 *     is FriendsApiException -> showSnackbar(t.message)
 *     is FriendsNetworkException -> showSnackbar("네트워크 오류가 발생하였습니다.")
 *     else -> showSnackbar("알 수 없는 오류")
 * }
 * ```
 *
 * 401(세션 만료)은 여기로 오지 않는다 — Ktor Auth(refreshTokens)가 전담하고,
 * 누출된 HTTP 401 은 RepositoryImpl 이 조용히 흡수하므로 ViewModel 은 401 을 모른다.
 */
class FriendsApiException(
    val code: Int,
    message: String,
) : RuntimeException(message.ifBlank { "알 수 없는 오류" })

/**
 * 친구 API 의 네트워크/직렬화/Unknown 에러.
 * [message] 는 디버그용 — ViewModel 은 보통 고정 문구("네트워크 오류가 발생하였습니다.") 노출 권장.
 */
class FriendsNetworkException(
    message: String,
) : RuntimeException(message)
