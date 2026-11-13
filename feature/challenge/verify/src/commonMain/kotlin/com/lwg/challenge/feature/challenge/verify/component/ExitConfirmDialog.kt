package com.lwg.challenge.feature.challenge.verify.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/** 촬영한 사진이 있을 때의 이탈 확인 — 사진이 없으면 묻지 않고 바로 나간다. */
@Composable
internal fun ExitConfirmDialog(
    onConfirmExit: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        containerColor = ChallengeTheme.colorScheme.background.screen,
        title = {
            Text(
                text = "인증을 그만둘까요?",
                style = ChallengeTheme.typography.bold18,
                color = ChallengeTheme.colorScheme.text.primary,
            )
        },
        text = {
            Text(
                text = "지금 나가면 촬영한 사진이 사라져요.",
                style = ChallengeTheme.typography.medium14,
                color = ChallengeTheme.colorScheme.text.secondary,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirmExit) {
                Text(
                    text = "나가기",
                    style = ChallengeTheme.typography.medium14,
                    color = ChallengeTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "계속하기",
                    style = ChallengeTheme.typography.medium14,
                    color = ChallengeTheme.colorScheme.text.point,
                )
            }
        },
    )
}

@Preview
@Composable
private fun ExitConfirmDialogPreview() {
    ChallengeTheme {
        ExitConfirmDialog(
            onConfirmExit = {},
            onDismiss = {},
        )
    }
}
