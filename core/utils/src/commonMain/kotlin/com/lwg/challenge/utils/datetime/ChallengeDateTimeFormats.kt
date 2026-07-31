package com.lwg.challenge.utils.datetime

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.char

/**
 * 서버 wire 포맷 파싱/포맷 (datetime-model-migration api-contract §1.1).
 *
 * 모든 시간 값은 KST 기준이며 표기에 타임존 정보가 없다.
 */
object ChallengeDateTimeFormats {

    /** `yyyy-MM-dd HH:mm:ss` — 구분자가 `T` 가 아니라 공백이라(ISO 아님) 기본 파서로 읽을 수 없다. */
    val WIRE_DATE_TIME: DateTimeFormat<LocalDateTime> = LocalDateTime.Format {
        year(); char('-'); monthNumber(); char('-'); day()
        char(' ')
        hour(); char(':'); minute(); char(':'); second()
    }

    /** 실패 시 예외 대신 null — repository 가 null 로 항목을 거른다 (api-contract §3). */
    fun parseWireDateTime(text: String): LocalDateTime? = WIRE_DATE_TIME.parseOrNull(text)

    /** 날짜 전용 `yyyy-MM-dd` 는 ISO date 와 동일해 기본 포맷으로 충분하다. 실패 시 null. */
    fun parseWireDate(text: String): LocalDate? = LocalDate.Formats.ISO.parseOrNull(text)
}
