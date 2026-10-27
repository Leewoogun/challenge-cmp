package com.lwg.challenge.remote.mapper

import com.lwg.challenge.domain.model.notification.Notification
import com.lwg.challenge.domain.model.notification.NotificationPage
import com.lwg.challenge.remote.model.notification.NotificationDto
import com.lwg.challenge.remote.model.notification.NotificationListResponse
import com.lwg.challenge.remote.model.notification.UnreadCountResponse

/**
 * 서버가 준 순서(`id DESC` = 최신순)를 그대로 보존한다 — 앱은 재정렬하지 않는다.
 *
 * [NotificationPage.nextNotificationId] 와 [NotificationPage.isPageEnd] 는 손대지 않고 통과시킨다. 항목이 걸러져도
 * 두 값은 서버가 정한 그대로여야 다음 쪽이 어긋나지 않는다.
 */
fun NotificationListResponse.toNotificationPage(): NotificationPage = NotificationPage(
    notifications = data.notifications.mapNotNull(NotificationDto::toNotificationOrNull),
    nextNotificationId = data.nextNotificationId,
    isPageEnd = data.isPageEnd,
)

fun UnreadCountResponse.toUnreadCount(): Int = data.unreadCount

/**
 * 🔴 **모르는 `type` 으로는 행을 버리지 않는다.** 문자열을 그대로 통과시키고, 표시(무채색 아이콘)와
 * 탭 가능 여부(`PushEvent.of` → null → 비탭) 판정은 화면이 한다. 여기서 걸러 내면 푸시로 본 알림이
 * 목록에서 사라지고 홈 뱃지 개수와 목록 길이가 어긋난다.
 *
 * 유일하게 행을 버리는 경우가 `createdAt` 파싱 실패다. 이것은 확장이 예정된 열린 집합이 아니라
 * **wire 포맷 위반**(ADR-0010)이고, 카드가 그리는 두 값 중 하나라 없으면 그릴 자리가 정해지지
 * 않는다. 보관함이 `challengeDate` 를 못 읽은 항목을 드롭하는 것과 같은 판단이다.
 */
private fun NotificationDto.toNotificationOrNull(): Notification? {
    val createdAt = createdAt ?: return null
    return Notification(
        notificationId = notificationId,
        type = type,
        body = body,
        referenceId = referenceId,
        referencePending = referencePending,
        createdAt = createdAt,
    )
}
