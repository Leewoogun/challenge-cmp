package com.lwg.challenge.remote.mapper

import com.lwg.challenge.domain.model.ActiveChallengeStatus
import com.lwg.challenge.domain.model.VerificationStatus
import com.lwg.challenge.domain.model.challenge.ChallengeOutcome
import com.lwg.challenge.remote.model.challenge.ActiveChallengeDto
import com.lwg.challenge.remote.model.challenge.ActiveChallengeListData
import com.lwg.challenge.remote.model.challenge.ActiveChallengeResponse
import com.lwg.challenge.remote.model.challenge.VerificationStatusDto
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
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
        status = "IN_PROGRESS",
        myResult = null,
    )

    private val completedDto = dto.copy(
        challengeId = 2001L,
        status = "COMPLETED",
        myResult = "WIN",
    )

    private fun response(vararg challenges: ActiveChallengeDto) =
        ActiveChallengeResponse(ActiveChallengeListData(activeChallenges = challenges.toList()))

    // KtorfitModule.provideJson() 과 같은 옵션. 실제 응답 파싱과 조건이 어긋나면
    // 이 테스트가 통과해도 런타임 파싱을 보장하지 못한다.
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        prettyPrint = true
    }

    // status/myResult 가 배포되기 전 서버 응답 — 키 자체가 없다. 이 시절 목록은 정의상 전부 진행 중이었다.
    private val legacyWireWithoutStatus =
        """{"error":false,"code":200,"message":"","data":{"activeChallenges":[""" +
            """{"challengeId":1001,"myMission":"오늘 운동 1시간 하기","opponentNickname":"민수","opponentMission":"책 30페이지 읽기","deadline":"2026-07-28 15:04:05","myVerificationStatus":"PENDING","opponentVerificationStatus":"VERIFIED","bet":"커피 사기"},""" +
            """{"challengeId":1002,"myMission":"물 2L 마시기","opponentNickname":"지영","opponentMission":"산책 30분","deadline":"2026-07-29 23:59:59","myVerificationStatus":"PENDING","opponentVerificationStatus":"PENDING","bet":"점심 사기"}""" +
            """]}}"""

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
        assertEquals(ActiveChallengeStatus.IN_PROGRESS, challenge.status)
        assertNull(challenge.outcome)
    }

    @Test
    fun `IN_PROGRESS - status 는 IN_PROGRESS 이고 outcome 은 null`() {
        val result = response(dto).toActiveChallenges()

        val challenge = result.items.single()
        assertEquals(ActiveChallengeStatus.IN_PROGRESS, challenge.status)
        assertNull(challenge.outcome)
    }

    @Test
    fun `COMPLETED + myResult WIN - status 는 COMPLETED 이고 outcome 은 WIN`() {
        val result = response(completedDto).toActiveChallenges()

        val challenge = result.items.single()
        assertEquals(ActiveChallengeStatus.COMPLETED, challenge.status)
        assertEquals(ChallengeOutcome.WIN, challenge.outcome)
    }

    @Test
    fun `myResult 4값 - 각각 대응하는 ChallengeOutcome 으로 매핑`() {
        val result = response(
            completedDto.copy(myResult = "WIN"),
            completedDto.copy(myResult = "LOSE"),
            completedDto.copy(myResult = "DRAW"),
            completedDto.copy(myResult = "BOTH_LOSE"),
        ).toActiveChallenges()

        assertEquals(
            listOf(
                ChallengeOutcome.WIN,
                ChallengeOutcome.LOSE,
                ChallengeOutcome.DRAW,
                ChallengeOutcome.BOTH_LOSE,
            ),
            result.items.map { it.outcome },
        )
    }

    @Test
    fun `모르는 myResult - outcome 만 null 이고 항목은 드롭되지 않는다`() {
        val result = response(completedDto.copy(myResult = "SOMETHING_NEW")).toActiveChallenges()

        assertEquals(0, result.droppedCount)
        val challenge = result.items.single()
        assertNull(challenge.outcome)
        assertEquals(2001L, challenge.challengeId)
        assertEquals(ActiveChallengeStatus.COMPLETED, challenge.status)
        assertEquals("오늘 운동 1시간 하기", challenge.myMission)
        assertEquals("민수", challenge.opponentNickname)
        assertEquals("책 30페이지 읽기", challenge.opponentMission)
        assertEquals(LocalDateTime(2026, 7, 28, 15, 4, 5), challenge.deadline)
        assertEquals(VerificationStatus.PENDING, challenge.myVerificationStatus)
        assertEquals(VerificationStatus.VERIFIED, challenge.opponentVerificationStatus)
        assertEquals("커피 사기 ☕", challenge.bet)
    }

    @Test
    fun `모르는 status - 해당 항목 제외되고 droppedCount 집계`() {
        val result = response(
            dto,
            dto.copy(challengeId = 1002L, status = "EXPIRED"),
        ).toActiveChallenges()

        assertEquals(listOf(1001L), result.items.map { it.challengeId })
        assertEquals(1, result.droppedCount)
    }

    @Test
    fun `CANCELLED 도 모르는 값 - 제외되고 droppedCount 에 누적`() {
        val result = response(
            dto,
            dto.copy(challengeId = 1003L, status = "CANCELLED"),
            dto.copy(challengeId = 1004L, status = "ARCHIVED"),
        ).toActiveChallenges()

        assertEquals(listOf(1001L), result.items.map { it.challengeId })
        assertEquals(2, result.droppedCount)
    }

    @Test
    fun `빈 status - 드롭되지 않고 IN_PROGRESS 로 매핑`() {
        val result = response(dto.copy(challengeId = 1005L, status = "")).toActiveChallenges()

        assertEquals(0, result.droppedCount)
        val challenge = result.items.single()
        assertEquals(1005L, challenge.challengeId)
        assertEquals(ActiveChallengeStatus.IN_PROGRESS, challenge.status)
        assertNull(challenge.outcome)
    }

    @Test
    fun `status 키가 없는 구버전 응답 - 목록이 비지 않고 전부 IN_PROGRESS`() {
        val result = json.decodeFromString<ActiveChallengeResponse>(legacyWireWithoutStatus).toActiveChallenges()

        assertEquals(0, result.droppedCount)
        assertEquals(listOf(1001L, 1002L), result.items.map { it.challengeId })
        assertEquals(
            listOf(ActiveChallengeStatus.IN_PROGRESS, ActiveChallengeStatus.IN_PROGRESS),
            result.items.map { it.status },
        )
        assertEquals(listOf(null, null), result.items.map { it.outcome })
    }

    @Test
    fun `진행 중과 판정 완료가 섞여도 서버가 준 순서가 보존된다`() {
        val result = response(
            dto.copy(challengeId = 1L, status = "IN_PROGRESS"),
            dto.copy(challengeId = 2L, status = "IN_PROGRESS"),
            completedDto.copy(challengeId = 3L, myResult = "LOSE"),
            completedDto.copy(challengeId = 4L, myResult = "DRAW"),
        ).toActiveChallenges()

        assertEquals(listOf(1L, 2L, 3L, 4L), result.items.map { it.challengeId })
        assertEquals(
            listOf(
                ActiveChallengeStatus.IN_PROGRESS,
                ActiveChallengeStatus.IN_PROGRESS,
                ActiveChallengeStatus.COMPLETED,
                ActiveChallengeStatus.COMPLETED,
            ),
            result.items.map { it.status },
        )
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
