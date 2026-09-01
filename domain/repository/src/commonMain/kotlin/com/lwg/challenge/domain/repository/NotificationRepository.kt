package com.lwg.challenge.domain.repository

import com.lwg.challenge.domain.model.notification.NotificationPage
import kotlinx.coroutines.flow.Flow

/**
 * 알림 목록 Repository.
 *
 * 목록 조회와 읽음 처리는 완전히 분리돼 있다 — [getNotifications] 는 읽음 처리를 하지 않으므로
 * (부수 효과 있는 GET 이 아니다) 읽음은 [markAllAsRead] 를 명시적으로 부를 때만 일어난다.
 */
interface NotificationRepository {

    /**
     * @param cursor 직전 응답의 [NotificationPage.nextCursor] 를 그대로 넘긴다. null 이면 맨 앞부터
     * @param size 한 번에 받을 개수. `1..50` 밖은 서버가 거부한다
     */
    fun getNotifications(
        cursor: Long?,
        size: Int,
        onError: (String) -> Unit,
    ): Flow<NotificationPage>

    /** 안 읽은 개수. 홈 벨 뱃지가 이 값 하나만 보고 켜진다. */
    fun getUnreadCount(onError: (String) -> Unit): Flow<Int>

    /** 전부 읽음 처리(멱등). 갱신 후 실제로 센 개수가 흐른다 — 앱이 0 을 가정하지 않는다. */
    fun markAllAsRead(onError: (String) -> Unit): Flow<Int>
}
