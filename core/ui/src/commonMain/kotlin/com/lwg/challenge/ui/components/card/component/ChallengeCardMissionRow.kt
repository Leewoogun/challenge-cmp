package com.lwg.challenge.ui.components.card.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.VerificationStatus
import com.lwg.challenge.ui.components.VerificationStatusPill
import com.lwg.challenge.ui.components.card.ChallengeCardScope
import org.jetbrains.compose.ui.tooling.preview.Preview

/** 미션 한 줄. 내 미션은 굵고 진하게, 상대 미션은 옅게 둬 누구 몫인지 바로 갈린다. */
@Composable
fun ChallengeCardMissionRow(
    label: String,
    mission: String,
    isMine: Boolean,
    verificationStatus: VerificationStatus?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = label,
                style = ChallengeTheme.typography.medium12,
                color = ChallengeTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = mission,
                style = if (isMine) ChallengeTheme.typography.bold14 else ChallengeTheme.typography.medium14,
                color = if (isMine) ChallengeTheme.colorScheme.onBackground else ChallengeTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (verificationStatus != null) {
            VerificationStatusPill(status = verificationStatus)
        }
    }
}

@Composable
internal fun ChallengeCardScope.ChallengeCardMyMissionRow(modifier: Modifier = Modifier) {
    ChallengeCardMissionRow(
        label = "나의 미션",
        mission = cardInfo.myMission,
        isMine = true,
        verificationStatus = cardInfo.myVerification,
        modifier = modifier,
    )
}

@Composable
internal fun ChallengeCardScope.ChallengeCardOpponentMissionRow(modifier: Modifier = Modifier) {
    ChallengeCardMissionRow(
        label = "${cardInfo.opponentNickname}의 미션",
        mission = cardInfo.opponentMission,
        isMine = false,
        verificationStatus = cardInfo.opponentVerification,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun ChallengeCardMissionRowPreview() {
    ChallengeTheme {
        Column(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.surface)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ChallengeCardMissionRow(
                label = "나의 미션",
                mission = "새벽 다섯 시에 일어나서 헬스장 가서 유산소 30분 웨이트 60분 하기",
                isMine = true,
                verificationStatus = VerificationStatus.VERIFIED,
            )
            ChallengeCardMissionRow(
                label = "우건의 미션",
                mission = "책 30페이지 읽기",
                isMine = false,
                verificationStatus = VerificationStatus.FAILED,
            )
            ChallengeCardMissionRow(label = "나의 미션", mission = "헬스장 가기", isMine = true, verificationStatus = null)
            ChallengeCardMissionRow(label = "준혁의 미션", mission = "금주", isMine = false, verificationStatus = null)
        }
    }
}
