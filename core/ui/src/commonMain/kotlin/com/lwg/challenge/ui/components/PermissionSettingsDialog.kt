package com.lwg.challenge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/** 권한이 영구 거부(`DENIED_ALWAYS`)됐을 때만 띄우는 설정 안내 다이얼로그. */
@Composable
fun PermissionSettingsDialog(
    title: String,
    message: String,
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
                text = title,
                style = ChallengeTheme.typography.bold18,
                color = ChallengeTheme.colorScheme.onBackground,
            )
        },
        text = {
            Text(
                text = message,
                style = ChallengeTheme.typography.medium14,
                color = ChallengeTheme.colorScheme.onSurfaceVariant,
            )
        },
        confirmButton = {
            TextButton(onClick = onOpenSettings) {
                Text(
                    text = "설정 열기",
                    style = ChallengeTheme.typography.medium14,
                    color = ChallengeTheme.colorScheme.primary,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "나중에",
                    style = ChallengeTheme.typography.medium14,
                    color = ChallengeTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    )
}

@Preview
@Composable
private fun PermissionSettingsDialogPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            PermissionSettingsDialog(
                title = "알림이 꺼져 있어요",
                message = "도전장이 도착해도 알려드릴 수 없어요. 설정에서 알림을 켤 수 있어요.",
                onOpenSettings = {},
                onDismiss = {},
            )
        }
    }
}
