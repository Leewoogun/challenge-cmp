package com.lwg.challenge.feature.challenge.create.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.VerticalSpacer
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.friend.Friend
import com.lwg.challenge.domain.model.signature.Signature
import com.lwg.challenge.domain.model.signature.SignaturePoint
import com.lwg.challenge.domain.model.signature.SignatureStroke
import com.lwg.challenge.feature.challenge.create.component.ChallengeCreateLoading
import com.lwg.challenge.feature.challenge.create.component.StepHeader
import com.lwg.challenge.feature.challenge.create.contract.ChallengeCreateUiState
import com.lwg.challenge.ui.components.ContractCard
import com.lwg.challenge.ui.components.LoadingIconButton
import com.lwg.challenge.ui.components.signature.SignatureField
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 계약서 확인 후 서명.
 */
@Composable
internal fun OathScreen(
    uiState: ChallengeCreateUiState.Oath,
    onSignatureChange: (Signature) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        OathForm(
            uiState = uiState,
            onSignatureChange = onSignatureChange,
            onSubmit = onSubmit,
        )

        if (uiState.isSubmitting) {
            ChallengeCreateLoading(overlay = true)
        }
    }
}

@Composable
private fun OathForm(
    uiState: ChallengeCreateUiState.Oath,
    onSignatureChange: (Signature) -> Unit,
    onSubmit: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp),
        ) {
            StepHeader(
                title = "영혼의 맹세 📜",
                subtitle = "서명하면 수정 불가! 진짜야?",
            )

            VerticalSpacer(24.dp)

            ContractCard(
                opponentNickname = uiState.opponent.nickname,
                myMission = uiState.myMission,
                penalty = uiState.penalty,
                deadlineText = uiState.deadlineText,
            )
        }

        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 20.dp),
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
            )

            VerticalSpacer(16.dp)

            LoadingIconButton(
                text = "맹세한다! 🔥",
                icon = Icons.Filled.Check,
                enabled = uiState.canSubmit,
                loading = false,
                onClick = onSubmit,
            )
        }
    }
}

@Preview
@Composable
private fun OathScreenEmptySignaturePreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            OathScreen(
                uiState = previewOathState(),
                onSignatureChange = {},
                onSubmit = {},
            )
        }
    }
}

@Preview
@Composable
private fun OathScreenSignedPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            OathScreen(
                uiState = previewOathState(signature = previewSignature()),
                onSignatureChange = {},
                onSubmit = {},
            )
        }
    }
}

@Preview
@Composable
private fun OathScreenSubmittingPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            OathScreen(
                uiState = previewOathState(
                    signature = previewSignature(),
                    isSubmitting = true,
                ),
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

private fun previewOathState(
    signature: Signature = Signature.Empty,
    isSubmitting: Boolean = false,
) = ChallengeCreateUiState.Oath(
    opponent = Friend(
        userId = 1L,
        nickname = "우건",
        profileImageUrl = null,
        since = LocalDateTime.parse("2026-01-01T00:00:00"),
    ),
    myMission = "오늘 운동 1시간 하기",
    penalty = "커피 사기",
    deadlineText = "7/28 24:00",
    signature = signature,
    isSubmitting = isSubmitting,
)
