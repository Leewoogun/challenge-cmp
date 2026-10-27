package com.lwg.challenge.domain.usecase

import com.lwg.challenge.domain.model.notification.Notification
import com.lwg.challenge.domain.repository.NotificationRepository
import com.lwg.challenge.domain.usecase.paging.PaginatedUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val PAGE_SIZE = 20

/** 알림 목록 페이지네이션. 다음 키(nextNotificationId)와 끝 여부(isPageEnd)는 서버 값을 그대로 쓴다. */
class GetNotificationsUseCase(
    private val notificationRepository: NotificationRepository,
) : PaginatedUseCase<Long, Notification>() {

    operator fun invoke(onError: (String) -> Unit): Flow<NotificationsResult> =
        pagingSource.flow(
            key = Unit,
            fetch = { nextNotificationId ->
                notificationRepository.getNotifications(
                    nextNotificationId = nextNotificationId,
                    size = PAGE_SIZE,
                    onError = onError,
                )
            },
            getItems = { it.notifications },
            getNextKey = { it.nextNotificationId },
            getIsPageEnd = { it.isPageEnd },
            keySelector = { it.notificationId },
        ).map { pagingResult ->
            NotificationsResult(
                notifications = pagingResult.items,
                isPageEnd = pagingResult.isPageEnd,
            )
        }
}

data class NotificationsResult(
    val notifications: List<Notification>,
    val isPageEnd: Boolean,
)
