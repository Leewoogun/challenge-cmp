package com.lwg.challenge.feature.home

import com.lwg.challenge.domain.model.notification.NotificationPage
import com.lwg.challenge.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow

class FakeNotificationRepository : NotificationRepository {

    var unreadCount: Int? = 0
    var error: String? = null

    var callCount: Int = 0
        private set

    // 실패 시 emit 하지 않는 것이 실제 구현 계약이다. 0 을 흘리면
    // GetHomeDataUseCase 의 onEmpty 폴백이 우회돼 회귀 테스트가 무의미해진다.
    override fun getUnreadCount(onError: (String) -> Unit): Flow<Int> = flow {
        callCount += 1
        val current = unreadCount
        val currentError = error
        when {
            currentError != null -> onError(currentError)
            current != null -> emit(current)
        }
    }

    override fun getNotifications(
        nextNotificationId: Long?,
        size: Int,
        onError: (String) -> Unit,
    ): Flow<NotificationPage> = emptyFlow()

    override fun markAllAsRead(onError: (String) -> Unit): Flow<Int> = emptyFlow()
}
