@file:OptIn(ExperimentalTime::class)

package com.lwg.challenge.utils.datetime

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * KST 기준 마감 시각 표시 문자열. 예: "7/28 24:00"
 *
 * `24:00` 표기를 쓰는 이유: "오늘 자정"의 실제 순간은 익일 00:00 이라
 * "7/29 00:00" 으로 적으면 "오늘"인데 왜 내일 날짜냐는 혼란이 생긴다.
 * "7/28 24:00" 은 "7/28 이 끝나는 순간" 으로 한 번에 읽힌다.
 *
 * 표시 전용 — 실제 마감 환산의 authority 는 서버다(클라이언트는 timestamp 를 보내지 않는다).
 * 기기 시계가 틀리면 이 표시도 틀리지만 전송값은 enum 이라 저장되는 마감은 영향받지 않는다.
 *
 * @param daysAhead 0 = 오늘, 1 = 내일
 * @param now 현재 시각. 기본값은 [Clock.System.now]. 도메인 필드가 아니라 시계 판독값이라
 *   Instant 를 유지한다 — "도메인·UI 에 Instant 를 남기지 않는다" 규약과 충돌하지 않는다.
 */
fun kstDeadlineHintText(daysAhead: Int, now: Instant = Clock.System.now()): String {
    val date = now.toLocalDateTime(KST).date.plus(daysAhead, DateTimeUnit.DAY)
    return "${date.monthNumber}/${date.day} 24:00"
}
