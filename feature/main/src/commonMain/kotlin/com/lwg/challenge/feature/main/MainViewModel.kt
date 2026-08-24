package com.lwg.challenge.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lwg.challenge.domain.usecase.LogoutUseCase
import com.lwg.challenge.navigation.Route
import com.lwg.challenge.permission.AppPermission
import com.lwg.challenge.permission.PermissionManager
import com.lwg.challenge.push.PushEvent
import com.lwg.challenge.push.PushEventBus
import com.lwg.challenge.utils.AuthEventBus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
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

    // 탭 이동으로 화면이 재구성돼도 다시 묻지 않는다. ViewModel 이 살아 있는 동안 1회.
    private var notificationPermissionRequested = false

    private val _isAuthenticatedArea = MutableStateFlow(false)

    private val _pushNavigation = MutableSharedFlow<Route>(extraBufferCapacity = 1)
    val pushNavigation: SharedFlow<Route> = _pushNavigation.asSharedFlow()

    // 세션 만료(자동)와 사용자 로그아웃(수동)은 정리·이동이 동일해 한 흐름으로 합친다.
    // 리소스 정리 후 UI 가 로그인으로 이동하도록 흘려보낸다 (네비게이션은 UI 책임)
    val loggedOut: Flow<Unit> = merge(authEventBus.sessionExpired, _logoutRequested)
        .onEach {
            logoutUseCase()
            pushEventBus.consume()
        }

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

/** 이벤트 → 목적지. `else ->` 를 쓰면 새 타입에서 목적지 지정을 강제하지 못한다. */
private fun PushEvent.toRoute(): Route = when (this) {
    // 수락/거절 액션이 홈 '받은 도전장' 섹션에만 있다.
    is PushEvent.ChallengeRequest -> Route.Home
    is PushEvent.ChallengeAccepted -> Route.Challenge.Detail(challengeId)
    // 거절된 챌린지는 계약서가 없어 상세에 볼 것이 없다.
    is PushEvent.ChallengeRejected -> Route.Home
    // 거절과 반대로 볼 것(상대 인증 사진)이 생겼고, 그 사진은 상세에서만 보인다. 홈 카드는 상태 뱃지뿐.
    is PushEvent.OpponentVerified -> Route.Challenge.Detail(challengeId)
}
