package com.lwg.challenge.push

/** 푸시 알림이 알리는 사건. 목적지 매핑은 [toRoute] 한 벌을 모든 소비처(푸시 수신, 알림 목록)가 공유한다. */
sealed interface PushEvent {

    data class ChallengeRequest(val challengeId: Long) : PushEvent

    data class ChallengeAccepted(val challengeId: Long) : PushEvent

    data class ChallengeRejected(val challengeId: Long) : PushEvent

    data class OpponentVerified(val challengeId: Long) : PushEvent

    companion object {
        const val KEY_TYPE = "type"
        const val KEY_CHALLENGE_ID = "challengeId"

        /** intent extras 에서 소비 후 지워야 할 키. 화면 회전 시 재소비를 막는다. */
        val KEYS = listOf(KEY_TYPE, KEY_CHALLENGE_ID)

        /**
         * 알림 타입 어휘의 **단일 출처**. 목적지 매핑([of])과 목록의 아이콘·색 매핑이 같은 값을 봐야
         * 하므로 공개한다 — 사본을 두면 서버가 타입 이름을 바꿀 때 한쪽만 따라가고, 나머지 한쪽은
         * 아무것도 실패시키지 않은 채 폴백으로 조용히 내려앉는다.
         */
        const val TYPE_CHALLENGE_REQUEST = "CHALLENGE_REQUEST"
        const val TYPE_CHALLENGE_ACCEPTED = "CHALLENGE_ACCEPTED"
        const val TYPE_CHALLENGE_REJECTED = "CHALLENGE_REJECTED"
        const val TYPE_OPPONENT_VERIFIED = "OPPONENT_VERIFIED"

        /**
         * 타입 문자열 + 참조 대상 id 를 이벤트로 읽는다. FCM data map 과 알림 목록 DTO 가 이 한 벌을 쓴다.
         * referenceId 필수 여부는 타입별로 판정한다 — 참조 대상 없는 타입이 붙어도 버리지 않도록.
         */
        fun of(type: String?, referenceId: Long?): PushEvent? = when (type) {
            TYPE_CHALLENGE_REQUEST -> referenceId?.let(::ChallengeRequest)
            TYPE_CHALLENGE_ACCEPTED -> referenceId?.let(::ChallengeAccepted)
            TYPE_CHALLENGE_REJECTED -> referenceId?.let(::ChallengeRejected)
            TYPE_OPPONENT_VERIFIED -> referenceId?.let(::OpponentVerified)
            else -> null
        }

        /** FCM `data` 를 이벤트로 읽는다. 값은 전부 문자열로 도착해 숫자도 "7" 이다. */
        fun from(data: Map<String, String>): PushEvent? =
            of(data[KEY_TYPE], data[KEY_CHALLENGE_ID]?.toLongOrNull())
    }
}
