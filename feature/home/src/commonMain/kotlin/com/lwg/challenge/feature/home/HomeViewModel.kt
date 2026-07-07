package com.lwg.challenge.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lwg.challenge.domain.usecase.GetHomeDataUseCase
import com.lwg.challenge.feature.home.contract.HomeUiEffect
import com.lwg.challenge.feature.home.contract.HomeUiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class HomeViewModel(
    getHomeDataUseCase: GetHomeDataUseCase,
) : ViewModel() {

    private val _uiEffect = Channel<HomeUiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<HomeUiEffect> = _uiEffect.receiveAsFlow()

    val uiState: StateFlow<HomeUiState> = getHomeDataUseCase(::showMessage)
        .map { data ->
            HomeUiState.Data(
                record = data.record,
                challenges = data.challenges,
                userInfo = data.userInfo,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(0),
            initialValue = HomeUiState.Loading,
        )

    private fun showMessage(message: String) {
        viewModelScope.launch {
            _uiEffect.send(HomeUiEffect.ShowMessage(message))
        }
    }
}
