package com.lwg.challenge.feature.mypage.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lwg.challenge.domain.usecase.GetChallengeArchiveUseCase
import com.lwg.challenge.feature.mypage.archive.contract.ArchiveUiEffect
import com.lwg.challenge.feature.mypage.archive.contract.ArchiveUiState
import com.lwg.challenge.utils.datetime.nowKst
import com.lwg.challenge.utils.flow.TriggerStateFlow
import com.lwg.challenge.utils.flow.triggerStateIn
import com.lwg.challenge.utils.flow.valueAs
import com.lwg.challenge.utils.flow.withData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minusMonth
import kotlinx.datetime.plusMonth
import org.koin.android.annotation.KoinViewModel

/** 계약서 보관함. 한 번에 한 달치만 조회하고 화살표로 달을 옮긴다. */
@OptIn(ExperimentalCoroutinesApi::class)
@KoinViewModel
class ArchiveViewModel(
    private val getChallengeArchiveUseCase: GetChallengeArchiveUseCase,
    now: () -> LocalDateTime = ::nowKst,
) : ViewModel() {

    private val thisMonth: YearMonth = now().let { YearMonth(it.year, it.month) }

    private val _uiEffect = MutableSharedFlow<ArchiveUiEffect>()
    val uiEffect: SharedFlow<ArchiveUiEffect> = _uiEffect.asSharedFlow()

    private val selectedMonth = MutableStateFlow(thisMonth)

    val uiState: TriggerStateFlow<ArchiveUiState> = selectedMonth
        .flatMapLatest(::monthFlow)
        .triggerStateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(0),
            initialValue = ArchiveUiState.Loading,
        )

    /**
     * 한 달치 조회를 화면 상태로 흘린다. 이전 [ArchiveUiState.Data] 가 있으면 월 전환이라 아무것도 내보내지 않고
     * 응답을 기다리며, 실패하면 이전 달을 덮지 않고 스낵바로 알린다. 이전 Data 가 없을 때(첫 로딩)만 Loading·Error 를 낸다.
     */
    private fun monthFlow(requested: YearMonth): Flow<ArchiveUiState> = channelFlow {
        val previous = uiState.valueAs<ArchiveUiState.Data>()
        if (previous == null) send(ArchiveUiState.Loading)

        getChallengeArchiveUseCase(
            month = requested,
            onError = { message ->
                if (previous == null) {
                    trySend(ArchiveUiState.Error)
                } else {
                    showMessage(message)
                    selectedMonth.update { previous.selectedMonth }
                }
            },
        ).collect { archive ->
            send(
                ArchiveUiState.Data(
                    selectedMonth = requested,
                    challenges = archive.histories,
                    firstArchivedMonth = archive.firstArchivedMonth,
                    canGoNext = requested < thisMonth,
                    isThisMonth = requested == thisMonth,
                ),
            )
        }
    }

    fun previousMonth() {
        uiState.withData<ArchiveUiState.Data> { data -> selectedMonth.update { data.selectedMonth.minusMonth() } }
    }

    fun nextMonth() {
        uiState.withData<ArchiveUiState.Data> { data ->
            if (data.canGoNext) selectedMonth.update { data.selectedMonth.plusMonth() }
        }
    }
    fun retry() {
        uiState.restart()
    }

    private fun showMessage(message: String) {
        viewModelScope.launch {
            _uiEffect.emit(ArchiveUiEffect.ShowMessage(message))
        }
    }
}
