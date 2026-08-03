package com.lwg.challenge.feature.home.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.lwg.challenge.designsystem.components.IconTextButton
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.ui.components.LabeledInputField
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 도전장 수락 시 나의 미션을 입력받는 다이얼로그. design.md §3.2 / §3.3 (v2 개정 — 바텀시트 아님).
 *
 * 입력 상태는 ViewModel 이 소유한다(hoisting) — 다이얼로그 내부 `remember` 로 두면 회전·프로세스 사망 시
 * 유실되고 제출 검증을 ViewModel 이 못 한다.
 *
 * @param isSubmitting 제출 in-flight. true 면 입력이 `readOnly` 가 되고 배경 탭 / 시스템 백 dismiss 를 막는다.
 */
@Composable
internal fun AcceptChallengeDialog(
    missionLabelText: String,
    challengerMission: String,
    betContent: String,
    deadlineText: String,
    myMission: String,
    onMyMissionChange: (String) -> Unit,
    counterText: String,
    counterAtMax: Boolean,
    canSubmit: Boolean,
    isSubmitting: Boolean,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = !isSubmitting,
            dismissOnClickOutside = !isSubmitting,
            usePlatformDefaultWidth = false,
        ),
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .imePadding(),
            shape = RoundedCornerShape(20.dp),
            color = ChallengeTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "받아친다 🔥",
                        style = ChallengeTheme.typography.bold18,
                        color = ChallengeTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = "수락하면 바로 시작돼. 네가 해낼 미션을 적어.",
                        style = ChallengeTheme.typography.medium14,
                        color = ChallengeTheme.colorScheme.onSurfaceVariant,
                    )
                }

                SummaryCard(
                    missionLabelText = missionLabelText,
                    challengerMission = challengerMission,
                    betContent = betContent,
                    deadlineText = deadlineText,
                )

                LabeledInputField(
                    label = "나의 미션",
                    value = myMission,
                    onValueChange = onMyMissionChange,
                    placeholder = "예) 책 30페이지 읽기",
                    counterText = counterText,
                    counterAtMax = counterAtMax,
                    imeAction = ImeAction.Done,
                    focusRequester = focusRequester,
                    readOnly = isSubmitting,
                )

                AcceptCta(
                    enabled = canSubmit,
                    loading = isSubmitting,
                    onClick = onSubmit,
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(
    missionLabelText: String,
    challengerMission: String,
    betContent: String,
    deadlineText: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ChallengeTheme.colorScheme.surface,
        border = BorderStroke(width = 1.dp, color = ChallengeTheme.colorScheme.outline),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SummaryRow(label = missionLabelText, value = challengerMission)
            SummaryRow(
                label = "내기",
                value = betContent,
                valueColor = ChallengeTheme.colorScheme.primary,
            )
            SummaryRow(label = "마감", value = deadlineText)
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    valueColor: Color = ChallengeTheme.colorScheme.onBackground,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            style = ChallengeTheme.typography.medium14,
            color = ChallengeTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = ChallengeTheme.typography.bold14,
            color = valueColor,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
        )
    }
}

/**
 * [IconTextButton] 에는 content 슬롯이 없어 인디케이터를 넣을 수 없다. 그래서 [loading] 일 때만
 * 같은 시각 스펙(52.dp / R12 / primary)의 [Button] 으로 대체 렌더한다 (create 모듈 `SubmitButton` 동일 방식).
 */
@Composable
private fun AcceptCta(
    enabled: Boolean,
    loading: Boolean,
    onClick: () -> Unit,
) {
    if (loading) {
        Button(
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = false,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                disabledContainerColor = ChallengeTheme.colorScheme.primary,
                disabledContentColor = ChallengeTheme.colorScheme.onPrimary,
            ),
            contentPadding = PaddingValues(horizontal = 12.dp),
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = ChallengeTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp,
            )
        }
    } else {
        IconTextButton(
            text = "수락하고 시작",
            icon = Icons.Filled.Check,
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            textStyle = ChallengeTheme.typography.bold14,
        )
    }
}

@Preview
@Composable
private fun AcceptChallengeDialogEmptyPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            AcceptChallengeDialog(
                missionLabelText = "도윤의 미션",
                challengerMission = "아침 6시 기상하기",
                betContent = "치킨 사기 🍗",
                deadlineText = "5시간 32분",
                myMission = "",
                onMyMissionChange = {},
                counterText = "0/100",
                counterAtMax = false,
                canSubmit = false,
                isSubmitting = false,
                onSubmit = {},
                onDismiss = {},
            )
        }
    }
}

@Preview
@Composable
private fun AcceptChallengeDialogFilledPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            AcceptChallengeDialog(
                missionLabelText = "도윤의 미션",
                challengerMission = "아침 6시 기상하기",
                betContent = "치킨 사기 🍗",
                deadlineText = "42분",
                myMission = "책 30페이지 읽기",
                onMyMissionChange = {},
                counterText = "10/100",
                counterAtMax = false,
                canSubmit = true,
                isSubmitting = false,
                onSubmit = {},
                onDismiss = {},
            )
        }
    }
}

@Preview
@Composable
private fun AcceptChallengeDialogAtMaxPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            AcceptChallengeDialog(
                missionLabelText = "도윤의 미션",
                challengerMission = "아침 6시 기상하기",
                betContent = "치킨 사기 🍗",
                deadlineText = "42분",
                myMission = "가".repeat(100),
                onMyMissionChange = {},
                counterText = "100/100",
                counterAtMax = true,
                canSubmit = true,
                isSubmitting = false,
                onSubmit = {},
                onDismiss = {},
            )
        }
    }
}

@Preview
@Composable
private fun AcceptChallengeDialogSubmittingPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            AcceptChallengeDialog(
                missionLabelText = "도윤의 미션",
                challengerMission = "아침 6시 기상하기",
                betContent = "치킨 사기 🍗",
                deadlineText = "42분",
                myMission = "책 30페이지 읽기",
                onMyMissionChange = {},
                counterText = "10/100",
                counterAtMax = false,
                canSubmit = false,
                isSubmitting = true,
                onSubmit = {},
                onDismiss = {},
            )
        }
    }
}
