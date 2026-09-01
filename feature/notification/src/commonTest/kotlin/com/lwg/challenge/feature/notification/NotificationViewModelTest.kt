@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.lwg.challenge.feature.notification

import app.cash.turbine.test
import com.lwg.challenge.domain.model.notification.Notification
import com.lwg.challenge.feature.notification.contract.NotificationAppendState
import com.lwg.challenge.feature.notification.contract.NotificationUiEffect
import com.lwg.challenge.feature.notification.contract.NotificationUiState
import com.lwg.challenge.push.PushEvent
import com.lwg.challenge.utils.datetime.KST
import com.lwg.challenge.utils.datetime.nowKst
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationViewModelTest {

    private lateinit var fakeNotification: FakeNotificationRepository

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fakeNotification = FakeNotificationRepository()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // uiState 가 평범한 MutableStateFlow 라 init 의 첫 조회가 구독 없이 즉시 흐른다.
    private fun createViewModel(): NotificationViewModel = NotificationViewModel(fakeNotification)

    private fun dataState(viewModel: NotificationViewModel): NotificationUiState.Data {
        val state = viewModel.uiState.value
        assertIs<NotificationUiState.Data>(state)
        return state
    }

    // ViewModel 이 내부에서 nowKst() 를 재측정하므로 픽스처도 실제 시계 기준 상대값으로 만든다.
    private fun LocalDateTime.minus(duration: Duration): LocalDateTime =
        (toInstant(KST) - duration).toLocalDateTime(KST)

    private fun notification(
        notificationId: Long,
        type: String = PushEvent.TYPE_CHALLENGE_REQUEST,
        body: String = "민수님이 도전장을 보냈어요",
        referenceId: Long? = 1005L,
        createdAt: LocalDateTime = nowKst().minus(5.minutes),
    ) = Notification(
        notificationId = notificationId,
        type = type,
        body = body,
        referenceId = referenceId,
        createdAt = createdAt,
    )

    private fun notifications(ids: List<Long>): List<Notification> = ids.map { notification(it) }

    @Test
    fun `첫 조회에 알림이 있으면 Data 로 전이하고 받은 만큼 담는다`() = runTest {
        fakeNotification.enqueuePage(notifications(listOf(9L, 8L, 7L)), nextCursor = null)

        val viewModel = createViewModel()

        val state = dataState(viewModel)
        assertEquals(3, state.items.size)
        assertEquals(listOf(9L, 8L, 7L), state.items.map { it.notificationId })
    }

    @Test
    fun `첫 조회가 빈 목록이면 Empty 다`() = runTest {
        fakeNotification.enqueuePage(emptyList(), nextCursor = null)

        val viewModel = createViewModel()

        assertIs<NotificationUiState.Empty>(viewModel.uiState.value)
    }

    /**
     * 0건과 실패는 서로 다른 사실이고 사용자가 취할 행동도 정반대다 — 전자는 나가면 되고 후자는
     * 다시 시도해야 한다. 실패를 Empty 로 뭉개면 재시도 수단이 화면에서 사라진다.
     */
    @Test
    fun `첫 조회가 실패하면 Empty 가 아니라 Failure 다`() = runTest {
        fakeNotification.enqueueFailure("알림 조회 실패")

        val viewModel = createViewModel()

        assertIs<NotificationUiState.Failure>(viewModel.uiState.value)
    }

    /**
     * 서버가 준 nextCursor 를 그대로 되돌려 보내는지 본다. 마지막 항목의 id 와 nextCursor 를 일부러
     * 다른 값으로 심어 두었으므로, 앱이 커서를 직접 계산하면 이 단언이 깨진다.
     */
    @Test
    fun `loadMore 는 서버가 준 nextCursor 를 그대로 다음 요청에 싣는다`() = runTest {
        fakeNotification.enqueuePage(notifications(listOf(9L, 8L, 7L)), nextCursor = 42L)
        fakeNotification.enqueuePage(notifications(listOf(6L, 5L)), nextCursor = 31L)
        fakeNotification.enqueuePage(notifications(listOf(4L)), nextCursor = null)

        val viewModel = createViewModel()
        viewModel.loadMore()
        viewModel.loadMore()

        assertEquals(listOf(null, 42L, 31L), fakeNotification.requestedCursors)
    }

    @Test
    fun `추가로 받은 쪽이 기존 목록 뒤에 누적된다`() = runTest {
        fakeNotification.enqueuePage(notifications(listOf(9L, 8L)), nextCursor = 42L)
        fakeNotification.enqueuePage(notifications(listOf(7L, 6L)), nextCursor = null)

        val viewModel = createViewModel()
        viewModel.loadMore()

        val state = dataState(viewModel)
        assertEquals(listOf(9L, 8L, 7L, 6L), state.items.map { it.notificationId })
        assertEquals(NotificationAppendState.Idle, state.append)
    }

    @Test
    fun `nextCursor 가 null 이면 loadMore 를 불러도 요청이 나가지 않는다`() = runTest {
        fakeNotification.enqueuePage(notifications(listOf(9L, 8L)), nextCursor = null)

        val viewModel = createViewModel()
        viewModel.loadMore()
        viewModel.loadMore()

        assertEquals(1, fakeNotification.getNotificationsCallCount)
        assertEquals(listOf<Long?>(null), fakeNotification.requestedCursors)
    }

    @Test
    fun `로딩이 끝나기 전에 loadMore 를 다시 불러도 요청은 한 번만 나간다`() = runTest {
        fakeNotification.enqueuePage(notifications(listOf(9L, 8L)), nextCursor = 42L)
        fakeNotification.enqueuePage(notifications(listOf(7L, 6L)), nextCursor = null)

        val viewModel = createViewModel()
        val pending = CompletableDeferred<Unit>()
        fakeNotification.pendingResponse = pending

        viewModel.loadMore()
        viewModel.loadMore()

        assertEquals(2, fakeNotification.getNotificationsCallCount)

        pending.complete(Unit)
        runCurrent()

        assertEquals(2, fakeNotification.getNotificationsCallCount)
        assertEquals(4, dataState(viewModel).items.size)
    }

    /** 스크롤 위치를 잃지 않고 그 자리에서 재시도하는 것이 이 화면의 복구 경로다. */
    @Test
    fun `추가 조회가 실패해도 목록은 남고 마지막 슬롯만 Failed 가 된다`() = runTest {
        fakeNotification.enqueuePage(notifications(listOf(9L, 8L)), nextCursor = 42L)
        fakeNotification.enqueueFailure("알림을 더 불러오지 못했어요")

        val viewModel = createViewModel()
        viewModel.loadMore()

        val state = dataState(viewModel)
        assertEquals(listOf(9L, 8L), state.items.map { it.notificationId })
        assertEquals(NotificationAppendState.Failed, state.append)
    }

    @Test
    fun `추가 조회가 실패하면 서버 메시지를 스낵바로 알린다`() = runTest {
        fakeNotification.enqueuePage(notifications(listOf(9L, 8L)), nextCursor = 42L)
        fakeNotification.enqueueFailure("일시적인 오류예요")

        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            viewModel.loadMore()
            val effect = awaitItem()
            assertIs<NotificationUiEffect.ShowMessage>(effect)
            assertEquals("일시적인 오류예요", effect.message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `진입하면 전부 읽음 처리를 1회 호출한다`() = runTest {
        fakeNotification.enqueuePage(notifications(listOf(9L)), nextCursor = null)

        createViewModel()

        assertEquals(1, fakeNotification.markAllAsReadCallCount)
    }

    /**
     * 목록 조회와 읽음 처리는 분리된 두 축이다. 쪽마다 읽음이 다시 돌면 같은 일을 쪽 수만큼 반복한다.
     */
    @Test
    fun `쪽을 더 받아도 읽음 처리 호출 수는 늘지 않는다`() = runTest {
        fakeNotification.enqueuePage(notifications(listOf(9L, 8L)), nextCursor = 42L)
        fakeNotification.enqueuePage(notifications(listOf(7L, 6L)), nextCursor = 31L)
        fakeNotification.enqueuePage(notifications(listOf(5L)), nextCursor = null)

        val viewModel = createViewModel()
        viewModel.loadMore()
        viewModel.loadMore()
        viewModel.loadMore()

        // 세 번째 loadMore 는 커서가 비어 요청이 나가지 않는다.
        assertEquals(3, fakeNotification.getNotificationsCallCount)
        assertEquals(1, fakeNotification.markAllAsReadCallCount)
    }

    /** 읽음 처리가 실패하면 뱃지가 안 꺼질 뿐이다. 목록을 막을 이유가 없다. */
    @Test
    fun `읽음 처리가 실패해도 목록은 Data 로 남는다`() = runTest {
        fakeNotification.enqueuePage(notifications(listOf(9L, 8L)), nextCursor = null)
        fakeNotification.markAllAsReadError = "읽음 처리 실패"

        val viewModel = createViewModel()

        val state = dataState(viewModel)
        assertEquals(2, state.items.size)
    }

    /**
     * 나란히 놓인 두 알림이 서로 다른 시계로 계산되면 안 된다. 기준 시각 자체는 화면 밖으로
     * 새지 않으므로, 쪽을 건너 같은 createdAt 이 같은 문구로 나오는 것으로 관측한다.
     */
    @Test
    fun `추가로 받은 항목도 첫 쪽과 같은 기준 시각으로 경과 시간을 계산한다`() = runTest {
        val base = nowKst()
        fakeNotification.enqueuePage(
            listOf(notification(notificationId = 9L, createdAt = base.minus(5.minutes))),
            nextCursor = 42L,
        )
        fakeNotification.enqueuePage(
            listOf(
                notification(notificationId = 8L, createdAt = base.minus(5.minutes)),
                notification(notificationId = 7L, createdAt = base.minus(3.hours)),
            ),
            nextCursor = null,
        )

        val viewModel = createViewModel()
        viewModel.loadMore()

        val items = dataState(viewModel).items
        assertEquals("5분 전", items[0].elapsedText)
        assertEquals(items[0].elapsedText, items[1].elapsedText)
        assertEquals("3시간 전", items[2].elapsedText)
    }

    /** 재시도는 처음부터 다시 받는 것이다 — 실패 직전의 커서를 들고 있으면 첫 쪽을 건너뛴다. */
    @Test
    fun `retry 는 커서를 비우고 맨 앞부터 다시 받는다`() = runTest {
        fakeNotification.enqueueFailure("알림 조회 실패")
        fakeNotification.enqueuePage(notifications(listOf(9L, 8L)), nextCursor = null)

        val viewModel = createViewModel()
        assertIs<NotificationUiState.Failure>(viewModel.uiState.value)

        viewModel.retry()

        assertEquals(listOf<Long?>(null, null), fakeNotification.requestedCursors)
        assertEquals(2, dataState(viewModel).items.size)
    }
}
