package com.lwg.challenge.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lwg.challenge.domain.usecase.LogoutUseCase
import com.lwg.challenge.permission.AppPermission
import com.lwg.challenge.permission.PermissionManager
import com.lwg.challenge.utils.AuthEventBus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class MainViewModel(
    authEventBus: AuthEventBus,
    private val logoutUseCase: LogoutUseCase,
    private val permissionManager: PermissionManager,
) : ViewModel() {

    private val _logoutRequested = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    // 탭 이동으로 화면이 재구성돼도 다시 묻지 않는다. ViewModel 이 살아 있는 동안 1회.
    private var notificationPermissionRequested = false

    // 세션 만료(자동)와 사용자 로그아웃(수동)은 정리·이동이 동일해 한 흐름으로 합친다.
    // 리소스 정리 후 UI 가 로그인으로 이동하도록 흘려보낸다 (네비게이션은 UI 책임)
    val loggedOut: Flow<Unit> = merge(authEventBus.sessionExpired, _logoutRequested)
        .onEach { logoutUseCase() }

    fun logout() {
        _logoutRequested.tryEmit(Unit)
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
