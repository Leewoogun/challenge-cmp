package com.lwg.challenge.remote.mapper

import com.lwg.challenge.domain.model.ActiveChallengeStatus
import com.lwg.challenge.domain.model.VerificationStatus
import com.lwg.challenge.domain.model.challenge.ChallengeOutcome
import com.lwg.challenge.remote.model.challenge.ActiveChallengeResponse
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * `GET /api/v1/challenges/active` 원문 JSON 에서 도메인까지 한 번에 통과시킨다.
 *
 * [ActiveChallengeResponseMapperTest] 는 DTO 를 코드로 조립하므로 JSON 키 이름·날짜 포맷·enum 문자열이
 * 사각지대다. 서버가 shape 을 바꾸면 이 테스트가 깨져야 한다.
 */
class ActiveChallengeWireFixtureTest {

    // KtorfitModule.provideJson() 과 같은 옵션. 실제 응답 파싱과 조건이 어긋나면
    // 이 테스트가 통과해도 런타임 파싱을 보장하지 못한다.
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        prettyPrint = true
    }

    private val wireResponse =
        """{"data":{"activeChallenges":[
 {"challengeId":30,"status":"IN_PROGRESS","myResult":null,
  "deadline":"2026-08-26 00:00:00","bet":"사진",
  "me":{"mission":"책","verificationStatus":"VERIFIED"},
  "opponent":{"nickname":"이우건","mission":"사진 테스크","verificationStatus":"VERIFIED"}},
 {"challengeId":29,"status":"COMPLETED","myResult":"BOTH_LOSE",
  "deadline":"2026-08-19 00:00:00","bet":"커피",
  "me":{"mission":"인증22","verificationStatus":"FAILED"},
  "opponent":{"nickname":"이우건","mission":"인증 테스트","verificationStatus":"FAILED"}}
]},"error":false,"code":200,"message":""}"""

    private fun parse(): ActiveChallengeResponse = json.decodeFromString(wireResponse)

    private fun rawChallengeAt(index: Int): JsonObject =
        json.parseToJsonElement(wireResponse)
            .jsonObject.getValue("data")
            .jsonObject.getValue("activeChallenges")
            .jsonArray[index].jsonObject

    @Test
    fun `응답 원문이 ActiveChallengeResponse 로 역직렬화된다`() {
        val response = parse()

        assertEquals(200, response.code)
        assertFalse(response.error)
        assertEquals(2, response.data.activeChallenges.size)
    }

    @Test
    fun `서버가 준 순서가 보존된다 - 진행 중이 먼저 판정 완료가 나중`() {
        val result = parse().toActiveChallenges()

        assertEquals(listOf(30L, 29L), result.map { it.challengeId })
        assertEquals(
            listOf(ActiveChallengeStatus.IN_PROGRESS, ActiveChallengeStatus.COMPLETED),
            result.map { it.status },
        )
    }

    @Test
    fun `IN_PROGRESS 항목 - me 와 opponent 객체가 도메인으로 그대로 매핑된다`() {
        val challenge = parse().toActiveChallenges().first()

        assertEquals(30L, challenge.challengeId)
        assertEquals(ActiveChallengeStatus.IN_PROGRESS, challenge.status)
        assertNull(challenge.outcome)
        assertEquals(LocalDateTime(2026, 8, 26, 0, 0, 0), challenge.deadline)
        assertEquals("책", challenge.me.mission)
        assertEquals(VerificationStatus.VERIFIED, challenge.me.verificationStatus)
        assertEquals("이우건", challenge.opponent.nickname)
        assertEquals("사진 테스크", challenge.opponent.mission)
        assertEquals(VerificationStatus.VERIFIED, challenge.opponent.verificationStatus)
        assertEquals("사진", challenge.penalty)
    }

    @Test
    fun `COMPLETED 항목 - myResult 와 FAILED 인증 상태가 도메인까지 온다`() {
        val challenge = parse().toActiveChallenges().last()

        assertEquals(29L, challenge.challengeId)
        assertEquals(ActiveChallengeStatus.COMPLETED, challenge.status)
        assertEquals(ChallengeOutcome.BOTH_LOSE, challenge.outcome)
        assertEquals(VerificationStatus.FAILED, challenge.me.verificationStatus)
        assertEquals(VerificationStatus.FAILED, challenge.opponent.verificationStatus)
    }

    @Test
    fun `IN_PROGRESS 항목의 myResult 는 키가 있고 값만 null 이다`() {
        assertEquals(JsonNull, rawChallengeAt(0)["myResult"])

        val challenge = assertNotNull(parse().toActiveChallenges().firstOrNull())
        assertNull(challenge.outcome)
    }

    @Test
    fun `예전 평평한 shape 이 오면 빈 값으로 넘어가지 않고 파싱이 실패한다`() {
        val legacyFlat = """{"data":{"activeChallenges":[
 {"challengeId":30,"status":"IN_PROGRESS","myResult":null,
  "myMission":"책","opponentNickname":"이우건","opponentMission":"사진 테스크",
  "deadline":"2026-08-26 00:00:00","myVerificationStatus":"VERIFIED",
  "opponentVerificationStatus":"VERIFIED","bet":"사진"}
]},"error":false,"code":200,"message":""}"""

        assertFailsWith<SerializationException> {
            json.decodeFromString<ActiveChallengeResponse>(legacyFlat)
        }
    }
}
