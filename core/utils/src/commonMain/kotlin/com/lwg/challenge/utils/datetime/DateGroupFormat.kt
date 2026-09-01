package com.lwg.challenge.utils.datetime

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.daysUntil

/**
 * 날짜를 목록 그룹 헤더 문구로 변환. "오늘" / "어제" / "M월 d일" (0 패딩 없음).
 *
 * 🔴 [toElapsedKoreanString] 과 어휘("어제"·"M월 d일")가 겹치지만 **축이 다르다.**
 * 이쪽은 **달력**만 본다 — 두 날짜가 며칠 떨어졌는지가 전부다. 저쪽은 **경과**를 먼저 보고 24시간
 * 미만이면 "N시간 전" 으로 답한다. 그래서 어제 23:00 을 오늘 01:00 에 보면 이 함수는 "어제",
 * 저 함수는 "2시간 전" 이고 **둘 다 맞다.** 한쪽으로 합치면 합쳐진 쪽의 축이 무너진다.
 *
 * 미래 날짜(서버·기기 시계 어긋남)는 "오늘" 도 "어제" 도 아닌 날짜 표기로 떨어진다 — 달력상 다른
 * 날인 것이 사실이라 거짓말이 되지 않는다.
 *
 * @param today 그룹을 가르는 기준 날짜. 목록 진입 시각의 날짜를 넘긴다.
 */
fun LocalDate.toDateGroupLabel(today: LocalDate): String = when (daysUntil(today)) {
    0 -> "오늘"
    1 -> "어제"
    else -> "${monthNumber}월 ${day}일"
}

/**
 * 시각을 `HH:mm` 로 (예: "20:05", "09:00"). 24시간제, 0 패딩 있음.
 *
 * 날짜 그룹 헤더가 달력을 맡은 목록에서 행이 쓰는 표기다 — 헤더가 이미 "어제" 라고 말했으면 행은
 * 그 날 안에서 언제인지만 말하면 된다.
 */
fun LocalDateTime.toClockTimeText(): String {
    val hourText = hour.toString().padStart(2, '0')
    val minuteText = minute.toString().padStart(2, '0')

    return "$hourText:$minuteText"
}
