package com.lwg.challenge.remote.mapper

import com.lwg.challenge.domain.model.challenge.ChallengeOutcome
import com.lwg.challenge.remote.model.challenge.ChallengeHistoryDto
import com.lwg.challenge.remote.model.challenge.ChallengeHistoryListData
import com.lwg.challenge.remote.model.challenge.ChallengeHistoryResponse
import com.lwg.challenge.remote.model.challenge.ChallengeOutcomeDto
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** `GET /api/v1/challenges/history` (월 단위) 의 원문 → DTO → 도메인 매핑 테스트. */
class ChallengeHistoryResponseMapperTest {

    // KtorfitModule.provideJson() 과 같은 옵션. 실제 응답 파싱과 조건이 어긋나면
    // 이 테스트가 통과해도 런타임 파싱을 보장하지 못한다.
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        prettyPrint = true
    }

    private val wireResponse =
        """{"error":false,"code":200,"message":"","data":{
 "month":"2026-08",
 "firstArchivedMonth":"2026-03",
 "histories":[
  {"challengeId":42,"challengeDate":"2026-08-14","opponentNickname":"준혁",
   "myMission":"헬스장 가기","opponentMission":"금주","bet":"치킨 한 마리","myResult":"WIN"},
  {"challengeId":41,"challengeDate":"2026-08-02","opponentNickname":"민수",
   "myMission":"책 30페이지","opponentMission":"러닝 3km","bet":"커피","myResult":"LOSE"}
 ]}}"""

    // 기록이 0건인 사용자. 계약상 firstArchivedMonth 는 값이 null 이어도 키가 항상 실린다(서버 실측).
    private val emptyArchiveWireResponse =
        """{"error":false,"code":200,"message":"","data":{
 "month":"2026-08","firstArchivedMonth":null,"histories":[]}}"""

    private val dto = ChallengeHistoryDto(
        challengeId = 42L,
        challengeDate = LocalDate(2026, 8, 14),
        opponentNickname = "준혁",
        myMission = "헬스장 가기",
        opponentMission = "금주",
        bet = "치킨 한 마리",
        myResult = ChallengeOutcomeDto.WIN,
    )

    private fun response(
        firstArchivedMonth: YearMonth? = YearMonth(2026, 3),
        histories: List<ChallengeHistoryDto> = listOf(dto),
    ) = ChallengeHistoryResponse(ChallengeHistoryListData(firstArchivedMonth, histories))

    private fun parse(raw: String): ChallengeHistoryResponse = json.decodeFromString(raw)

    @Test
    fun `대표 응답 원문이 도메인 보관함으로 매핑된다`() {
        val archive = parse(wireResponse).toChallengeArchive()

        assertEquals(YearMonth(2026, 3), archive.firstArchivedMonth)

        val first = archive.histories.first()
        assertEquals(42L, first.challengeId)
        assertEquals(LocalDate(2026, 8, 14), first.challengeDate)
        assertEquals("준혁", first.opponentNickname)
        assertEquals("헬스장 가기", first.myMission)
        assertEquals("금주", first.opponentMission)
        assertEquals("치킨 한 마리", first.bet)
        assertEquals(ChallengeOutcome.WIN, first.myResult)
    }

    @Test
    fun `서버가 준 challengeDate 내림차순 순서가 그대로 보존된다`() {
        val archive = parse(wireResponse).toChallengeArchive()

        assertEquals(listOf(42L, 41L), archive.histories.map { it.challengeId })
    }

    /** zero-padding 이 빠진 `2026-8` 은 직렬화기가 null 로 떨어뜨리고, 매퍼는 그것을 결측과 똑같이 다룬다. */
    /**
     * `null` 은 결측이 아니라 **종료한 챌린지가 하나도 없다**는 뜻이라, 도메인까지 null 로 실어 보내야
     * 화면이 *"이 달만 비었다"* 와 *"기록이 아예 없다"* 를 구분할 수 있다.
     */
    @Test
    fun `firstArchivedMonth 가 JSON null 이면 도메인도 null 이다`() {
        // 계약: 값이 null 이어도 키는 항상 실린다. 키 자체가 사라지는 것과 구분되지 않게 하려는 것.
        assertEquals(
            JsonNull,
            json.parseToJsonElement(emptyArchiveWireResponse).jsonObject.getValue("data")
                .jsonObject["firstArchivedMonth"],
        )

        val archive = parse(emptyArchiveWireResponse).toChallengeArchive()

        assertNull(archive.firstArchivedMonth)
        assertTrue(archive.histories.isEmpty())
    }

    @Test
    fun `firstArchivedMonth 가 정상이면 그대로 실린다`() {
        val archive = response(firstArchivedMonth = YearMonth(2025, 11)).toChallengeArchive()

        assertEquals(YearMonth(2025, 11), archive.firstArchivedMonth)
    }

    @Test
    fun `histories 가 빈 배열이어도 firstArchivedMonth 는 그대로 온다`() {
        val archive = response(histories = emptyList()).toChallengeArchive()

        assertTrue(archive.histories.isEmpty())
        // firstArchivedMonth 가 non-null 인 채로 목록만 비는 것이 "이 달만 비었다" 상태다.
        assertEquals(YearMonth(2026, 3), archive.firstArchivedMonth)
    }
}
