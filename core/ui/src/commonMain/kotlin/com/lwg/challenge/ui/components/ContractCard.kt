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

/** [myMission] 이 없을 때 값 자리에 채우는 대체 문구. */
private const val EMPTY_VALUE_TEXT = "—"

/**
 * 챌린지 계약 내용을 보여주는 "영혼의 맹세" 카드.
 *
 * 위저드 맹세 step · 상대 맹세 화면 · 챌린지 상세 세 곳이 같은 계약서를 보여줘야 해서
 * `:core:ui` 에 둔다. 서명란은 화면마다 다르므로 [footer] 슬롯으로 위임한다.
 *
 * @param myMission 아직 미션이 정해지지 않았으면 `null`/공백 — 값 자리에 [EMPTY_VALUE_TEXT] 를 채운다.
 * @param opponentMission 상대가 이미 건 미션. 생성 위저드처럼 아직 상대 미션이 없는 화면은 `null` 로 두면
 *   행 자체가 빠진다 — 빈 칸을 남기면 "상대가 아직 안 정했다" 가 아니라 "정보가 없다" 로 읽힌다.
 * @param opponentMissionLabel [opponentMission] 행의 라벨. 닉네임을 붙일지는 화면이 정한다.
 * @param myMissionLabel 관점(누가 '나'인지)을 모를 때 상세 화면이 실제 닉네임으로 대체한다.
 *   라벨만 "나" 라고 말하고 실제로는 상대 데이터면 계약서를 잘못 읽게 된다.
 */
@Composable
fun ContractCard(
    opponentNickname: String,
    myMission: String?,
    betContent: String,
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
        color = ChallengeTheme.colorScheme.surface,
        border = BorderStroke(width = 1.dp, color = ChallengeTheme.colorScheme.outline),
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
                        ChallengeTheme.colorScheme.onSurfaceVariant
                    } else {
                        ChallengeTheme.colorScheme.onBackground
                    },
                )
                ContractRow(
                    label = "내기",
                    value = betContent,
                    valueColor = ChallengeTheme.colorScheme.primary,
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
            color = ChallengeTheme.colorScheme.primary,
        )

        VerticalSpacer(4.dp)

        Text(
            text = "SOUL CONTRACT",
            style = ChallengeTheme.typography.medium10,
            color = ChallengeTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ContractDivider() {
    HorizontalDivider(
        thickness = 1.dp,
        color = ChallengeTheme.colorScheme.outline,
    )
}

@Composable
private fun ContractRow(
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

@Preview
@Composable
private fun ContractCardPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            ContractCard(
                opponentNickname = "우건",
                myMission = "오늘 운동 1시간 하기",
                betContent = "커피 사기",
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
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            ContractCard(
                opponentNickname = "우건",
                myMission = null,
                betContent = "커피 사기",
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
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            ContractCard(
                opponentNickname = "우건",
                myMission = null,
                betContent = "커피 사기",
                deadlineText = "5시간 32분",
                opponentMission = "오늘 운동 1시간 하기",
                opponentMissionLabel = "우건의 미션",
            )
        }
    }
}

/** 관점 미상 — 두 미션 행 모두 "나" 없이 실명 라벨로 나온다. */
@Preview
@Composable
private fun ContractCardCustomMyMissionLabelPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            ContractCard(
                opponentNickname = "지민",
                myMission = "오늘 운동 1시간 하기",
                betContent = "커피 사기",
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
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            ContractCard(
                opponentNickname = "우건",
                myMission = "오늘 운동 1시간 하기",
                betContent = "커피 사기",
                deadlineText = "7/28 24:00",
                footer = {
                    Text(
                        text = "서명 완료",
                        style = ChallengeTheme.typography.medium12,
                        color = ChallengeTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )
        }
    }
}
