package com.lwg.challenge.feature.notification

import com.lwg.challenge.domain.model.notification.Notification
import com.lwg.challenge.push.PushEvent
import kotlinx.datetime.LocalDateTime

/** 프리뷰 기준 시각. 아래 픽스처의 날짜 그룹과 시각 표기가 이 값에서 나온다. */
internal val PreviewNotificationNow: LocalDateTime = LocalDateTime(2026, 9, 1, 13, 0)

/** `body` 는 서버가 박제하는 실제 문형 그대로다. */
internal fun notificationPreviewItem(
    notificationId: Long = 1L,
    type: String = PushEvent.TYPE_CHALLENGE_REQUEST,
    body: String = "민수과 계약을 하시렵니까?",
    referenceId: Long? = 7L,
    // 신청 외 타입에는 대기 개념이 없어 null 을 넘긴다.
    referencePending: Boolean? = true,
    createdAt: LocalDateTime = LocalDateTime(2026, 9, 1, 11, 0),
): Notification = Notification(
    notificationId = notificationId,
    type = type,
    body = body,
    referenceId = referenceId,
    referencePending = referencePending,
    createdAt = createdAt,
)

/** 긴 닉네임으로 메시지가 2줄. 닉네임이 잘리지 않는지, 아이콘은 위·chevron 은 가운데인지 본다. */
internal val PreviewLongBodyNotification: Notification = notificationPreviewItem(
    notificationId = 5L,
    type = PushEvent.TYPE_OPPONENT_VERIFIED,
    body = "오늘도지지않는불꽃같은도전자김준혁입니다님이 인증 사진을 올렸습니다",
    referencePending = null,
    createdAt = LocalDateTime(2026, 8, 31, 20, 0),
)

/** 이미 응답했거나 만료된 신청. 대기 중 신청과 같은 타입인데 chevron 이 없다. */
internal val PreviewSettledRequestNotification: Notification = notificationPreviewItem(
    notificationId = 7L,
    body = "지훈과 계약을 하시렵니까?",
    referencePending = false,
    createdAt = LocalDateTime(2026, 9, 1, 10, 30),
)

/** 앱이 모르는 타입(폐기된 `SIGN_REQUEST`). 무채색 아이콘에 탭은 막히지만 숨기지 않는다. */
internal val PreviewUnknownTypeNotification: Notification = notificationPreviewItem(
    notificationId = 6L,
    type = "SIGN_REQUEST",
    body = "계약서에 서명해주세요",
    referencePending = null,
    createdAt = LocalDateTime(2026, 8, 28, 9, 0),
)

/** 발송 5종 + 모르는 타입 + 2줄 메시지. 기준 시각에서 `오늘` 5건 · `어제` 2건 · `8월 28일` 1건으로 헤더가 셋 나온다. */
internal val PreviewNotifications: List<Notification> = listOf(
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
