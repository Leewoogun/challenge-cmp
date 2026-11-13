package com.lwg.challenge.feature.challenge.verify.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/** CAMERA 가 DENIED_ALWAYS 일 때의 설정 이동 안내. 촬영이 막히면 인증이 불가능해 다이얼로그로 알린다. */
@Composable
internal fun CameraPermissionDialog(
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        containerColor = ChallengeTheme.colorScheme.background.screen,
        title = {
            Text(
                text = "카메라 권한이 필요해요",
                style = ChallengeTheme.typography.bold18,
                color = ChallengeTheme.colorScheme.text.primary,
            )
        },
        text = {
            Text(
                text = "인증 사진은 실시간 촬영으로만 올릴 수 있어요. 설정에서 카메라 권한을 허용해주세요.",
                style = ChallengeTheme.typography.medium14,
                color = ChallengeTheme.colorScheme.text.secondary,
            )
        },
        confirmButton = {
            TextButton(onClick = onOpenSettings) {
                Text(
                    text = "설정으로 이동",
                    style = ChallengeTheme.typography.medium14,
                    color = ChallengeTheme.colorScheme.text.point,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "닫기",
                    style = ChallengeTheme.typography.medium14,
                    color = ChallengeTheme.colorScheme.text.secondary,
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
