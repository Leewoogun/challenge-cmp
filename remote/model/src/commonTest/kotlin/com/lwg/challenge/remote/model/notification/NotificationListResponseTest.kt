package com.lwg.challenge.remote.model.notification

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

/**
 * 실제로 내려온 응답 원문을 그대로 역직렬화한다.
 *
 * DTO 를 손으로 만들어 비교하면 이 계층에서 유일하게 틀릴 수 있는 것 — **JSON 이 필드에 어떻게
 * 실려 오는가** — 을 건너뛴다. 그래서 원문 문자열이 픽스처다.
 */
class NotificationListResponseTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val wire = """
        {"data":{"notifications":[
          {"notificationId":7,"type":"CHALLENGE_REQUEST","body":"아직 없는 타입","referenceId":1005,"createdAt":"2026-09-01 15:00:00"},
          {"notificationId":6,"type":"FRIEND_REQUEST","body":"유저2님이 친구 요청","referenceId":null,"createdAt":"2026-09-01 14:00:00"}
         ],"nextCursor":null},
         "error":false,"code":200,"message":""}
    """.trimIndent()

    private fun decode(body: String = wire) = json.decodeFromString<NotificationListResponse>(body)

    @Test
    fun `알림 한 건의 다섯 필드가 전부 제 값으로 들어온다`() {
        val first = decode().data.notifications.first()

        assertEquals(7L, first.notificationId)
        assertEquals("CHALLENGE_REQUEST", first.type)
        assertEquals("아직 없는 타입", first.body)
        assertEquals(1005L, first.referenceId)
        assertEquals(LocalDateTime(2026, 9, 1, 15, 0, 0), first.createdAt)
    }

    @Test
    fun `envelope 세 필드도 함께 읽힌다`() {
        val response = decode()

        assertFalse(response.error)
        assertEquals(200, response.code)
        assertEquals("", response.message)
    }

    /** 서버가 null 을 생략하지 않고 키째 싣는다. 기본값으로 흡수된 것과 구분이 안 되면 안 된다. */
    @Test
    fun `키가 실린 null 은 null 로 들어온다`() {
        val response = decode()

        assertNull(response.data.notifications[1].referenceId)
        assertNull(response.data.nextCursor)
    }

    /**
     * type 을 enum 으로 받지 않은 이유가 이 한 줄이다 — typed 였다면 이 응답 전체가 터져
     * 알림 화면이 통째로 비고, 아는 타입 한 건까지 함께 사라진다.
     */
    @Test
    fun `앱이 모르는 type 이 와도 응답이 깨지지 않고 원문 문자열 그대로 남는다`() {
        val notifications = decode().data.notifications

        assertEquals(2, notifications.size)
        assertEquals("FRIEND_REQUEST", notifications[1].type)
        assertEquals("유저2님이 친구 요청", notifications[1].body)
    }

    /** 목록은 body 만 그리고 읽음은 화면 단위라, 두 필드는 누락이 아니라 계약상 부재다. */
    @Test
    fun `title 과 isRead 가 응답에 없어도 파싱된다`() {
        val response = decode()

        assertEquals(listOf(7L, 6L), response.data.notifications.map { it.notificationId })

        val encoded = json.encodeToString(NotificationListResponse.serializer(), response)
        assertFalse(encoded.contains("title"))
        assertFalse(encoded.contains("isRead"))
    }

    /**
     * wire 포맷은 공백 구분이라 ISO 를 읽지 않는다. 그 한 행이 null 로 떨어져도 응답 전체와
     * 나머지 행은 살아야 한다 — 시간 문자열 하나에 화면이 통째로 비면 안 된다.
     */
    @Test
    fun `T 구분자가 든 createdAt 은 그 행만 null 이 된다`() {
        val isoWire = wire.replace("\"2026-09-01 15:00:00\"", "\"2026-09-01T15:00:00\"")

        val notifications = decode(isoWire).data.notifications

        assertEquals(2, notifications.size)
        assertNull(notifications[0].createdAt)
        assertEquals(1005L, notifications[0].referenceId)
        assertEquals(LocalDateTime(2026, 9, 1, 14, 0, 0), notifications[1].createdAt)
    }
}
