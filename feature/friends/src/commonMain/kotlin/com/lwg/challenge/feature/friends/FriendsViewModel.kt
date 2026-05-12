package com.lwg.challenge.feature.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lwg.challenge.feature.friends.contract.FriendsModalEffect
import com.lwg.challenge.feature.friends.contract.FriendsUiEffect
import com.lwg.challenge.feature.friends.contract.FriendsUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class FriendsViewModel(
    // TODO: 의존성을 주입하세요.
) : ViewModel() {

    private val _uiState: MutableStateFlow<FriendsUiState> = MutableStateFlow(FriendsUiState.Data())
    val uiState: StateFlow<FriendsUiState> get() = _uiState

    private val _modalEffect = MutableStateFlow<FriendsModalEffect>(FriendsModalEffect.Hidden)
    val modalEffect: StateFlow<FriendsModalEffect> get() = _modalEffect

    private val _uiEffect = MutableSharedFlow<FriendsUiEffect>()
    val uiEffect: SharedFlow<FriendsUiEffect> get() = _uiEffect

    fun dismiss() {
        _modalEffect.update { FriendsModalEffect.Hidden }
    }

    private fun showMessage(message: String) {
        viewModelScope.launch {
            _uiEffect.emit(FriendsUiEffect.ShowMessage(message))
        }
    }
}
