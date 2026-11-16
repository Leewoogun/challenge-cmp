package com.lwg.challenge.utils.datetime

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number

/**
 * 마감 날짜 선택지 표시 문자열. 예: "11/20(금) 24:00"
 *
 * `24:00` 표기를 쓰는 이유: "오늘 자정"의 실제 순간은 익일 00:00 이라
 * "7/29 00:00" 으로 적으면 "오늘"인데 왜 내일 날짜냐는 혼란이 생긴다.
 * "7/28 24:00" 은 "7/28 이 끝나는 순간" 으로 한 번에 읽힌다.
 *
 * 요일은 고르는 순간의 방향감 용도라 생성 화면 전용이다 — 조회 화면은 [endOfDayText] 를 쓴다.
 */
fun kstDeadlineHintText(date: LocalDate): String =
    "${date.month.number}/${date.day}(${date.dayOfWeek.koreanLabel}) 24:00"

val DayOfWeek.koreanLabel: String
    get() = when (this) {
        DayOfWeek.MONDAY -> "월"
        DayOfWeek.TUESDAY -> "화"
        DayOfWeek.WEDNESDAY -> "수"
        DayOfWeek.THURSDAY -> "목"
        DayOfWeek.FRIDAY -> "금"
        DayOfWeek.SATURDAY -> "토"
        DayOfWeek.SUNDAY -> "일"
    }
