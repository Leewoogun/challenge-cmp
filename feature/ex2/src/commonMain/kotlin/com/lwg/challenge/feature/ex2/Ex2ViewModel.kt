package com.lwg.challenge.feature.ex2

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lwg.challenge.feature.ex2.contract.Ex2ModalEffect
import com.lwg.challenge.feature.ex2.contract.Ex2UiEffect
import com.lwg.challenge.feature.ex2.contract.Ex2UiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class Ex2ViewModel(
    // TODO: 의존성을 주입하세요.
) : ViewModel() {

    private val _uiState: MutableStateFlow<Ex2UiState> = MutableStateFlow(Ex2UiState.Loading)
    val uiState: StateFlow<Ex2UiState> get() = _uiState

    private val _modalEffect = MutableStateFlow<Ex2ModalEffect>(Ex2ModalEffect.Hidden)
    val modalEffect: StateFlow<Ex2ModalEffect> get() = _modalEffect

    private val _uiEffect = MutableSharedFlow<Ex2UiEffect>()
    val uiEffect: SharedFlow<Ex2UiEffect> get() = _uiEffect

    fun dismiss() {
        _modalEffect.update { Ex2ModalEffect.Hidden }
    }

    private fun showMessage(message: String) {
        viewModelScope.launch {
            _uiEffect.emit(Ex2UiEffect.ShowMessage(message))
        }
    }
}
