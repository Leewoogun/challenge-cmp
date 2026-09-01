package com.lwg.challenge.feature.notification.contract

import com.lwg.challenge.domain.model.notification.Notification
import com.lwg.challenge.push.PushEvent
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 누적 목록에서 날짜 그룹을 파생시키는 규칙. design.md §3.6.
 *
 * `NotificationViewModelTest` 는 쪽이 이어붙는 경로를 보고, 여기서는 기준 시각을 고정해 **묶는 규칙
 * 자체**를 본다 — 그쪽은 `nowKst()` 를 쓰므로 달력 경계를 마음대로 세울 수 없다.
 */
class NotificationDateGroupTest {

    private val now = LocalDateTime(2026, 9, 1, 15, 0, 0)

    private fun item(notificationId: Long, createdAt: LocalDateTime) = NotificationItemState(
        Notification(
            notificationId = notificationId,
            type = PushEvent.TYPE_CHALLENGE_REQUEST,
            body = "민수님이 도전장을 보냈어요",
            referenceId = 1005L,
            referencePending = true,
            createdAt = createdAt,
        ),
        now,
    )

    private fun dataOf(vararg items: NotificationItemState) =
        NotificationUiState.Data(items.toList())

    @Test
    fun `같은 날짜가 연속으로 놓이면 한 그룹이다`() {
        val data = dataOf(
            item(9L, LocalDateTime(2026, 9, 1, 14, 0, 0)),
            item(8L, LocalDateTime(2026, 9, 1, 9, 0, 0)),
            item(7L, LocalDateTime(2026, 9, 1, 0, 30, 0)),
        )

        assertEquals(listOf("오늘"), data.dateGroups.map { it.label })
        assertEquals(listOf(9L, 8L, 7L), data.dateGroups[0].items.map { it.notificationId })
    }

    @Test
    fun `날짜가 바뀌는 자리에서 그룹이 갈린다`() {
        val data = dataOf(
            item(9L, LocalDateTime(2026, 9, 1, 14, 0, 0)),
            item(8L, LocalDateTime(2026, 8, 31, 20, 0, 0)),
            item(7L, LocalDateTime(2026, 8, 31, 8, 0, 0)),
            item(6L, LocalDateTime(2026, 8, 28, 9, 0, 0)),
        )

        assertEquals(listOf("오늘", "어제", "8월 28일"), data.dateGroups.map { it.label })
        assertEquals(
            listOf(listOf(9L), listOf(8L, 7L), listOf(6L)),
            data.dateGroups.map { group -> group.items.map { it.notificationId } },
        )
    }

    /** 앱이 다시 세우면 서버가 정렬 규칙을 바꾸는 날 두 규칙이 조용히 어긋난다. */
    @Test
    fun `그룹은 항목 순서를 바꾸지 않는다`() {
        val data = dataOf(
            item(9L, LocalDateTime(2026, 9, 1, 14, 0, 0)),
            item(8L, LocalDateTime(2026, 8, 28, 9, 0, 0)),
            item(7L, LocalDateTime(2026, 8, 31, 20, 0, 0)),
        )

        assertEquals(
            data.items.map { it.notificationId },
            data.dateGroups.flatMap { group -> group.items.map { it.notificationId } },
        )
    }

    /**
     * 위 케이스처럼 날짜가 오르내리면 같은 라벨의 그룹이 둘 생길 수 있다. 헤더 key 를 라벨로 만들면
     * 그때 `LazyColumn` 이 중복 key 로 터진다.
     */
    @Test
    fun `헤더 키는 라벨이 겹쳐도 그룹마다 다르다`() {
        val data = dataOf(
            item(9L, LocalDateTime(2026, 8, 28, 14, 0, 0)),
            item(8L, LocalDateTime(2026, 9, 1, 9, 0, 0)),
            item(7L, LocalDateTime(2026, 8, 28, 8, 0, 0)),
        )

        val keys = data.dateGroups.map { it.headerKey }
        assertEquals(listOf("8월 28일", "오늘", "8월 28일"), data.dateGroups.map { it.label })
        assertEquals(keys.size, keys.toSet().size)
    }

    @Test
    fun `항목이 없으면 그룹도 없다`() {
        assertEquals(emptyList<NotificationDateGroup>(), NotificationUiState.Data(emptyList()).dateGroups)
    }
}
