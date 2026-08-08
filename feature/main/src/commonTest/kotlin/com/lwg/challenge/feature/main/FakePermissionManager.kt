package com.lwg.challenge.feature.main

import androidx.compose.runtime.Composable
import com.lwg.challenge.permission.AppPermission
import com.lwg.challenge.permission.PermissionManager
import com.lwg.challenge.permission.PermissionStatus

/** 권한 요청은 딥링크 게이트와 무관해 항상 허용으로 답한다. */
class FakePermissionManager : PermissionManager {

    var status: PermissionStatus = PermissionStatus.GRANTED

    override suspend fun status(permission: AppPermission): PermissionStatus = status

    override suspend fun request(permission: AppPermission): PermissionStatus = status

    override fun openAppSettings() = Unit

    @Composable
    override fun Bind() {
    }
}
