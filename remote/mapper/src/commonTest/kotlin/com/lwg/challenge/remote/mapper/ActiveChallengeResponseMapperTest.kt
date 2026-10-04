package com.lwg.challenge.remote.mapper

import com.lwg.challenge.domain.model.ActiveChallengeStatus
import com.lwg.challenge.domain.model.VerificationStatus
import com.lwg.challenge.domain.model.challenge.ChallengeOutcome
import com.lwg.challenge.remote.model.challenge.ActiveChallengeDto
import com.lwg.challenge.remote.model.challenge.ActiveChallengeListData
import com.lwg.challenge.remote.model.challenge.ActiveChallengeResponse
import com.lwg.challenge.remote.model.challenge.ActiveChallengeStatusDto
import com.lwg.challenge.remote.model.challenge.ChallengeOutcomeDto
import com.lwg.challenge.remote.model.challenge.VerificationStatusDto
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

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
        status = ActiveChallengeStatusDto.IN_PROGRESS,
        myResult = null,
    )

    private val completedDto = dto.copy(
        challengeId = 2001L,
        status = ActiveChallengeStatusDto.COMPLETED,
        myResult = ChallengeOutcomeDto.WIN,
    )

    private fun response(vararg challenges: ActiveChallengeDto) =
        ActiveChallengeResponse(ActiveChallengeListData(activeChallenges = challenges.toList()))

    @Test
    fun `정상 wire 포맷 - 필드가 도메인으로 정확히 매핑`() {
        val result = response(dto).toActiveChallenges()
        val challenge = result.single()
        assertEquals(1001L, challenge.challengeId)
        assertEquals("오늘 운동 1시간 하기", challenge.me.mission)
        assertEquals("민수", challenge.opponent.nickname)
        assertEquals("책 30페이지 읽기", challenge.opponent.mission)
        assertEquals(LocalDateTime(2026, 7, 28, 15, 4, 5), challenge.deadline)
        assertEquals(VerificationStatus.PENDING, challenge.me.verificationStatus)
        assertEquals(VerificationStatus.VERIFIED, challenge.opponent.verificationStatus)
        assertEquals("커피 사기 ☕", challenge.penalty)
        assertEquals(ActiveChallengeStatus.IN_PROGRESS, challenge.status)
        assertNull(challenge.outcome)
    }

    @Test
    fun `IN_PROGRESS - status 는 IN_PROGRESS 이고 outcome 은 null`() {
        val result = response(dto).toActiveChallenges()

        val challenge = result.single()
        assertEquals(ActiveChallengeStatus.IN_PROGRESS, challenge.status)
        assertNull(challenge.outcome)
    }

    @Test
    fun `COMPLETED + myResult WIN - status 는 COMPLETED 이고 outcome 은 WIN`() {
        val result = response(completedDto).toActiveChallenges()

        val challenge = result.single()
        assertEquals(ActiveChallengeStatus.COMPLETED, challenge.status)
        assertEquals(ChallengeOutcome.WIN, challenge.outcome)
    }

    @Test
    fun `myResult 4값 - 각각 대응하는 ChallengeOutcome 으로 매핑`() {
        val result = response(
            completedDto.copy(myResult = ChallengeOutcomeDto.WIN),
            completedDto.copy(myResult = ChallengeOutcomeDto.LOSE),
            completedDto.copy(myResult = ChallengeOutcomeDto.DRAW),
            completedDto.copy(myResult = ChallengeOutcomeDto.BOTH_LOSE),
        ).toActiveChallenges()

        assertEquals(
            listOf(
                ChallengeOutcome.WIN,
                ChallengeOutcome.LOSE,
                ChallengeOutcome.DRAW,
                ChallengeOutcome.BOTH_LOSE,
            ),
            result.map { it.outcome },
        )
    }

    @Test
    fun `진행 중과 판정 완료가 섞여도 서버가 준 순서가 보존된다`() {
        val result = response(
            dto.copy(challengeId = 1L, status = ActiveChallengeStatusDto.IN_PROGRESS),
            dto.copy(challengeId = 2L, status = ActiveChallengeStatusDto.IN_PROGRESS),
            completedDto.copy(challengeId = 3L, myResult = ChallengeOutcomeDto.LOSE),
            completedDto.copy(challengeId = 4L, myResult = ChallengeOutcomeDto.DRAW),
        ).toActiveChallenges()

        assertEquals(listOf(1L, 2L, 3L, 4L), result.map { it.challengeId })
        assertEquals(
            listOf(
                ActiveChallengeStatus.IN_PROGRESS,
                ActiveChallengeStatus.IN_PROGRESS,
                ActiveChallengeStatus.COMPLETED,
                ActiveChallengeStatus.COMPLETED,
            ),
            result.map { it.status },
        )
    }

    @Test
    fun `deadline null - 해당 항목 제외`() {
        val result = response(
            dto,
            dto.copy(challengeId = 1002L, deadline = null),
        ).toActiveChallenges()

        assertEquals(listOf(1001L), result.map { it.challengeId })
    }

    @Test
    fun `VerificationStatusDto - 도메인 3종 모두 매핑`() {
        val result = response(
            dto.copy(myVerificationStatus = VerificationStatusDto.PENDING, opponentVerificationStatus = VerificationStatusDto.VERIFIED),
            dto.copy(myVerificationStatus = VerificationStatusDto.FAILED, opponentVerificationStatus = VerificationStatusDto.PENDING),
        ).toActiveChallenges()

        assertEquals(
            listOf(VerificationStatus.PENDING, VerificationStatus.FAILED),
            result.map { it.me.verificationStatus },
        )
        assertEquals(
            listOf(VerificationStatus.VERIFIED, VerificationStatus.PENDING),
            result.map { it.opponent.verificationStatus },
        )
    }
}
