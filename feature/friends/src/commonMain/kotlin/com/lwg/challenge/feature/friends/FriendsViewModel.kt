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

/**
 * 친구 탭 ViewModel.
 *
 * 1차 1단계 — 백엔드 호출 0건. 진입과 동시에 [FriendsUiState.Data] 상태.
 * 1차 2단계 도입 시 GetFriendsUseCase 주입 + Flow 결합.
 */
@KoinViewModel
class FriendsViewModel : ViewModel() {

    private val _uiState: MutableStateFlow<FriendsUiState> = MutableStateFlow(FriendsUiState.Data)
    val uiState: StateFlow<FriendsUiState> get() = _uiState

    private val _modalEffect = MutableStateFlow<FriendsModalEffect>(FriendsModalEffect.Hidden)
    val modalEffect: StateFlow<FriendsModalEffect> get() = _modalEffect

    private val _uiEffect = MutableSharedFlow<FriendsUiEffect>()
    val uiEffect: SharedFlow<FriendsUiEffect> get() = _uiEffect

    fun dismiss() {
        _modalEffect.update { FriendsModalEffect.Hidden }
    }

    internal fun showMessage(message: String) {
        viewModelScope.launch {
            _uiEffect.emit(FriendsUiEffect.ShowMessage(message))
        }
    }
}
