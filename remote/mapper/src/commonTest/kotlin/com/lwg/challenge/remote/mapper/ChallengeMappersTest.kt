package com.lwg.challenge.remote.mapper

import com.lwg.challenge.remote.model.challenge.ReceivedChallengeDto
import com.lwg.challenge.remote.model.challenge.ReceivedChallengesData
import com.lwg.challenge.remote.model.challenge.ReceivedChallengesResponse
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ChallengeMappersTest {

    private val dto = ReceivedChallengeDto(
        challengeId = 7L,
        challengerId = 42L,
        challengerNickname = "민수",
        challengerProfileImageUrl = "https://example.com/profile.jpg",
        challengerMission = "오늘 운동 1시간 하기",
        betContent = "커피 사기",
        challengeDate = LocalDate(2026, 7, 28),
        deadline = LocalDateTime(2026, 7, 28, 15, 4, 5),
        createdAt = LocalDateTime(2026, 7, 28, 2, 11, 0),
    )

    private fun response(vararg challenges: ReceivedChallengeDto) =
        ReceivedChallengesResponse(ReceivedChallengesData(challenges = challenges.toList()))

    @Test
    fun `정상 wire 포맷 - 필드가 도메인으로 정확히 매핑`() {
        val result = response(dto).toReceivedChallenges()

        assertEquals(0, result.droppedCount)
        val challenge = result.items.single()
        assertEquals(7L, challenge.challengeId)
        assertEquals(42L, challenge.challengerId)
        assertEquals("민수", challenge.challengerNickname)
        assertEquals("https://example.com/profile.jpg", challenge.challengerProfileImageUrl)
        assertEquals("오늘 운동 1시간 하기", challenge.challengerMission)
        assertEquals("커피 사기", challenge.betContent)
        assertEquals(LocalDateTime(2026, 7, 28, 15, 4, 5), challenge.deadline)
        assertEquals(LocalDateTime(2026, 7, 28, 2, 11, 0), challenge.createdAt)
    }

    @Test
    fun `deadline 이 null - 해당 항목 제외되고 droppedCount 1`() {
        val result = response(dto.copy(deadline = null)).toReceivedChallenges()

        assertEquals(emptyList(), result.items)
        assertEquals(1, result.droppedCount)
    }

    @Test
    fun `여러 건 중 deadline null 인 항목만 제외`() {
        val result = response(dto, dto.copy(deadline = null)).toReceivedChallenges()

        assertEquals(listOf(7L), result.items.map { it.challengeId })
        assertEquals(1, result.droppedCount)
    }

    @Test
    fun `createdAt 만 null - 항목은 유지되고 createdAt null`() {
        val result = response(dto.copy(createdAt = null)).toReceivedChallenges()

        assertEquals(0, result.droppedCount)
        val challenge = result.items.single()
        assertNull(challenge.createdAt)
        assertEquals(LocalDateTime(2026, 7, 28, 15, 4, 5), challenge.deadline)
    }

    @Test
    fun `challengerProfileImageUrl null - null 유지`() {
        val result = response(dto.copy(challengerProfileImageUrl = null)).toReceivedChallenges()

        assertNull(result.items.single().challengerProfileImageUrl)
    }

    @Test
    fun `빈 배열 - items 비고 droppedCount 0`() {
        val result = response().toReceivedChallenges()

        assertEquals(emptyList(), result.items)
        assertEquals(0, result.droppedCount)
    }
}
