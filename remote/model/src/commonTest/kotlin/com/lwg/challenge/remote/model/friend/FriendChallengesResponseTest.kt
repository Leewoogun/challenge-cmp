package com.lwg.challenge.remote.model.friend

import com.lwg.challenge.remote.model.challenge.ChallengeOutcomeDto
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FriendChallengesResponseTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private fun decode(body: String) = json.decodeFromString<FriendChallengesResponse>(body)

    /** 다음 값은 이번 쪽 마지막 항목(17)이 아니라 다음 쪽 첫 항목(12)의 id 다. */
    @Test
    fun `중간 쪽은 카드 여섯 필드와 nextChallengeId 와 isPageEnd false 로 들어온다`() {
        val data = decode(
            """
            {"data":{"challenges":[
              {"challengeId":19,"challengeDate":"2026-11-02","myMission":"운동 1시간","friendMission":"책 30페이지","bet":"커피 사기","myResult":"WIN"},
              {"challengeId":17,"challengeDate":"2026-11-05","myMission":"a","friendMission":"b","bet":"c","myResult":"BOTH_LOSE"}
             ],"nextChallengeId":12,"isPageEnd":false},
             "error":false,"code":200,"message":""}
            """.trimIndent(),
        ).data

        val first = data.challenges[0]
        assertEquals(19L, first.challengeId)
        assertEquals(LocalDate(2026, 11, 2), first.challengeDate)
        assertEquals("운동 1시간", first.myMission)
        assertEquals("책 30페이지", first.friendMission)
        assertEquals("커피 사기", first.bet)
        assertEquals(ChallengeOutcomeDto.WIN, first.myResult)
        assertEquals(ChallengeOutcomeDto.BOTH_LOSE, data.challenges[1].myResult)
        assertEquals(12L, data.nextChallengeId)
        assertFalse(data.isPageEnd)
    }

    @Test
    fun `끝 쪽은 nextChallengeId null 과 isPageEnd true 로 들어온다`() {
        val data = decode(
            """{"data":{"challenges":[],"nextChallengeId":null,"isPageEnd":true},"error":false,"code":200,"message":""}""",
        ).data

        assertTrue(data.challenges.isEmpty())
        assertNull(data.nextChallengeId)
        assertTrue(data.isPageEnd)
    }

    /** false 로 떨어지면 null 키로 첫 쪽을 반복 요청한다. */
    @Test
    fun `isPageEnd 키가 빠지면 true 다`() {
        val data = decode(
            """{"data":{"challenges":[],"nextChallengeId":null},"error":false,"code":200,"message":""}""",
        ).data

        assertTrue(data.isPageEnd)
    }
}
