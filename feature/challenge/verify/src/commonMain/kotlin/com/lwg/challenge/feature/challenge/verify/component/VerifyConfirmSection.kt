package com.lwg.challenge.feature.challenge.verify.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lwg.challenge.camera.CapturedPhoto
import com.lwg.challenge.designsystem.components.VerticalSpacer
import com.lwg.challenge.designsystem.components.button.NormalButton
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.feature.challenge.verify.contract.SubmitFailure
import com.lwg.challenge.feature.challenge.verify.contract.VerifyUiState
import org.jetbrains.compose.ui.tooling.preview.Preview

/** 미리보기 + 제출 확인. 제출은 되돌릴 수 없어 이 단계에서 재촬영 기회를 보장한다. */
@Composable
internal fun VerifyConfirmSection(
    uiState: VerifyUiState.Confirm,
    onRetakeClick: () -> Unit,
    onSubmitClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "이 사진으로 인증할까요?",
            style = ChallengeTheme.typography.bold20,
            color = ChallengeTheme.colorScheme.onBackground,
        )

        VerticalSpacer(8.dp)

        Text(
            text = "제출을 확정하면 사진을 바꿀 수 없어요.",
            style = ChallengeTheme.typography.medium14,
            color = ChallengeTheme.colorScheme.onSurfaceVariant,
        )

        VerticalSpacer(20.dp)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            CapturedPhotoImage(
                photo = uiState.photo,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(uiState.photoAspectRatio),
            )

            if (uiState.isSubmitting) {
                CircularProgressIndicator(color = ChallengeTheme.colorScheme.primary)
            }
        }

        if (uiState.hasFailure) {
            VerticalSpacer(12.dp)

            Text(
                text = uiState.failureMessage,
                style = ChallengeTheme.typography.medium14,
                color = ChallengeTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
        }

        VerticalSpacer(20.dp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            NormalButton(
                text = "다시 촬영",
                onClick = onRetakeClick,
                modifier = Modifier.weight(1f),
                enabled = uiState.canRetake,
                textStyle = ChallengeTheme.typography.bold16,
            )

            NormalButton(
                text = uiState.submitButtonText,
                onClick = onSubmitClick,
                modifier = Modifier.weight(1f),
                enabled = uiState.canSubmit,
                containerColor = ChallengeTheme.colorScheme.primary,
                contentColor = ChallengeTheme.colorScheme.onPrimary,
                textStyle = ChallengeTheme.typography.bold16,
            )
        }
    }
}

private fun previewConfirm(
    isSubmitting: Boolean = false,
    failure: SubmitFailure? = null,
) = VerifyUiState.Confirm(
    photo = CapturedPhoto(jpegBytes = ByteArray(0), width = 3, height = 4),
    isSubmitting = isSubmitting,
    failure = failure,
)

@Preview
@Composable
private fun VerifyConfirmSectionPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            VerifyConfirmSection(
                uiState = previewConfirm(),
                onRetakeClick = {},
                onSubmitClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun VerifyConfirmSectionSubmittingPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            VerifyConfirmSection(
                uiState = previewConfirm(isSubmitting = true),
                onRetakeClick = {},
                onSubmitClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun VerifyConfirmSectionFailurePreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            VerifyConfirmSection(
                uiState = previewConfirm(
                    failure = SubmitFailure(
                        message = "업로드 가능 시간이 지났어요. 다시 시도해주세요",
                        isRetryable = true,
                    ),
                ),
                onRetakeClick = {},
                onSubmitClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun VerifyConfirmSectionNotRetryablePreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            VerifyConfirmSection(
                uiState = previewConfirm(
                    failure = SubmitFailure(
                        message = "사진 용량이 커서 올릴 수 없어요. 다시 촬영해주세요",
                        isRetryable = false,
                    ),
                ),
                onRetakeClick = {},
                onSubmitClick = {},
            )
        }
    }
}
