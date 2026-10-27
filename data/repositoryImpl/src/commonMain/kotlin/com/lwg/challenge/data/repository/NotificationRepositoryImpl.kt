package com.lwg.challenge.data.repository

import com.lwg.challenge.data.datasource.remote.NotificationRemoteDataSource
import com.lwg.challenge.domain.model.notification.NotificationPage
import com.lwg.challenge.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single(binds = [NotificationRepository::class])
class NotificationRepositoryImpl(
    private val notificationRemoteDataSource: NotificationRemoteDataSource,
) : NotificationRepository {

    override fun getNotifications(
        nextNotificationId: Long?,
        size: Int,
        onError: (String) -> Unit,
    ): Flow<NotificationPage> = notificationRemoteDataSource.getNotifications(
        nextNotificationId = nextNotificationId,
        size = size,
        onError = onError,
    )

    override fun getUnreadCount(onError: (String) -> Unit): Flow<Int> =
        notificationRemoteDataSource.getUnreadCount(onError)

    override fun markAllAsRead(onError: (String) -> Unit): Flow<Int> =
        notificationRemoteDataSource.markAllAsRead(onError)
}
