package com.lwg.challenge.feature.friends.list.kakao

/**
 * 카카오톡 공유(KakaoLink) 로 친구 초대 메시지를 전송하는 플랫폼 wrapper.
 *
 * 외부 SDK 호출 wrapper 이므로 Repository 패턴(onSuccess/onError 콜백) 과 무관하게
 * 성공/실패를 [Result] 로 정규화한다.
 */
interface KakaoInviter {
    suspend fun sendInvite(inviterNickname: String): Result<Unit>
}
