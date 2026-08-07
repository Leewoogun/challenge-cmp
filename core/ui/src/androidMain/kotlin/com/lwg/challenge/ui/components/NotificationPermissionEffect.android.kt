package com.lwg.challenge.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
actual fun NotificationPermissionEffect(enabled: Boolean) {
    // Android 13 미만은 POST_NOTIFICATIONS 런타임 권한 자체가 존재하지 않는다.
    // SDK_INT 는 런타임 내내 불변이라 이 조기 return 으로 컴포지션 그룹이 어긋나지 않는다.
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val context = LocalContext.current
    // 탭 이동으로 재구성될 때마다 다시 묻지 않도록 프로세스 재생성까지 살아남는 플래그로 1회만 요청한다.
    var requested by rememberSaveable { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) {
        // 거부돼도 되돌릴 동작이 없다 — 푸시만 안 올 뿐 앱 흐름은 그대로다.
    }

    LaunchedEffect(enabled, requested) {
        if (!enabled || requested) return@LaunchedEffect
        requested = true

        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
