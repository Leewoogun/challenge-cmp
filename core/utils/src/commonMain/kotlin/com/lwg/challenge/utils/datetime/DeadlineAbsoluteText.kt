package com.lwg.challenge.utils.datetime

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.minus

/**
 * 챌린지의 절대 마감 시각 표시 문자열. 예: "7/28 24:00"
 *
 * 서버 `deadline` 은 챌린지 당일이 끝나는 순간을 **익일 00:00 이라는 배타적 끝점**으로 내려준다
 * (challengeDate `2026-08-03` → deadline `2026-08-04 00:00:00`).
 * 그래서 deadline 을 그대로 찍으면 "8/4" 가 나와 **표기가 하루 밀린다**.
 * [challengeDate] 가 있으면 그쪽을 우선하는 이유가 이것이다 — 자정 롤오버 산술 자체가 없어 안전하다.
 * deadline 우선으로 되돌리면 하루 밀림이 그대로 재발한다.
 *
 * "00:00" 대신 "24:00" 으로 적는 근거는 [kstDeadlineHintText] KDoc 참고.
 * 생성 플로우 전용인 [kstDeadlineHintText] 는 `daysAhead` 를 받으므로 조회 화면에서는 쓸 수 없다.
 *
 * @param challengeDate 챌린지 수행 날짜. null 이면 [deadline] 에서 역산한다.
 * @param deadline 마감 시각(KST). 00:00 이면 전날 24:00 으로, 그 외 시각은 그대로 표기한다 —
 *   "서버는 항상 00:00 을 준다" 는 가정을 코드가 강제하면 예외적인 값이 조용히 24:00 으로 위장된다.
 */
fun deadlineAbsoluteText(challengeDate: LocalDate?, deadline: LocalDateTime): String {
    if (challengeDate != null) return "${challengeDate.monthNumber}/${challengeDate.day} 24:00"

    if (deadline.hour == 0 && deadline.minute == 0) {
        val endingDate = deadline.date.minus(1, DateTimeUnit.DAY)
        return "${endingDate.monthNumber}/${endingDate.day} 24:00"
    }

    val hour = deadline.hour.toString().padStart(2, '0')
    val minute = deadline.minute.toString().padStart(2, '0')
    return "${deadline.date.monthNumber}/${deadline.date.day} $hour:$minute"
}
