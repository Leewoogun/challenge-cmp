@file:OptIn(ExperimentalTime::class)

package com.lwg.challenge.utils.datetime

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private const val SECONDS_PER_DAY = 86_400L
private const val KST_OFFSET_SECONDS = 9 * 3600L

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
 * @param now 현재 시각. 기본값은 [Clock.System.now()]. 테스트에서는 고정 Instant 주입.
 */
fun kstDeadlineHintText(daysAhead: Int, now: Instant = Clock.System.now()): String {
    // KST 는 DST 없는 고정 UTC+9 라 오프셋 덧셈만으로 정확하다 — kotlinx-datetime 의존을 피한다.
    val kstEpochDay = floorDiv(now.epochSeconds + KST_OFFSET_SECONDS, SECONDS_PER_DAY)
    val (_, month, day) = civilFromEpochDay(kstEpochDay + daysAhead)
    return "$month/$day 24:00"
}

/** 1970 이전(음수 epochSeconds)에서도 날짜가 하루 밀리지 않도록 0 방향 절삭 대신 floor 절삭을 쓴다. */
private fun floorDiv(a: Long, b: Long): Long {
    val q = a / b
    return if (a % b != 0L && ((a xor b) < 0)) q - 1 else q
}

/**
 * Howard Hinnant 의 `civil_from_days` (public domain). epochDay(1970-01-01 = 0) → (year, month, day).
 *
 * 3월을 연초로 보는 좌표계라 윤일이 연말에 오고, 윤년 분기 없이 정수 연산만으로 환산된다.
 * 정수 나눗셈이 0 방향 절삭이므로 `doe` 가 항상 비음수여야 성립한다 — [floorDiv] 로 구한 `era` 가 이를 보장한다.
 */
private fun civilFromEpochDay(epochDay: Long): Triple<Long, Int, Int> {
    val z = epochDay + 719468
    val era = floorDiv(z, 146097L)
    val doe = z - era * 146097                                      // [0, 146096]
    val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365 // [0, 399]
    val y = yoe + era * 400
    val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)               // [0, 365]
    val mp = (5 * doy + 2) / 153                                    // [0, 11]
    val d = (doy - (153 * mp + 2) / 5 + 1).toInt()                  // [1, 31]
    val m = (mp + if (mp < 10) 3 else -9).toInt()                   // [1, 12]
    return Triple(if (m <= 2) y + 1 else y, m, d)
}
