package com.lwg.challenge.feature.challenge.verify.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * CAMERA 가 DENIED_ALWAYS 일 때의 설정 이동 안내.
 * 촬영이 막히면 인증 자체가 불가능한 화면이라, 부가 기능(알림)과 달리 안내 다이얼로그를 쓴다.
 */
@Composable
internal fun CameraPermissionDialog(
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        containerColor = ChallengeTheme.colorScheme.background,
        title = {
            Text(
                text = "카메라 권한이 필요해요",
                style = ChallengeTheme.typography.bold18,
                color = ChallengeTheme.colorScheme.onBackground,
            )
        },
        text = {
            Text(
                text = "인증 사진은 실시간 촬영으로만 올릴 수 있어요. 설정에서 카메라 권한을 허용해주세요.",
                style = ChallengeTheme.typography.medium14,
                color = ChallengeTheme.colorScheme.onSurfaceVariant,
            )
        },
        confirmButton = {
            TextButton(onClick = onOpenSettings) {
                Text(
                    text = "설정으로 이동",
                    style = ChallengeTheme.typography.medium14,
                    color = ChallengeTheme.colorScheme.primary,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "닫기",
                    style = ChallengeTheme.typography.medium14,
                    color = ChallengeTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    )
}

@Preview
@Composable
private fun CameraPermissionDialogPreview() {
    ChallengeTheme {
        CameraPermissionDialog(
            onOpenSettings = {},
            onDismiss = {},
        )
    }
}
