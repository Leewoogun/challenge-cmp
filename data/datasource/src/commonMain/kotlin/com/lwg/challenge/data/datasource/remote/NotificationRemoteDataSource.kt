package com.lwg.challenge.data.datasource.remote

import com.lwg.challenge.domain.model.notification.NotificationPage
import kotlinx.coroutines.flow.Flow

interface NotificationRemoteDataSource {

    fun getNotifications(
        nextNotificationId: Long?,
        size: Int,
        onError: (String) -> Unit,
    ): Flow<NotificationPage>

    fun getUnreadCount(onError: (String) -> Unit): Flow<Int>

    fun markAllAsRead(onError: (String) -> Unit): Flow<Int>
}
