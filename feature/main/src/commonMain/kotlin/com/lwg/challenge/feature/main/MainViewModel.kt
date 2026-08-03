package com.lwg.challenge.feature.main

import androidx.lifecycle.ViewModel
import com.lwg.challenge.domain.usecase.LogoutUseCase
import com.lwg.challenge.utils.AuthEventBus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class MainViewModel(
    authEventBus: AuthEventBus,
    private val logoutUseCase: LogoutUseCase,
) : ViewModel() {

    private val _logoutRequested = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    // 세션 만료(자동)와 사용자 로그아웃(수동)은 정리·이동이 동일해 한 흐름으로 합친다.
    // 리소스 정리 후 UI 가 로그인으로 이동하도록 흘려보낸다 (네비게이션은 UI 책임)
    val loggedOut: Flow<Unit> = merge(authEventBus.sessionExpired, _logoutRequested)
        .onEach { logoutUseCase() }

    fun logout() {
        _logoutRequested.tryEmit(Unit)
    }
}
