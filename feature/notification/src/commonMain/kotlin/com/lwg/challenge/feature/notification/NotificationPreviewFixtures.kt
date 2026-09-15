package com.lwg.challenge.feature.notification

import com.lwg.challenge.domain.model.notification.Notification
import com.lwg.challenge.feature.notification.contract.NotificationItemState
import com.lwg.challenge.push.PushEvent
import kotlinx.datetime.LocalDateTime

/** 프리뷰의 고정 기준 시각. 아래 픽스처의 날짜 그룹과 시각 표기가 전부 이 값에서 나온다. */
internal val PreviewNotificationNow: LocalDateTime = LocalDateTime(2026, 9, 1, 13, 0)

/**
 * 알림 카드 프리뷰 픽스처. `body` 는 서버가 박제하는 실측 문형을 그대로 쓴다 — 앱이 문자열을 만들지
 * 않으므로 픽스처가 곧 계약이다.
 */
internal fun notificationPreviewItem(
    notificationId: Long = 1L,
    type: String = PushEvent.TYPE_CHALLENGE_REQUEST,
    body: String = "민수과 계약을 하시렵니까?",
    referenceId: Long? = 7L,
    // 기본 타입이 신청이라 기본값도 «대기 중» 이다. 신청 외 타입에는 대기라는 개념이 없어 null 을 넘긴다.
    referencePending: Boolean? = true,
    createdAt: LocalDateTime = LocalDateTime(2026, 9, 1, 11, 0),
    now: LocalDateTime = PreviewNotificationNow,
): NotificationItemState = NotificationItemState(
    Notification(
        notificationId = notificationId,
        type = type,
        body = body,
        referenceId = referenceId,
        referencePending = referencePending,
        createdAt = createdAt,
    ),
    now = now,
)

/**
 * 닉네임이 길어 메시지가 2줄이 되는 경우. 길이가 튀는 원인이 닉네임 하나라 `maxLines` 로 자르면
 * 하필 닉네임이 잘려 **누가 한 일인지 모르는 알림**이 된다. 카드가 상한을 두지 않는 이유이고,
 * `Alignment.Top` 이 이때 아이콘을 위에 붙이는지, 반대로 chevron 만 세로 중앙으로 빠지는지 눈으로
 * 확인하는 자리다.
 */
internal val PreviewLongBodyNotification: NotificationItemState = notificationPreviewItem(
    notificationId = 5L,
    type = PushEvent.TYPE_OPPONENT_VERIFIED,
    body = "오늘도지지않는불꽃같은도전자김준혁입니다님이 인증 사진을 올렸습니다",
    referencePending = null,
    createdAt = LocalDateTime(2026, 8, 31, 20, 0),
)

/**
 * 이미 응답했거나 만료된 신청. 🔴 위 [notificationPreviewItem] 기본값(대기 중 신청)과 **같은 타입**인데
 * chevron 이 없다 — 목록 탭 게이트가 타입만이 아니라 대상의 상태까지 보기 때문이다.
 *
 * 모르는 타입과 나란히 놓으면 chevron 이 죽는 이유가 둘이라는 것이 한눈에 보인다.
 */
internal val PreviewSettledRequestNotification: NotificationItemState = notificationPreviewItem(
    notificationId = 7L,
    body = "지훈과 계약을 하시렵니까?",
    referencePending = false,
    createdAt = LocalDateTime(2026, 9, 1, 10, 30),
)

/**
 * 앱이 모르는 타입. 폐기된 `SIGN_REQUEST` 가 옛 row 로 남아 있는 실제 사례다.
 *
 * 무채색 아이콘으로 그려지고 탭이 죽지만 **흐려지지도, 숨겨지지도 않는다** — 사람은 `body` 를
 * 읽을 수 있고, 홈 뱃지 개수와 목록 길이도 어긋나지 않는다.
 */
internal val PreviewUnknownTypeNotification: NotificationItemState = notificationPreviewItem(
    notificationId = 6L,
    type = "SIGN_REQUEST",
    body = "계약서에 서명해주세요",
    referencePending = null,
    createdAt = LocalDateTime(2026, 8, 28, 9, 0),
)

/**
 * 목록 다건 픽스처. 발송 5종 + 모르는 타입 + 2줄 메시지가 전부 들어 있다.
 *
 * 🔴 **세 날짜에 걸쳐 있다** — 기준 시각(9/1 13:00)에서 `오늘` 5건 · `어제` 2건 · `8월 28일` 1건으로
 * 헤더가 셋 나온다. 날짜별 건수를 다르게 둔 것은 그룹 경계가 눈에 띄게 하려는 것이다.
 *
 * 시각 표기는 그룹에 따라 갈린다 — `오늘` 그룹만 상대 표기(`"방금 전"`·`"10분 전"`·`"2시간 전"`)이고
 * 나머지는 `HH:mm` 이다. 날짜는 헤더가 이미 말했다.
 *
 * 🔴 chevron 이 붙는 행은 셋뿐이다 — 상대 인증 둘과 **대기 중인** 신청 하나. 수락·거절·기권은
 * 목록에서 갈 곳이 없고, 응답이 끝난 신청과 모르는 타입도 마찬가지다. 색도 문구도 같은 채
 * chevron 만 갈린다.
 *
 * 서버가 주는 최신순을 그대로 둔다 — 앱은 재정렬하지 않고 연속 런만 묶는다.
 */
internal val PreviewNotifications: List<NotificationItemState> = listOf(
    notificationPreviewItem(
        notificationId = 1L,
        type = PushEvent.TYPE_OPPONENT_VERIFIED,
        body = "민지님이 인증 사진을 올렸습니다",
        referencePending = null,
        createdAt = LocalDateTime(2026, 9, 1, 12, 59, 30),
    ),
    notificationPreviewItem(
        notificationId = 2L,
        type = PushEvent.TYPE_CHALLENGE_ACCEPTED,
        body = "준혁님이 수락했습니다",
        referencePending = null,
        createdAt = LocalDateTime(2026, 9, 1, 12, 50),
    ),
    // 기권 알림. referencePending 이 null 인 것은 계약이 정한 값이다 — 이 알림의 수신자에게는 응답할
    // 것이 없어 «대기» 라는 개념 자체가 없다. 값과 무관하게 타입 단계에서 비탭이라 chevron 도 없다.
    notificationPreviewItem(
        notificationId = 8L,
        type = PushEvent.TYPE_CHALLENGE_FORFEITED,
        body = "우건님이 기권했습니다",
        referencePending = null,
        createdAt = LocalDateTime(2026, 9, 1, 12, 20),
    ),
    notificationPreviewItem(
        notificationId = 3L,
        type = PushEvent.TYPE_CHALLENGE_REQUEST,
        body = "민수과 계약을 하시렵니까?",
        createdAt = LocalDateTime(2026, 9, 1, 11, 0),
    ),
    PreviewSettledRequestNotification,
    PreviewLongBodyNotification,
    notificationPreviewItem(
        notificationId = 4L,
        type = PushEvent.TYPE_CHALLENGE_REJECTED,
        body = "도윤님이 도망쳤습니다",
        referencePending = null,
        createdAt = LocalDateTime(2026, 8, 31, 10, 0),
    ),
    PreviewUnknownTypeNotification,
)
