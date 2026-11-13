package com.lwg.challenge.ui.components.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.challenge.ChallengeCardInfo
import com.lwg.challenge.domain.model.challenge.ChallengeOutcome
import com.lwg.challenge.ui.components.card.component.ChallengeCardHeader
import com.lwg.challenge.ui.components.card.component.ChallengeCardMyMissionRow
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ChallengeCardContainer(
    cardInfo: ChallengeCardInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ChallengeCardScope.() -> Unit,
) {
    val scope = rememberChallengeCardScope(cardInfo)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = ChallengeTheme.colorScheme.background.card,
        border = BorderStroke(width = 1.dp, color = ChallengeTheme.colorScheme.line.border),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            scope.content()
        }
    }
}

/** 행을 골라 조합할 수 있다 — 여기선 헤더와 내 미션만. */
@Preview
@Composable
private fun ChallengeCardContainerPreview() {
    ChallengeTheme {
        Column(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(16.dp),
        ) {
            ChallengeCardContainer(
                cardInfo = ChallengeCardInfo(
                    opponentNickname = "준혁",
                    myMission = "헬스장 가기",
                    opponentMission = "금주",
                    penalty = "치킨 한 마리",
                    outcome = ChallengeOutcome.WIN,
                    challengeDate = null,
                    myVerification = null,
                    opponentVerification = null,
                ),
                onClick = {},
            ) {
                ChallengeCardHeader(deadlineTimeText = null)
                ChallengeCardMyMissionRow()
            }
        }
    }
}
