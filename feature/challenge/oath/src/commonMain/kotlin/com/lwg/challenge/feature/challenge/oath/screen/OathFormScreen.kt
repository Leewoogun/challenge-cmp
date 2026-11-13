package com.lwg.challenge.feature.challenge.oath.screen

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.ChallengeScaffold
import com.lwg.challenge.designsystem.components.VerticalSpacer
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.challenge.ChallengeDetail
import com.lwg.challenge.domain.model.challenge.Contract
import com.lwg.challenge.domain.model.challenge.ContractUserInfo
import com.lwg.challenge.domain.model.signature.Signature
import com.lwg.challenge.domain.model.signature.SignaturePoint
import com.lwg.challenge.domain.model.signature.SignatureStroke
import com.lwg.challenge.feature.challenge.oath.contract.OathUiState
import com.lwg.challenge.ui.components.ContractCard
import com.lwg.challenge.ui.components.LabeledInputField
import com.lwg.challenge.ui.components.LoadingIconButton
import com.lwg.challenge.ui.components.signature.SignatureField
import com.lwg.challenge.ui.components.signature.SignatureSlot
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 계약서 확인 → 미션 입력 → 서명을 한 스크롤에 두고 제출 버튼만 하단에 고정한다.
 * 서명란만 고정하면 미션 입력이 숨어 버튼이 왜 비활성인지 알 수 없다.
 * [ChallengeScaffold] 가 이미 IME 를 반영하므로 `imePadding()` 은 걸지 않는다.
 */
@Composable
internal fun OathFormScreen(
    uiState: OathUiState.Data,
    onMyMissionChange: (String) -> Unit,
    onSignatureChange: (Signature) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isSigning by remember { mutableStateOf(false) }
    val opponentSignature = uiState.detail.contract?.opponentSignature
    val opponentSignatureFooter: (@Composable ColumnScope.() -> Unit)? =
        if (opponentSignature == null) {
            null
        } else {
            {
                Text(
                    text = uiState.opponentSignatureLabel,
                    style = ChallengeTheme.typography.medium12,
                    color = ChallengeTheme.colorScheme.text.secondary,
                )

                VerticalSpacer(6.dp)

                // 상세 화면의 서명 칸과 같은 껍데기를 쓴다 — 왕복해도 같은 서명이 같아 보여야 한다.
                SignatureSlot(
                    signature = opponentSignature,
                    modifier = Modifier.fillMaxWidth(0.5f),
                )
            }
        }

    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                // 획을 긋는 동안 스크롤을 잠근다 — 캔버스 드래그가 스크롤로 새면 획이 끊긴다.
                .verticalScroll(rememberScrollState(), enabled = !isSigning)
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp),
        ) {
            Text(
                text = "영혼의 맹세 📜",
                style = ChallengeTheme.typography.bold20,
                color = ChallengeTheme.colorScheme.text.primary,
            )

            VerticalSpacer(4.dp)

            Text(
                text = "서명하면 수정 불가! 진짜야?",
                style = ChallengeTheme.typography.medium14,
                color = ChallengeTheme.colorScheme.text.secondary,
            )

            VerticalSpacer(24.dp)

            // 입력 중인 미션을 실시간으로 되비춰 서명할 문서가 눈앞에서 완성되게 한다.
            ContractCard(
                opponentNickname = uiState.detail.opponent.nickname,
                myMission = uiState.myMission,
                penalty = uiState.detail.betContent,
                deadlineText = uiState.deadlineText,
                opponentMission = uiState.detail.opponent.mission.orEmpty(),
                opponentMissionLabel = uiState.opponentMissionLabel,
                footer = opponentSignatureFooter,
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

            VerticalSpacer(20.dp)

            Text(
                text = "서명란",
                style = ChallengeTheme.typography.medium12,
                color = ChallengeTheme.colorScheme.text.secondary,
            )

            VerticalSpacer(6.dp)

            SignatureField(
                signature = uiState.signature,
                onSignatureChange = onSignatureChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSubmitting,
                onDrawingChange = { isSigning = it },
            )

            VerticalSpacer(16.dp)
        }

        LoadingIconButton(
            text = "맹세한다! 🔥",
            icon = Icons.Filled.Check,
            enabled = uiState.canSubmit,
            loading = uiState.isSubmitting,
            onClick = onSubmit,
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp),
        )
    }
}

@Preview
@Composable
private fun OathFormScreenEmptyPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background.screen)) {
            OathFormScreen(
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
private fun OathFormScreenFilledPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background.screen)) {
            OathFormScreen(
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
private fun OathFormScreenSubmittingPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background.screen)) {
            OathFormScreen(
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
private fun OathFormScreenNoOpponentSignaturePreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background.screen)) {
            OathFormScreen(
                uiState = previewOathData(opponentSignature = null),
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

private val PREVIEW_NOW = LocalDateTime(2026, 7, 28, 18, 28, 0)

private fun previewOathData(
    myMission: String = "",
    signature: Signature = Signature.Empty,
    opponentSignature: Signature? = previewSignature(),
    isSubmitting: Boolean = false,
) = OathUiState.Data(
    detail = ChallengeDetail(
        challengeId = 1L,
        status = "PENDING",
        myResult = null,
        forfeitedBy = null,
        challengeDate = LocalDate(2026, 7, 28),
        deadline = LocalDateTime(2026, 7, 29, 0, 0, 0),
        betContent = "커피 사기",
        me = ContractUserInfo(nickname = "나", profileImageUrl = null, mission = null),
        opponent = ContractUserInfo(nickname = "우건", profileImageUrl = null, mission = "오늘 운동 1시간 하기"),
        contract = Contract(
            content = "영혼의 맹세",
            isFinalized = false,
            mySignature = null,
            mySignedAt = null,
            opponentSignature = opponentSignature,
            opponentSignedAt = PREVIEW_NOW.takeIf { opponentSignature != null },
        ),
    ),
    now = PREVIEW_NOW,
    myMission = myMission,
    signature = signature,
    isSubmitting = isSubmitting,
)
