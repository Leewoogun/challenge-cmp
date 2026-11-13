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

/**
 * 확인/취소 2버튼 다이얼로그. design.md §3.3.2.
 *
 * @param destructive 되돌릴 수 없는 행위인지. 색을 개별 파라미터로 열지 않고 이 플래그 하나로 묶는다 —
 *   열어 두면 호출부마다 다른 색 조합을 넘길 수 있게 되어 위계 규칙이 컴포넌트 밖으로 새어 나간다.
 */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    dismissLabel: String = "취소",
    destructive: Boolean = false,
) {
    // 파괴적일 때 확인=error / 취소=primary 로 뒤집는다. PermissionSettingsDialog 와 반대인 것이 의도다 —
    // 안전한 쪽이 먼저 눈에 들어와야 하는데 Material3 AlertDialog 는 confirmButton 이 오른쪽(강조 위치)에
    // 고정돼 있어 자리를 바꿀 수 없다. 그래서 색으로 뒤집는다.
    val confirmColor = if (destructive) {
        ChallengeTheme.colorScheme.error
    } else {
        ChallengeTheme.colorScheme.point
    }
    val dismissColor = if (destructive) {
        ChallengeTheme.colorScheme.point
    } else {
        ChallengeTheme.colorScheme.text.secondary
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        containerColor = ChallengeTheme.colorScheme.background.screen,
        title = {
            Text(
                text = title,
                style = ChallengeTheme.typography.bold18,
                color = ChallengeTheme.colorScheme.text.primary,
            )
        },
        text = {
            Text(
                text = message,
                style = ChallengeTheme.typography.medium14,
                color = ChallengeTheme.colorScheme.text.secondary,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = confirmLabel,
                    style = ChallengeTheme.typography.medium14,
                    color = confirmColor,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = dismissLabel,
                    style = ChallengeTheme.typography.medium14,
                    color = dismissColor,
                )
            }
        },
    )
}

@Preview
@Composable
private fun ConfirmDialogDestructivePreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(24.dp),
        ) {
            ConfirmDialog(
                title = "정말 탈퇴하시겠어요?",
                message = "계정 정보와 인증 사진이 삭제되고 되돌릴 수 없어요.\n\n" +
                    "지난 챌린지의 계약서와 결과는 상대방의 기록이라 남아요 — " +
                    "상대 화면에서 나는 '탈퇴한 사용자' 로 보여요.",
                confirmLabel = "탈퇴하기",
                onConfirm = {},
                onDismiss = {},
                destructive = true,
            )
        }
    }
}

@Preview
@Composable
private fun ConfirmDialogPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(24.dp),
        ) {
            ConfirmDialog(
                title = "챌린지를 취소할까요?",
                message = "상대가 아직 수락하지 않은 도전장이에요.",
                confirmLabel = "취소하기",
                onConfirm = {},
                onDismiss = {},
            )
        }
    }
}
