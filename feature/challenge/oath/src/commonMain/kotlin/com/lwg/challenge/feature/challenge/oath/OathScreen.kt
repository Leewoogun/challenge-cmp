package com.lwg.challenge.feature.challenge.oath

import androidx.compose.runtime.Composable
import com.lwg.challenge.designsystem.components.ChallengeScaffold
import com.lwg.challenge.designsystem.components.ChallengeTopBar
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.signature.Signature
import com.lwg.challenge.domain.model.signature.SignaturePoint
import com.lwg.challenge.domain.model.signature.SignatureStroke
import com.lwg.challenge.feature.challenge.oath.component.ExitConfirmDialog
import com.lwg.challenge.feature.challenge.oath.component.OathError
import com.lwg.challenge.feature.challenge.oath.component.OathForm
import com.lwg.challenge.feature.challenge.oath.component.OathLoading
import com.lwg.challenge.feature.challenge.oath.contract.OathUiState
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun OathScreen(
    uiState: OathUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onMyMissionChange: (String) -> Unit,
    onSignatureChange: (Signature) -> Unit,
    onSubmit: () -> Unit,
    onConfirmExit: () -> Unit,
    onDismissExitConfirm: () -> Unit,
) {
    ChallengeScaffold(
        statusBarColor = ChallengeTheme.colorScheme.surface,
        topBar = {
            ChallengeTopBar(
                title = "영혼의 맹세",
                onBack = onBack,
            )
        },
    ) {
        when (uiState) {
            OathUiState.Loading -> OathLoading()

            is OathUiState.Error -> OathError(
                message = uiState.message,
                onRetry = onRetry,
            )

            is OathUiState.Data -> {
                OathForm(
                    uiState = uiState,
                    onMyMissionChange = onMyMissionChange,
                    onSignatureChange = onSignatureChange,
                    onSubmit = onSubmit,
                )

                if (uiState.showExitConfirm) {
                    ExitConfirmDialog(
                        onConfirmExit = onConfirmExit,
                        onDismiss = onDismissExitConfirm,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun OathScreenLoadingPreview() {
    ChallengeTheme {
        PreviewOathScreen(uiState = OathUiState.Loading)
    }
}

@Preview
@Composable
private fun OathScreenErrorPreview() {
    ChallengeTheme {
        PreviewOathScreen(uiState = OathUiState.Error("네트워크 오류가 발생하였습니다."))
    }
}

@Preview
@Composable
private fun OathScreenEmptyPreview() {
    ChallengeTheme {
        PreviewOathScreen(uiState = previewOathData())
    }
}

@Preview
@Composable
private fun OathScreenFilledPreview() {
    ChallengeTheme {
        PreviewOathScreen(
            uiState = previewOathData(
                myMission = "책 30페이지 읽기",
                signature = previewSignature(),
            ),
        )
    }
}

@Preview
@Composable
private fun OathScreenSubmittingPreview() {
    ChallengeTheme {
        PreviewOathScreen(
            uiState = previewOathData(
                myMission = "책 30페이지 읽기",
                signature = previewSignature(),
                isSubmitting = true,
            ),
        )
    }
}

/** 챌린저가 아직 서명하지 않았거나 서명을 읽지 못한 계약 — 서명 블록만 빠지고 맹세는 그대로 가능하다. */
@Preview
@Composable
private fun OathScreenNoChallengerSignaturePreview() {
    ChallengeTheme {
        PreviewOathScreen(uiState = previewOathData(challengerSignature = null))
    }
}

@Preview
@Composable
private fun OathScreenExitConfirmPreview() {
    ChallengeTheme {
        PreviewOathScreen(
            uiState = previewOathData(
                myMission = "책 30페이지 읽기",
                signature = previewSignature(),
                showExitConfirm = true,
            ),
        )
    }
}

@Composable
private fun PreviewOathScreen(uiState: OathUiState) {
    OathScreen(
        uiState = uiState,
        onBack = {},
        onRetry = {},
        onMyMissionChange = {},
        onSignatureChange = {},
        onSubmit = {},
        onConfirmExit = {},
        onDismissExitConfirm = {},
    )
}

private fun previewSignature() = Signature(
    strokes = listOf(
        SignatureStroke(
            listOf(
                SignaturePoint(120, 620),
                SignaturePoint(300, 360),
                SignaturePoint(480, 640),
                SignaturePoint(700, 400),
            ),
        ),
    ),
)

private fun previewOathData(
    myMission: String = "",
    signature: Signature = Signature.Empty,
    challengerSignature: Signature? = previewSignature(),
    isSubmitting: Boolean = false,
    showExitConfirm: Boolean = false,
) = OathUiState.Data(
    challengeId = 1L,
    challengerNickname = "우건",
    challengerMission = "오늘 운동 1시간 하기",
    challengerSignature = challengerSignature,
    betContent = "커피 사기",
    deadlineText = "5시간 32분",
    myMission = myMission,
    signature = signature,
    isSubmitting = isSubmitting,
    showExitConfirm = showExitConfirm,
)
