package com.lwg.challenge.domain.usecase

import app.cash.turbine.test
import com.lwg.challenge.domain.model.notification.Notification
import com.lwg.challenge.domain.model.notification.NotificationPage
import com.lwg.challenge.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GetNotificationsUseCaseTest {

    private val fakeRepository = FakeNotificationRepository()
    private val useCase = GetNotificationsUseCase(fakeRepository)

    private fun notification(notificationId: Long, body: String = "민수님이 도전장을 보냈어요") = Notification(
        notificationId = notificationId,
        type = "CHALLENGE_REQUEST",
        body = body,
        referenceId = 1005L,
        referencePending = true,
        createdAt = LocalDateTime(2026, 10, 26, 9, 0),
    )

    private fun page(ids: List<Long>, nextNotificationId: Long?) = NotificationPage(
        notifications = ids.map(::notification),
        nextNotificationId = nextNotificationId,
        isPageEnd = nextNotificationId == null,
    )

    /** 마지막 항목 8, 그 - 1 인 7 과 다른 5 를 심어 앱이 키를 계산하면 깨진다. */
    @Test
    fun `다음 쪽은 응답의 nextNotificationId 를 그대로 싣고 20 개씩 요청한다`() = runTest {
        fakeRepository.pages += page(listOf(9L, 8L), nextNotificationId = 5L)
        fakeRepository.pages += page(listOf(5L, 4L), nextNotificationId = null)

        useCase(onError = {}).test {
            assertFalse(awaitItem().isPageEnd)

            useCase.nextPage()

            val result = awaitItem()
            assertEquals(listOf(9L, 8L, 5L, 4L), result.notifications.map { it.notificationId })
            assertTrue(result.isPageEnd)
            assertEquals(listOf(null, 5L), fakeRepository.requestedNextNotificationIds)
            assertEquals(listOf(20, 20), fakeRepository.requestedSizes)
        }
    }

    @Test
    fun `쪽 사이에 같은 notificationId 가 겹치면 먼저 받은 것 하나만 담는다`() = runTest {
        fakeRepository.pages += page(listOf(9L, 8L), nextNotificationId = 5L)
        fakeRepository.pages += NotificationPage(
            notifications = listOf(notification(8L, body = "겹친 행"), notification(5L)),
            nextNotificationId = null,
            isPageEnd = true,
        )

        useCase(onError = {}).test {
            awaitItem()
            useCase.nextPage()

            val notifications = awaitItem().notifications
            assertEquals(listOf(9L, 8L, 5L), notifications.map { it.notificationId })
            assertEquals("민수님이 도전장을 보냈어요", notifications[1].body)
        }
    }

    private class FakeNotificationRepository : NotificationRepository {

        val pages = ArrayDeque<NotificationPage>()
        val requestedNextNotificationIds = mutableListOf<Long?>()
        val requestedSizes = mutableListOf<Int>()

        override fun getNotifications(
            nextNotificationId: Long?,
            size: Int,
            onError: (String) -> Unit,
        ): Flow<NotificationPage> = flow {
            requestedNextNotificationIds += nextNotificationId
            requestedSizes += size
            emit(pages.removeFirst())
        }

        override fun getUnreadCount(onError: (String) -> Unit): Flow<Int> = emptyFlow()

        override fun markAllAsRead(onError: (String) -> Unit): Flow<Int> = emptyFlow()
    }
}
