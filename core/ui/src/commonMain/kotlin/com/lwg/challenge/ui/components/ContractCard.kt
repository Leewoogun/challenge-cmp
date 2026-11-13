package com.lwg.challenge.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.VerticalSpacer
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

private const val EMPTY_VALUE_TEXT = "—"

/** 챌린지 계약 내용을 보여주는 "영혼의 맹세" 카드. 서명란은 [footer] 슬롯으로 위임한다. */
@Composable
fun ContractCard(
    opponentNickname: String,
    myMission: String?,
    penalty: String,
    deadlineText: String,
    modifier: Modifier = Modifier,
    opponentMission: String? = null,
    opponentMissionLabel: String = "상대 미션",
    myMissionLabel: String = "나의 미션",
    footer: (@Composable ColumnScope.() -> Unit)? = null,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ChallengeTheme.colorScheme.background.card,
        border = BorderStroke(width = 1.dp, color = ChallengeTheme.colorScheme.line.border),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ContractTitle()

            ContractDivider()

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ContractRow(label = "대결 상대", value = opponentNickname)
                if (opponentMission != null) {
                    ContractRow(label = opponentMissionLabel, value = opponentMission)
                }
                ContractRow(
                    label = myMissionLabel,
                    value = myMission?.takeIf { it.isNotBlank() } ?: EMPTY_VALUE_TEXT,
                    valueColor = if (myMission.isNullOrBlank()) {
                        ChallengeTheme.colorScheme.text.secondary
                    } else {
                        ChallengeTheme.colorScheme.text.primary
                    },
                )
                ContractRow(
                    label = "내기",
                    value = penalty,
                    valueColor = ChallengeTheme.colorScheme.text.point,
                )
                ContractRow(label = "마감", value = deadlineText)
            }

            if (footer != null) {
                ContractDivider()
                Column(content = footer)
            }
        }
    }
}

@Composable
private fun ContractTitle() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "영혼의 맹세",
            style = ChallengeTheme.typography.bold18,
            color = ChallengeTheme.colorScheme.text.point,
        )

        VerticalSpacer(4.dp)

        Text(
            text = "SOUL CONTRACT",
            style = ChallengeTheme.typography.medium10,
            color = ChallengeTheme.colorScheme.text.secondary,
        )
    }
}

@Composable
private fun ContractDivider() {
    HorizontalDivider(
        thickness = 1.dp,
        color = ChallengeTheme.colorScheme.line.divider,
    )
}

@Composable
private fun ContractRow(
    label: String,
    value: String,
    valueColor: Color = ChallengeTheme.colorScheme.text.primary,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            style = ChallengeTheme.typography.medium14,
            color = ChallengeTheme.colorScheme.text.secondary,
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

@Preview
@Composable
private fun ContractCardPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(24.dp),
        ) {
            ContractCard(
                opponentNickname = "우건",
                myMission = "오늘 운동 1시간 하기",
                penalty = "커피 사기",
                deadlineText = "7/28 24:00",
            )
        }
    }
}

@Preview
@Composable
private fun ContractCardMissionEmptyPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(24.dp),
        ) {
            ContractCard(
                opponentNickname = "우건",
                myMission = null,
                penalty = "커피 사기",
                deadlineText = "7/28 24:00",
            )
        }
    }
}

@Preview
@Composable
private fun ContractCardWithOpponentMissionPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(24.dp),
        ) {
            ContractCard(
                opponentNickname = "우건",
                myMission = null,
                penalty = "커피 사기",
                deadlineText = "5시간 32분",
                opponentMission = "오늘 운동 1시간 하기",
                opponentMissionLabel = "우건의 미션",
            )
        }
    }
}

@Preview
@Composable
private fun ContractCardCustomMyMissionLabelPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(24.dp),
        ) {
            ContractCard(
                opponentNickname = "지민",
                myMission = "오늘 운동 1시간 하기",
                penalty = "커피 사기",
                deadlineText = "7/28 24:00",
                opponentMission = "책 30페이지 읽기",
                opponentMissionLabel = "지민의 미션",
                myMissionLabel = "우건의 미션",
            )
        }
    }
}

@Preview
@Composable
private fun ContractCardWithFooterPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(24.dp),
        ) {
            ContractCard(
                opponentNickname = "우건",
                myMission = "오늘 운동 1시간 하기",
                penalty = "커피 사기",
                deadlineText = "7/28 24:00",
                footer = {
                    Text(
                        text = "서명 완료",
                        style = ChallengeTheme.typography.medium12,
                        color = ChallengeTheme.colorScheme.text.secondary,
                    )
                },
            )
        }
    }
}
