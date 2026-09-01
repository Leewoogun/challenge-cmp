package com.lwg.challenge.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lwg.challenge.domain.model.LogoutReason
import com.lwg.challenge.domain.usecase.LogoutUseCase
import com.lwg.challenge.navigation.Route
import com.lwg.challenge.permission.AppPermission
import com.lwg.challenge.permission.PermissionManager
import com.lwg.challenge.push.PushEventBus
import com.lwg.challenge.push.toRoute
import com.lwg.challenge.utils.AuthEventBus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
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

    private val _logoutRequested = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    private val _withdrawn = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    // 탭 이동으로 화면이 재구성돼도 다시 묻지 않는다. ViewModel 이 살아 있는 동안 1회.
    private var notificationPermissionRequested = false

    private val _isAuthenticatedArea = MutableStateFlow(false)

    private val _pushNavigation = MutableSharedFlow<Route>(extraBufferCapacity = 1)
    val pushNavigation: SharedFlow<Route> = _pushNavigation.asSharedFlow()

    // 세션 만료(자동)와 사용자 로그아웃(수동)은 정리·이동이 동일해 한 흐름으로 합친다.
    // 다만 서버 logout 호출 여부는 갈리므로 이유를 실어 보낸다 — 만료는 이미 세션이 끊긴 상태라
    // 서버를 부르면 401 → 갱신 실패 → 만료 이벤트로 이 흐름에 재진입한다.
    // 리소스 정리 후 UI 가 로그인으로 이동하도록 흘려보낸다 (네비게이션은 UI 책임)
    // 탈퇴도 합류시킨다 — 목적지(로그인 화면)와 정리 대상이 같다. WITHDRAWN 의 로컬 정리는
    // WithdrawUseCase 가 이미 끝냈지만 clearTokens/clearUserInfoCache 가 멱등이라 여기서 한 번 더
    // 타도 무해하고, 그 대신 이동 경로가 하나로 유지된다.
    val loggedOut: Flow<Unit> = merge(
        authEventBus.sessionExpired.map { LogoutReason.SESSION_EXPIRED },
        _logoutRequested.map { LogoutReason.USER },
        _withdrawn.map { LogoutReason.WITHDRAWN },
    ).onEach { reason ->
        logoutUseCase(reason)
        pushEventBus.consume()
    }.map { }

    init {
        viewModelScope.launch {
            combine(pushEventBus.pending, _isAuthenticatedArea) { event, isReady ->
                event.takeIf { isReady }
            }.collect { event ->
                if (event == null) return@collect

                pushEventBus.consume()
                _pushNavigation.emit(event.toRoute())
            }
        }
    }

    /** Splash/Login 을 벗어났는지. 화면(MainRoute)이 백스택에서 판정해 알려준다. */
    fun setAuthenticatedArea(isAuthenticatedArea: Boolean) {
        _isAuthenticatedArea.update { isAuthenticatedArea }
    }

    fun logout() {
        _logoutRequested.tryEmit(Unit)
    }

    fun withdrawn() {
        _withdrawn.tryEmit(Unit)
    }

    /**
     * 알림 권한을 요청한다.
     *
     * 결과를 보지 않는다 — 알림은 없어도 앱이 도는 부가 기능이라, 거부한 사람을 설정으로 보내는 건
     * 과하다. 특히 iOS 는 첫 거부가 곧바로 영구 거부라, 방금 거절한 사람에게 바로 설정 안내를
     * 띄우게 된다. 기능이 막히는 권한(카메라)에서만 안내 다이얼로그를 쓴다.
     */
    fun requestNotificationPermission() {
        if (notificationPermissionRequested) return
        notificationPermissionRequested = true

        viewModelScope.launch {
            permissionManager.request(AppPermission.NOTIFICATION)
        }
    }
}
