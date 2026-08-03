package com.lwg.challenge.feature.challenge.oath.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.ChallengeScaffold
import com.lwg.challenge.designsystem.components.VerticalSpacer
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.signature.Signature
import com.lwg.challenge.domain.model.signature.SignaturePoint
import com.lwg.challenge.domain.model.signature.SignatureStroke
import com.lwg.challenge.feature.challenge.oath.contract.OathUiState
import com.lwg.challenge.ui.components.ContractCard
import com.lwg.challenge.ui.components.LabeledInputField
import com.lwg.challenge.ui.components.LoadingIconButton
import com.lwg.challenge.ui.components.signature.SignatureField
import com.lwg.challenge.ui.components.signature.SignatureSlot
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 계약서 확인 → 미션 입력 → 서명 → 제출.
 *
 * **서명 캔버스는 스크롤 영역 밖 고정 슬롯에 둔다.** 캔버스 위 드래그가 "획 긋기"인지 "부모 스크롤"인지
 * 모호해져 획이 끊기기 때문이다. 이 화면이 다이얼로그가 아니라 전체 화면인 이유가 바로 이 배치를
 * 가능하게 하려는 것이므로, 캔버스를 위쪽 스크롤 Column 으로 옮기지 말 것.
 *
 * `imePadding()` 은 걸지 않는다 — [ChallengeScaffold] 의 contentWindowInsets(safeDrawing)가
 * 이미 IME 를 포함해 본문 높이를 줄여주므로, 여기서 또 걸면 키보드 높이만큼 두 번 밀린다.
 */
@Composable
internal fun OathForm(
    uiState: OathUiState.Data,
    onMyMissionChange: (String) -> Unit,
    onSignatureChange: (Signature) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val challengerSignature = uiState.challengerSignature
    val challengerSignatureFooter: (@Composable ColumnScope.() -> Unit)? =
        if (challengerSignature == null) {
            null
        } else {
            {
                Text(
                    text = uiState.challengerSignatureLabel,
                    style = ChallengeTheme.typography.medium12,
                    color = ChallengeTheme.colorScheme.onSurfaceVariant,
                )

                VerticalSpacer(6.dp)

                // 상세 화면의 서명 칸과 같은 껍데기를 쓴다 — 왕복해도 같은 서명이 같아 보여야 한다.
                SignatureSlot(
                    signature = challengerSignature,
                    modifier = Modifier.fillMaxWidth(0.5f),
                )
            }
        }

    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp),
        ) {
            Text(
                text = "영혼의 맹세 📜",
                style = ChallengeTheme.typography.bold20,
                color = ChallengeTheme.colorScheme.onBackground,
            )

            VerticalSpacer(4.dp)

            Text(
                text = "서명하면 수정 불가! 진짜야?",
                style = ChallengeTheme.typography.medium14,
                color = ChallengeTheme.colorScheme.onSurfaceVariant,
            )

            VerticalSpacer(24.dp)

            // myMission 은 아래 입력값을 실시간으로 되비춘다 — 내가 서명할 문서가 눈앞에서 완성되게 한다.
            ContractCard(
                opponentNickname = uiState.challengerNickname,
                myMission = uiState.myMission,
                betContent = uiState.betContent,
                deadlineText = uiState.deadlineText,
                opponentMission = uiState.challengerMission,
                opponentMissionLabel = uiState.challengerMissionLabel,
                footer = challengerSignatureFooter,
            )

            VerticalSpacer(16.dp)

            LabeledInputField(
                label = "나의 미션",
                value = uiState.myMission,
                onValueChange = onMyMissionChange,
                placeholder = "예) 책 30페이지 읽기",
                counterText = uiState.myMissionCounterText,
                counterAtMax = uiState.isMyMissionAtMax,
                imeAction = ImeAction.Done,
                readOnly = uiState.isSubmitting,
            )

            VerticalSpacer(16.dp)
        }

        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp),
        ) {
            Text(
                text = "서명란",
                style = ChallengeTheme.typography.medium12,
                color = ChallengeTheme.colorScheme.onSurfaceVariant,
            )

            VerticalSpacer(6.dp)

            SignatureField(
                signature = uiState.signature,
                onSignatureChange = onSignatureChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSubmitting,
            )

            VerticalSpacer(16.dp)

            LoadingIconButton(
                text = "맹세한다! 🔥",
                icon = Icons.Filled.Check,
                enabled = uiState.canSubmit,
                loading = uiState.isSubmitting,
                onClick = onSubmit,
            )
        }
    }
}

@Preview
@Composable
private fun OathFormEmptyPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            OathForm(
                uiState = previewOathData(),
                onMyMissionChange = {},
                onSignatureChange = {},
                onSubmit = {},
            )
        }
    }
}

@Preview
@Composable
private fun OathFormFilledPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            OathForm(
                uiState = previewOathData(
                    myMission = "책 30페이지 읽기",
                    signature = previewSignature(),
                ),
                onMyMissionChange = {},
                onSignatureChange = {},
                onSubmit = {},
            )
        }
    }
}

@Preview
@Composable
private fun OathFormSubmittingPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            OathForm(
                uiState = previewOathData(
                    myMission = "책 30페이지 읽기",
                    signature = previewSignature(),
                    isSubmitting = true,
                ),
                onMyMissionChange = {},
                onSignatureChange = {},
                onSubmit = {},
            )
        }
    }
}

@Preview
@Composable
private fun OathFormNoChallengerSignaturePreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            OathForm(
                uiState = previewOathData(challengerSignature = null),
                onMyMissionChange = {},
                onSignatureChange = {},
                onSubmit = {},
            )
        }
    }
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
    showExitConfirm = false,
)
