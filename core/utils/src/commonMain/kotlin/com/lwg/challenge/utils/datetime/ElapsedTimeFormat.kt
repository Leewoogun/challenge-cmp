@file:OptIn(ExperimentalTime::class)

package com.lwg.challenge.utils.datetime

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toInstant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime

/**
 * 이미 지난 시각(알림 `createdAt` 등)을 한국어 경과 시간 텍스트로 변환.
 *
 * 마감 카운트다운인 [toRelativeKoreanString] 과 방향이 반대다. 과거 시각을 그쪽에 넘기면 전부 "마감" 이 된다.
 *
 * - 1분 미만: "방금 전"
 * - 1시간 미만: "X분 전"
 * - 24시간 미만: "X시간 전"
 * - 달력상 하루 전: "어제"
 * - 그 외: "M월 d일"
 *
 * "X시간 전" 을 "어제" 보다 **먼저** 판정하는 이유: 두 표기의 축이 다르다. 시간 가지를 달력일로 제한하면
 * 어제 23:00 을 오늘 01:00 에 보며 "어제" 로 적어 정보를 잃고, 반대로 "어제" 를 24~48시간 구간으로 잡으면
 * 그저께 23:00 을 오늘 01:00 에 "어제" 로 적어 거짓이 된다. 그래서 경과는 순간 차로, 어제 판정은
 * 달력 날짜로만 구한다.
 *
 * 1분 미만 가지가 음수 경과까지 흡수하는 이유: 시각은 서버가, [now] 는 기기가 만들어 두 시계는 늘 어긋난다.
 * 흡수하지 않으면 "-3분 전" 이 그려진다.
 *
 * @param now 현재 시각(KST). 기본값은 [nowKst]. 테스트에서는 고정값 주입.
 */
fun LocalDateTime.toElapsedKoreanString(now: LocalDateTime = nowKst()): String {
    val elapsed: Duration = now.toInstant(KST) - this.toInstant(KST)

    if (elapsed < 1.minutes) return "방금 전"
    if (elapsed < 1.hours) return "${elapsed.inWholeMinutes}분 전"
    if (elapsed < 24.hours) return "${elapsed.inWholeHours}시간 전"
    if (this.date.daysUntil(now.date) == 1) return "어제"

    return "${this.date.monthNumber}월 ${this.date.day}일"
}
