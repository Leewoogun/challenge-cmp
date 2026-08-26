package com.lwg.challenge.remote.mapper

import com.lwg.challenge.remote.model.ranking.LoserRankingResponse
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * `GET /api/v1/rankings/losers` 의 원문 → DTO → 도메인 전 구간 wire 테스트.
 *
 * [realWireResponse] 가 정본 대표 응답이다 — backend 가 실서버에서 떠 넘긴 진짜 응답 본문
 * (테스터1 = user 14 시점)이라 손으로 쓴 픽스처가 아니고, 서버가 shape 을 바꾸면 이 테스트가 깨진다.
 * 이 한 응답이 화면 분기를 전부 덮는다.
 *
 * | rank | 유저 | 이 행이 덮는 것 |
 * |---|---|---|
 * | 1 | 테스터1 | 유일한 `isMe` 행이면서 `currentLossStreak == 0` 인 1위 — 포디움 연패 절 생략 분기 |
 * | 2 | 테스터2 | 연패 중 — 명단 뱃지가 붙는 쪽 |
 * | 3 | 이우건 | `profileImageUrl` 이 non-null 실 URL |
 * | 4 | 테스터3 | 패배율 100 인데 4위 — `losses DESC` 가 1차 정렬 키라는 증거 |
 * | 5 | 테스터4 | `totalChallenges == 0` — `user_stats` row 가 없는 유저 |
 *
 * 🔴 rank 5 를 이 픽스처에서 빼지 마라. backend 가 이 실측을 위해 계정을 일부러 심었다가 지웠고,
 * native query 의 `LEFT JOIN` + `COALESCE 0` 폴백이 실제 SQL 에서 탔다는 유일한 증거다.
 *
 * [wireResponse] 는 계약 확정 시점에 손으로 쓴 응답이다. 실서버 원문에 없는 조합
 * (앱이 모르는 키 `tier`, 같은 응답 안의 프로필 URL null/non-null 대조)을 덮어서 남긴다.
 */
class LoserRankingResponseMapperTest {

    // KtorfitModule.provideJson() 과 같은 옵션. 실제 응답 파싱과 조건이 어긋나면
    // 이 테스트가 통과해도 런타임 파싱을 보장하지 못한다.
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        prettyPrint = true
    }

    // rank 4 의 "tier" 는 앱 DTO 에 없는 키다. 서버가 필드를 늘려도 안 깨진다는 증거로 일부러 심었다.
    private val wireResponse =
        """{"data":{"rankings":[
 {"rank":1,"userId":3,"nickname":"준혁","profileImageUrl":null,"isMe":false,
  "losses":25,"lossRate":78,"currentLossStreak":0,"totalChallenges":32},
 {"rank":2,"userId":14,"nickname":"이우건","profileImageUrl":"https://cdn.example.com/p14.jpg","isMe":true,
  "losses":18,"lossRate":60,"currentLossStreak":4,"totalChallenges":30},
 {"rank":3,"userId":7,"nickname":"민수","profileImageUrl":null,"isMe":false,
  "losses":12,"lossRate":40,"currentLossStreak":1,"totalChallenges":30},
 {"rank":4,"userId":21,"nickname":"지훈","profileImageUrl":null,"isMe":false,
  "losses":3,"lossRate":10,"currentLossStreak":0,"totalChallenges":30,"tier":"BRONZE"},
 {"rank":5,"userId":33,"nickname":"신입","profileImageUrl":null,"isMe":false,
  "losses":0,"lossRate":0,"currentLossStreak":0,"totalChallenges":0}
]},"error":false,"code":200,"message":""}"""

    // backend 가 실서버에서 떠 넘긴 원문 그대로. 값을 손보지 않는다 — 손보는 순간 실측이 아니게 된다.
    private val realWireResponse =
        """{"data":{"rankings":[
 {"rank":1,"userId":14,"nickname":"테스터1","profileImageUrl":null,"isMe":true, "losses":4,"lossRate":80, "currentLossStreak":0,"totalChallenges":5},
 {"rank":2,"userId":15,"nickname":"테스터2","profileImageUrl":null,"isMe":false,"losses":3,"lossRate":100,"currentLossStreak":3,"totalChallenges":3},
 {"rank":3,"userId":1, "nickname":"이우건","profileImageUrl":"http://img1.kakaocdn.net/thumb/R640x640.q70/?fname=http%3A%2F%2Ft1.kakaocdn.net%2Faccount_images%2Fdefault_profile.jpeg","isMe":false,"losses":3,"lossRate":75,"currentLossStreak":0,"totalChallenges":4},
 {"rank":4,"userId":16,"nickname":"테스터3","profileImageUrl":null,"isMe":false,"losses":2,"lossRate":100,"currentLossStreak":2,"totalChallenges":2},
 {"rank":5,"userId":17,"nickname":"테스터4","profileImageUrl":null,"isMe":false,"losses":0,"lossRate":0,  "currentLossStreak":0,"totalChallenges":0}
]},"error":false,"code":200,"message":""}"""

    // totalChallenges 키가 통째로 빠진 응답. 이 필드는 계약 확정 대기 중이라 실제로 발생할 수 있다.
    private val totalChallengesMissingWireResponse =
        """{"data":{"rankings":[
 {"rank":1,"userId":3,"nickname":"준혁","profileImageUrl":null,"isMe":false,
  "losses":25,"lossRate":78,"currentLossStreak":7},
 {"rank":2,"userId":14,"nickname":"이우건","profileImageUrl":null,"isMe":true,
  "losses":18,"lossRate":60,"currentLossStreak":0}
]},"error":false,"code":200,"message":""}"""

    private fun parse(): LoserRankingResponse = json.decodeFromString(wireResponse)

    private fun parseReal(): LoserRankingResponse = json.decodeFromString(realWireResponse)

    private fun rawEntryAt(index: Int): JsonObject = rawEntryAt(wireResponse, index)

    private fun rawRealEntryAt(index: Int): JsonObject = rawEntryAt(realWireResponse, index)

    private fun rawEntryAt(rawResponse: String, index: Int): JsonObject =
        json.parseToJsonElement(rawResponse)
            .jsonObject.getValue("data")
            .jsonObject.getValue("rankings")
            .jsonArray[index].jsonObject

    @Test
    fun `대표 응답 원문이 LoserRankingResponse 로 역직렬화된다`() {
        val response = parse()

        assertEquals(200, response.code)
        assertFalse(response.error)
        assertEquals(5, response.data.rankings.size)
    }

    @Test
    fun `다섯 항목이 모두 도메인까지 살아남는다`() {
        val entries = parse().toLoserRankingEntries()

        assertEquals(5, entries.size)
    }

    @Test
    fun `서버가 준 rank 오름차순 순서가 그대로 보존된다`() {
        val entries = parse().toLoserRankingEntries()

        assertEquals(
            listOf(1, 2, 3, 4, 5),
            entries.map { it.rank },
            "정렬과 랭크 부여는 서버 책임이다. 앱이 뒤집으면 서버 정렬 정책과 화면이 어긋난다",
        )
        assertEquals(listOf(3L, 14L, 7L, 21L, 33L), entries.map { it.userId })
    }

    @Test
    fun `연패 0 인 1위의 모든 필드가 도메인까지 그대로 매핑된다`() {
        val first = parse().toLoserRankingEntries().first()

        assertEquals(1, first.rank)
        assertEquals(3L, first.userId)
        assertEquals("준혁", first.nickname)
        assertNull(first.profileImageUrl)
        assertFalse(first.isMe)
        assertEquals(25, first.losses)
        assertEquals(78, first.lossRate)
        // 서버 정렬 1차 키가 losses DESC 라 연패 0 인 1위가 실제로 발생한다.
        assertEquals(0, first.currentLossStreak)
        assertEquals(32, first.totalChallenges)
    }

    @Test
    fun `연패 중인 항목의 연패 수가 그대로 온다`() {
        val entries = parse().toLoserRankingEntries()

        assertEquals(4, entries.first { it.userId == 14L }.currentLossStreak)
        assertEquals(1, entries.first { it.userId == 7L }.currentLossStreak)
    }

    /**
     * isMe 는 "나" 판정의 정본이라 앱이 userId 대조로 다시 구하지 않는다.
     * 서버가 이 키를 떨구면 기본값 false 로 전원이 남이 되어 내 행 강조가 조용히 사라지는데,
     * 이 단언이 그 순간 깨져서 알려준다.
     */
    @Test
    fun `isMe 가 true 인 항목이 정확히 1건이다`() {
        val entries = parse().toLoserRankingEntries()

        assertEquals(1, entries.count { it.isMe })
        assertEquals(14L, entries.first { it.isMe }.userId)
        assertEquals("이우건", entries.first { it.isMe }.nickname)
    }

    @Test
    fun `profileImageUrl 이 null 로 와도 항목이 드롭되지 않는다`() {
        // 계약: 키는 값이 null 이어도 항상 존재한다. 기본값은 키 누락만 덮으므로
        // 타입 자체가 nullable 이어야 명시적 null 이 예외 없이 통과한다.
        assertEquals(JsonNull, rawEntryAt(0)["profileImageUrl"])

        val entries = parse().toLoserRankingEntries()

        assertEquals(5, entries.size)
        assertEquals(4, entries.count { it.profileImageUrl == null })
        assertEquals("https://cdn.example.com/p14.jpg", entries.first { it.isMe }.profileImageUrl)
    }

    @Test
    fun `앱이 모르는 키가 있어도 파싱이 깨지지 않는다 - ignoreUnknownKeys 를 끄면 랭킹이 통째로 죽는다`() {
        assertTrue(rawEntryAt(3).containsKey("tier"))

        val strictJson = Json {
            ignoreUnknownKeys = false
            isLenient = true
            encodeDefaults = true
            prettyPrint = true
        }
        assertFailsWith<SerializationException>(
            "ignoreUnknownKeys 를 끄는 순간 항목 하나가 아니라 응답 전체의 역직렬화가 터진다",
        ) {
            strictJson.decodeFromString<LoserRankingResponse>(wireResponse)
        }

        assertEquals(5, parse().toLoserRankingEntries().size)
    }

    @Test
    fun `totalChallenges 가 0 이면 명단 캡션이 아직 기록 없음 으로 떨어진다`() {
        // 캡션 문자열 자체는 :feature:ranking 의 LoserRankingItemState 소관이라 여기서 만들 수 없다.
        // 여기서는 그 분기를 트리거하는 유일한 입력이 0 이라는 것만 고정한다.
        val newcomer = parse().toLoserRankingEntries().last()

        assertEquals(33L, newcomer.userId)
        assertEquals(0, newcomer.totalChallenges)
        assertEquals(0, newcomer.losses)
        assertEquals(0, newcomer.lossRate)
    }

    /**
     * 🔴 지금 계약 확정 대기 중인 필드의 위험을 코드로 박아 둔다.
     *
     * `totalChallenges` 는 DTO 기본값이 0 이라 서버가 키를 안 내려주면 **역직렬화가 조용히 성공**하고
     * 전원이 0 이 된다. 그러면 화면상 모든 사람의 명단 캡션이 "아직 기록 없음" 이 되어,
     * 25패 78% 인 개돼지왕까지 기록이 없는 사람처럼 보인다. 패배율은 멀쩡히 왔는데도 그렇다.
     *
     * backend 가 이 키를 확정해 내려주기 시작하면 이 테스트가 그 사실을 증명하는 자리로 바뀐다.
     */
    @Test
    fun `totalChallenges 키가 없으면 0 으로 읽혀 전원이 기록 없음 캡션으로 보인다`() {
        val entries = json.decodeFromString<LoserRankingResponse>(totalChallengesMissingWireResponse)
            .toLoserRankingEntries()

        assertEquals(2, entries.size)
        assertTrue(
            entries.all { it.totalChallenges == 0 },
            "키가 없으면 DTO 기본값 0 이 그대로 도메인까지 간다",
        )
        // 패배 기록은 멀쩡히 도착했는데도 캡션 분기만 기록 없음 쪽으로 넘어간다는 대조.
        assertEquals(25, entries.first().losses)
        assertEquals(78, entries.first().lossRate)
    }

    @Test
    fun `실서버 원문이 역직렬화되고 다섯 항목이 모두 도메인까지 살아남는다`() {
        val response = parseReal()

        assertEquals(200, response.code)
        assertFalse(response.error)
        assertEquals(5, response.data.rankings.size)
        assertEquals(5, response.toLoserRankingEntries().size)
    }

    @Test
    fun `실서버 원문에서 isMe 가 true 인 항목이 정확히 1건이며 그게 1위다`() {
        val entries = parseReal().toLoserRankingEntries()

        assertEquals(1, entries.count { it.isMe })

        val me = entries.first { it.isMe }
        assertEquals(1, me.rank)
        assertEquals(14L, me.userId)
        assertEquals("테스터1", me.nickname)
    }

    /**
     * 🔴 이 행을 픽스처에서 빼지 마라.
     *
     * rank 5 는 `user_stats` row 자체가 없는 유저다. backend 가 이 실측을 위해 계정을 일부러 심었다가
     * 지웠고, 랭킹 native query 의 `LEFT JOIN` + `COALESCE 0` 폴백이 실제 SQL 에서 탔다는 유일한
     * 증거가 이 행이다. 0 이 도메인까지 그대로 오면 명단 캡션이 "아직 기록 없음" 으로 떨어진다.
     */
    @Test
    fun `실서버의 user_stats row 가 없는 유저가 totalChallenges 0 으로 도착한다`() {
        val newcomer = parseReal().toLoserRankingEntries().last()

        assertEquals(5, newcomer.rank)
        assertEquals(17L, newcomer.userId)
        assertEquals(0, newcomer.totalChallenges)
        assertEquals(0, newcomer.losses)
        assertEquals(0, newcomer.lossRate)
        assertEquals(0, newcomer.currentLossStreak)
    }

    /**
     * 포디움 캡션의 연패 절 생략 분기(`LoserRankingItemState.podiumCaptionText`)가 가정이 아니라
     * 실데이터라는 증거다. 개발 DB 로 랭킹을 처음 띄우면 1위가 바로 이 상태다.
     */
    @Test
    fun `실서버 1위가 연패 0 이다`() {
        val first = parseReal().toLoserRankingEntries().first()

        assertEquals(1, first.rank)
        assertEquals(4, first.losses)
        assertEquals(80, first.lossRate)
        assertEquals(0, first.currentLossStreak)
    }

    @Test
    fun `실 URL 이 담긴 profileImageUrl 이 그대로 파싱되고 항목이 살아남는다`() {
        // 파싱과 표시는 별개다 — 화면은 이니셜 placeholder 를 유지한다. 여기서 고정하는 건 파싱뿐이다.
        assertEquals(JsonPrimitive(REAL_PROFILE_IMAGE_URL), rawRealEntryAt(2)["profileImageUrl"])

        val entries = parseReal().toLoserRankingEntries()

        assertEquals(5, entries.size)
        assertEquals(REAL_PROFILE_IMAGE_URL, entries.first { it.userId == 1L }.profileImageUrl)
        assertEquals(4, entries.count { it.profileImageUrl == null })
    }

    /**
     * 🔴 앱이 랭킹을 재정렬하는 회귀를 잡는 단언.
     *
     * 서버 1차 정렬 키는 `losses DESC` 다. 실서버 데이터에서 rank 2·4 는 패배율 100 인데 rank 1 은 80 이라,
     * 앱이 `lossRate` 로 다시 줄 세우는 순간 이 순서가 깨진다. 패배율이 내림차순이 **아니라는** 것까지
     * 못 박아 둬야 "패배율 랭킹이니 패배율로 정렬" 이라는 그럴듯한 오해가 코드로 들어올 때 걸린다.
     */
    @Test
    fun `서버 순서가 보존되며 패배율 내림차순이 아니다`() {
        val entries = parseReal().toLoserRankingEntries()

        assertEquals(listOf(1, 2, 3, 4, 5), entries.map { it.rank })
        assertEquals(listOf(14L, 15L, 1L, 16L, 17L), entries.map { it.userId })
        assertEquals(
            listOf(4, 3, 3, 2, 0),
            entries.map { it.losses },
            "1차 정렬 키는 losses 내림차순이다",
        )

        val lossRates = entries.map { it.lossRate }
        assertEquals(listOf(80, 100, 75, 100, 0), lossRates)
        assertNotEquals(
            lossRates.sortedDescending(),
            lossRates,
            "앱이 lossRate 로 재정렬하면 이 순서가 된다. 서버가 준 순서가 정본이라 이렇게 되면 안 된다",
        )
    }
}

private const val REAL_PROFILE_IMAGE_URL =
    "http://img1.kakaocdn.net/thumb/R640x640.q70/?fname=http%3A%2F%2Ft1.kakaocdn.net%2Faccount_images%2Fdefault_profile.jpeg"
