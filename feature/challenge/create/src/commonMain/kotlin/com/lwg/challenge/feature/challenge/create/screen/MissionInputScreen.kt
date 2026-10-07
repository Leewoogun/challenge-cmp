package com.lwg.challenge.feature.challenge.create.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.ChallengeScaffold
import com.lwg.challenge.designsystem.components.VerticalSpacer
import com.lwg.challenge.ui.components.LabeledInputField
import com.lwg.challenge.ui.components.LoadingIconButton
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.challenge.DeadlineType
import com.lwg.challenge.feature.challenge.create.component.StepHeader
import com.lwg.challenge.feature.challenge.create.component.DeadlineSelector
import com.lwg.challenge.feature.challenge.create.contract.ChallengeCreateUiState
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun MissionInputScreen(
    uiState: ChallengeCreateUiState.MissionInput,
    onMyMissionChange: (String) -> Unit,
    onPenaltyChange: (String) -> Unit,
    onDeadlineChange: (DeadlineType) -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
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
            LabeledInputField(
                label = "나의 미션",
                value = uiState.myMission,
                onValueChange = onMyMissionChange,
                placeholder = "예) 오늘 운동 1시간 하기",
                counterText = uiState.myMissionCounterText,
                counterAtMax = uiState.isMyMissionAtMax,
                imeAction = ImeAction.Next,
            )

            LabeledInputField(
                label = "내기 (진 사람이 해야 할 것)",
                value = uiState.penalty,
                onValueChange = onPenaltyChange,
                placeholder = "예) 커피 사기",
                counterText = uiState.penaltyCounterText,
                counterAtMax = uiState.isPenaltyAtMax,
                imeAction = ImeAction.Done,
            )

            DeadlineSelector(
                value = uiState.deadlineType,
                onValueChange = onDeadlineChange,
                todayHintText = uiState.todayDeadlineHint,
                tomorrowHintText = uiState.tomorrowDeadlineHint,
            )

            LoadingIconButton(
                text = "다음",
                icon = Icons.Filled.SportsKabaddi,
                enabled = uiState.canProceedToOath,
                loading = false,
                onClick = onNext,
            )
        }
    }
}

@Preview
@Composable
private fun MissionInputScreenEmptyPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            MissionInputScreen(
                uiState = previewState(),
                onMyMissionChange = {},
                onPenaltyChange = {},
                onDeadlineChange = {},
                onNext = {},
            )
        }
    }
}

@Preview
@Composable
private fun MissionInputScreenFilledPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            MissionInputScreen(
                uiState = previewState(
                    myMission = "오늘 운동 1시간 하기",
                    penalty = "커피 사기",
                    deadlineType = DeadlineType.TOMORROW,
                ),
                onMyMissionChange = {},
                onPenaltyChange = {},
                onDeadlineChange = {},
                onNext = {},
            )
        }
    }
}

private fun previewState(
    myMission: String = "",
    penalty: String = "",
    deadlineType: DeadlineType = DeadlineType.TODAY,
) = ChallengeCreateUiState.MissionInput(
    myMission = myMission,
    penalty = penalty,
    deadlineType = deadlineType,
    todayDeadlineHint = "7/28 24:00",
    tomorrowDeadlineHint = "7/29 24:00",
)
