@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.lwg.challenge.feature.notification

import app.cash.turbine.test
import com.lwg.challenge.domain.model.notification.Notification
import com.lwg.challenge.domain.usecase.GetNotificationsUseCase
import com.lwg.challenge.feature.notification.contract.NotificationUiEffect
import com.lwg.challenge.feature.notification.contract.NotificationUiState
import com.lwg.challenge.push.PushEvent
import com.lwg.challenge.utils.datetime.KST
import com.lwg.challenge.utils.datetime.nowKst
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
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
import kotlin.time.Duration.Companion.days
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

    private fun newViewModel(): NotificationViewModel =
        NotificationViewModel(GetNotificationsUseCase(fakeNotification), fakeNotification)

    // uiState 가 WhileSubscribed 라 구독해야 조회가 시작된다. 화면 진입 = 구독, 상세에 다녀오기 = 구독 해제 후 재구독.
    private var subscription: Job? = null

    private fun TestScope.enter(viewModel: NotificationViewModel) {
        subscription = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
    }

    private fun TestScope.reenter(viewModel: NotificationViewModel) {
        subscription?.cancel()
        runCurrent()
        enter(viewModel)
        runCurrent()
    }

    private fun TestScope.createViewModel(): NotificationViewModel = newViewModel().also { enter(it) }

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
        referencePending: Boolean? = true,
        createdAt: LocalDateTime = nowKst().minus(5.minutes),
    ) = Notification(
        notificationId = notificationId,
        type = type,
        body = body,
        referenceId = referenceId,
        referencePending = referencePending,
        createdAt = createdAt,
    )

    private fun notifications(ids: List<Long>): List<Notification> = ids.map { notification(it) }

    @Test
    fun `구독 전에는 조회하지 않는다`() = runTest {
        newViewModel()

        assertEquals(0, fakeNotification.getNotificationsCallCount)
    }

    /** 구독마다 첫 쪽 키를 다시 넣어도 replay 가 하나라 첫 진입 요청은 한 번이다. */
    @Test
    fun `첫 진입에는 첫 쪽 요청이 한 번만 나간다`() = runTest {
        fakeNotification.enqueuePage(notifications(listOf(9L, 8L)), nextNotificationId = 5L)

        createViewModel()

        assertEquals(listOf<Long?>(null), fakeNotification.requestedNextNotificationIds)
    }

    @Test
    fun `첫 조회에 알림이 있으면 Data 로 전이하고 받은 만큼 담는다`() = runTest {
        fakeNotification.enqueuePage(notifications(listOf(9L, 8L, 7L)), nextNotificationId = null)

        val viewModel = createViewModel()

        val state = dataState(viewModel)
        assertEquals(3, state.notifications.size)
        assertEquals(listOf(9L, 8L, 7L), state.notifications.map { it.notificationId })
    }

    @Test
    fun `첫 조회가 빈 목록이면 Empty 다`() = runTest {
        fakeNotification.enqueuePage(emptyList(), nextNotificationId = null)

        val viewModel = createViewModel()

        assertIs<NotificationUiState.Empty>(viewModel.uiState.value)
    }

    /** 실패를 Empty 로 뭉개면 재시도 수단이 화면에서 사라진다. */
    @Test
    fun `첫 조회가 실패하면 Empty 가 아니라 Error 다`() = runTest {
        fakeNotification.enqueueFailure("알림 조회 실패")

        val viewModel = createViewModel()

        assertIs<NotificationUiState.Error>(viewModel.uiState.value)
    }

    /** nextNotificationId 를 마지막 항목 id 와도, 그 id - 1 과도 다르게 심어 앱이 키를 직접 계산하면 깨진다. */
    @Test
    fun `nextPage 는 서버가 준 nextNotificationId 를 그대로 다음 요청에 싣는다`() = runTest {
        fakeNotification.enqueuePage(notifications(listOf(9L, 8L, 7L)), nextNotificationId = 5L)
        fakeNotification.enqueuePage(notifications(listOf(5L, 4L)), nextNotificationId = 2L)
        fakeNotification.enqueuePage(notifications(listOf(2L)), nextNotificationId = null)

        val viewModel = createViewModel()
        viewModel.nextPage()
        viewModel.nextPage()

        assertEquals(listOf(null, 5L, 2L), fakeNotification.requestedNextNotificationIds)
    }

    @Test
    fun `추가로 받은 쪽이 기존 목록 뒤에 누적된다`() = runTest {
        fakeNotification.enqueuePage(notifications(listOf(9L, 8L)), nextNotificationId = 5L)
        fakeNotification.enqueuePage(notifications(listOf(5L, 4L)), nextNotificationId = null)

        val viewModel = createViewModel()
        viewModel.nextPage()

        val state = dataState(viewModel)
        assertEquals(listOf(9L, 8L, 5L, 4L), state.notifications.map { it.notificationId })
    }

    @Test
    fun `isPageEnd 면 nextPage 를 불러도 요청이 나가지 않는다`() = runTest {
        fakeNotification.enqueuePage(notifications(listOf(9L, 8L)), nextNotificationId = null)

        val viewModel = createViewModel()
        viewModel.nextPage()
        viewModel.nextPage()

        assertEquals(1, fakeNotification.getNotificationsCallCount)
        assertEquals(listOf<Long?>(null), fakeNotification.requestedNextNotificationIds)
    }

    @Test
    fun `로딩이 끝나기 전에 nextPage 를 다시 불러도 요청은 한 번만 나간다`() = runTest {
        fakeNotification.enqueuePage(notifications(listOf(9L, 8L)), nextNotificationId = 5L)
        fakeNotification.enqueuePage(notifications(listOf(5L, 4L)), nextNotificationId = null)

        val viewModel = createViewModel()
        val pending = CompletableDeferred<Unit>()
        fakeNotification.pendingResponse = pending

        viewModel.nextPage()
        viewModel.nextPage()

        assertEquals(2, fakeNotification.getNotificationsCallCount)

        pending.complete(Unit)
        runCurrent()

        assertEquals(2, fakeNotification.getNotificationsCallCount)
        assertEquals(4, dataState(viewModel).notifications.size)
    }

    @Test
    fun `추가 조회가 실패해도 목록은 남는다`() = runTest {
        fakeNotification.enqueuePage(notifications(listOf(9L, 8L)), nextNotificationId = 5L)
        fakeNotification.enqueueFailure("알림을 더 불러오지 못했어요")

        val viewModel = createViewModel()
        viewModel.nextPage()

        val state = dataState(viewModel)
        assertEquals(listOf(9L, 8L), state.notifications.map { it.notificationId })
    }

    @Test
    fun `추가 조회가 실패하면 서버 메시지를 스낵바로 알린다`() = runTest {
        fakeNotification.enqueuePage(notifications(listOf(9L, 8L)), nextNotificationId = 5L)
        fakeNotification.enqueueFailure("일시적인 오류예요")

        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            viewModel.nextPage()
            val effect = awaitItem()
            assertIs<NotificationUiEffect.ShowMessage>(effect)
            assertEquals("일시적인 오류예요", effect.message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `진입하면 전부 읽음 처리를 1회 호출한다`() = runTest {
        fakeNotification.enqueuePage(notifications(listOf(9L)), nextNotificationId = null)

        createViewModel()

        assertEquals(1, fakeNotification.markAllAsReadCallCount)
    }

    /**
     * 목록 조회와 읽음 처리는 분리된 두 축이다. 쪽마다 읽음이 다시 돌면 같은 일을 쪽 수만큼 반복한다.
     */
    @Test
    fun `쪽을 더 받아도 읽음 처리 호출 수는 늘지 않는다`() = runTest {
        fakeNotification.enqueuePage(notifications(listOf(9L, 8L)), nextNotificationId = 5L)
        fakeNotification.enqueuePage(notifications(listOf(5L, 4L)), nextNotificationId = 2L)
        fakeNotification.enqueuePage(notifications(listOf(2L)), nextNotificationId = null)

        val viewModel = createViewModel()
        viewModel.nextPage()
        viewModel.nextPage()
        viewModel.nextPage()

        // 세 번째 nextPage 는 isPageEnd 라 요청이 나가지 않는다.
        assertEquals(3, fakeNotification.getNotificationsCallCount)
        assertEquals(1, fakeNotification.markAllAsReadCallCount)
    }

    /** 읽음 처리가 실패하면 뱃지가 안 꺼질 뿐이다. 목록을 막을 이유가 없다. */
    @Test
    fun `읽음 처리가 실패해도 목록은 Data 로 남는다`() = runTest {
        fakeNotification.enqueuePage(notifications(listOf(9L, 8L)), nextNotificationId = null)
        fakeNotification.markAllAsReadError = "읽음 처리 실패"

        val viewModel = createViewModel()

        val state = dataState(viewModel)
        assertEquals(2, state.notifications.size)
    }

    /** 쪽을 건너도 같은 createdAt 이 같은 문구로 나오면 한 시계로 계산한 것이다. */
    @Test
    fun `추가로 받은 항목도 첫 쪽과 같은 기준 시각으로 경과 시간을 계산한다`() = runTest {
        val base = nowKst()
        fakeNotification.enqueuePage(
            listOf(notification(notificationId = 9L, createdAt = base.minus(5.minutes))),
            nextNotificationId = 7L,
        )
        fakeNotification.enqueuePage(
            listOf(
                notification(notificationId = 7L, createdAt = base.minus(5.minutes)),
                // 세 항목을 전부 같은 달력일 안에 둔다 — 오늘 그룹만 상대 표기라 자정을 걸치면
                // 표기 축이 바뀐다. 여기서 보려는 것은 축이 아니라 "쪽을 건너 시계가 하나인가" 다.
                notification(notificationId = 6L, createdAt = base.minus(30.minutes)),
            ),
            nextNotificationId = null,
        )

        val viewModel = createViewModel()
        viewModel.nextPage()

        val data = dataState(viewModel)
        val items = data.notifications
        assertEquals("5분 전", data.timeTextOf(items[0]))
        assertEquals(data.timeTextOf(items[0]), data.timeTextOf(items[1]))
        assertEquals("30분 전", data.timeTextOf(items[2]))
    }

    /** 1쪽 끝과 2쪽 첫 항목이 같은 날짜다. 쪽 단위로 묶으면 `오늘` 헤더가 두 번 찍힌다. */
    @Test
    fun `페이지 경계에 같은 날짜가 걸쳐도 헤더는 하나다`() = runTest {
        val base = nowKst()
        val older = base.minus(3.days)
        fakeNotification.enqueuePage(
            listOf(
                notification(notificationId = 9L, createdAt = base),
                notification(notificationId = 8L, createdAt = base),
            ),
            nextNotificationId = 5L,
        )
        fakeNotification.enqueuePage(
            listOf(
                notification(notificationId = 5L, createdAt = base),
                notification(notificationId = 4L, createdAt = older),
            ),
            nextNotificationId = null,
        )

        val viewModel = createViewModel()
        viewModel.nextPage()

        val groups = dataState(viewModel).dateGroups
        assertEquals(
            listOf("오늘", "${older.date.monthNumber}월 ${older.date.day}일"),
            groups.map { it.label },
        )
        assertEquals(listOf(9L, 8L, 5L), groups[0].notifications.map { it.notificationId })
        assertEquals(listOf(4L), groups[1].notifications.map { it.notificationId })
    }

    /** 서버가 준 최신순이 정본이라 앱은 다시 세우지 않는다 — 연속 런만 묶는다. */
    @Test
    fun `그룹은 서버가 준 최신순 그대로 이어진다`() = runTest {
        val base = nowKst()
        val older = base.minus(4.days)
        fakeNotification.enqueuePage(
            listOf(
                notification(notificationId = 9L, createdAt = base),
                notification(notificationId = 8L, createdAt = base.minus(1.days)),
                notification(notificationId = 7L, createdAt = older),
            ),
            nextNotificationId = null,
        )

        val viewModel = createViewModel()

        assertEquals(
            listOf("오늘", "어제", "${older.date.monthNumber}월 ${older.date.day}일"),
            dataState(viewModel).dateGroups.map { it.label },
        )
    }

    /** 그룹이 달력 축을 맡으면 행이 24시간 산술을 겸할 이유가 없다. */
    @Test
    fun `오늘 그룹은 상대 시각이고 지난 날짜 그룹은 시 분 표기다`() = runTest {
        val base = nowKst()
        val older = base.minus(2.days)
        fakeNotification.enqueuePage(
            listOf(
                notification(notificationId = 9L, createdAt = base.minus(5.minutes)),
                notification(notificationId = 8L, createdAt = older),
            ),
            nextNotificationId = null,
        )

        val viewModel = createViewModel()

        val data = dataState(viewModel)
        val groups = data.dateGroups
        val expectedClock =
            "${older.hour.toString().padStart(2, '0')}:${older.minute.toString().padStart(2, '0')}"
        assertEquals("5분 전", data.timeTextOf(groups[0].notifications.first()))
        assertEquals(expectedClock, data.timeTextOf(groups[1].notifications.first()))
    }

    /** 재시도는 처음부터 다시 받는 것이다 — 실패 직전의 키를 들고 있으면 첫 쪽을 건너뛴다. */
    @Test
    fun `retry 는 키 없이 맨 앞부터 다시 받는다`() = runTest {
        fakeNotification.enqueueFailure("알림 조회 실패")
        fakeNotification.enqueuePage(notifications(listOf(9L, 8L)), nextNotificationId = null)

        val viewModel = createViewModel()
        assertIs<NotificationUiState.Error>(viewModel.uiState.value)

        viewModel.retry()

        assertEquals(listOf<Long?>(null, null), fakeNotification.requestedNextNotificationIds)
        assertEquals(2, dataState(viewModel).notifications.size)
    }

    /** 재시도는 흐름 전체를 다시 시작해 읽음 처리도 다시 돈다. 멱등이라 문제없다. */
    @Test
    fun `retry 하면 읽음 처리도 다시 돈다`() = runTest {
        fakeNotification.enqueueFailure("알림 조회 실패")
        fakeNotification.enqueuePage(notifications(listOf(9L)), nextNotificationId = null)

        val viewModel = createViewModel()
        viewModel.retry()

        assertEquals(2, fakeNotification.markAllAsReadCallCount)
    }

    /** VM 이 백스택에 살아 있어 재진입에 다시 받지 않으면 referencePending 과 기준 시각이 낡는다. */
    @Test
    fun `재진입하면 키 없이 첫 쪽부터 다시 받는다`() = runTest {
        fakeNotification.enqueuePage(notifications(listOf(9L, 8L)), nextNotificationId = 5L)
        fakeNotification.enqueuePage(notifications(listOf(7L)), nextNotificationId = null)

        val viewModel = createViewModel()
        reenter(viewModel)

        assertEquals(listOf<Long?>(null, null), fakeNotification.requestedNextNotificationIds)
        assertEquals(listOf(7L), dataState(viewModel).notifications.map { it.notificationId })
    }

    /** 페이지 요청이 마지막 키를 replay 해서, 재구독만 하면 첫 쪽이 아니라 마지막에 받은 쪽만 다시 받는다. */
    @Test
    fun `다음 쪽까지 받은 뒤 재진입해도 첫 쪽부터 다시 받는다`() = runTest {
        fakeNotification.enqueuePage(notifications(listOf(9L, 8L)), nextNotificationId = 5L)
        fakeNotification.enqueuePage(notifications(listOf(5L, 4L)), nextNotificationId = null)
        fakeNotification.enqueuePage(notifications(listOf(10L, 9L)), nextNotificationId = 8L)

        val viewModel = createViewModel()
        viewModel.nextPage()
        reenter(viewModel)

        assertEquals(listOf(null, 5L, null), fakeNotification.requestedNextNotificationIds)
        assertEquals(listOf(10L, 9L), dataState(viewModel).notifications.map { it.notificationId })
    }

    /** 자리를 비운 사이 도착한 알림까지 읽어야 *"목록을 열면 전부 읽음"* 규약이 성립한다. 멱등이다. */
    @Test
    fun `재진입하면 읽음 처리도 다시 돈다`() = runTest {
        fakeNotification.enqueuePage(notifications(listOf(9L)), nextNotificationId = null)
        fakeNotification.enqueuePage(notifications(listOf(9L)), nextNotificationId = null)

        val viewModel = createViewModel()
        reenter(viewModel)

        assertEquals(2, fakeNotification.markAllAsReadCallCount)
    }

    /**
     * 재진입 갱신이 화면을 Loading 으로 덮으면 읽던 목록이 사라진다.
     * 페이크가 즉시 응답하면 Loading 이 conflate 돼 관측되지 않아 응답을 붙잡아 둔다.
     */
    @Test
    fun `재진입 갱신 중에도 이미 있던 목록이 화면에 남는다`() = runTest {
        fakeNotification.enqueuePage(notifications(listOf(9L, 8L)), nextNotificationId = null)
        fakeNotification.enqueuePage(notifications(listOf(7L)), nextNotificationId = null)

        val viewModel = createViewModel()
        val pending = CompletableDeferred<Unit>()
        fakeNotification.pendingResponse = pending

        reenter(viewModel)

        // 조회가 아직 도는 중이다. 여기가 Loading 이면 화면에서 목록이 사라졌다는 뜻이다.
        val duringReload = viewModel.uiState.value
        assertIs<NotificationUiState.Data>(duringReload)
        assertEquals(listOf(9L, 8L), duringReload.notifications.map { it.notificationId })

        pending.complete(Unit)
        runCurrent()

        assertEquals(listOf(7L), dataState(viewModel).notifications.map { it.notificationId })
    }

    /** 남길 목록이 있으면 화면을 Error 로 덮지 않고 스낵바로만 알린다. */
    @Test
    fun `재진입 조회가 실패해도 목록은 남고 스낵바로만 알린다`() = runTest {
        fakeNotification.enqueuePage(notifications(listOf(9L, 8L)), nextNotificationId = null)
        fakeNotification.enqueueFailure("일시적인 오류예요")

        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            reenter(viewModel)
            val effect = awaitItem()
            assertIs<NotificationUiEffect.ShowMessage>(effect)
            assertEquals("일시적인 오류예요", effect.message)
            cancelAndIgnoreRemainingEvents()
        }

        val state = dataState(viewModel)
        assertEquals(listOf(9L, 8L), state.notifications.map { it.notificationId })
    }

    /** 실패한 재진입이 다음 키를 지우면 남은 목록이 끝까지 스크롤돼도 다음 쪽 요청이 나가지 않는다. */
    @Test
    fun `재진입 조회가 실패해도 남은 목록은 다음 쪽을 계속 받는다`() = runTest {
        fakeNotification.enqueuePage(notifications(listOf(9L, 8L)), nextNotificationId = 5L)
        fakeNotification.enqueueFailure("일시적인 오류예요")
        fakeNotification.enqueuePage(notifications(listOf(5L, 4L)), nextNotificationId = null)

        val viewModel = createViewModel()
        reenter(viewModel)
        viewModel.nextPage()

        // 다음 키가 지워졌다면 세 번째 요청 자체가 나가지 않는다.
        assertEquals(3, fakeNotification.getNotificationsCallCount)
        assertEquals(listOf(null, null, 5L), fakeNotification.requestedNextNotificationIds)
        assertEquals(listOf(9L, 8L, 5L, 4L), dataState(viewModel).notifications.map { it.notificationId })
    }

    /** 목록을 남겨 두는 것은 조회가 도는 동안뿐이다. 성공하면 낡은 목록이 통째로 새 쪽으로 바뀐다. */
    @Test
    fun `재진입 조회가 성공하면 목록이 새 값으로 교체된다`() = runTest {
        fakeNotification.enqueuePage(notifications(listOf(9L, 8L)), nextNotificationId = null)
        fakeNotification.enqueuePage(notifications(listOf(11L, 10L, 9L)), nextNotificationId = null)

        val viewModel = createViewModel()
        reenter(viewModel)

        // 이어붙이는 것이 아니라 교체다 — 8L 은 새 쪽에 없으므로 화면에서도 사라져야 한다.
        assertEquals(listOf(11L, 10L, 9L), dataState(viewModel).notifications.map { it.notificationId })
    }

    /** 그 사이 전부 지워진 경우다. 목록을 남길 근거가 없다 — 실제로 비었다. */
    @Test
    fun `재진입 조회가 0건이면 Empty 로 간다`() = runTest {
        fakeNotification.enqueuePage(notifications(listOf(9L, 8L)), nextNotificationId = null)
        fakeNotification.enqueuePage(emptyList(), nextNotificationId = null)

        val viewModel = createViewModel()
        reenter(viewModel)

        assertIs<NotificationUiState.Empty>(viewModel.uiState.value)
    }

    /** 첫 진입에는 지킬 목록이 없다. 로딩을 감추면 빈 화면이 응답까지 그대로 서 있는다. */
    @Test
    fun `첫 진입은 조회가 끝날 때까지 Loading 이다`() = runTest {
        fakeNotification.enqueuePage(notifications(listOf(9L)), nextNotificationId = null)
        val pending = CompletableDeferred<Unit>()
        fakeNotification.pendingResponse = pending

        val viewModel = newViewModel()
        enter(viewModel)

        assertIs<NotificationUiState.Loading>(viewModel.uiState.value)

        pending.complete(Unit)
        runCurrent()

        assertEquals(listOf(9L), dataState(viewModel).notifications.map { it.notificationId })
    }

    /** Empty 는 스피너로 바꾸지 않고 그대로 둔 채 갱신한다. */
    @Test
    fun `Empty 에서 재진입하면 Empty 를 둔 채 갱신한다`() = runTest {
        fakeNotification.enqueuePage(emptyList(), nextNotificationId = null)
        fakeNotification.enqueuePage(notifications(listOf(9L)), nextNotificationId = null)

        val viewModel = createViewModel()
        assertIs<NotificationUiState.Empty>(viewModel.uiState.value)

        val pending = CompletableDeferred<Unit>()
        fakeNotification.pendingResponse = pending

        reenter(viewModel)
        assertIs<NotificationUiState.Empty>(viewModel.uiState.value)

        pending.complete(Unit)
        runCurrent()

        assertEquals(listOf(9L), dataState(viewModel).notifications.map { it.notificationId })
    }

    /** 스피너가 없으면 Error 화면이 그대로 남아, 다시 실패했을 때 재시도가 됐는지조차 보이지 않는다. */
    @Test
    fun `재시도하면 응답이 올 때까지 Loading 이다`() = runTest {
        fakeNotification.enqueueFailure("알림 조회 실패")
        fakeNotification.enqueuePage(notifications(listOf(9L)), nextNotificationId = null)

        val viewModel = createViewModel()
        assertIs<NotificationUiState.Error>(viewModel.uiState.value)

        val pending = CompletableDeferred<Unit>()
        fakeNotification.pendingResponse = pending

        viewModel.retry()
        runCurrent()
        assertIs<NotificationUiState.Loading>(viewModel.uiState.value)

        pending.complete(Unit)
        runCurrent()

        assertEquals(listOf(9L), dataState(viewModel).notifications.map { it.notificationId })
    }
}
