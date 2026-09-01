package com.lwg.challenge.remote.datasource

import com.lwg.challenge.data.datasource.remote.NotificationRemoteDataSource
import com.lwg.challenge.domain.model.notification.NotificationPage
import com.lwg.challenge.remote.api.NotificationApi
import com.lwg.challenge.remote.mapper.toNotificationPage
import com.lwg.challenge.remote.mapper.toUnreadCount
import com.lwg.challenge.remote.network.util.suspendOnFailureWithErrorHandling
import com.lwg.challenge.remote.network.util.suspendOnSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Single

@Single(binds = [NotificationRemoteDataSource::class])
class NotificationRemoteDataSourceImpl(
    private val notificationApi: NotificationApi,
) : NotificationRemoteDataSource {

    override fun getNotifications(
        cursor: Long?,
        size: Int,
        onError: (String) -> Unit,
    ): Flow<NotificationPage> = flow {
        notificationApi.getNotifications(cursor = cursor, size = size)
            .suspendOnSuccess { this@flow.emit(response.toNotificationPage()) }
            .suspendOnFailureWithErrorHandling(onError)
    }

    override fun getUnreadCount(onError: (String) -> Unit): Flow<Int> = flow {
        notificationApi.getUnreadCount()
            .suspendOnSuccess { this@flow.emit(response.toUnreadCount()) }
            .suspendOnFailureWithErrorHandling(onError)
    }

    override fun markAllAsRead(onError: (String) -> Unit): Flow<Int> = flow {
        notificationApi.markAllAsRead()
            .suspendOnSuccess { this@flow.emit(response.toUnreadCount()) }
            .suspendOnFailureWithErrorHandling(onError)
    }
}
