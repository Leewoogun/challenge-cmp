package com.lwg.challenge.remote.model.serializer

import com.lwg.challenge.remote.model.challenge.ReceivedChallengesResponse
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WireDateTimeSerializersTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private fun body(deadline: String, createdAt: String = "\"2026-07-31 09:31:01\"") = """
        { "error": false, "code": 200, "message": "",
          "data": { "challenges": [
            { "challengeId": 7, "betContent": "커피 사기",
              "opponent": { "id": 42, "nickname": "민수",
                "profileImageUrl": "https://cdn/u/42.jpg", "mission": "오늘 운동 1시간 하기" },
              "challengeDate": "2026-07-31",
              "deadline": $deadline,
              "createdAt": $createdAt }
          ] } }
    """.trimIndent()

    private fun decodeFirst(deadline: String, createdAt: String = "\"2026-07-31 09:31:01\"") =
        json.decodeFromString<ReceivedChallengesResponse>(body(deadline, createdAt))
            .data.challenges.single()

    @Test
    fun `공백 구분 wire 포맷을 파싱한다`() {
        val challenge = decodeFirst("\"2026-08-01 00:00:00\"")

        assertEquals(LocalDateTime(2026, 8, 1, 0, 0, 0), challenge.deadline)
        assertEquals(LocalDateTime(2026, 7, 31, 9, 31, 1), challenge.createdAt)
        assertEquals(LocalDate(2026, 7, 31), challenge.challengeDate)
    }

    /** 서버가 ISO(T 구분자)로 바꿔 보내도 응답 전체가 터지지 않고 해당 필드만 비어야 한다. */
    @Test
    fun `ISO 구분자는 파싱하지 않고 null 로 떨어진다`() {
        assertNull(decodeFirst("\"2026-08-01T00:00:00\"").deadline)
    }

    @Test
    fun `깨진 문자열은 null 로 떨어진다`() {
        assertNull(decodeFirst("\"garbage\"").deadline)
    }

    @Test
    fun `JSON null 과 문자열 아닌 값도 null 로 흡수한다`() {
        assertNull(decodeFirst("null").deadline)
        assertNull(decodeFirst("12345").deadline)
    }

    @Test
    fun `필드 누락은 기본값 null`() {
        val missing = """
            { "error": false, "code": 200, "message": "",
              "data": { "challenges": [ { "challengeId": 7, "opponent": { "id": 42 } } ] } }
        """.trimIndent()

        val challenge = json.decodeFromString<ReceivedChallengesResponse>(missing)
            .data.challenges.single()

        assertNull(challenge.deadline)
        assertNull(challenge.createdAt)
        assertNull(challenge.challengeDate)
    }

    @Test
    fun `직렬화하면 wire 포맷으로 되돌아간다`() {
        val encoded = json.encodeToString(
            ReceivedChallengesResponse.serializer(),
            decodeFirst("\"2026-08-01 00:00:00\"").let { challenge ->
                ReceivedChallengesResponse(
                    data = com.lwg.challenge.remote.model.challenge.ReceivedChallengesData(
                        challenges = listOf(challenge),
                    ),
                )
            },
        )

        assertEquals(true, encoded.contains("\"deadline\":\"2026-08-01 00:00:00\""))
        assertEquals(true, encoded.contains("\"challengeDate\":\"2026-07-31\""))
    }
}
