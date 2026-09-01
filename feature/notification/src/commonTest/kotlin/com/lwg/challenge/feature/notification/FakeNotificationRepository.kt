package com.lwg.challenge.feature.notification

import com.lwg.challenge.domain.model.notification.Notification
import com.lwg.challenge.domain.model.notification.NotificationPage
import com.lwg.challenge.domain.repository.NotificationRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow

/**
 * 테스트용 [NotificationRepository] 페이크.
 *
 * 응답을 큐로 받는 이유: 커서 페이징은 **호출 순서마다 다른 쪽**이 와야 검증이 성립한다.
 * 실패는 emit 없이 onError 만 부른다 — 실제 RemoteDataSource 와 같은 계약이라야
 * "실패는 값이 아니다" 를 전제로 짜인 ViewModel 분기가 그대로 검증된다.
 */
class FakeNotificationRepository : NotificationRepository {

    private sealed interface Outcome {
        data class Page(val page: NotificationPage) : Outcome
        data class Failure(val message: String) : Outcome
    }

    private val outcomes = ArrayDeque<Outcome>()

    /** 요청에 실려 나간 cursor 인자. 서버가 준 nextCursor 를 그대로 되돌려 보내는지 여기서 본다. */
    val requestedCursors = mutableListOf<Long?>()

    var getNotificationsCallCount: Int = 0
        private set

    var markAllAsReadCallCount: Int = 0
        private set

    var markAllAsReadError: String? = null

    /**
     * non-null 이면 조회가 이 신호를 기다렸다 응답한다. 페이크가 즉시 응답하면 중복 요청 가드가
     * 열려 있는 순간 자체가 만들어지지 않아 재진입을 검증할 수 없다.
     */
    var pendingResponse: CompletableDeferred<Unit>? = null

    fun enqueuePage(notifications: List<Notification>, nextCursor: Long?) {
        outcomes += Outcome.Page(NotificationPage(notifications, nextCursor))
    }

    fun enqueueFailure(message: String) {
        outcomes += Outcome.Failure(message)
    }

    override fun getNotifications(
        cursor: Long?,
        size: Int,
        onError: (String) -> Unit,
    ): Flow<NotificationPage> = flow {
        getNotificationsCallCount += 1
        requestedCursors += cursor
        pendingResponse?.await()

        val outcome = outcomes.removeFirstOrNull() ?: Outcome.Page(NotificationPage(emptyList(), null))
        when (outcome) {
            is Outcome.Page -> emit(outcome.page)
            is Outcome.Failure -> onError(outcome.message)
        }
    }

    /** 알림 목록 화면은 이 축을 쓰지 않는다 — 홈 벨 뱃지 전용이다. */
    override fun getUnreadCount(onError: (String) -> Unit): Flow<Int> = emptyFlow()

    override fun markAllAsRead(onError: (String) -> Unit): Flow<Int> = flow {
        markAllAsReadCallCount += 1
        val error = markAllAsReadError
        if (error != null) onError(error) else emit(0)
    }
}
