package com.lwg.challenge.remote.mapper

import com.lwg.challenge.domain.model.challenge.ChallengeResult
import com.lwg.challenge.domain.model.challenge.SignatureSlotState
import com.lwg.challenge.domain.model.signature.Signature
import com.lwg.challenge.domain.model.signature.SignaturePoint
import com.lwg.challenge.domain.model.signature.SignatureStroke
import com.lwg.challenge.remote.model.challenge.ChallengeDetailData
import com.lwg.challenge.remote.model.challenge.ChallengeDetailResponse
import com.lwg.challenge.remote.model.challenge.ContractDto
import com.lwg.challenge.remote.model.challenge.ContractPartyDto
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ChallengeDetailMapperTest {

    private val challengerSignature = Signature(
        listOf(SignatureStroke(listOf(SignaturePoint(123, 456), SignaturePoint(130, 460)))),
    )

    private val opponentSignature = Signature(
        listOf(SignatureStroke(listOf(SignaturePoint(700, 200), SignaturePoint(705, 210)))),
    )

    private val contract = ContractDto(
        content = "나는 나의 미션을 완수할 것을 맹세한다.",
        isFinalized = true,
        challengerSignature = SignatureCodec.encode(challengerSignature),
        challengerSignedAt = LocalDateTime(2026, 8, 3, 11, 20, 0),
        opponentSignature = SignatureCodec.encode(opponentSignature),
        opponentSignedAt = LocalDateTime(2026, 8, 3, 12, 5, 0),
    )

    private val data = ChallengeDetailData(
        challengeId = 7L,
        status = "IN_PROGRESS",
        challengeDate = LocalDate(2026, 8, 3),
        deadline = LocalDateTime(2026, 8, 4, 0, 0, 0),
        betContent = "커피 사기",
        challenger = ContractPartyDto(
            userId = 1L,
            nickname = "이우건",
            profileImageUrl = null,
            mission = "운동 1시간",
        ),
        opponent = ContractPartyDto(
            userId = 14L,
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
        assertEquals(LocalDateTime(2026, 8, 3, 11, 20, 0), contract.challengerSignedAt)
        assertEquals(LocalDateTime(2026, 8, 3, 12, 5, 0), contract.opponentSignedAt)
        assertEquals(SignatureSlotState.SIGNED, contract.challengerSignatureState)
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
        assertEquals(SignatureSlotState.SIGNED, mapped.challengerSignatureState)
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

        assertEquals(challengerSignature, mapped.challengerSignature)
        assertEquals(opponentSignature, mapped.opponentSignature)
    }

    @Test
    fun `challenger 와 opponent 는 역할 그대로 매핑된다`() {
        val detail = assertNotNull(response().toChallengeDetail())

        assertEquals(1L, detail.challenger.userId)
        assertEquals("이우건", detail.challenger.nickname)
        assertEquals("운동 1시간", detail.challenger.mission)
        assertNull(detail.challenger.profileImageUrl)

        assertEquals(14L, detail.opponent.userId)
        assertEquals("테스터1", detail.opponent.nickname)
        assertEquals("책 30페이지", detail.opponent.mission)
        assertEquals("https://example.com/profile.jpg", detail.opponent.profileImageUrl)
    }

    @Test
    fun `result 4종 문자열이 각각 대응 ChallengeResult 로 매핑된다`() {
        val expected = mapOf(
            "CHALLENGER_WIN" to ChallengeResult.CHALLENGER_WIN,
            "OPPONENT_WIN" to ChallengeResult.OPPONENT_WIN,
            "DRAW" to ChallengeResult.DRAW,
            "BOTH_LOSE" to ChallengeResult.BOTH_LOSE,
        )

        expected.forEach { (raw, result) ->
            val detail = assertNotNull(
                response(data.copy(status = "COMPLETED", result = raw)).toChallengeDetail(),
            )

            assertEquals(result, detail.result, "'$raw' 이 $result 로 매핑돼야 한다")
        }
    }

    @Test
    fun `result 가 null - 도메인도 null 이다`() {
        val detail = assertNotNull(response(data.copy(result = null)).toChallengeDetail())

        assertNull(detail.result, "COMPLETED 이전에는 판정 결과가 없다")
    }

    @Test
    fun `forfeitedBy 가 값 그대로 통과한다`() {
        val detail = assertNotNull(
            response(
                data.copy(status = "COMPLETED", result = "OPPONENT_WIN", forfeitedBy = 1L),
            ).toChallengeDetail(),
        )

        assertEquals(
            1L,
            detail.forfeitedBy,
            "매퍼가 판단할 것이 없다. userId → 역할 환산은 내 userId 를 아는 화면 계층의 몫이다",
        )
    }

    @Test
    fun `forfeitedBy 가 null - 도메인도 null 이다`() {
        val detail = assertNotNull(response().toChallengeDetail())

        assertNull(detail.forfeitedBy, "기권 없이 끝났거나 아직 끝나지 않았다")
    }

    @Test
    fun `모르는 result 문자열 - null 로 흡수되고 나머지 필드는 정상 매핑된다`() {
        val detail = assertNotNull(
            response(data.copy(status = "COMPLETED", result = "CANCELLED")).toChallengeDetail(),
            "모르는 결과값 하나가 상세 매핑 전체를 무너뜨리면 안 된다",
        )

        assertNull(
            detail.result,
            "모르는 결과를 아무 결과로 날조하는 것보다 '결과 없음'이 보수적이다",
        )
        assertEquals(7L, detail.challengeId)
        assertEquals("COMPLETED", detail.status)
        assertEquals("커피 사기", detail.betContent)
        assertEquals(LocalDate(2026, 8, 3), detail.challengeDate)
        assertEquals(LocalDateTime(2026, 8, 4, 0, 0, 0), detail.deadline)
        assertEquals("이우건", detail.challenger.nickname)
        assertEquals("테스터1", detail.opponent.nickname)
        assertNotNull(detail.contract)
    }
}
