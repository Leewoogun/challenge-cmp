package com.lwg.challenge.feature.challenge.verify

import androidx.compose.runtime.Composable
import com.lwg.challenge.permission.AppPermission
import com.lwg.challenge.permission.PermissionManager
import com.lwg.challenge.permission.PermissionStatus

class FakePermissionManager : PermissionManager {

    var statusResult: PermissionStatus = PermissionStatus.GRANTED

    var requestResult: PermissionStatus = PermissionStatus.GRANTED

    var requestCallCount: Int = 0

    var openAppSettingsCallCount: Int = 0

    override suspend fun status(permission: AppPermission): PermissionStatus = statusResult

    override suspend fun request(permission: AppPermission): PermissionStatus {
        requestCallCount++
        return requestResult
    }

    override fun openAppSettings() {
        openAppSettingsCallCount++
    }

    @Composable
    override fun Bind() = Unit
}
