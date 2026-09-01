package com.lwg.challenge.feature.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lwg.challenge.domain.model.notification.Notification
import com.lwg.challenge.domain.repository.NotificationRepository
import com.lwg.challenge.feature.notification.contract.NotificationAppendState
import com.lwg.challenge.feature.notification.contract.NotificationItemState
import com.lwg.challenge.feature.notification.contract.NotificationUiEffect
import com.lwg.challenge.feature.notification.contract.NotificationUiState
import com.lwg.challenge.utils.datetime.nowKst
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import org.koin.android.annotation.KoinViewModel

/**
 * 알림 목록 화면. 커서 무한 스크롤 + 진입 시 전체 읽음 처리.
 *
 * 파이프라인이 아니라 명령형인 이유: 이 화면의 상태는 **페이지가 누적되는 캐시**다.
 * `triggerStateIn` 의 재구독 갱신은 상류가 자체 누적 상태를 들고 있으면 의도대로 동작하지 않는다고
 * 그 문서가 직접 적어 두었고, 갱신 인자가 커서라 `flatMapLatest` 로 바꾸면 이전 쪽이 버려진다.
 */
@KoinViewModel
class NotificationViewModel(
    private val notificationRepository: NotificationRepository,
) : ViewModel() {

    private val _uiState: MutableStateFlow<NotificationUiState> =
        MutableStateFlow(NotificationUiState.Loading)
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<NotificationUiEffect>()
    val uiEffect: SharedFlow<NotificationUiEffect> = _uiEffect.asSharedFlow()

    /**
     * 🔴 **서버가 준 값만 되돌려 보낸다.** 마지막 항목의 id 로 계산하지 않는다 — 그러면 커서 규칙의
     * 사본이 앱에 생기고, 서버가 커서 표현을 바꾸는 날 그 사본이 조용히 틀린다. `null` 은 끝이다.
     */
    private var nextCursor: Long? = null

    /**
     * 중복 요청 가드. 첫 조회와 추가 조회를 **한 축으로** 막는다 — 첫 조회 중에는 화면에 목록이 없어
     * 추가 조회가 걸릴 일이 없지만, 재시도와 자동 트리거가 겹치는 경로가 있어 축을 나누지 않는다.
     *
     * [viewModelScope] 가 메인 디스패처라 이 변수를 읽고 쓰는 지점이 전부 한 스레드다.
     */
    private var isLoading = false

    /**
     * 첫 매핑 시점에 1회 측정해 전 항목이 공유한다. 쪽이 추가돼도 같은 값을 쓴다 — 같은 목록 안에서
     * 기준 시각이 갈리면 나란히 놓인 두 알림의 경과 시간이 서로 다른 시계로 계산된다.
     *
     * 목록을 처음부터 다시 받을 때만 다시 잰다([reloadFromFirstPage]). 목록 하나가 한 시계를 쓰는
     * 것이지, ViewModel 하나가 한 시계를 쓰는 것이 아니다.
     */
    private var now: LocalDateTime? = null

    /**
     * 화면이 **진입할 때마다** 부른다. 재진입도 포함이다 — 이 화면은 상세로 갔다 와도 백스택에 남아
     * ViewModel 이 살아 있어, 생애당 1회(`init`) 조회로는 두 값이 낡은 채 굳는다.
     *
     * 하나는 기준 시각이다. 앱을 며칠 켜 두면 `오늘` 헤더가 사흘 전 그룹에 붙는다.
     * 🔴 다른 하나는 `referencePending` 이다. 낡은 `true` 는 이미 끝난 신청의 탭을 열어 사용자를
     * 홈의 빈 자리로 보낸다 — 게이트가 막으려던 바로 그 증상이다.
     *
     * 🔴 그러면서도 **이미 보여주고 있던 목록은 지우지 않는다**([reloadFromFirstPage]).
     *
     * [markAllAsRead] 가 재진입마다 함께 도는 것은 의도다. 멱등이고, 자리를 비운 사이 도착한
     * 알림까지 읽어야 *"목록을 열면 전부 읽음"* 규약이 성립한다.
     */
    fun refresh() {
        reloadFromFirstPage()
        markAllAsRead()
    }

    /**
     * 마지막 항목에 닿았을 때 화면이 부른다. **더 받을 것이 남았는지 판단하는 곳은 여기 한 곳이다** —
     * 화면은 커서를 보지 않으므로 끝에 도달한 뒤 아무리 다시 불려도 요청이 나가지 않는다.
     *
     * "다시 시도" 도 같은 함수를 부른다. 재시도는 같은 커서로 같은 요청을 다시 보내는 것이라
     * 다른 경로가 될 이유가 없다.
     */
    fun loadMore() {
        val cursor = nextCursor ?: return
        if (isLoading) return
        isLoading = true
        updateAppend(NotificationAppendState.Loading)

        viewModelScope.launch {
            var appended = false
            var failureMessage: String? = null

            notificationRepository.getNotifications(
                cursor = cursor,
                size = PAGE_SIZE,
                onError = { failureMessage = it },
            ).collect { page ->
                appended = true
                nextCursor = page.nextCursor
                _uiState.update { state ->
                    if (state !is NotificationUiState.Data) return@update state
                    state.copy(
                        items = state.items + itemsOf(page.notifications),
                        append = NotificationAppendState.Idle,
                    )
                }
            }

            isLoading = false

            if (!appended) {
                // 🔴 목록을 비우지 않는다. 인디케이터 자리만 "다시 시도" 로 바뀌어 스크롤 위치를
                // 유지한 채 그 자리에서 재시도한다 — 스낵바는 사라지고 나면 더 볼 수단이 없다.
                updateAppend(NotificationAppendState.Failed)
                _uiEffect.emit(NotificationUiEffect.ShowMessage(failureMessage ?: APPEND_FAILURE_MESSAGE))
            }
        }
    }

    /**
     * 첫 조회 실패 화면의 "다시 시도".
     *
     * [refresh] 와 달리 읽음 처리를 부르지 않는다 — 진입 시점에 이미 한 번 돌았고, 실패한 것은
     * 목록 조회 쪽이라 되풀이할 이유가 없다.
     *
     * [reloadFromFirstPage] 의 «목록 유지» 분기는 이 경로에 걸리지 않는다. 이 버튼은
     * [NotificationUiState.Failure] 화면에만 있어 지킬 목록이 애초에 없다.
     */
    fun retry() {
        reloadFromFirstPage()
    }

    /**
     * 커서도 기준 시각도 이 조회가 새로 정한다 — 둘 중 하나만 비우면 낡은 축이 남는다.
     *
     * 🔴 **이미 [NotificationUiState.Data] 면 화면을 [NotificationUiState.Loading] 으로 되돌리지
     * 않는다.** design.md §6.3 이 다음 쪽 로딩을 두고 내린 판단 — *"전체 화면 로딩으로 덮지 마라.
     * 읽고 있던 목록이 사라지고 스크롤 위치를 잃는다"* — 이 재진입 갱신에도 같은 근거로 그대로
     * 적용된다. 오히려 더 나쁘다. 다음 쪽은 아직 없는 것을 받는 것이지만, 재진입은 **이미 보여주고
     * 있던 것**을 지웠다가 다시 그린다.
     *
     * 내부 상태 리셋은 화면을 비우는 것과 별개다 — 낡은 기준 시각과 낡은 `referencePending` 을
     * 걷어내는 것이 이 갱신의 목적이라 커서와 기준 시각은 어느 경로든 비운다.
     */
    private fun reloadFromFirstPage() {
        val cursorBeforeReset = nextCursor
        nextCursor = null
        now = null
        loadFirstPage(
            keepsVisibleList = _uiState.value is NotificationUiState.Data,
            cursorOnKeptListFailure = cursorBeforeReset,
        )
    }

    /**
     * @param cursorOnKeptListFailure 목록을 남기는 경로에서 조회가 실패했을 때 되돌릴 커서.
     * 리셋된 `null` 을 그대로 두면 화면에 남은 목록이 끝까지 스크롤돼도 [loadMore] 가 조용히 빠져나가
     * 다음 쪽을 영영 못 받는다 — design.md §6.3 ② 는 끝 도달에 아무 표시도 하지 않으므로 그 침묵이
     * *"목록이 끝났다"* 와 구분되지 않는다. 실패는 스낵바로 알렸는데 부작용만 소리 없이 남는 셈이다.
     */
    private fun loadFirstPage(keepsVisibleList: Boolean, cursorOnKeptListFailure: Long?) {
        if (isLoading) return
        isLoading = true
        if (!keepsVisibleList) _uiState.update { NotificationUiState.Loading }

        viewModelScope.launch {
            var loaded = false
            var failureMessage: String? = null

            notificationRepository.getNotifications(
                cursor = null,
                size = PAGE_SIZE,
                // 첫 조회 실패는 화면 전체가 말한다(NotificationUiState.Failure). 그 위에 스낵바를
                // 겹치면 같은 사실을 두 번 말하게 된다. 목록을 남기는 경로는 화면이 아무 말도
                // 하지 않으므로 반대로 이 메시지가 유일한 통로다.
                onError = { failureMessage = it },
            ).collect { page ->
                loaded = true
                nextCursor = page.nextCursor
                _uiState.update {
                    // 남겨 둔 목록도 여기서 교체된다. 0건이면 그 사이 전부 지워진 것이라 실제로 비었다.
                    if (page.notifications.isEmpty()) NotificationUiState.Empty
                    else NotificationUiState.Data(itemsOf(page.notifications))
                }
            }

            isLoading = false
            if (loaded) return@launch

            if (keepsVisibleList) {
                // 남긴 목록에는 이어붙일 다음 쪽도 함께 남아 있어야 한다. 성공 경로에서는 서버가 준
                // 새 커서가 이미 자리를 잡았으므로 여기서만 되돌린다.
                nextCursor = cursorOnKeptListFailure
                // 🔴 목록을 비우지 않는다. 화면 전체를 실패로 덮으면 이미 읽고 있던 알림까지 사라진다
                // — 추가 조회 실패를 마지막 슬롯에만 가두는 것과 같은 근거다(design.md §6.3 ③).
                _uiEffect.emit(NotificationUiEffect.ShowMessage(failureMessage ?: RELOAD_FAILURE_MESSAGE))
            } else {
                // 실패를 값으로 만들지 않으면 상류가 아무것도 보내지 않은 채 끝나 화면이 로딩에 갇힌다.
                _uiState.update { NotificationUiState.Failure }
            }
        }
    }

    /**
     * 🔴 **진입 1회.** 쪽을 더 받을 때는 부르지 않는다 — 목록 조회(`GET /notifications`)는 읽음
     * 처리를 하지 않아 두 축이 완전히 분리돼 있고, 쪽마다 부르면 같은 일을 쪽 수만큼 반복한다.
     * 호출부가 [refresh] 하나뿐인 것이 그 보장이다 — [loadMore] 와 [retry] 는 부르지 않는다.
     *
     * 실패해도 목록을 막지 않는다 — 뱃지가 안 꺼질 뿐이다. 사용자가 그 자리에서 할 수 있는 일이
     * 없어 스낵바도 띄우지 않는다.
     *
     * 응답의 `unreadCount` 를 여기서 쓰지 않는 것은 홈 벨 뱃지가 아직 없기 때문이다. 뱃지가 붙어도
     * *"read-all 을 불렀으니 0"* 이라는 규칙을 앱에 두지 않고 서버가 준 값 하나만 본다.
     */
    private fun markAllAsRead() {
        viewModelScope.launch {
            notificationRepository.markAllAsRead(onError = {}).collect { }
        }
    }

    private fun updateAppend(append: NotificationAppendState) {
        _uiState.update { state ->
            if (state is NotificationUiState.Data) state.copy(append = append) else state
        }
    }

    private fun itemsOf(notifications: List<Notification>): List<NotificationItemState> {
        val measuredNow = now ?: nowKst().also { now = it }
        return notifications.map { NotificationItemState(it, measuredNow) }
    }

    private companion object {
        /** 계약 기본값과 같은 값. 서버가 `1..50` 밖을 clamp 하지 않고 거부하므로 임의로 키우지 않는다. */
        const val PAGE_SIZE = 20

        /** 서버가 메시지를 주지 않은 추가 조회 실패의 문구. */
        const val APPEND_FAILURE_MESSAGE = "알림을 더 불러오지 못했어요"

        /** 서버가 메시지를 주지 않은 재진입 갱신 실패의 문구. 화면에 남은 목록은 갱신 전 것이다. */
        const val RELOAD_FAILURE_MESSAGE = "알림을 새로 불러오지 못했어요"
    }
}
