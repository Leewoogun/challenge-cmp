package com.lwg.challenge.feature.ex1

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lwg.challenge.feature.ex1.contract.Ex1ModalEffect
import com.lwg.challenge.feature.ex1.contract.Ex1UiEffect
import com.lwg.challenge.feature.ex1.contract.Ex1UiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class Ex1ViewModel(
    // TODO: 의존성을 주입하세요.
) : ViewModel() {

    private val _uiState: MutableStateFlow<Ex1UiState> = MutableStateFlow(Ex1UiState.Loading)
    val uiState: StateFlow<Ex1UiState> get() = _uiState

    private val _modalEffect = MutableStateFlow<Ex1ModalEffect>(Ex1ModalEffect.Hidden)
    val modalEffect: StateFlow<Ex1ModalEffect> get() = _modalEffect

    private val _uiEffect = MutableSharedFlow<Ex1UiEffect>()
    val uiEffect: SharedFlow<Ex1UiEffect> get() = _uiEffect

    fun dismiss() {
        _modalEffect.update { Ex1ModalEffect.Hidden }
    }

    private fun showMessage(message: String) {
        viewModelScope.launch {
            _uiEffect.emit(Ex1UiEffect.ShowMessage(message))
        }
    }
}
