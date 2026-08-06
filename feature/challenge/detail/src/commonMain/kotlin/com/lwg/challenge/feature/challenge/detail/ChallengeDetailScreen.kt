package com.lwg.challenge.feature.challenge.detail

import androidx.compose.runtime.Composable
import com.lwg.challenge.designsystem.components.ChallengeScaffold
import com.lwg.challenge.designsystem.components.ChallengeTopBar
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.signature.Signature
import com.lwg.challenge.domain.model.signature.SignaturePoint
import com.lwg.challenge.domain.model.signature.SignatureStroke
import com.lwg.challenge.feature.challenge.detail.component.ChallengeDetailContract
import com.lwg.challenge.feature.challenge.detail.component.ChallengeDetailError
import com.lwg.challenge.feature.challenge.detail.component.ChallengeDetailLoading
import com.lwg.challenge.feature.challenge.detail.contract.ChallengeDetailUiState
import com.lwg.challenge.feature.challenge.detail.contract.SignatureSection
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun ChallengeDetailScreen(
    uiState: ChallengeDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
) {
    ChallengeScaffold(
        statusBarColor = ChallengeTheme.colorScheme.surface,
        topBar = {
            ChallengeTopBar(
                title = "챌린지 상세",
                onBack = onBack,
            )
        },
    ) {
        when (uiState) {
            ChallengeDetailUiState.Loading -> ChallengeDetailLoading()

            is ChallengeDetailUiState.Error -> ChallengeDetailError(
                message = uiState.message,
                onRetry = onRetry,
            )

            is ChallengeDetailUiState.Data -> ChallengeDetailContract(uiState = uiState)
        }
    }
}

@Preview
@Composable
private fun ChallengeDetailScreenLoadingPreview() {
    ChallengeTheme {
        PreviewChallengeDetailScreen(uiState = ChallengeDetailUiState.Loading)
    }
}

@Preview
@Composable
private fun ChallengeDetailScreenErrorPreview() {
    ChallengeTheme {
        PreviewChallengeDetailScreen(
            uiState = ChallengeDetailUiState.Error("네트워크 오류가 발생하였습니다."),
        )
    }
}

@Preview
@Composable
private fun ChallengeDetailScreenBothSignedPreview() {
    ChallengeTheme {
        PreviewChallengeDetailScreen(uiState = previewData())
    }
}

@Preview
@Composable
private fun ChallengeDetailScreenOpponentNotSignedPreview() {
    ChallengeTheme {
        PreviewChallengeDetailScreen(
            uiState = previewData(
                opponentSignature = null,
                opponentSignatureHint = "서명 대기 중",
                isFinalized = false,
            ),
        )
    }
}

/** 상대가 서명은 했는데 그 서명을 읽지 못한 계약 — '대기 중' 으로 위장하지 않는다. */
@Preview
@Composable
private fun ChallengeDetailScreenUnreadableSignaturePreview() {
    ChallengeTheme {
        PreviewChallengeDetailScreen(
            uiState = previewData(
                opponentSignature = null,
                opponentSignatureHint = "서명을 불러오지 못했어요",
            ),
        )
    }
}

/** 계약서 없음(soul-oath 이전 생성) — 에러가 아니라 서명 블록만 빠진 정상 화면이다. */
@Preview
@Composable
private fun ChallengeDetailScreenNoContractPreview() {
    ChallengeTheme {
        PreviewChallengeDetailScreen(uiState = previewData(signatures = null))
    }
}

@Composable
private fun PreviewChallengeDetailScreen(uiState: ChallengeDetailUiState) {
    ChallengeDetailScreen(
        uiState = uiState,
        onBack = {},
        onRetry = {},
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

private fun previewData(
    opponentSignature: Signature? = previewSignature(),
    opponentSignatureHint: String = "서명 대기 중",
    isFinalized: Boolean = true,
    signatures: SignatureSection? = SignatureSection(
        mySignature = previewSignature(),
        mySignatureHint = "서명 대기 중",
        opponentSignature = opponentSignature,
        opponentSignatureHint = opponentSignatureHint,
        isFinalized = isFinalized,
    ),
) = ChallengeDetailUiState.Data(
    myLabel = "나",
    myMissionLabel = "나의 미션",
    myMission = "책 30페이지 읽기",
    opponentNickname = "우건",
    opponentMission = "오늘 운동 1시간 하기",
    betContent = "커피 사기",
    deadlineText = "5시간 32분",
    signatures = signatures,
)
