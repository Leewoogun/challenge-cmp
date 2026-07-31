@file:OptIn(ExperimentalTime::class)

package com.lwg.challenge.utils.datetime

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toInstant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

/**
 * 챌린지 마감 시각(deadline)을 한국어 상대 시간 텍스트로 변환.
 *
 * 카드 UI 의 우상단 "5시간 32분" 같은 표기에 사용한다.
 * - 1시간 이상 남음: "X시간 Y분" (정각이면 "X시간")
 * - 1분 ~ 1시간: "X분"
 * - 1분 미만(양수): "곧 마감"
 * - 이미 지난 시각(또는 0): "마감"
 *
 * @param now 현재 시각(KST). 기본값은 [nowKst]. 테스트에서는 고정값 주입.
 */
fun LocalDateTime.toRelativeKoreanString(now: LocalDateTime = nowKst()): String {
    // LocalDateTime 끼리는 직접 뺄 수 없어 KST 순간으로 환산해 간격을 구한다.
    val remaining: Duration = this.toInstant(KST) - now.toInstant(KST)

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
