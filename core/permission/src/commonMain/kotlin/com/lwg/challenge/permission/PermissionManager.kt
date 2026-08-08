package com.lwg.challenge.permission

import androidx.compose.runtime.Composable

/**
 * 런타임 권한 조회·요청 창구. ViewModel 이 주입받아 쓴다.
 */
interface PermissionManager {

    suspend fun status(permission: AppPermission): PermissionStatus

    suspend fun request(permission: AppPermission): PermissionStatus

    fun openAppSettings()

    /**
     * iOS 권한 API 는 완료 핸들러로 바로 끝나 호스트가 필요 없다. 그래서 iOS 구현은 no-op 이고
     * 호출하지 않아도 된다 — 현재 `MainActivity` 에서만 부른다.
     */
    @Composable
    fun Bind()
}
