package com.lwg.challenge.feature.challenge.create.component

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
import com.lwg.challenge.domain.model.challenge.DeadlineType
import com.lwg.challenge.domain.model.friend.Friend
import com.lwg.challenge.domain.model.signature.Signature
import com.lwg.challenge.domain.model.signature.SignaturePoint
import com.lwg.challenge.domain.model.signature.SignatureStroke
import com.lwg.challenge.feature.challenge.create.contract.ChallengeCreateStep
import com.lwg.challenge.feature.challenge.create.contract.ChallengeCreateUiState
import com.lwg.challenge.ui.components.ContractCard
import com.lwg.challenge.ui.components.LoadingIconButton
import com.lwg.challenge.ui.components.signature.SignatureField
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * step2 — 계약서 확인 후 서명.
 *
 * **서명 캔버스는 스크롤 영역 밖에 둔다.** 캔버스 위 드래그가 "획 긋기"인지 "부모 스크롤"인지
 * 모호해져 획이 끊기기 때문이다. 그래서 계약서만 스크롤시키고 서명란·CTA 는 고정 슬롯에 남긴다.
 */
@Composable
internal fun OathStep(
    uiState: ChallengeCreateUiState.Data,
    onSignatureChange: (Signature) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
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

            // footer 는 넘기지 않는다 — 이 시점엔 상대가 서명할 수 없어 빈 칸만 생긴다.
            ContractCard(
                opponentNickname = uiState.opponentNicknameText,
                myMission = uiState.myMission,
                betContent = uiState.betContent,
                deadlineText = uiState.deadlineHintText,
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
private fun OathStepEmptySignaturePreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            OathStep(
                uiState = previewOathState(),
                onSignatureChange = {},
                onSubmit = {},
            )
        }
    }
}

@Preview
@Composable
private fun OathStepSignedPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            OathStep(
                uiState = previewOathState(signature = previewSignature()),
                onSignatureChange = {},
                onSubmit = {},
            )
        }
    }
}

@Preview
@Composable
private fun OathStepSubmittingPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            OathStep(
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
) = ChallengeCreateUiState.Data(
    step = ChallengeCreateStep.OATH,
    friends = listOf(
        Friend(
            userId = 1L,
            nickname = "우건",
            profileImageUrl = null,
            since = LocalDateTime.parse("2026-01-01T00:00:00"),
        ),
    ),
    selectedFriendId = 1L,
    myMission = "오늘 운동 1시간 하기",
    betContent = "커피 사기",
    deadlineType = DeadlineType.TODAY,
    todayDeadlineHint = "7/28 24:00",
    tomorrowDeadlineHint = "7/29 24:00",
    signature = signature,
    isSubmitting = isSubmitting,
    loadFailed = false,
)
