package com.lwg.challenge.feature.ex3

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lwg.challenge.feature.ex3.contract.Ex3ModalEffect
import com.lwg.challenge.feature.ex3.contract.Ex3UiEffect
import com.lwg.challenge.feature.ex3.contract.Ex3UiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class Ex3ViewModel(
    // TODO: 의존성을 주입하세요.
) : ViewModel() {

    private val _uiState: MutableStateFlow<Ex3UiState> = MutableStateFlow(Ex3UiState.Loading)
    val uiState: StateFlow<Ex3UiState> get() = _uiState

    private val _modalEffect = MutableStateFlow<Ex3ModalEffect>(Ex3ModalEffect.Hidden)
    val modalEffect: StateFlow<Ex3ModalEffect> get() = _modalEffect

    private val _uiEffect = MutableSharedFlow<Ex3UiEffect>()
    val uiEffect: SharedFlow<Ex3UiEffect> get() = _uiEffect

    fun dismiss() {
        _modalEffect.update { Ex3ModalEffect.Hidden }
    }

    private fun showMessage(message: String) {
        viewModelScope.launch {
            _uiEffect.emit(Ex3UiEffect.ShowMessage(message))
        }
    }
}
