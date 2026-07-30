package com.lwg.challenge.feature.challenge.create.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.VerticalSpacer
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.challenge.DeadlineType
import com.lwg.challenge.feature.challenge.create.contract.ChallengeCreateStep
import com.lwg.challenge.feature.challenge.create.contract.ChallengeCreateUiState
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * step1 — 미션 · 내기 · 마감 입력. design.md §1.3.
 *
 * 키보드가 올라오면 CTA 가 가려지므로 세로 스크롤 + imePadding 을 건다.
 */
@Composable
internal fun MissionInputStep(
    uiState: ChallengeCreateUiState.Data,
    onMyMissionChange: (String) -> Unit,
    onBetContentChange: (String) -> Unit,
    onDeadlineChange: (DeadlineType) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 20.dp),
    ) {
        StepHeader(
            title = "미션을 입력해 ✍️",
            subtitle = "오늘 반드시 해낼 것을 적어",
        )

        VerticalSpacer(24.dp)

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            MissionInputField(
                label = "나의 미션",
                value = uiState.myMission,
                onValueChange = onMyMissionChange,
                placeholder = "예) 오늘 운동 1시간 하기",
                counterText = uiState.myMissionCounterText,
                counterAtMax = uiState.isMyMissionAtMax,
                imeAction = ImeAction.Next,
                enabled = !uiState.isSubmitting,
            )

            MissionInputField(
                label = "내기 (진 사람이 해야 할 것)",
                value = uiState.betContent,
                onValueChange = onBetContentChange,
                placeholder = "예) 커피 사기",
                counterText = uiState.betContentCounterText,
                counterAtMax = uiState.isBetContentAtMax,
                imeAction = ImeAction.Done,
                enabled = !uiState.isSubmitting,
            )

            DeadlineSelector(
                value = uiState.deadlineType,
                onValueChange = onDeadlineChange,
                todayHintText = uiState.todayDeadlineHint,
                tomorrowHintText = uiState.tomorrowDeadlineHint,
                enabled = !uiState.isSubmitting,
            )

            SubmitButton(
                text = "챌린지 걸기",
                enabled = uiState.canSubmit,
                loading = uiState.isSubmitting,
                onClick = onSubmit,
            )
        }
    }
}

@Preview
@Composable
private fun MissionInputStepEmptyPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            MissionInputStep(
                uiState = previewState(),
                onMyMissionChange = {},
                onBetContentChange = {},
                onDeadlineChange = {},
                onSubmit = {},
            )
        }
    }
}

@Preview
@Composable
private fun MissionInputStepFilledPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            MissionInputStep(
                uiState = previewState(
                    myMission = "오늘 운동 1시간 하기",
                    betContent = "커피 사기",
                    deadlineType = DeadlineType.TOMORROW,
                ),
                onMyMissionChange = {},
                onBetContentChange = {},
                onDeadlineChange = {},
                onSubmit = {},
            )
        }
    }
}

@Preview
@Composable
private fun MissionInputStepSubmittingPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            MissionInputStep(
                uiState = previewState(
                    myMission = "오늘 운동 1시간 하기",
                    betContent = "커피 사기",
                    isSubmitting = true,
                ),
                onMyMissionChange = {},
                onBetContentChange = {},
                onDeadlineChange = {},
                onSubmit = {},
            )
        }
    }
}

private fun previewState(
    myMission: String = "",
    betContent: String = "",
    deadlineType: DeadlineType = DeadlineType.TODAY,
    isSubmitting: Boolean = false,
) = ChallengeCreateUiState.Data(
    step = ChallengeCreateStep.MISSION_INPUT,
    friends = emptyList(),
    selectedFriendId = 1L,
    myMission = myMission,
    betContent = betContent,
    deadlineType = deadlineType,
    todayDeadlineHint = "7/28 24:00",
    tomorrowDeadlineHint = "7/29 24:00",
    isSubmitting = isSubmitting,
    loadFailed = false,
)
