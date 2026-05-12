package com.lwg.challenge.feature.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lwg.challenge.feature.mypage.contract.MyPageModalEffect
import com.lwg.challenge.feature.mypage.contract.MyPageUiEffect
import com.lwg.challenge.feature.mypage.contract.MyPageUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class MyPageViewModel(
    // TODO: 의존성을 주입하세요.
) : ViewModel() {

    private val _uiState: MutableStateFlow<MyPageUiState> = MutableStateFlow(MyPageUiState.Data())
    val uiState: StateFlow<MyPageUiState> get() = _uiState

    private val _modalEffect = MutableStateFlow<MyPageModalEffect>(MyPageModalEffect.Hidden)
    val modalEffect: StateFlow<MyPageModalEffect> get() = _modalEffect

    private val _uiEffect = MutableSharedFlow<MyPageUiEffect>()
    val uiEffect: SharedFlow<MyPageUiEffect> get() = _uiEffect

    fun dismiss() {
        _modalEffect.update { MyPageModalEffect.Hidden }
    }

    private fun showMessage(message: String) {
        viewModelScope.launch {
            _uiEffect.emit(MyPageUiEffect.ShowMessage(message))
        }
    }
}
