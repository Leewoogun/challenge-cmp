package com.lwg.challenge.remote.mapper

import com.lwg.challenge.domain.model.VerificationStatus
import com.lwg.challenge.remote.model.challenge.ActiveChallengeDto
import com.lwg.challenge.remote.model.challenge.ActiveChallengeListData
import com.lwg.challenge.remote.model.challenge.ActiveChallengeResponse
import com.lwg.challenge.remote.model.challenge.VerificationStatusDto
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class ActiveChallengeResponseMapperTest {

    private val dto = ActiveChallengeDto(
        challengeId = 1001L,
        myMission = "오늘 운동 1시간 하기",
        opponentNickname = "민수",
        opponentMission = "책 30페이지 읽기",
        deadline = LocalDateTime(2026, 7, 28, 15, 4, 5),
        myVerificationStatus = VerificationStatusDto.PENDING,
        opponentVerificationStatus = VerificationStatusDto.VERIFIED,
        bet = "커피 사기 ☕",
    )

    private fun response(vararg challenges: ActiveChallengeDto) =
        ActiveChallengeResponse(ActiveChallengeListData(activeChallenges = challenges.toList()))

    @Test
    fun `정상 wire 포맷 - 필드가 도메인으로 정확히 매핑`() {
        val result = response(dto).toActiveChallenges()

        assertEquals(0, result.droppedCount)
        val challenge = result.items.single()
        assertEquals(1001L, challenge.challengeId)
        assertEquals("오늘 운동 1시간 하기", challenge.myMission)
        assertEquals("민수", challenge.opponentNickname)
        assertEquals("책 30페이지 읽기", challenge.opponentMission)
        assertEquals(LocalDateTime(2026, 7, 28, 15, 4, 5), challenge.deadline)
        assertEquals(VerificationStatus.PENDING, challenge.myVerificationStatus)
        assertEquals(VerificationStatus.VERIFIED, challenge.opponentVerificationStatus)
        assertEquals("커피 사기 ☕", challenge.bet)
    }

    @Test
    fun `deadline null - 해당 항목 제외되고 droppedCount 집계`() {
        val result = response(
            dto,
            dto.copy(challengeId = 1002L, deadline = null),
        ).toActiveChallenges()

        assertEquals(listOf(1001L), result.items.map { it.challengeId })
        assertEquals(1, result.droppedCount)
    }

    @Test
    fun `VerificationStatusDto - 도메인 3종 모두 매핑`() {
        val result = response(
            dto.copy(myVerificationStatus = VerificationStatusDto.PENDING, opponentVerificationStatus = VerificationStatusDto.VERIFIED),
            dto.copy(myVerificationStatus = VerificationStatusDto.FAILED, opponentVerificationStatus = VerificationStatusDto.PENDING),
        ).toActiveChallenges()

        assertEquals(
            listOf(VerificationStatus.PENDING, VerificationStatus.FAILED),
            result.items.map { it.myVerificationStatus },
        )
        assertEquals(
            listOf(VerificationStatus.VERIFIED, VerificationStatus.PENDING),
            result.items.map { it.opponentVerificationStatus },
        )
    }
}
