package com.lwg.challenge.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lwg.challenge.domain.model.LogoutReason
import com.lwg.challenge.domain.usecase.LogoutUseCase
import com.lwg.challenge.feature.main.contract.MainUiEffect
import com.lwg.challenge.permission.AppPermission
import com.lwg.challenge.permission.PermissionManager
import com.lwg.challenge.push.PushEventBus
import com.lwg.challenge.push.toRoute
import com.lwg.challenge.utils.AuthEventBus
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class MainViewModel(
    authEventBus: AuthEventBus,
    private val pushEventBus: PushEventBus,
    private val logoutUseCase: LogoutUseCase,
    private val permissionManager: PermissionManager,
) : ViewModel() {

    private val _logoutRequested = MutableSharedFlow<LogoutReason>(extraBufferCapacity = 1)

    // 탭 이동으로 화면이 재구성돼도 다시 묻지 않는다. ViewModel 이 살아 있는 동안 1회.
    private var notificationPermissionRequested = false

    private val _isAuthenticatedArea = MutableStateFlow(false)

    private val _uiEffect = MutableSharedFlow<MainUiEffect>(extraBufferCapacity = 1)
    val uiEffect: SharedFlow<MainUiEffect> = _uiEffect.asSharedFlow()

    init {
        // token 만료, 사용자 로그아웃, 회원탈퇴
        viewModelScope.launch {
            merge(
                authEventBus.sessionExpired.map { LogoutReason.SESSION_EXPIRED },
                _logoutRequested,
            ).collect { reason ->
                logoutUseCase(reason)
                pushEventBus.consume()
                _uiEffect.emit(MainUiEffect.NavigateToLogin)
            }
        }

        viewModelScope.launch {
            combine(pushEventBus.pending, _isAuthenticatedArea) { event, isReady ->
                event.takeIf { isReady }
            }.collect { event ->
                if (event == null) return@collect

                pushEventBus.consume()
                _uiEffect.emit(MainUiEffect.NavigateToPush(event.toRoute()))
            }
        }
    }

    /** Splash/Login 을 벗어났는지. 화면(MainRoute)이 백스택에서 판정해 알려준다. */
    fun setAuthenticatedArea(isAuthenticatedArea: Boolean) {
        _isAuthenticatedArea.update { isAuthenticatedArea }
    }

    fun logout() {
        _logoutRequested.tryEmit(LogoutReason.USER)
    }

    fun withdrawn() {
        _logoutRequested.tryEmit(LogoutReason.WITHDRAWN)
    }

    /**
     * 알림 권한을 요청한다.
     */
    fun requestNotificationPermission() {
        if (notificationPermissionRequested) return
        notificationPermissionRequested = true

        viewModelScope.launch {
            permissionManager.request(AppPermission.NOTIFICATION)
        }
    }
}
