package com.lwg.challenge.permission

import androidx.compose.runtime.Composable
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.RequestCanceledException
import dev.icerock.moko.permissions.camera.CAMERA
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.notifications.REMOTE_NOTIFICATION

internal class PermissionManagerImpl(
    private val controller: PermissionsController,
) : PermissionManager {

    override suspend fun status(permission: AppPermission): PermissionStatus =
        controller.getPermissionState(permission.toMoko()).toStatus()

    override suspend fun request(permission: AppPermission): PermissionStatus = try {
        controller.providePermission(permission.toMoko())
        PermissionStatus.GRANTED
    } catch (e: DeniedAlwaysException) {
        PermissionStatus.DENIED_ALWAYS
    } catch (e: DeniedException) {
        PermissionStatus.DENIED
    } catch (e: RequestCanceledException) {
        // 프롬프트 밖을 탭하는 등으로 답 없이 닫힌 경우. 거부로 확정하지 않는다 — 다시 물을 수 있다.
        PermissionStatus.DENIED
    }

    override fun openAppSettings() {
        controller.openAppSettings()
    }

    @Composable
    override fun Bind() {
        BindEffect(controller)
    }
}

private fun AppPermission.toMoko(): Permission = when (this) {
    AppPermission.NOTIFICATION -> Permission.REMOTE_NOTIFICATION
    AppPermission.CAMERA -> Permission.CAMERA
}

/**
 * `NotDetermined` 는 아직 물어본 적이 없는 상태라 "거부" 가 아니다. 다시 물을 수 있다는 점에서
 * [PermissionStatus.DENIED] 와 같은 취급을 받는다.
 */
private fun PermissionState.toStatus(): PermissionStatus = when (this) {
    PermissionState.Granted -> PermissionStatus.GRANTED
    PermissionState.DeniedAlways -> PermissionStatus.DENIED_ALWAYS
    PermissionState.NotDetermined,
    PermissionState.NotGranted,
    PermissionState.Denied,
    -> PermissionStatus.DENIED
}
