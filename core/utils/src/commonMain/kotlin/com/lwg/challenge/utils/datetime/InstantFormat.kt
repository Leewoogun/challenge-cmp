@file:OptIn(ExperimentalTime::class)

package com.lwg.challenge.utils.datetime

import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * 챌린지 마감 시각(deadline)을 한국어 상대 시간 텍스트로 변환.
 *
 * 카드 UI 의 우상단 "5시간 32분" 같은 표기에 사용한다.
 * - 1시간 이상 남음: "X시간 Y분"
 * - 1분 ~ 1시간: "X분"
 * - 1분 미만(양수): "곧 마감"
 * - 이미 지난 시각(또는 0): "마감"
 *
 * @param now 현재 시각. 기본값은 [Clock.System.now()]. 테스트에서는 고정 Instant 주입.
 *
 * 주의: Kotlin 2.2 부터 `kotlin.time.Instant` / `kotlin.time.Clock` 이 신설되었고
 * `kotlinx.datetime.Instant` 는 `kotlin.time.Instant` 의 typealias 로 통합됨.
 * 둘 다 [ExperimentalTime] 마커를 요구해 파일 단위로 @OptIn 처리.
 */
fun Instant.toRelativeKoreanString(now: Instant = Clock.System.now()): String {
    val remaining: Duration = this - now

    if (remaining <= 0.seconds) return "마감"
    if (remaining < 1.minutes) return "곧 마감"

    val totalMinutes = remaining.inWholeMinutes
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60

    return when {
        hours <= 0L -> "${minutes}분"
        minutes == 0L -> "${hours}시간"
        else -> "${hours}시간 ${minutes}분"
    }
}
