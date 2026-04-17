package com.lwg.challenge.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lwg.challenge.domain.repository.MovieRepository
import com.lwg.challenge.feature.home.contract.HomeModalEffect
import com.lwg.challenge.feature.home.contract.HomeUiEffect
import com.lwg.challenge.feature.home.contract.HomeUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class HomeViewModel(
    private val movieRepository: MovieRepository,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = movieRepository.getTopRatedMovies(
        onError = ::showMessage,
    ).map { movies ->
        if (movies.isEmpty()) {
            HomeUiState.Empty
        } else {
            HomeUiState.Data(movies = movies)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState.Loading,
    )

    private val _modalEffect = MutableStateFlow<HomeModalEffect>(HomeModalEffect.Hidden)
    val modalEffect: StateFlow<HomeModalEffect> get() = _modalEffect

    private val _uiEffect = MutableSharedFlow<HomeUiEffect>()
    val uiEffect: SharedFlow<HomeUiEffect> get() = _uiEffect

    fun dismiss() {
        _modalEffect.update { HomeModalEffect.Hidden }
    }

    private fun showMessage(message: String) {
        viewModelScope.launch {
            _uiEffect.emit(HomeUiEffect.ShowMessage(message))
        }
    }
}
