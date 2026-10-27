package com.lwg.challenge.feature.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lwg.challenge.domain.repository.NotificationRepository
import com.lwg.challenge.domain.usecase.GetNotificationsUseCase
import com.lwg.challenge.feature.notification.contract.NotificationUiEffect
import com.lwg.challenge.feature.notification.contract.NotificationUiState
import com.lwg.challenge.utils.datetime.nowKst
import com.lwg.challenge.utils.flow.TriggerStateFlow
import com.lwg.challenge.utils.flow.triggerStateIn
import com.lwg.challenge.utils.flow.valueAs
import com.lwg.challenge.utils.flow.withData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

/** 알림 목록. 누적과 다음 키는 [GetNotificationsUseCase] 가 쥔다. */
@KoinViewModel
class NotificationViewModel(
    private val getNotificationsUseCase: GetNotificationsUseCase,
    private val notificationRepository: NotificationRepository,
) : ViewModel() {

    private val _uiEffect = MutableSharedFlow<NotificationUiEffect>()
    val uiEffect: SharedFlow<NotificationUiEffect> = _uiEffect.asSharedFlow()

    // 상세에 다녀오면 재구독으로 첫 쪽부터 다시 받는다. VM 이 백스택에 살아 있어 referencePending 과 기준 시각이 낡기 때문이다.
    val uiState: TriggerStateFlow<NotificationUiState> = notificationsFlow()
        .triggerStateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(0),
            initialValue = NotificationUiState.Loading,
        )

    // 끝에 닿았을 때 부른다. 끝 도달·중복 요청은 PagingSource 가 거른다. 실패하면 다시 스크롤해 재시도한다.
    fun nextPage() {
        getNotificationsUseCase.nextPage()
    }

    /** 첫 조회 실패 화면의 "다시 시도". */
    fun retry() {
        uiState.restart()
    }

    private fun notificationsFlow(): Flow<NotificationUiState> = channelFlow {
        uiState.withData<NotificationUiState.Error> { send(NotificationUiState.Loading) }

        launch { markAllAsRead() }

        // 페이지 요청이 replay 로 마지막 키를 기억해, 그대로 재구독하면 첫 쪽이 아니라 마지막 쪽만 다시 받는다.
        getNotificationsUseCase.refresh()

        getNotificationsUseCase(
            onError = { message ->
                // 남길 목록이 있으면(재진입·다음 쪽 실패) 스낵바로만, 없으면(첫 조회 실패) 재시도 화면.
                if (uiState.valueAs<NotificationUiState.Data>() != null) {
                    showMessage(message)
                } else {
                    trySend(NotificationUiState.Error)
                }
            },
        ).collect { result ->
            send(
                if (result.notifications.isEmpty()) {
                    NotificationUiState.Empty
                } else {
                    NotificationUiState.Data(notifications = result.notifications, now = nowKst())
                },
            )
        }
    }

    // 멱등이라 구독마다 돌려 자리를 비운 사이 온 알림까지 읽음으로 만든다. 실패해도 할 일이 없어 알리지 않는다.
    private suspend fun markAllAsRead() {
        notificationRepository.markAllAsRead(onError = {}).collect { }
    }

    private fun showMessage(message: String) {
        viewModelScope.launch {
            _uiEffect.emit(NotificationUiEffect.ShowMessage(message))
        }
    }
}
