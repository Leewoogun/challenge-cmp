@file:OptIn(ExperimentalTime::class)

package com.lwg.challenge.utils.datetime

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** 서비스 전역 표준 타임존. 서버의 모든 시간 값이 KST 기준이라 (api-contract §1.1) 한 곳에만 정의한다. */
val KST: TimeZone = TimeZone.of("Asia/Seoul")

fun nowKst(): LocalDateTime = Clock.System.now().toLocalDateTime(KST)
