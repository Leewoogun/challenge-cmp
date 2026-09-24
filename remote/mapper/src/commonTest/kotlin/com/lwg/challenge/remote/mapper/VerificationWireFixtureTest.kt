package com.lwg.challenge.remote.mapper

import com.lwg.challenge.domain.model.VerificationStatus
import com.lwg.challenge.remote.model.verification.ChallengeVerificationsResponse
import com.lwg.challenge.remote.model.verification.SubmitVerificationResponse
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * 인증 API 응답 원문 바이트 → DTO → 도메인 통과 검증.
 *
 * [VerificationMappersTest] 는 DTO 를 코드로 조립해 매퍼만 검증한다 — DTO 바인딩 층
 * (JSON 키 이름, 날짜 포맷, `uploadHeaders` 가 객체로 오는지, null 이 명시적 키로 오는지)
 * 이 통째로 사각지대라 이 테스트가 그 구간을 덮는다.
 *
 * 핵심은 [nullMaxVerificationsWire] 다 — 미인증 party 의 `photoUrl`/`verifiedAt` 이
 * **키는 존재하되 값이 null** 로 온다. DTO 를 non-nullable 로 잡으면 여기서 역직렬화가 터진다
 * (`contract: null` 로 같은 사고가 이미 있었다).
 */
class VerificationWireFixtureTest {

    // KtorfitModule.provideJson() 과 같은 옵션. 실제 응답 파싱과 조건이 어긋나면
    // 이 테스트가 통과해도 런타임 파싱을 보장하지 못한다.
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        prettyPrint = true
    }

    private val submitWire =
        """{"error":false,"code":200,"message":"","data":{"challengeId":7,"status":"VERIFIED","verifiedAt":"2026-08-14 15:32:10"}}"""

    private val submitNullVerifiedAtWire =
        """{"error":false,"code":200,"message":"","data":{"challengeId":7,"status":"VERIFIED","verifiedAt":null}}"""

    // 한쪽만 인증한 상태 — 미인증 party 는 두 필드가 명시적 null (키는 존재)
    private val nullMaxVerificationsWire =
        """{"error":false,"code":200,"message":"","data":{"challengeId":7,"challenger":{"userId":1,"status":"VERIFIED","photoUrl":"/api/v1/challenges/7/photos/challenger","verifiedAt":"2026-08-14 15:32:10"},"opponent":{"userId":14,"status":"PENDING","photoUrl":null,"verifiedAt":null}}}"""

    private val bothVerifiedWire =
        """{"error":false,"code":200,"message":"","data":{"challengeId":7,"challenger":{"userId":1,"status":"VERIFIED","photoUrl":"/api/v1/challenges/7/photos/challenger","verifiedAt":"2026-08-14 15:32:10"},"opponent":{"userId":14,"status":"VERIFIED","photoUrl":"/api/v1/challenges/7/photos/opponent","verifiedAt":"2026-08-14 15:41:03"}}}"""

    // 탈퇴자가 낀 실측 응답 — 상대는 VERIFIED 인데 photoUrl 이 null 이고 photoDeleted 만 true 다.
    private val photoDeletedWire =
        """{"error":false,"code":200,"message":"","data":{"challengeId":7,"challenger":{"userId":1,"status":"VERIFIED","photoUrl":"/api/v1/challenges/7/photos/challenger","verifiedAt":"2026-08-14 15:32:10","photoDeleted":false},"opponent":{"userId":14,"status":"VERIFIED","photoUrl":null,"verifiedAt":"2026-08-14 15:41:03","photoDeleted":true}}}"""

    // AI 판정이 실린 실측 응답 — 반려된 challenger 는 FAILED 인데 photoUrl 이 살아 있고, 판정이
    // 없는 opponent 는 aiReason 이 **명시적 null 키**로 온다 (서버가 그 모양을 계약 테스트로 잠갔다).
    private val aiReasonWire =
        """{"error":false,"code":200,"message":"","data":{"challengeId":7,"challenger":{"userId":1,"status":"FAILED","photoUrl":"/api/v1/challenges/7/photos/challenger","verifiedAt":"2026-09-22 21:14:03","photoDeleted":false,"aiReason":"책상 위 커피잔 사진으로, 미션(주 3회 운동) 수행 증거로 보기 어렵습니다."},"opponent":{"userId":14,"status":"PENDING","photoUrl":null,"verifiedAt":null,"photoDeleted":false,"aiReason":null}}}"""

    @Test
    fun `null 최대 응답 원문이 역직렬화된다 - 명시적 null 키가 예외가 아니다`() {
        val response = json.decodeFromString<ChallengeVerificationsResponse>(nullMaxVerificationsWire)

        assertEquals(200, response.code)
        assertFalse(response.error)
        assertEquals(7L, response.data.challengeId)
        assertEquals(14L, response.data.opponent.userId)
        assertEquals("PENDING", response.data.opponent.status)
        assertNull(response.data.opponent.photoUrl)
        assertNull(response.data.opponent.verifiedAt)
    }

    @Test
    fun `null 최대 응답이 도메인까지 통과하고 미인증측 두 필드는 null 로 남는다`() {
        val verifications = json.decodeFromString<ChallengeVerificationsResponse>(nullMaxVerificationsWire)
            .toChallengeVerifications(TEST_BASE_URL)

        assertEquals(7L, verifications.challengeId)
        assertEquals(14L, verifications.opponent.userId)
        assertEquals(VerificationStatus.PENDING, verifications.opponent.status)
        assertNull(verifications.opponent.photoUrl)
        assertNull(verifications.opponent.verifiedAt)

        assertEquals(VerificationStatus.VERIFIED, verifications.challenger.status)
        assertEquals(
            "http://10.0.2.2:8080/api/v1/challenges/7/photos/challenger",
            verifications.challenger.photoUrl,
        )
        assertEquals(LocalDateTime(2026, 8, 14, 15, 32, 10), verifications.challenger.verifiedAt)
    }

    @Test
    fun `양측 VERIFIED 응답이 역할 그대로 매핑된다`() {
        val verifications = json.decodeFromString<ChallengeVerificationsResponse>(bothVerifiedWire)
            .toChallengeVerifications(TEST_BASE_URL)

        assertEquals(1L, verifications.challenger.userId)
        assertEquals(14L, verifications.opponent.userId)
        assertEquals(VerificationStatus.VERIFIED, verifications.challenger.status)
        assertEquals(VerificationStatus.VERIFIED, verifications.opponent.status)
        assertNotNull(verifications.challenger.photoUrl)
        assertNotNull(verifications.opponent.photoUrl)
        assertEquals(LocalDateTime(2026, 8, 14, 15, 41, 3), verifications.opponent.verifiedAt)
    }

    @Test
    fun `탈퇴자 응답 원문이 도메인까지 통과하고 photoDeleted 만 true 로 남는다`() {
        val verifications = json.decodeFromString<ChallengeVerificationsResponse>(photoDeletedWire)
            .toChallengeVerifications(TEST_BASE_URL)

        assertEquals(VerificationStatus.VERIFIED, verifications.opponent.status)
        assertNull(verifications.opponent.photoUrl)
        assertTrue(verifications.opponent.photoDeleted)

        assertNotNull(verifications.challenger.photoUrl)
        assertFalse(verifications.challenger.photoDeleted)
    }

    /**
     * 서버가 아직 이 키를 주지 않는 환경 — 키 누락이 예외가 되면 인증 현황 조회가 통째로 죽는다.
     * 이 값이 false 로 들어와야 URL 유실 감지 분기가 탈퇴 문구에 가려지지 않는다.
     */
    @Test
    fun `photoDeleted 키가 없는 응답도 역직렬화되고 false 로 채워진다`() {
        val verifications = json.decodeFromString<ChallengeVerificationsResponse>(nullMaxVerificationsWire)
            .toChallengeVerifications(TEST_BASE_URL)

        assertFalse(verifications.challenger.photoDeleted)
        assertFalse(verifications.opponent.photoDeleted)
    }

    /**
     * AI 반려 건의 원문 — 이 프로젝트에서 처음으로 `FAILED` 인데 `photoUrl` 이 살아 있는 응답이다.
     * 상태만 보고 사진이 없다고 단정하던 전제가 여기서 깨진다.
     */
    @Test
    fun `AI 판정이 실린 원문이 도메인까지 통과하고 반려 건의 사진이 남는다`() {
        val verifications = json.decodeFromString<ChallengeVerificationsResponse>(aiReasonWire)
            .toChallengeVerifications(TEST_BASE_URL)

        assertEquals(VerificationStatus.FAILED, verifications.challenger.status)
        assertEquals(
            "http://10.0.2.2:8080/api/v1/challenges/7/photos/challenger",
            verifications.challenger.photoUrl,
        )
        assertEquals(LocalDateTime(2026, 9, 22, 21, 14, 3), verifications.challenger.verifiedAt)

        val reason = assertNotNull(verifications.challenger.aiReason)
        assertTrue(reason.startsWith("책상 위 커피잔"))
    }

    /** `photoUrl`·`verifiedAt` 과 같은 취급 — 값이 없을 때도 키는 남고, 그게 예외가 되면 안 된다. */
    @Test
    fun `aiReason 이 명시적 null 키로 와도 역직렬화가 성립한다`() {
        val response = json.decodeFromString<ChallengeVerificationsResponse>(aiReasonWire)

        assertEquals(200, response.code)
        assertNull(response.data.opponent.aiReason)
        assertEquals("PENDING", response.data.opponent.status)
    }

    /**
     * 서버가 아직 이 키를 주지 않는 환경 — `photoDeleted` 와 같은 선례다. 기본값이 없으면 키 누락이
     * `MissingFieldException` 이 돼 인증 현황 조회가 통째로 죽는다. 이 픽스처에는 `aiReason` 키가
     * 아예 없다.
     */
    @Test
    fun `aiReason 키가 없는 응답도 역직렬화되고 null 로 채워진다`() {
        val verifications = json.decodeFromString<ChallengeVerificationsResponse>(nullMaxVerificationsWire)
            .toChallengeVerifications(TEST_BASE_URL)

        assertNull(verifications.challenger.aiReason)
        assertNull(verifications.opponent.aiReason)
        assertEquals(VerificationStatus.VERIFIED, verifications.challenger.status, "응답 전체가 살아남는다")
    }

    @Test
    fun `제출 확정 원문이 도메인까지 통과한다`() {
        val result = json.decodeFromString<SubmitVerificationResponse>(submitWire).toSubmitResult()

        assertEquals(7L, result.challengeId)
        assertEquals(VerificationStatus.VERIFIED, result.status)
        assertEquals(LocalDateTime(2026, 8, 14, 15, 32, 10), result.verifiedAt)
    }

    @Test
    fun `제출 확정의 verifiedAt 이 명시적 null 이어도 역직렬화와 매핑이 성립한다`() {
        val result = json.decodeFromString<SubmitVerificationResponse>(submitNullVerifiedAtWire).toSubmitResult()

        assertEquals(VerificationStatus.VERIFIED, result.status)
        assertNull(result.verifiedAt)
    }

    @Test
    fun `verifiedAt 이 ISO 로 오면 그 값만 null 이고 응답 전체는 살아남는다`() {
        val isoWire = nullMaxVerificationsWire.replace("2026-08-14 15:32:10", "2026-08-14T15:32:10")
        val verifications = json.decodeFromString<ChallengeVerificationsResponse>(isoWire)
            .toChallengeVerifications(TEST_BASE_URL)

        assertNull(verifications.challenger.verifiedAt, "T 구분자는 wire 포맷이 아니라 읽지 않는다")
        assertEquals(VerificationStatus.VERIFIED, verifications.challenger.status)
        assertNotNull(verifications.challenger.photoUrl)
        assertEquals(VerificationStatus.PENDING, verifications.opponent.status)
    }
}
