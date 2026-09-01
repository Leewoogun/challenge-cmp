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
 * 상수로 참조해 **목록에서 어떤 행이 어디로 가는지**를 본다.
 *
 * 🔴 목적지 한 벌(`toRoute`)은 푸시와 공유하지만 **탭 가능 여부는 목록만의 판정**이다 — 푸시에서
 * 상세로 가는 수락 알림이 여기서는 눌리지 않는 것이 그 차이다. 목적지 매핑 자체는 `PushEventRouteTest`
 * 가 고정하고, 이 파일은 그 위에 얹힌 게이트를 고정한다.
 */
class NotificationItemStateTest {

    private val now = LocalDateTime(2026, 9, 1, 15, 0, 0)

    private fun item(
        type: String,
        referenceId: Long? = 1005L,
        // 기본값이 «대기 중» 이라 게이트를 보지 않는 아래 표시 테스트들이 엉뚱한 이유로 깨지지 않는다.
        referencePending: Boolean? = true,
        createdAt: LocalDateTime = now,
        body: String = "민수님이 도전장을 보냈어요",
        notificationId: Long = 7L,
    ) = NotificationItemState(
        Notification(
            notificationId = notificationId,
            type = type,
            body = body,
            referenceId = referenceId,
            referencePending = referencePending,
            createdAt = createdAt,
        ),
        now,
    )

    /** 수락·거절 액션이 홈 '받은 도전장' 섹션에만 있다. */
    @Test
    fun `응답을 기다리는 신청은 홈으로 보낸다`() {
        val state = item(PushEvent.TYPE_CHALLENGE_REQUEST, referencePending = true)

        assertEquals(Route.Home, state.route)
        assertTrue(state.isTappable)
    }

    /**
     * 🔴 이 개정의 요지. 이미 응답했거나 만료된 신청은 홈 '받은 도전장' 섹션에서 사라져, 눌러 보내면
     * 사용자가 빈 자리에 닿는다. 타입은 아는데도 목적지를 내주지 않는 유일한 경우다.
     */
    @Test
    fun `응답이 끝난 신청은 목적지가 없고 탭할 수 없다`() {
        val state = item(PushEvent.TYPE_CHALLENGE_REQUEST, referencePending = false)

        assertNull(state.route)
        assertFalse(state.isTappable)
    }

    /**
     * 🔴 `null` 을 «비탭» 으로 접지 않았다는 증거. 상대 인증에는 대기라는 개념이 없어 이 필드가 늘
     * null 인데, null 을 막는 구현이면 계약이 *"항상 탭 가능"* 이라고 못박은 타입이 통째로 죽는다.
     */
    @Test
    fun `상대 인증 알림은 referencePending 이 null 이어도 상세로 보낸다`() {
        val state = item(
            PushEvent.TYPE_OPPONENT_VERIFIED,
            referenceId = 2001L,
            referencePending = null,
        )

        assertEquals(Route.Challenge.Detail(2001L), state.route)
        assertTrue(state.isTappable)
    }

    /**
     * 🔴 반대편 증거 — `null` 을 «탭 가능» 으로 접지도 않았다. 두 타입 다 이 필드가 늘 null 이라,
     * null 을 통과시키는 구현이면 여기가 열린다. 판정은 타입이 먼저 내리고 이 값은 신청에서만 본다.
     */
    @Test
    fun `수락과 거절 알림은 referencePending 이 null 이어도 탭할 수 없다`() {
        val accepted = item(PushEvent.TYPE_CHALLENGE_ACCEPTED, referencePending = null)
        val rejected = item(PushEvent.TYPE_CHALLENGE_REJECTED, referencePending = null)

        assertNull(accepted.route)
        assertFalse(accepted.isTappable)
        assertNull(rejected.route)
        assertFalse(rejected.isTappable)
    }

    /**
     * 서버 enum 8종 중 4종만 발송 중이라 값 추가가 예정돼 있다. 모르는 타입이 와도 목록에서
     * 숨기지 않고 탭만 막는다 — 갈 곳을 모르는 채 화면을 여는 것보다 낫다.
     *
     * 게이트가 아니라 그 앞의 `PushEvent.of` 가 막는다 — 모르는 타입은 게이트에 닿지도 않는다.
     */
    @Test
    fun `모르는 타입은 목적지가 없고 탭할 수 없다`() {
        val state = item("FRIEND_REQUEST")

        assertNull(state.route)
        assertFalse(state.isTappable)
    }

    @Test
    fun `아는 타입이고 대기 중이어도 referenceId 가 없으면 목적지가 없다`() {
        val state = item(
            PushEvent.TYPE_CHALLENGE_REQUEST,
            referenceId = null,
            referencePending = true,
        )

        assertNull(state.route)
        assertFalse(state.isTappable)
    }

    @Test
    fun `경과 시간은 주입한 기준 시각으로 계산한다`() {
        val minutesAgo = item(
            PushEvent.TYPE_CHALLENGE_REQUEST,
            createdAt = LocalDateTime(2026, 9, 1, 14, 30, 0),
        )

        assertEquals("30분 전", minutesAgo.timeText)
    }

    /**
     * 날짜는 그룹 헤더가 이미 말했다 — 행이 다시 말하면 `어제` 헤더 아래 `어제` 행이 반복된다
     * (design.md §3.6.1). 그래서 오늘이 아닌 항목은 경과가 아니라 그 날 안에서의 시각을 적는다.
     */
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

        assertEquals("15:00", yesterday.timeText)
        assertEquals("09:05", lastMonth.timeText)
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

        assertEquals("오늘", today.dateGroupLabel)
        assertEquals("어제", yesterday.dateGroupLabel)
        assertEquals("8월 28일", older.dateGroupLabel)
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
