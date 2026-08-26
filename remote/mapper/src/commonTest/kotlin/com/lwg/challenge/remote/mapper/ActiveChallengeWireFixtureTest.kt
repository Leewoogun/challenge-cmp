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
import kotlin.test.assertTrue

/**
 * 2026-08-25 backend-dev 가 넘긴 실서버 `GET /api/v1/challenges/active` 실제 응답(테스터1 = user 14).
 * 손으로 쓴 픽스처가 아니라 서버가 실제로 보낸 본문이다. 서버가 shape 을 바꾸면 이 테스트가 깨진다.
 *
 * [ActiveChallengeResponseMapperTest] 는 DTO 를 코드로 조립해 매퍼만 검증한다 — 그래서 DTO 바인딩 층
 * (JSON 키 이름, "yyyy-MM-dd HH:mm:ss" 날짜 포맷, enum 문자열) 이 통째로 사각지대다.
 * 이 테스트는 원문 바이트에서 시작해 도메인 모델까지 한 번에 통과시켜 그 구간을 덮는다.
 * `GET /challenges/{id}` 와 `/verifications` 에는 이미 원문 픽스처가 있는데 이 엔드포인트에만 없었다.
 *
 * IN_PROGRESS 와 COMPLETED 두 상태를 한 응답 안에 모두 담는다. 상세 쪽에서 한 번 밟은 사건이라
 * 반복하지 않는다 — 그때는 유일하게 동작하는 상태만 떠 뒀고 화면이 실제로 여는 다른 상태는 역직렬화가
 * 깨져 있었다. 실서버에서 떴다는 사실이 대표 응답을 떴다는 뜻은 아니다. 이 목록은 정의상 두 상태가
 * 섞여 오므로 한쪽만 덮으면 절반이 사각지대로 남는다.
 *
 * 픽스처가 둘인 이유. [legacyFoldedWireResponse] 는 backend 가 `myResult` 를 3값으로 접던 시점의
 * 원문 그대로이고([ChallengeOutcome.BOTH_LOSE] 를 `LOSE` 로 접어 보냈다), [wireResponse] 는 이후
 * 4값으로 확정된 **현행 계약의 대표 응답**이다. 앞의 것은 하위호환 증거로 남기고, 뒤의 것이 지금
 * 서버가 실제로 보내는 것이다(backend 2차 실측: id 29·26·21·11 이 전부 `result=BOTH_LOSE myResult=BOTH_LOSE`).
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

    // 현행 4값 계약의 대표 응답. 아래 원문에서 id=29 의 myResult 만 확정된 BOTH_LOSE 로 맞춘 것이다.
    private val wireResponse =
        """{"data":{"activeChallenges":[
 {"challengeId":30,"status":"IN_PROGRESS","result":null,"myResult":null,
  "myMission":"책","opponentNickname":"이우건","opponentMission":"사진 테스크",
  "deadline":"2026-08-26 00:00:00","myVerificationStatus":"VERIFIED",
  "opponentVerificationStatus":"VERIFIED","bet":"사진"},
 {"challengeId":29,"status":"COMPLETED","result":"BOTH_LOSE","myResult":"BOTH_LOSE",
  "myMission":"인증22","opponentNickname":"이우건","opponentMission":"인증 테스트",
  "deadline":"2026-08-19 00:00:00","myVerificationStatus":"FAILED",
  "opponentVerificationStatus":"FAILED","bet":"커피"}
]},"error":false,"code":200,"message":""}"""

    // backend 가 넘긴 원문 그대로. `myResult` 를 WIN/LOSE/DRAW 3값으로 접던 시점이라 id=29 가
    // result=BOTH_LOSE 인데 myResult=LOSE 로 온다. 지금 서버는 이렇게 보내지 않지만, 앱이 이 응답을
    // 받아도 깨지지 않고 LOSE 로 읽는다는 하위호환 증거로 고친 데 없이 남긴다.
    private val legacyFoldedWireResponse =
        """{"data":{"activeChallenges":[
 {"challengeId":30,"status":"IN_PROGRESS","result":null,"myResult":null,
  "myMission":"책","opponentNickname":"이우건","opponentMission":"사진 테스크",
  "deadline":"2026-08-26 00:00:00","myVerificationStatus":"VERIFIED",
  "opponentVerificationStatus":"VERIFIED","bet":"사진"},
 {"challengeId":29,"status":"COMPLETED","result":"BOTH_LOSE","myResult":"LOSE",
  "myMission":"인증22","opponentNickname":"이우건","opponentMission":"인증 테스트",
  "deadline":"2026-08-19 00:00:00","myVerificationStatus":"FAILED",
  "opponentVerificationStatus":"FAILED","bet":"커피"}
]},"error":false,"code":200,"message":""}"""

    private fun parse(): ActiveChallengeResponse = json.decodeFromString(wireResponse)

    private fun parseLegacyFolded(): ActiveChallengeResponse = json.decodeFromString(legacyFoldedWireResponse)

    private fun rawChallengeAt(index: Int): JsonObject =
        json.parseToJsonElement(wireResponse)
            .jsonObject.getValue("data")
            .jsonObject.getValue("activeChallenges")
            .jsonArray[index].jsonObject

    @Test
    fun `실서버 응답 원문이 ActiveChallengeResponse 로 역직렬화된다`() {
        val response = parse()

        assertEquals(200, response.code)
        assertFalse(response.error)
        assertEquals(2, response.data.activeChallenges.size)
    }

    @Test
    fun `실서버 원문의 두 항목이 모두 도메인까지 살아남고 드롭이 없다`() {
        val result = parse().toActiveChallenges()

        assertEquals(0, result.droppedCount)
        assertEquals(2, result.items.size)
    }

    @Test
    fun `서버가 준 순서가 보존된다 - 진행 중이 먼저 판정 완료가 나중`() {
        val result = parse().toActiveChallenges()

        assertEquals(
            listOf(30L, 29L),
            result.items.map { it.challengeId },
            "정렬은 서버 책임이다. 앱이 뒤집으면 홈 카드 순서가 서버 정책과 어긋난다",
        )
        assertEquals(
            listOf(ActiveChallengeStatus.IN_PROGRESS, ActiveChallengeStatus.COMPLETED),
            result.items.map { it.status },
        )
    }

    @Test
    fun `IN_PROGRESS 항목 - 마감 시각과 인증 상태와 텍스트가 그대로 매핑된다`() {
        val challenge = parse().toActiveChallenges().items.first()

        assertEquals(30L, challenge.challengeId)
        assertEquals(ActiveChallengeStatus.IN_PROGRESS, challenge.status)
        assertNull(challenge.outcome)
        assertEquals(LocalDateTime(2026, 8, 26, 0, 0, 0), challenge.deadline)
        assertEquals(VerificationStatus.VERIFIED, challenge.myVerificationStatus)
        assertEquals(VerificationStatus.VERIFIED, challenge.opponentVerificationStatus)
        assertEquals("책", challenge.myMission)
        assertEquals("이우건", challenge.opponentNickname)
        assertEquals("사진 테스크", challenge.opponentMission)
        assertEquals("사진", challenge.bet)
    }

    @Test
    fun `COMPLETED 항목 - 현행 4값 myResult BOTH_LOSE 가 outcome 으로 매핑된다`() {
        val challenge = parse().toActiveChallenges().items.last()

        assertEquals(29L, challenge.challengeId)
        assertEquals(ActiveChallengeStatus.COMPLETED, challenge.status)
        assertEquals(ChallengeOutcome.BOTH_LOSE, challenge.outcome)
        assertEquals(LocalDateTime(2026, 8, 19, 0, 0, 0), challenge.deadline)
    }

    @Test
    fun `COMPLETED 항목 - 판정 feature 로 처음 실제로 흐르기 시작한 FAILED 가 양측 모두 도메인까지 온다`() {
        val challenge = parse().toActiveChallenges().items.last()

        // FAILED 는 VerificationStatusDto 에 정의만 돼 있고 실서버 응답에 실려 온 적이 없던 값이다.
        // 판정 feature 가 붙으면서 처음 흐르기 시작했으므로 원문에서 도메인까지의 경로를 여기서 고정한다.
        assertEquals(VerificationStatus.FAILED, challenge.myVerificationStatus)
        assertEquals(VerificationStatus.FAILED, challenge.opponentVerificationStatus)
    }

    @Test
    fun `myResult 를 3값으로 접던 시절 원문도 깨지지 않고 LOSE 로 읽힌다`() {
        val result = parseLegacyFolded().toActiveChallenges()

        assertEquals(0, result.droppedCount)
        assertEquals(listOf(30L, 29L), result.items.map { it.challengeId })
        assertEquals(
            ChallengeOutcome.LOSE,
            result.items.last().outcome,
            "3값 시절 서버는 BOTH_LOSE 를 LOSE 로 접어 보냈다. 접힌 값도 그대로 읽혀야 한다",
        )
        assertEquals(ActiveChallengeStatus.COMPLETED, result.items.last().status)
    }

    @Test
    fun `역할 기준 result 키를 앱이 모르는데도 파싱이 깨지지 않는다 - ignoreUnknownKeys 를 끄면 홈 목록이 통째로 죽는다`() {
        // ActiveChallengeDto 에는 result 필드가 없다(앱은 내 시점 myResult 만 본다). 그런데 이 키는
        // 계약상 계속 내려오므로, ignoreUnknownKeys 가 유일하게 이걸 흡수하고 있다.
        assertTrue(rawChallengeAt(1).containsKey("result"))

        val strictJson = Json {
            ignoreUnknownKeys = false
            isLenient = true
            encodeDefaults = true
            prettyPrint = true
        }
        assertFailsWith<SerializationException>(
            "ignoreUnknownKeys 를 끄는 순간 항목 하나가 아니라 응답 전체의 역직렬화가 터진다",
        ) {
            strictJson.decodeFromString<ActiveChallengeResponse>(wireResponse)
        }

        val result = parse().toActiveChallenges()
        assertEquals(0, result.droppedCount)
        assertEquals(2, result.items.size)
    }

    @Test
    fun `IN_PROGRESS 항목에도 result 와 myResult 키가 존재하고 값만 null 이다`() {
        // 계약: 키는 값이 null 이어도 항상 존재한다. 기본값은 키 누락만 덮으므로 myResult 타입 자체가
        // nullable 이어야 명시적 null 이 예외 없이 통과한다.
        val rawInProgress = rawChallengeAt(0)

        assertEquals(JsonNull, rawInProgress["result"])
        assertEquals(JsonNull, rawInProgress["myResult"])

        val dto = parse().data.activeChallenges.first()
        assertNull(dto.myResult)

        val challenge = assertNotNull(parse().toActiveChallenges().items.firstOrNull())
        assertNull(challenge.outcome)
    }
}
