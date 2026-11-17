package com.lwg.challenge.feature.friends.record.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.preview.ThemePreviews
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.friend.FriendPastChallenge
import com.lwg.challenge.feature.friends.record.PreviewPastChallenges
import com.lwg.challenge.feature.friends.record.friendRecordPreviewData
import com.lwg.challenge.ui.components.ChallengePenaltyArea
import com.lwg.challenge.ui.components.card.ChallengeCardContainer
import com.lwg.challenge.ui.components.card.component.ChallengeCardHeader
import com.lwg.challenge.ui.components.card.component.ChallengeCardMissionRow

/** 지난 챌린지 카드. 보관함 카드 문법에서 `vs 닉네임` 만 빼고 날짜를 남긴다. */
@Composable
internal fun FriendPastChallengeCard(
    challenge: FriendPastChallenge,
    friendNickname: String,
    dateText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChallengeCardContainer(onClick = onClick, modifier = modifier) {
        ChallengeCardHeader(leadingText = dateText, outcome = challenge.myResult)
        ChallengeCardMissionRow(
            label = "나의 미션",
            mission = challenge.myMission,
            isMine = true,
            verificationStatus = null,
        )
        ChallengeCardMissionRow(
            label = "${friendNickname}의 미션",
            mission = challenge.friendMission,
            isMine = false,
            verificationStatus = null,
        )
        ChallengePenaltyArea(text = "내기: ${challenge.bet}")
    }
}

/** 결과 4값 — 양측 패배가 패배로 접히지 않는다. */
@ThemePreviews
@Composable
private fun FriendPastChallengeCardPreview() {
    val data = friendRecordPreviewData()
    ChallengeTheme {
        Column(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PreviewPastChallenges.take(4).forEach { challenge ->
                FriendPastChallengeCard(
                    challenge = challenge,
                    friendNickname = data.friend.nickname,
                    dateText = data.dateTextOf(challenge),
                    onClick = {},
                )
            }
        }
    }
}
