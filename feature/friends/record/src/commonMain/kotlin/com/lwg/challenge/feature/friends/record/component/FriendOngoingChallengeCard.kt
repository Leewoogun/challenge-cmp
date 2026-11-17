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
import com.lwg.challenge.domain.model.friend.FriendOngoingChallenge
import com.lwg.challenge.feature.friends.record.PreviewInProgressChallenge
import com.lwg.challenge.feature.friends.record.PreviewReceivedChallenge
import com.lwg.challenge.feature.friends.record.PreviewSentChallenge
import com.lwg.challenge.feature.friends.record.friendRecordPreviewData
import com.lwg.challenge.ui.components.ChallengePenaltyArea
import com.lwg.challenge.ui.components.card.ChallengeCardContainer
import com.lwg.challenge.ui.components.card.component.ChallengeCardHeader
import com.lwg.challenge.ui.components.card.component.ChallengeCardMissionRow

/**
 * 진행 중 카드. 상대가 전부 같은 사람이라 `vs 닉네임` 대신 카드끼리 갈리는 상태를 첫 줄에 둔다.
 * 인증 현황은 홈과 상세가 보여 줘 pill 을 두지 않는다.
 */
@Composable
internal fun FriendOngoingChallengeCard(
    challenge: FriendOngoingChallenge,
    friendNickname: String,
    statusLabel: String,
    deadlineText: String,
    isUrgent: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChallengeCardContainer(onClick = onClick, modifier = modifier) {
        ChallengeCardHeader(
            leadingText = statusLabel,
            outcome = null,
            deadlineTimeText = deadlineText,
            isDeadlineUrgent = isUrgent,
        )
        ChallengeCardMissionRow(
            label = "나의 미션",
            mission = challenge.myMission,
            isMine = true,
            verificationStatus = null,
            emptyMissionText = "수락할 때 정해요",
        )
        ChallengeCardMissionRow(
            label = "${friendNickname}의 미션",
            mission = challenge.friendMission,
            isMine = false,
            verificationStatus = null,
            emptyMissionText = "수락하면 정해져요",
        )
        ChallengePenaltyArea(text = "내기: ${challenge.bet}")
    }
}

@ThemePreviews
@Composable
private fun FriendOngoingChallengeCardInProgressPreview() {
    FriendOngoingChallengeCardPreview(PreviewInProgressChallenge)
}

/** 받은 도전장 — 내 미션이 아직 없고 마감이 1시간 안이라 남은 시간이 error 색이다. */
@ThemePreviews
@Composable
private fun FriendOngoingChallengeCardReceivedPreview() {
    FriendOngoingChallengeCardPreview(PreviewReceivedChallenge)
}

/** 보낸 도전장 — 친구 미션이 아직 없다. */
@ThemePreviews
@Composable
private fun FriendOngoingChallengeCardSentPreview() {
    FriendOngoingChallengeCardPreview(PreviewSentChallenge)
}

@Composable
private fun FriendOngoingChallengeCardPreview(challenge: FriendOngoingChallenge) {
    val data = friendRecordPreviewData()
    ChallengeTheme {
        Column(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FriendOngoingChallengeCard(
                challenge = challenge,
                friendNickname = data.friend.nickname,
                statusLabel = data.statusLabelOf(challenge),
                deadlineText = data.deadlineTextOf(challenge),
                isUrgent = data.isUrgent(challenge),
                onClick = {},
            )
        }
    }
}
