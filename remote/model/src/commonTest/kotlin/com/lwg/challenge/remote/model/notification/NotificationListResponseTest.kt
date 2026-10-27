package com.lwg.challenge.remote.model.notification

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

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

    /**
     * 네 행 모두 `referencePending` 키를 달고 있고 값만 다르다 — 대기 중 신청(`true`), 대기 개념이
     * 없는 상대 인증(`null`), 응답이 끝난 신청(`false`), 모르는 타입(`null`).
     */
    private val wire = """
        {"data":{"notifications":[
          {"notificationId":24,"type":"OPPONENT_VERIFIED","body":"증거","referenceId":2001,"referencePending":null,"createdAt":"2026-09-01 20:04:00"},
          {"notificationId":20,"type":"CHALLENGE_REQUEST","body":"대기중 신청","referenceId":2001,"referencePending":true,"createdAt":"2026-09-01 20:00:00"},
          {"notificationId":7,"type":"CHALLENGE_REQUEST","body":"아직 없는 타입","referenceId":1005,"referencePending":false,"createdAt":"2026-09-01 15:00:00"},
          {"notificationId":6,"type":"FRIEND_REQUEST","body":"유저2님이 친구 요청","referenceId":null,"referencePending":null,"createdAt":"2026-09-01 14:00:00"}
         ],"nextNotificationId":null,"isPageEnd":true},
         "error":false,"code":200,"message":""}
    """.trimIndent()

    private fun decode(body: String = wire) = json.decodeFromString<NotificationListResponse>(body)

    @Test
    fun `알림 한 건의 여섯 필드가 전부 제 값으로 들어온다`() {
        val request = decode().data.notifications[1]

        assertEquals(20L, request.notificationId)
        assertEquals("CHALLENGE_REQUEST", request.type)
        assertEquals("대기중 신청", request.body)
        assertEquals(2001L, request.referenceId)
        assertEquals(true, request.referencePending)
        assertEquals(LocalDateTime(2026, 9, 1, 20, 0, 0), request.createdAt)
    }

    /**
     * 🔴 세 값이 서로 다른 사실이라 어느 쪽으로도 접히면 안 된다. 목록 탭 게이트가 이 값을 그대로
     * 보고 판정하므로, `false` 가 `null` 로 뭉개지면 끝난 신청의 탭이 열린다.
     */
    @Test
    fun `referencePending 은 true 와 false 와 null 이 전부 제 값으로 들어온다`() {
        val notifications = decode().data.notifications

        assertNull(notifications[0].referencePending)
        assertEquals(true, notifications[1].referencePending)
        assertEquals(false, notifications[2].referencePending)
        assertNull(notifications[3].referencePending)
    }

    @Test
    fun `envelope 세 필드도 함께 읽힌다`() {
        val response = decode()

        assertFalse(response.error)
        assertEquals(200, response.code)
        assertEquals("", response.message)
    }

    /**
     * 서버가 null 을 생략하지 않고 키째 싣는다. 원문에 키가 있다는 사실 자체를 함께 고정한다 —
     * 파싱 결과만 보면 키가 빠져 기본값으로 흡수된 응답과 구분되지 않는다.
     */
    @Test
    fun `키가 실린 null 은 null 로 들어온다`() {
        val response = decode()

        assertTrue(wire.contains("\"referencePending\":null"))
        assertNull(response.data.notifications[0].referencePending)
        assertNull(response.data.notifications[3].referenceId)
        assertNull(response.data.nextNotificationId)
    }

    /** 다음 값은 이번 쪽 마지막 항목(7)이 아니라 다음 쪽 첫 항목(5)의 id 다. 계산 없이 원문 그대로 들어와야 한다. */
    @Test
    fun `중간 쪽은 nextNotificationId 값과 isPageEnd false 로 들어온다`() {
        val data = decode(
            """
            {"data":{"notifications":[
              {"notificationId":9,"type":"CHALLENGE_REQUEST","body":"a","referenceId":1,"referencePending":true,"createdAt":"2026-10-26 10:00:00"},
              {"notificationId":7,"type":"CHALLENGE_REQUEST","body":"b","referenceId":2,"referencePending":true,"createdAt":"2026-10-26 09:00:00"}
             ],"nextNotificationId":5,"isPageEnd":false},
             "error":false,"code":200,"message":""}
            """.trimIndent(),
        ).data

        assertEquals(5L, data.nextNotificationId)
        assertFalse(data.isPageEnd)
    }

    @Test
    fun `끝 쪽은 nextNotificationId null 과 isPageEnd true 로 들어온다`() {
        val data = decode().data

        assertTrue(wire.contains("\"nextNotificationId\":null"))
        assertNull(data.nextNotificationId)
        assertTrue(data.isPageEnd)
    }

    @Test
    fun `0건 응답은 빈 목록과 isPageEnd true 다`() {
        val data = decode(
            """{"data":{"notifications":[],"nextNotificationId":null,"isPageEnd":true},"error":false,"code":200,"message":""}""",
        ).data

        assertTrue(data.notifications.isEmpty())
        assertNull(data.nextNotificationId)
        assertTrue(data.isPageEnd)
    }

    /** DTO 가 v2 키를 읽지 않는다는 것을 고정한다 — 읽어서 다음 요청에 실으면 서버가 조용히 무시해 첫 쪽이 반복된다. */
    @Test
    fun `v2 키만 있고 새 키가 없는 응답이면 nextNotificationId 는 null 이다`() {
        val data = decode(
            """{"data":{"notifications":[],"nextCursor":42},"error":false,"code":200,"message":""}""",
        ).data

        assertNull(data.nextNotificationId)
    }

    /**
     * type 을 enum 으로 받지 않은 이유가 이 한 줄이다 — typed 였다면 이 응답 전체가 터져
     * 알림 화면이 통째로 비고, 아는 타입 한 건까지 함께 사라진다.
     */
    @Test
    fun `앱이 모르는 type 이 와도 응답이 깨지지 않고 원문 문자열 그대로 남는다`() {
        val notifications = decode().data.notifications

        assertEquals(4, notifications.size)
        assertEquals("FRIEND_REQUEST", notifications[3].type)
        assertEquals("유저2님이 친구 요청", notifications[3].body)
    }

    /** 목록은 body 만 그리고 읽음은 화면 단위라, 두 필드는 누락이 아니라 계약상 부재다. */
    @Test
    fun `title 과 isRead 가 응답에 없어도 파싱된다`() {
        val response = decode()

        assertEquals(listOf(24L, 20L, 7L, 6L), response.data.notifications.map { it.notificationId })

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

        assertEquals(4, notifications.size)
        assertNull(notifications[2].createdAt)
        assertEquals(1005L, notifications[2].referenceId)
        assertEquals(false, notifications[2].referencePending)
        assertEquals(LocalDateTime(2026, 9, 1, 14, 0, 0), notifications[3].createdAt)
    }
}
