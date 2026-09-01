package com.lwg.challenge.feature.notification

import com.lwg.challenge.domain.model.notification.Notification
import com.lwg.challenge.feature.notification.contract.NotificationItemState
import com.lwg.challenge.push.PushEvent
import kotlinx.datetime.LocalDateTime

/** 프리뷰의 고정 기준 시각. 아래 픽스처의 경과 시간 표기가 전부 이 값에서 나온다. */
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
    createdAt: LocalDateTime = LocalDateTime(2026, 9, 1, 11, 0),
    now: LocalDateTime = PreviewNotificationNow,
): NotificationItemState = NotificationItemState(
    Notification(
        notificationId = notificationId,
        type = type,
        body = body,
        referenceId = referenceId,
        createdAt = createdAt,
    ),
    now = now,
)

/**
 * 닉네임이 길어 메시지가 2줄이 되는 경우. 길이가 튀는 원인이 닉네임 하나라 `maxLines` 로 자르면
 * 하필 닉네임이 잘려 **누가 한 일인지 모르는 알림**이 된다. 카드가 상한을 두지 않는 이유이고,
 * `Alignment.Top` 이 이때 아이콘을 위에 붙이는지 눈으로 확인하는 자리다.
 */
internal val PreviewLongBodyNotification: NotificationItemState = notificationPreviewItem(
    notificationId = 5L,
    type = PushEvent.TYPE_OPPONENT_VERIFIED,
    body = "오늘도지지않는불꽃같은도전자김준혁입니다님이 인증 사진을 올렸습니다",
    createdAt = LocalDateTime(2026, 8, 31, 20, 0),
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
    createdAt = LocalDateTime(2026, 8, 28, 9, 0),
)

/**
 * 목록 다건 픽스처. 발송 4종 + 모르는 타입 + 2줄 메시지가 전부 들어 있고, 경과 시간 5가지 표기
 * (`"방금 전"`·`"N분 전"`·`"N시간 전"`·`"어제"`·`"M월 d일"`)도 한 번씩 나온다.
 *
 * 서버가 주는 최신순을 그대로 둔다 — 앱은 재정렬하지 않는다.
 */
internal val PreviewNotifications: List<NotificationItemState> = listOf(
    notificationPreviewItem(
        notificationId = 1L,
        type = PushEvent.TYPE_OPPONENT_VERIFIED,
        body = "민지님이 인증 사진을 올렸습니다",
        createdAt = LocalDateTime(2026, 9, 1, 12, 59, 30),
    ),
    notificationPreviewItem(
        notificationId = 2L,
        type = PushEvent.TYPE_CHALLENGE_ACCEPTED,
        body = "준혁님이 수락했습니다",
        createdAt = LocalDateTime(2026, 9, 1, 12, 50),
    ),
    notificationPreviewItem(
        notificationId = 3L,
        type = PushEvent.TYPE_CHALLENGE_REQUEST,
        body = "민수과 계약을 하시렵니까?",
        createdAt = LocalDateTime(2026, 9, 1, 11, 0),
    ),
    PreviewLongBodyNotification,
    notificationPreviewItem(
        notificationId = 4L,
        type = PushEvent.TYPE_CHALLENGE_REJECTED,
        body = "도윤님이 도망쳤습니다",
        createdAt = LocalDateTime(2026, 8, 31, 10, 0),
    ),
    PreviewUnknownTypeNotification,
)
