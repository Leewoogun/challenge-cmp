package com.lwg.challenge.feature.challenge.oath.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/** 작성 중 이탈 확인. 미션·서명이 하나라도 있을 때만 띄운다 — clean 이면 묻지 않고 바로 나간다. */
@Composable
internal fun ExitConfirmDialog(
    onConfirmExit: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        containerColor = ChallengeTheme.colorScheme.background,
        title = {
            Text(
                text = "작성 중인 내용이 사라져요",
                style = ChallengeTheme.typography.bold18,
                color = ChallengeTheme.colorScheme.onBackground,
            )
        },
        text = {
            Text(
                text = "맹세를 마치지 않고 나가면 입력한 미션과 서명이 저장되지 않아요.",
                style = ChallengeTheme.typography.medium14,
                color = ChallengeTheme.colorScheme.onSurfaceVariant,
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
                    text = "계속 작성",
                    style = ChallengeTheme.typography.medium14,
                    color = ChallengeTheme.colorScheme.primary,
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
