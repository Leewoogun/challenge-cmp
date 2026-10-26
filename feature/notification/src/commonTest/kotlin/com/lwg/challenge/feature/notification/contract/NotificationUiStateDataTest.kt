package com.lwg.challenge.feature.notification.contract

import com.lwg.challenge.domain.model.notification.Notification
import com.lwg.challenge.navigation.Route
import com.lwg.challenge.push.PushEvent
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** 행의 목적지·탭 게이트와 시각 표기. 목적지 매핑 자체는 `PushEventRouteTest` 가 고정한다. */
class NotificationUiStateDataTest {

    private val now = LocalDateTime(2026, 9, 1, 15, 0, 0)

    private fun item(
        type: String,
        referenceId: Long? = 1005L,
        // 기본값이 «대기 중» 이라 게이트를 보지 않는 아래 표시 테스트들이 엉뚱한 이유로 깨지지 않는다.
        referencePending: Boolean? = true,
        createdAt: LocalDateTime = now,
        body: String = "민수님이 도전장을 보냈어요",
        notificationId: Long = 7L,
    ) = Notification(
        notificationId = notificationId,
        type = type,
        body = body,
        referenceId = referenceId,
        referencePending = referencePending,
        createdAt = createdAt,
    )

    private fun routeOf(notification: Notification) = NotificationUiState.Data(listOf(notification), now).routeOf(notification)

    private fun timeTextOf(notification: Notification) =
        NotificationUiState.Data(listOf(notification), now).timeTextOf(notification)

    /** 수락·거절 액션이 홈 '받은 도전장' 섹션에만 있다. */
    @Test
    fun `응답을 기다리는 신청은 홈으로 보낸다`() {
        val state = item(PushEvent.TYPE_CHALLENGE_REQUEST, referencePending = true)

        assertEquals(Route.Home, routeOf(state))
    }

    /** 이미 응답했거나 만료된 신청은 홈에서 사라져, 보내면 빈 자리에 닿는다. */
    @Test
    fun `응답이 끝난 신청은 목적지가 없고 탭할 수 없다`() {
        val state = item(PushEvent.TYPE_CHALLENGE_REQUEST, referencePending = false)

        assertNull(routeOf(state))
    }

    /** null 을 비탭으로 접지 않았다 — 상대 인증은 이 값이 늘 null 이다. */
    @Test
    fun `상대 인증 알림은 referencePending 이 null 이어도 상세로 보낸다`() {
        val state = item(
            PushEvent.TYPE_OPPONENT_VERIFIED,
            referenceId = 2001L,
            referencePending = null,
        )

        assertEquals(Route.Challenge.Detail(2001L), routeOf(state))
    }

    /** null 을 탭 가능으로 접지도 않았다 — 판정은 타입이 먼저 내린다. */
    @Test
    fun `수락과 거절 알림은 referencePending 이 null 이어도 탭할 수 없다`() {
        val accepted = item(PushEvent.TYPE_CHALLENGE_ACCEPTED, referencePending = null)
        val rejected = item(PushEvent.TYPE_CHALLENGE_REJECTED, referencePending = null)

        assertNull(routeOf(accepted))
        assertNull(routeOf(rejected))
    }

    /** 세 값을 다 넣어야 "타입이 닫았다" 와 "그 값이 닫았다" 가 갈린다. */
    @Test
    fun `기권 알림은 referencePending 이 무엇이든 목록에서 탭할 수 없다`() {
        listOf(true, false, null).forEach { pending ->
            val state = item(PushEvent.TYPE_CHALLENGE_FORFEITED, referencePending = pending)

            assertNull(routeOf(state), "referencePending = $pending")
        }
    }

    /** 모르는 타입은 숨기지 않고 탭만 막는다. */
    @Test
    fun `모르는 타입은 목적지가 없고 탭할 수 없다`() {
        val state = item("FRIEND_REQUEST")

        assertNull(routeOf(state))
    }

    @Test
    fun `아는 타입이고 대기 중이어도 referenceId 가 없으면 목적지가 없다`() {
        val state = item(
            PushEvent.TYPE_CHALLENGE_REQUEST,
            referenceId = null,
            referencePending = true,
        )

        assertNull(routeOf(state))
    }

    @Test
    fun `경과 시간은 주입한 기준 시각으로 계산한다`() {
        val minutesAgo = item(
            PushEvent.TYPE_CHALLENGE_REQUEST,
            createdAt = LocalDateTime(2026, 9, 1, 14, 30, 0),
        )

        assertEquals("30분 전", timeTextOf(minutesAgo))
    }

    /** 날짜는 헤더가 말해서 행은 그 날 안의 시각만 적는다. */
    @Test
    fun `오늘이 아닌 항목의 시각은 시 분 표기다`() {
        val yesterday = item(
            PushEvent.TYPE_CHALLENGE_REQUEST,
            createdAt = LocalDateTime(2026, 8, 31, 15, 0, 0),
        )
        val lastMonth = item(
            PushEvent.TYPE_CHALLENGE_REQUEST,
            createdAt = LocalDateTime(2026, 8, 28, 9, 5, 0),
        )

        assertEquals("15:00", timeTextOf(yesterday))
        assertEquals("09:05", timeTextOf(lastMonth))
    }

    @Test
    fun `날짜 그룹 라벨은 기준 시각의 날짜로 가른다`() {
        val today = item(
            PushEvent.TYPE_CHALLENGE_REQUEST,
            createdAt = LocalDateTime(2026, 9, 1, 0, 10, 0),
        )
        val yesterday = item(
            PushEvent.TYPE_CHALLENGE_REQUEST,
            createdAt = LocalDateTime(2026, 8, 31, 23, 50, 0),
        )
        val older = item(
            PushEvent.TYPE_CHALLENGE_REQUEST,
            createdAt = LocalDateTime(2026, 8, 28, 9, 0, 0),
        )

        val labels = NotificationUiState.Data(listOf(today, yesterday, older), now).dateGroups.map { it.label }
        assertEquals(listOf("오늘", "어제", "8월 28일"), labels)
    }
}
