package com.lwg.challenge.feature.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lwg.challenge.domain.repository.NotificationRepository
import com.lwg.challenge.feature.notification.contract.NotificationAppendState
import com.lwg.challenge.feature.notification.contract.NotificationUiEffect
import com.lwg.challenge.feature.notification.contract.NotificationUiState
import com.lwg.challenge.utils.datetime.nowKst
import com.lwg.challenge.utils.flow.updateWithData
import com.lwg.challenge.utils.flow.valueAs
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

private const val PAGE_SIZE = 20

/**
 * 알림 목록. 커서 무한 스크롤 + 진입 시 전체 읽음 처리.
 * 쪽이 누적되는 캐시라 파이프라인(`flatMapLatest`) 대신 명령형으로 둔다 — 갱신 인자가 커서라 이전 쪽이 버려진다.
 */
@KoinViewModel
class NotificationViewModel(
    private val notificationRepository: NotificationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<NotificationUiState>(NotificationUiState.Loading)
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<NotificationUiEffect>()
    val uiEffect: SharedFlow<NotificationUiEffect> = _uiEffect.asSharedFlow()

    // 서버가 준 값을 그대로 되돌려 보낸다. 마지막 항목 id 로 계산하면 커서 규칙의 사본이 앱에 생긴다. null 이면 끝이다.
    private var nextCursor: Long? = null

    // 첫 조회·추가 조회를 한 축으로 막는다. viewModelScope 가 메인이라 읽고 쓰는 지점이 전부 한 스레드다.
    private var isLoading = false

    /**
     * 화면에 진입할 때마다 부른다. 상세에 갔다 와도 VM 이 살아 있어, 기준 시각과 `referencePending` 이 낡지 않게 다시 받는다.
     * 읽음 처리는 멱등이라 재진입마다 돌려 자리를 비운 사이 온 알림까지 읽음으로 만든다.
     */
    fun refresh() {
        reloadFromFirstPage()
        markAllAsRead()
    }

    /** 마지막 항목에 닿았을 때와 "다시 시도" 가 함께 부른다. 더 받을지는 커서를 쥔 여기서만 판단한다. */
    fun loadMore() {
        val cursor = nextCursor ?: return
        if (isLoading) return
        isLoading = true
        updateAppend(NotificationAppendState.Loading)

        viewModelScope.launch {
            notificationRepository.getNotifications(
                cursor = cursor,
                size = PAGE_SIZE,
                onError = { message ->
                    // 스낵바는 사라지면 끝이라 마지막 슬롯을 "다시 시도" 로 남긴다.
                    updateAppend(NotificationAppendState.Failed)
                    showMessage(message)
                },
            ).collect { page ->
                nextCursor = page.nextCursor
                _uiState.updateWithData<NotificationUiState, NotificationUiState.Data> { data ->
                    data.copy(
                        notifications = data.notifications + page.notifications,
                        append = NotificationAppendState.Idle,
                    )
                }
            }
            isLoading = false
        }
    }

    /** 첫 조회 실패 화면의 "다시 시도". 읽음 처리는 진입 때 이미 돌았다. */
    fun retry() {
        reloadFromFirstPage()
    }

    // 보이던 목록이 있으면 Loading 으로 덮지 않는다. 덮으면 읽던 목록과 스크롤 위치를 잃는다.
    private fun reloadFromFirstPage() {
        if (isLoading) return
        isLoading = true
        val cursorBeforeReset = nextCursor
        nextCursor = null
        val keepsVisibleList = _uiState.valueAs<NotificationUiState.Data>() != null
        if (!keepsVisibleList) _uiState.update { NotificationUiState.Loading }

        viewModelScope.launch {
            notificationRepository.getNotifications(
                cursor = null,
                size = PAGE_SIZE,
                onError = { message ->
                    if (keepsVisibleList) {
                        // 남긴 목록이 다음 쪽을 계속 받을 수 있게 리셋 전 커서를 되돌린다.
                        nextCursor = cursorBeforeReset
                        showMessage(message)
                    } else {
                        _uiState.update { NotificationUiState.Error }
                    }
                },
            ).collect { page ->
                nextCursor = page.nextCursor
                _uiState.update {
                    if (page.notifications.isEmpty()) {
                        NotificationUiState.Empty
                    } else {
                        NotificationUiState.Data(notifications = page.notifications, now = nowKst())
                    }
                }
            }
            isLoading = false
        }
    }

    // 쪽을 더 받을 때는 부르지 않는다. 실패해도 뱃지가 안 꺼질 뿐이고 사용자가 할 일이 없어 알리지 않는다.
    private fun markAllAsRead() {
        viewModelScope.launch {
            notificationRepository.markAllAsRead(onError = {}).collect { }
        }
    }

    private fun updateAppend(append: NotificationAppendState) {
        _uiState.updateWithData<NotificationUiState, NotificationUiState.Data> { it.copy(append = append) }
    }

    private fun showMessage(message: String) {
        viewModelScope.launch {
            _uiEffect.emit(NotificationUiEffect.ShowMessage(message))
        }
    }
}
