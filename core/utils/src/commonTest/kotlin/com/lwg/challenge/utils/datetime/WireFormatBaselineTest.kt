@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.lwg.challenge.utils.datetime

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

/**
 * datetime-model-migration spec 의 baseline 대조를 자동화한 테스트.
 *
 * 마이그레이션 전(`Instant`, UTC 표기 `2026-07-31T15:00:00Z`)과
 * 마이그레이션 후(`LocalDateTime`, KST 표기 `2026-08-01 00:00:00`)는
 * **같은 순간의 다른 표기**여야 한다. 이 테스트가 실패하면 실제 시각이
 * 이동한 것이므로 마이그레이션을 중단하고 원인을 찾아야 한다.
 */
class WireFormatBaselineTest {

    @Test
    fun `새 KST 표기를 파싱해 순간으로 환산하면 옛 UTC 표기와 같은 순간이다`() {
        val migrated = ChallengeDateTimeFormats.parseWireDateTime("2026-08-01 00:00:00")

        assertNotNull(migrated)
        assertEquals(Instant.parse("2026-07-31T15:00:00Z"), migrated.toInstant(KST))
    }

    @Test
    fun `남은 시간이 옛 방식(Instant 차)과 새 방식(LocalDateTime 차)에서 같다`() {
        val nowInstant = Instant.parse("2026-07-31T14:00:00Z")
        val nowKst = LocalDateTime(2026, 7, 31, 23, 0, 0)
        val oldDeadline = Instant.parse("2026-07-31T15:00:00Z")
        val newDeadline = ChallengeDateTimeFormats.parseWireDateTime("2026-08-01 00:00:00")

        assertNotNull(newDeadline)
        val oldRemaining = oldDeadline - nowInstant
        val newRemaining = newDeadline.toInstant(KST) - nowKst.toInstant(KST)

        assertEquals(1.hours, oldRemaining)
        assertEquals(oldRemaining, newRemaining)
    }

    @Test
    fun `상대 시간 텍스트가 마이그레이션 전 Instant 기반 구현과 같은 값을 낸다`() {
        val nowKst = LocalDateTime(2026, 7, 31, 23, 0, 0)
        val newDeadline = ChallengeDateTimeFormats.parseWireDateTime("2026-08-01 00:00:00")

        assertNotNull(newDeadline)
        assertEquals("1시간", newDeadline.toRelativeKoreanString(nowKst))
    }

    @Test
    fun `KST 는 DST 없는 고정 UTC+9 오프셋이다`() {
        val kstNewYear = LocalDateTime(2026, 1, 1, 0, 0, 0)

        assertEquals(Instant.parse("2025-12-31T15:00:00Z"), kstNewYear.toInstant(KST))
    }
}
