package com.lwg.challenge.feature.notification.contract

import com.lwg.challenge.domain.model.notification.Notification
import com.lwg.challenge.navigation.Route
import com.lwg.challenge.push.PushEvent
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * 알림 1행의 목적지와 표시 규칙.
 *
 * 타입 문자열 자체가 서버 어휘와 맞는지는 `PushEventTest` 가 리터럴로 고정한다. 여기서는 그 어휘를
 * 상수로 참조해 **어떤 타입이 어디로 가는지**만 본다 — 같은 알림이 푸시로 눌렸을 때와 목록에서
 * 눌렸을 때 다른 화면으로 가는 날이 오면 이 파일이 깨진다.
 */
class NotificationItemStateTest {

    private val now = LocalDateTime(2026, 9, 1, 15, 0, 0)

    private fun item(
        type: String,
        referenceId: Long? = 1005L,
        createdAt: LocalDateTime = now,
        body: String = "민수님이 도전장을 보냈어요",
        notificationId: Long = 7L,
    ) = NotificationItemState(
        Notification(
            notificationId = notificationId,
            type = type,
            body = body,
            referenceId = referenceId,
            createdAt = createdAt,
        ),
        now,
    )

    /** 수락·거절 액션이 홈 '받은 도전장' 섹션에만 있다. */
    @Test
    fun `도전장 도착은 홈으로 보낸다`() {
        assertEquals(Route.Home, item(PushEvent.TYPE_CHALLENGE_REQUEST).route)
    }

    @Test
    fun `수락 알림은 챌린지 상세로 보낸다`() {
        assertEquals(
            Route.Challenge.Detail(1005L),
            item(PushEvent.TYPE_CHALLENGE_ACCEPTED, referenceId = 1005L).route,
        )
    }

    /** 거절된 챌린지는 계약서가 없어 상세에 볼 것이 없다. */
    @Test
    fun `거절 알림은 홈으로 보낸다`() {
        assertEquals(Route.Home, item(PushEvent.TYPE_CHALLENGE_REJECTED).route)
    }

    /** 거절과 달리 볼 것이 생겼고, 상대 인증 사진은 상세에서만 보인다. */
    @Test
    fun `상대 인증 알림은 챌린지 상세로 보낸다`() {
        assertEquals(
            Route.Challenge.Detail(2001L),
            item(PushEvent.TYPE_OPPONENT_VERIFIED, referenceId = 2001L).route,
        )
    }

    /**
     * 서버 enum 8종 중 4종만 발송 중이라 값 추가가 예정돼 있다. 모르는 타입이 와도 목록에서
     * 숨기지 않고 탭만 막는다 — 갈 곳을 모르는 채 화면을 여는 것보다 낫다.
     */
    @Test
    fun `모르는 타입은 목적지가 없고 탭할 수 없다`() {
        val state = item("FRIEND_REQUEST")

        assertNull(state.route)
        assertFalse(state.isTappable)
    }

    @Test
    fun `아는 타입이어도 referenceId 가 없으면 목적지가 없다`() {
        val state = item(PushEvent.TYPE_CHALLENGE_ACCEPTED, referenceId = null)

        assertNull(state.route)
        assertFalse(state.isTappable)
    }

    @Test
    fun `목적지가 있으면 탭할 수 있다`() {
        assertTrue(item(PushEvent.TYPE_CHALLENGE_REQUEST).isTappable)
    }

    @Test
    fun `경과 시간은 주입한 기준 시각으로 계산한다`() {
        val minutesAgo = item(
            PushEvent.TYPE_CHALLENGE_REQUEST,
            createdAt = LocalDateTime(2026, 9, 1, 14, 30, 0),
        )
        val yesterday = item(
            PushEvent.TYPE_CHALLENGE_REQUEST,
            createdAt = LocalDateTime(2026, 8, 31, 15, 0, 0),
        )

        assertEquals("30분 전", minutesAgo.elapsedText)
        assertEquals("어제", yesterday.elapsedText)
    }

    /** 문구는 서버가 발송 시점에 박제한다 — 앱이 타입을 보고 다시 조립하지 않는다. */
    @Test
    fun `식별자와 타입과 문구는 모델 값 그대로 나온다`() {
        val state = item(
            PushEvent.TYPE_OPPONENT_VERIFIED,
            body = "민수님이 인증을 마쳤어요",
            notificationId = 57L,
        )

        assertEquals(57L, state.notificationId)
        assertEquals(PushEvent.TYPE_OPPONENT_VERIFIED, state.type)
        assertEquals("민수님이 인증을 마쳤어요", state.body)
    }
}
