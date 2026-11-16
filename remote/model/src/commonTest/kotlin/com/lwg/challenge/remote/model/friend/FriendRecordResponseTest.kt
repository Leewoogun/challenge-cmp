package com.lwg.challenge.remote.model.friend

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class FriendRecordResponseTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val wire = """
        {"data":{
          "friend":{"userId":42,"nickname":"수아","profileImageUrl":null},
          "score":{"myWins":2,"friendWins":1,"draws":1,"bothLose":1},
          "ongoing":[
            {"challengeId":31,"status":"IN_PROGRESS","sentByMe":true,"deadline":"2026-11-21 00:00:00","myMission":"책 한 권 읽기","friendMission":"러닝 5km","bet":"커피 사기"},
            {"challengeId":33,"status":"PENDING","sentByMe":false,"deadline":"2026-11-23 00:00:00","myMission":null,"friendMission":"물 2L 마시기","bet":"밥 사기"}
          ]},
         "error":false,"code":200,"message":""}
    """.trimIndent()

    private fun decode(body: String = wire) = json.decodeFromString<FriendRecordResponse>(body)

    @Test
    fun `친구와 스코어 4값이 제 값으로 들어온다`() {
        val data = decode().data

        assertEquals(42L, data.friend.userId)
        assertEquals("수아", data.friend.nickname)
        assertNull(data.friend.profileImageUrl)
        assertEquals(FriendScoreDto(myWins = 2, friendWins = 1, draws = 1, bothLose = 1), data.score)
    }

    @Test
    fun `받은 PENDING 은 sentByMe false 와 myMission null 로 들어온다`() {
        val pending = decode().data.ongoing[1]

        assertEquals(33L, pending.challengeId)
        assertEquals(FriendChallengeStatusDto.PENDING, pending.status)
        assertEquals(false, pending.sentByMe)
        assertNull(pending.myMission)
        assertEquals("물 2L 마시기", pending.friendMission)
        assertEquals(LocalDateTime(2026, 11, 23, 0, 0, 0), pending.deadline)
    }

    @Test
    fun `IN_PROGRESS 는 sentByMe true 와 두 미션이 그대로 들어온다`() {
        val inProgress = decode().data.ongoing[0]

        assertEquals(FriendChallengeStatusDto.IN_PROGRESS, inProgress.status)
        assertEquals(true, inProgress.sentByMe)
        assertEquals("책 한 권 읽기", inProgress.myMission)
        assertEquals("러닝 5km", inProgress.friendMission)
        assertEquals("커피 사기", inProgress.bet)
    }

    /** 키 누락이 false 로 흡수되면 받은 도전장이 상세로 열린다 — 조용히 넘어가지 않고 응답째 실패해야 한다. */
    @Test
    fun `sentByMe 키가 없으면 역직렬화가 실패한다`() {
        val missingSentByMe = wire.replace("\"sentByMe\":false,", "")

        assertFailsWith<SerializationException> { decode(missingSentByMe) }
    }

    @Test
    fun `진행 중이 없으면 빈 목록이다`() {
        val data = decode(
            """
            {"data":{"friend":{"userId":42,"nickname":"수아","profileImageUrl":null},
             "score":{"myWins":0,"friendWins":0,"draws":0,"bothLose":0},"ongoing":[]},
             "error":false,"code":200,"message":""}
            """.trimIndent(),
        ).data

        assertEquals(emptyList(), data.ongoing)
    }
}
