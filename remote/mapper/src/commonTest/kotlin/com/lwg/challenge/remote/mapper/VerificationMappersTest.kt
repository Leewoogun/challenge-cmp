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
import kotlin.test.assertFalse
import kotlin.test.assertNull

/** 운영 BuildKonfig.BASE_URL 과 같은 형태 — trailing slash 가 있다. 이게 실제 조합 조건이다. */
internal const val TEST_BASE_URL = "http://10.0.2.2:8080/"

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
        ).toChallengeVerifications(TEST_BASE_URL)

        assertEquals(
            "http://10.0.2.2:8080/api/v1/challenges/7/photos/challenger",
            verifications.challenger.photoUrl,
        )
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

    // 아래 조인 테스트가 이 기능의 핵심 방어선이다 — 겹친 슬래시로 생기는 404 는
    // '상대가 아직 인증 안 함' 404 와 구분되지 않아 실패가 정상 상태로 위장된다.

    @Test
    fun `baseUrl 끝 슬래시와 path 앞 슬래시가 겹쳐도 슬래시는 하나다`() {
        assertEquals(
            "http://10.0.2.2:8080/api/v1/challenges/7/photos/challenger",
            joinPhotoUrl("http://10.0.2.2:8080/", "/api/v1/challenges/7/photos/challenger"),
        )
    }

    @Test
    fun `baseUrl 이 슬래시 없이 끝나도 결과가 같다`() {
        assertEquals(
            "http://10.0.2.2:8080/api/v1/challenges/7/photos/challenger",
            joinPhotoUrl("http://10.0.2.2:8080", "/api/v1/challenges/7/photos/challenger"),
        )
    }

    @Test
    fun `path 가 슬래시 없이 시작해도 결과가 같다`() {
        assertEquals(
            "http://10.0.2.2:8080/api/v1/challenges/7/photos/challenger",
            joinPhotoUrl("http://10.0.2.2:8080/", "api/v1/challenges/7/photos/challenger"),
        )
        assertEquals(
            "http://10.0.2.2:8080/api/v1/challenges/7/photos/challenger",
            joinPhotoUrl("http://10.0.2.2:8080", "api/v1/challenges/7/photos/challenger"),
        )
    }

    @Test
    fun `조립된 URL 에는 스킴을 뺀 나머지에 연속 슬래시가 없다`() {
        val bases = listOf("http://10.0.2.2:8080/", "http://10.0.2.2:8080")
        val paths = listOf("/api/v1/challenges/7/photos/challenger", "api/v1/challenges/7/photos/challenger")

        for (base in bases) {
            for (path in paths) {
                val joined = joinPhotoUrl(base, path).orEmpty()
                assertFalse(
                    joined.substringAfter("://").contains("//"),
                    "base=$base path=$path joined=$joined",
                )
            }
        }
    }

    @Test
    fun `photoUrl 이 null 이면 조인 결과도 null 이다`() {
        assertNull(joinPhotoUrl(TEST_BASE_URL, null), "미인증 party 는 조인 대상이 없다")
    }

    @Test
    fun `매퍼를 통과해도 photoUrl 은 절대 URL 이고 미인증측은 null 로 남는다`() {
        val verifications = ChallengeVerificationsResponse(
            ChallengeVerificationsData(
                challengeId = 7L,
                challenger = verifiedParty,
                opponent = PartyVerificationDto(userId = 14L, status = "PENDING"),
            ),
        ).toChallengeVerifications("http://10.0.2.2:8080")

        assertEquals(
            "http://10.0.2.2:8080/api/v1/challenges/7/photos/challenger",
            verifications.challenger.photoUrl,
        )
        assertNull(verifications.opponent.photoUrl)
    }
}
