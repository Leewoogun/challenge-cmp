package com.lwg.challenge.feature.mypage.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lwg.challenge.domain.usecase.GetMyPageDataUseCase
import com.lwg.challenge.domain.usecase.WithdrawUseCase
import com.lwg.challenge.feature.mypage.mypage.contract.MyPageModalEffect
import com.lwg.challenge.feature.mypage.mypage.contract.MyPageUiEffect
import com.lwg.challenge.feature.mypage.mypage.contract.MyPageUiState
import com.lwg.challenge.utils.flow.TriggerStateFlow
import com.lwg.challenge.utils.flow.triggerStateIn
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class MyPageViewModel(
    getMyPageDataUseCase: GetMyPageDataUseCase,
    private val withdrawUseCase: WithdrawUseCase,
) : ViewModel() {

    private val _modalEffect = MutableStateFlow<MyPageModalEffect>(MyPageModalEffect.Hidden)
    val modalEffect: StateFlow<MyPageModalEffect> get() = _modalEffect

    private val _uiEffect = MutableSharedFlow<MyPageUiEffect>()
    val uiEffect: SharedFlow<MyPageUiEffect> = _uiEffect.asSharedFlow()

    // 조회 파이프라인 밖에 둔다 — 탈퇴는 재조회 없이 진행 여부만 바꾸는 상태다.
    private val _isWithdrawing = MutableStateFlow(false)

    val uiState: TriggerStateFlow<MyPageUiState> = combine(
        getMyPageDataUseCase(::showMessage),
        _isWithdrawing,
    ) { data, isWithdrawing ->
        MyPageUiState.Data(
            record = data.record,
            userInfo = data.userInfo,
            isWithdrawing = isWithdrawing,
        )
    }.triggerStateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(0),
        initialValue = MyPageUiState.Loading,
    )

    fun requestWithdraw() {
        if (_isWithdrawing.value) return
        _modalEffect.update { MyPageModalEffect.ConfirmWithdraw }
    }

    fun withdraw() {
        if (_isWithdrawing.value) return
        _isWithdrawing.update { true }
        dismiss()

        viewModelScope.launch {
            withdrawUseCase(
                onSuccess = {
                    viewModelScope.launch { _uiEffect.emit(MyPageUiEffect.Withdrawn) }
                },
                onError = { message ->
                    _isWithdrawing.update { false }
                    showMessage(message)
                },
            )
        }
    }

    fun dismiss() {
        _modalEffect.update { MyPageModalEffect.Hidden }
    }

    private fun showMessage(message: String) {
        viewModelScope.launch {
            _uiEffect.emit(MyPageUiEffect.ShowMessage(message))
        }
    }
}
