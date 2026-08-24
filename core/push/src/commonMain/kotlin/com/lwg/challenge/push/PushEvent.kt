package com.lwg.challenge.push

/** 푸시 알림이 알리는 사건. 목적지 매핑은 소비처(:feature:main)가 정한다. */
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

        private const val TYPE_CHALLENGE_REQUEST = "CHALLENGE_REQUEST"
        private const val TYPE_CHALLENGE_ACCEPTED = "CHALLENGE_ACCEPTED"
        private const val TYPE_CHALLENGE_REJECTED = "CHALLENGE_REJECTED"
        private const val TYPE_OPPONENT_VERIFIED = "OPPONENT_VERIFIED"

        /**
         * FCM `data` 를 이벤트로 읽는다. 값은 전부 문자열로 도착해 숫자도 "7" 이다.
         * challengeId 필수 여부는 타입별로 판정한다 — 참조 대상 없는 타입이 붙어도 버리지 않도록.
         */
        fun from(data: Map<String, String>): PushEvent? {
            val challengeId = data[KEY_CHALLENGE_ID]?.toLongOrNull()

            return when (data[KEY_TYPE]) {
                TYPE_CHALLENGE_REQUEST -> challengeId?.let(::ChallengeRequest)
                TYPE_CHALLENGE_ACCEPTED -> challengeId?.let(::ChallengeAccepted)
                TYPE_CHALLENGE_REJECTED -> challengeId?.let(::ChallengeRejected)
                TYPE_OPPONENT_VERIFIED -> challengeId?.let(::OpponentVerified)
                else -> null
            }
        }
    }
}
