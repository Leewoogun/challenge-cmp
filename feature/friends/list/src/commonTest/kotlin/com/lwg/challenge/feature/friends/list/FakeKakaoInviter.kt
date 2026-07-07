package com.lwg.challenge.feature.friends.list

import com.lwg.challenge.feature.friends.list.kakao.KakaoInviter

/**
 * 테스트용 [KakaoInviter] 페이크.
 *
 * - [result] 로 성공/실패 시나리오 주입.
 * - 호출 횟수/마지막 닉네임 기록 (호출 검증용).
 */
class FakeKakaoInviter : KakaoInviter {

    var result: Result<Unit> = Result.success(Unit)

    var callCount = 0
        private set
    var lastNickname: String? = null
        private set

    override suspend fun sendInvite(inviterNickname: String): Result<Unit> {
        callCount++
        lastNickname = inviterNickname
        return result
    }
}
