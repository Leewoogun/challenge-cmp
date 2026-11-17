package com.lwg.challenge.ui.components.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.preview.ThemePreviews
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.challenge.ChallengeCardInfo
import com.lwg.challenge.domain.model.challenge.ChallengeOutcome
import com.lwg.challenge.ui.components.card.component.ChallengeCardHeader
import com.lwg.challenge.ui.components.card.component.ChallengeCardMissionRow
import com.lwg.challenge.ui.components.card.component.ChallengeCardMyMissionRow

@Composable
fun ChallengeCardContainer(
    cardInfo: ChallengeCardInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ChallengeCardScope.() -> Unit,
) {
    val scope = rememberChallengeCardScope(cardInfo)

    ChallengeCardContainer(onClick = onClick, modifier = modifier) {
        scope.content()
    }
}

/** [ChallengeCardInfo] 로 담을 수 없는 카드(미션이 아직 없는 도전장 등)가 행을 직접 조합할 때 쓴다. */
@Composable
fun ChallengeCardContainer(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
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
            content = content,
        )
    }
}

/** 행을 골라 조합할 수 있다 — 여기선 헤더와 내 미션만. */
@ThemePreviews
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

/** cardInfo 없이 행을 직접 넘긴다 — 여기선 상태 라벨 헤더와 미정 미션. */
@ThemePreviews
@Composable
private fun ChallengeCardContainerWithoutCardInfoPreview() {
    ChallengeTheme {
        Column(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(16.dp),
        ) {
            ChallengeCardContainer(onClick = {}) {
                ChallengeCardHeader(leadingText = "받은 도전장", outcome = null, deadlineTimeText = "2일 남음")
                ChallengeCardMissionRow(
                    label = "나의 미션",
                    mission = null,
                    isMine = true,
                    verificationStatus = null,
                    emptyMissionText = "수락할 때 정해요",
                )
            }
        }
    }
}
