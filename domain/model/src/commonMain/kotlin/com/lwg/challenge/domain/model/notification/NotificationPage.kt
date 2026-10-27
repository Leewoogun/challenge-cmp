package com.lwg.challenge.domain.model.notification

/**
 * 알림 목록 한 쪽.
 *
 * @property notifications 최신순. 서버가 준 순서를 앱이 재정렬하지 않는다
 * @property nextNotificationId 다음 쪽 요청에 **그대로 되돌려 보낼** 값(다음 쪽 첫 항목의 id). 서버가 이 값을
 *   포함해 내려주므로, 앱이 마지막 항목 id 로 키를 만들면 경계 항목이 중복된다
 * @property isPageEnd 마지막 쪽인가. 더 받을지는 이 값으로만 판단한다
 */
data class NotificationPage(
    val notifications: List<Notification>,
    val nextNotificationId: Long?,
    val isPageEnd: Boolean,
)
