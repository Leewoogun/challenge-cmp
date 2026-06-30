package com.lwg.challenge.feature.main

import androidx.lifecycle.ViewModel
import com.lwg.challenge.domain.usecase.LogoutUseCase
import com.lwg.challenge.utils.AuthEventBus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class MainViewModel(
    authEventBus: AuthEventBus,
    private val logoutUseCase: LogoutUseCase,
) : ViewModel() {

    // 세션 만료 시 리소스 정리 후 UI 가 로그인으로 이동하도록 흘려보낸다 (네비게이션은 UI 책임)
    val sessionExpired: Flow<Unit> = authEventBus.sessionExpired
        .onEach { logoutUseCase() }
}
