package com.lwg.challenge.domain.model

/**
 * 사용자 누적 전적(record). 홈 화면 상단 전적 카드 모델.
 *
 * 신규 사용자(user_stats row 없음)도 백엔드가 0 으로 채워 응답하므로 모바일은
 * nullable 처리 없이 항상 4개 필드를 보유한다.
 *
 * @property win 승 (CHALLENGER_WIN / OPPONENT_WIN 의 본인 측)
 * @property lose 패 (상대 측 승 또는 BOTH_LOSE)
 * @property draw 무 (DRAW 양쪽 동시 증가)
 * @property currentStreak 가장 최근 결과 시점부터 win 만 연속된 횟수. lose/draw/both_lose 시 0
 */
data class UserRecord(
    val win: Int,
    val lose: Int,
    val draw: Int,
    val currentStreak: Int,
) {

    /** 4개 값이 모두 0이면 신규 사용자(전적 없음)로 간주. */
    val isEmpty: Boolean
        get() = win == 0 && lose == 0 && draw == 0 && currentStreak == 0

    companion object {
        val Empty = UserRecord(win = 0, lose = 0, draw = 0, currentStreak = 0)
    }
}
