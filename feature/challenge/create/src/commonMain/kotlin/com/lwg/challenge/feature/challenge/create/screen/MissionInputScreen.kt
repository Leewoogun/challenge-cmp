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
import com.lwg.challenge.designsystem.components.ChallengeToolTip
import com.lwg.challenge.designsystem.components.VerticalSpacer
import com.lwg.challenge.designsystem.preview.ThemePreviews
import com.lwg.challenge.ui.components.LabeledInputField
import com.lwg.challenge.ui.components.LoadingIconButton
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.feature.challenge.create.component.StepHeader
import com.lwg.challenge.feature.challenge.create.component.DeadlineSelector
import com.lwg.challenge.feature.challenge.create.contract.ChallengeCreateUiState
import com.lwg.challenge.feature.challenge.create.contract.selectableDatesFrom
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

@Composable
internal fun MissionInputScreen(
    uiState: ChallengeCreateUiState.MissionInput,
    onMyMissionChange: (String) -> Unit,
    onPenaltyChange: (String) -> Unit,
    onChallengeDateChange: (LocalDate) -> Unit,
    onOpenDeadlineCalendar: () -> Unit,
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
            subtitle = "마감까지 반드시 해낼 것을 적어",
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
                labelEndContent = {
                    ChallengeToolTip(
                        title = "사진으로 인증할 수 있는 미션을 적어줘",
                        text = "금연·금주처럼 안 하는 것을 지키는 미션은 사진으로 증명할 수 없어",
                    )
                },
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
                selected = uiState.selectedDeadlineOption,
                today = uiState.today,
                tomorrow = uiState.tomorrow,
                todayHintText = uiState.todayHintText,
                tomorrowHintText = uiState.tomorrowHintText,
                customHintText = uiState.customHintText,
                onDateSelect = onChallengeDateChange,
                onCustomClick = onOpenDeadlineCalendar,
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

@ThemePreviews
@Composable
private fun MissionInputScreenEmptyPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background.screen)) {
            MissionInputScreen(
                uiState = previewState(),
                onMyMissionChange = {},
                onPenaltyChange = {},
                onChallengeDateChange = {},
                onOpenDeadlineCalendar = {},
                onNext = {},
            )
        }
    }
}

@ThemePreviews
@Composable
private fun MissionInputScreenFilledPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background.screen)) {
            MissionInputScreen(
                uiState = previewState(
                    myMission = "오늘 운동 1시간 하기",
                    penalty = "커피 사기",
                    challengeDate = PREVIEW_TODAY.plus(1, DateTimeUnit.DAY),
                ),
                onMyMissionChange = {},
                onPenaltyChange = {},
                onChallengeDateChange = {},
                onOpenDeadlineCalendar = {},
                onNext = {},
            )
        }
    }
}

private fun previewState(
    myMission: String = "",
    penalty: String = "",
    challengeDate: LocalDate = PREVIEW_TODAY,
) = ChallengeCreateUiState.MissionInput(
    myMission = myMission,
    penalty = penalty,
    challengeDate = challengeDate,
    today = PREVIEW_TODAY,
    selectableDates = selectableDatesFrom(PREVIEW_TODAY),
)

private val PREVIEW_TODAY = LocalDate(2026, 11, 16)
