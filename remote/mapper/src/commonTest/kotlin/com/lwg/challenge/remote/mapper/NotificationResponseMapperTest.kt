package com.lwg.challenge.remote.mapper

import com.lwg.challenge.remote.model.notification.NotificationDto
import com.lwg.challenge.remote.model.notification.NotificationListData
import com.lwg.challenge.remote.model.notification.NotificationListResponse
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NotificationResponseMapperTest {

    private fun dto(notificationId: Long, createdAt: LocalDateTime? = LocalDateTime(2026, 10, 26, 9, 0)) = NotificationDto(
        notificationId = notificationId,
        type = "CHALLENGE_REQUEST",
        body = "민수님이 도전장을 보냈어요",
        referenceId = 1005L,
        referencePending = true,
        createdAt = createdAt,
    )

    private fun response(vararg notifications: NotificationDto, nextNotificationId: Long?, isPageEnd: Boolean) =
        NotificationListResponse(NotificationListData(notifications.toList(), nextNotificationId, isPageEnd))

    /** 마지막 항목 8, 그 - 1 인 7 과 다른 5 를 심어 매퍼가 항목으로 키를 계산하면 깨진다. */
    @Test
    fun `중간 쪽의 nextNotificationId 와 isPageEnd 가 그대로 통과한다`() {
        val page = response(dto(9L), dto(8L), nextNotificationId = 5L, isPageEnd = false).toNotificationPage()

        assertEquals(listOf(9L, 8L), page.notifications.map { it.notificationId })
        assertEquals(5L, page.nextNotificationId)
        assertFalse(page.isPageEnd)
    }

    @Test
    fun `끝 쪽의 null 과 true 가 그대로 통과한다`() {
        val page = response(dto(9L), dto(8L), nextNotificationId = null, isPageEnd = true).toNotificationPage()

        assertNull(page.nextNotificationId)
        assertTrue(page.isPageEnd)
    }

    /** 행이 걸러져 마지막 항목이 바뀌어도 다음 키는 서버가 정한 값이다. */
    @Test
    fun `createdAt 이 없는 행이 걸러져도 두 값은 바뀌지 않는다`() {
        val page = response(dto(9L), dto(8L, createdAt = null), nextNotificationId = 5L, isPageEnd = false)
            .toNotificationPage()

        assertEquals(listOf(9L), page.notifications.map { it.notificationId })
        assertEquals(5L, page.nextNotificationId)
        assertFalse(page.isPageEnd)
    }
}
