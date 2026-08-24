package com.lwg.challenge.remote.mapper

import com.lwg.challenge.domain.model.VerificationStatus
import com.lwg.challenge.remote.model.verification.ChallengeVerificationsData
import com.lwg.challenge.remote.model.verification.ChallengeVerificationsResponse
import com.lwg.challenge.remote.model.verification.PartyVerificationDto
import com.lwg.challenge.remote.model.verification.SubmitVerificationData
import com.lwg.challenge.remote.model.verification.SubmitVerificationResponse
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class VerificationMappersTest {

    private val verifiedParty = PartyVerificationDto(
        userId = 1L,
        status = "VERIFIED",
        photoUrl = "/api/v1/challenges/7/photos/challenger",
        verifiedAt = LocalDateTime(2026, 8, 14, 15, 32, 10),
    )

    @Test
    fun `제출 확정 응답이 도메인으로 매핑된다`() {
        val result = SubmitVerificationResponse(
            SubmitVerificationData(
                challengeId = 7L,
                status = "VERIFIED",
                verifiedAt = LocalDateTime(2026, 8, 14, 15, 32, 10),
            ),
        ).toSubmitResult()

        assertEquals(7L, result.challengeId)
        assertEquals(VerificationStatus.VERIFIED, result.status)
        assertEquals(LocalDateTime(2026, 8, 14, 15, 32, 10), result.verifiedAt)
    }

    @Test
    fun `제출 확정의 verifiedAt null 은 그대로 통과한다`() {
        val result = SubmitVerificationResponse(
            SubmitVerificationData(challengeId = 7L, status = "VERIFIED", verifiedAt = null),
        ).toSubmitResult()

        assertEquals(VerificationStatus.VERIFIED, result.status)
        assertNull(result.verifiedAt, "도메인 계약이 nullable 로 명시한 값이라 매핑 실패로 승격하지 않는다")
    }

    @Test
    fun `양측 현황이 그대로 매핑된다`() {
        val verifications = ChallengeVerificationsResponse(
            ChallengeVerificationsData(
                challengeId = 7L,
                challenger = verifiedParty,
                opponent = PartyVerificationDto(userId = 14L, status = "PENDING"),
            ),
        ).toChallengeVerifications()

        assertEquals(verifiedParty.photoUrl, verifications.challenger.photoUrl)
        assertEquals(LocalDateTime(2026, 8, 14, 15, 32, 10), verifications.challenger.verifiedAt)
        assertEquals(1L, verifications.challenger.userId)
        assertEquals(14L, verifications.opponent.userId)
        assertNull(verifications.opponent.photoUrl)
    }

    @Test
    fun `status 문자열이 도메인 enum 으로 변환된다`() {
        assertEquals(VerificationStatus.PENDING, "PENDING".toVerificationStatusOrDefault())
        assertEquals(VerificationStatus.VERIFIED, "VERIFIED".toVerificationStatusOrDefault())
        assertEquals(VerificationStatus.FAILED, "FAILED".toVerificationStatusOrDefault())
    }

    @Test
    fun `모르는 status 값은 PENDING 으로 흡수된다`() {
        assertEquals(VerificationStatus.PENDING, "EXPIRED".toVerificationStatusOrDefault())
        assertEquals(VerificationStatus.PENDING, "".toVerificationStatusOrDefault())
    }
}
