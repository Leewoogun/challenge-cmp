package com.lwg.challenge.remote.mapper

import com.lwg.challenge.domain.model.challenge.ChallengeOutcome
import com.lwg.challenge.domain.model.challenge.ChallengeSide
import com.lwg.challenge.domain.model.challenge.SignatureSlotState
import com.lwg.challenge.domain.model.signature.Signature
import com.lwg.challenge.domain.model.signature.SignaturePoint
import com.lwg.challenge.domain.model.signature.SignatureStroke
import com.lwg.challenge.remote.model.challenge.ChallengeDetailData
import com.lwg.challenge.remote.model.challenge.ChallengeDetailResponse
import com.lwg.challenge.remote.model.challenge.ChallengeOutcomeDto
import com.lwg.challenge.remote.model.challenge.ChallengeSideDto
import com.lwg.challenge.remote.model.challenge.ContractDto
import com.lwg.challenge.remote.model.challenge.ContractPartyDto
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ChallengeDetailMapperTest {

    private val mySignature = Signature(
        listOf(SignatureStroke(listOf(SignaturePoint(123, 456), SignaturePoint(130, 460)))),
    )

    private val opponentSignature = Signature(
        listOf(SignatureStroke(listOf(SignaturePoint(700, 200), SignaturePoint(705, 210)))),
    )

    private val contract = ContractDto(
        content = "나는 나의 미션을 완수할 것을 맹세한다.",
        isFinalized = true,
        mySignature = SignatureCodec.encode(mySignature),
        mySignedAt = LocalDateTime(2026, 8, 3, 11, 20, 0),
        opponentSignature = SignatureCodec.encode(opponentSignature),
        opponentSignedAt = LocalDateTime(2026, 8, 3, 12, 5, 0),
    )

    private val data = ChallengeDetailData(
        challengeId = 7L,
        status = "IN_PROGRESS",
        challengeDate = LocalDate(2026, 8, 3),
        deadline = LocalDateTime(2026, 8, 4, 0, 0, 0),
        betContent = "커피 사기",
        me = ContractPartyDto(
            nickname = "이우건",
            profileImageUrl = null,
            mission = "운동 1시간",
        ),
        opponent = ContractPartyDto(
            nickname = "테스터1",
            profileImageUrl = "https://example.com/profile.jpg",
            mission = "책 30페이지",
        ),
        contract = contract,
    )

    private fun response(data: ChallengeDetailData = this.data) = ChallengeDetailResponse(data)

    @Test
    fun `양측 서명이 있는 정상 응답 - 모든 필드 매핑되고 두 슬롯 모두 SIGNED`() {
        val detail = assertNotNull(response().toChallengeDetail())
        val contract = assertNotNull(detail.contract)

        assertEquals(7L, detail.challengeId)
        assertEquals("IN_PROGRESS", detail.status)
        assertEquals(LocalDate(2026, 8, 3), detail.challengeDate)
        assertEquals(LocalDateTime(2026, 8, 4, 0, 0, 0), detail.deadline)
        assertEquals("커피 사기", detail.betContent)
        assertEquals("나는 나의 미션을 완수할 것을 맹세한다.", contract.content)
        assertEquals(true, contract.isFinalized)
        assertEquals(LocalDateTime(2026, 8, 3, 11, 20, 0), contract.mySignedAt)
        assertEquals(LocalDateTime(2026, 8, 3, 12, 5, 0), contract.opponentSignedAt)
        assertEquals(SignatureSlotState.SIGNED, contract.mySignatureState)
        assertEquals(SignatureSlotState.SIGNED, contract.opponentSignatureState)
    }

    @Test
    fun `상대 미서명 - opponent 슬롯은 NOT_SIGNED 이고 isFinalized false`() {
        val detail = assertNotNull(
            response(
                data.copy(
                    contract = contract.copy(
                        isFinalized = false,
                        opponentSignature = null,
                        opponentSignedAt = null,
                    ),
                ),
            ).toChallengeDetail(),
        )
        val mapped = assertNotNull(detail.contract)

        assertEquals(false, mapped.isFinalized)
        assertEquals(SignatureSlotState.NOT_SIGNED, mapped.opponentSignatureState)
        assertNull(mapped.opponentSignature)
        assertNull(mapped.opponentSignedAt)
        assertEquals(SignatureSlotState.SIGNED, mapped.mySignatureState)
    }

    @Test
    fun `서명 문자열이 깨진 경우 - UNREADABLE 이고 signedAt 은 보존된다`() {
        val detail = assertNotNull(
            response(
                data.copy(contract = contract.copy(opponentSignature = "not-a-signature")),
            ).toChallengeDetail(),
        )
        val mapped = assertNotNull(detail.contract)

        assertEquals(SignatureSlotState.UNREADABLE, mapped.opponentSignatureState)
        assertNull(mapped.opponentSignature)
        assertEquals(LocalDateTime(2026, 8, 3, 12, 5, 0), mapped.opponentSignedAt)
    }

    @Test
    fun `서명 문자열이 빈 문자열 - 깨진 경우와 같은 UNREADABLE 경로`() {
        val detail = assertNotNull(
            response(
                data.copy(contract = contract.copy(opponentSignature = "")),
            ).toChallengeDetail(),
        )
        val mapped = assertNotNull(detail.contract)

        assertEquals(SignatureSlotState.UNREADABLE, mapped.opponentSignatureState)
        assertEquals(LocalDateTime(2026, 8, 3, 12, 5, 0), mapped.opponentSignedAt)
    }

    @Test
    fun `deadline 이 null - 전체가 null`() {
        assertNull(response(data.copy(deadline = null)).toChallengeDetail())
    }

    @Test
    fun `challengeDate 만 null - 매핑 성공하고 challengeDate 는 null`() {
        val detail = assertNotNull(response(data.copy(challengeDate = null)).toChallengeDetail())

        assertNull(detail.challengeDate)
        assertEquals(LocalDateTime(2026, 8, 4, 0, 0, 0), detail.deadline)
    }

    @Test
    fun `서명 왕복 - encode 한 문자열이 원본 Signature 로 그대로 복원된다`() {
        val detail = assertNotNull(response().toChallengeDetail())
        val mapped = assertNotNull(detail.contract)

        assertEquals(mySignature, mapped.mySignature)
        assertEquals(opponentSignature, mapped.opponentSignature)
    }

    @Test
    fun `me 와 opponent 가 그대로 매핑된다`() {
        val detail = assertNotNull(response().toChallengeDetail())

        assertEquals("이우건", detail.me.nickname)
        assertEquals("운동 1시간", detail.me.mission)
        assertNull(detail.me.profileImageUrl)

        assertEquals("테스터1", detail.opponent.nickname)
        assertEquals("책 30페이지", detail.opponent.mission)
        assertEquals("https://example.com/profile.jpg", detail.opponent.profileImageUrl)
    }

    @Test
    fun `myResult 4종이 각각 대응 ChallengeOutcome 으로 매핑된다`() {
        val expected = mapOf(
            ChallengeOutcomeDto.WIN to ChallengeOutcome.WIN,
            ChallengeOutcomeDto.LOSE to ChallengeOutcome.LOSE,
            ChallengeOutcomeDto.DRAW to ChallengeOutcome.DRAW,
            ChallengeOutcomeDto.BOTH_LOSE to ChallengeOutcome.BOTH_LOSE,
        )

        expected.forEach { (dto, outcome) ->
            val detail = assertNotNull(
                response(data.copy(status = "COMPLETED", myResult = dto)).toChallengeDetail(),
            )

            assertEquals(outcome, detail.myResult)
        }
    }

    @Test
    fun `myResult 가 null - 도메인도 null 이다`() {
        val detail = assertNotNull(response(data.copy(myResult = null)).toChallengeDetail())

        assertNull(detail.myResult)
    }

    @Test
    fun `forfeitedBy ME 와 OPPONENT 가 ChallengeSide 로 매핑된다`() {
        val byMe = assertNotNull(
            response(data.copy(status = "COMPLETED", forfeitedBy = ChallengeSideDto.ME)).toChallengeDetail(),
        )
        val byOpponent = assertNotNull(
            response(data.copy(status = "COMPLETED", forfeitedBy = ChallengeSideDto.OPPONENT)).toChallengeDetail(),
        )

        assertEquals(ChallengeSide.ME, byMe.forfeitedBy)
        assertEquals(ChallengeSide.OPPONENT, byOpponent.forfeitedBy)
    }

    @Test
    fun `forfeitedBy 가 null - 도메인도 null 이다`() {
        val detail = assertNotNull(response().toChallengeDetail())

        assertNull(detail.forfeitedBy)
    }
}
