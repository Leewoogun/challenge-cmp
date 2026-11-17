package com.lwg.challenge.feature.friends.record.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.VerticalSpacer
import com.lwg.challenge.designsystem.preview.ThemePreviews
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.friend.FriendScore
import com.lwg.challenge.feature.friends.record.PreviewInProgressChallenge
import com.lwg.challenge.feature.friends.record.friendRecordPreviewData
import com.lwg.challenge.feature.friends.record.component.FriendOngoingChallengeCard
import com.lwg.challenge.feature.friends.record.component.FriendPastChallengeCard
import com.lwg.challenge.feature.friends.record.component.FriendPastChallengesEmptyText
import com.lwg.challenge.feature.friends.record.component.FriendRecordNextPageIndicator
import com.lwg.challenge.feature.friends.record.component.FriendRecordScoreBoard
import com.lwg.challenge.feature.friends.record.component.FriendRecordScoreCard
import com.lwg.challenge.feature.friends.record.component.FriendRecordSectionHeader
import com.lwg.challenge.feature.friends.record.contract.FriendRecordUiState
import com.lwg.challenge.navigation.Route

// 카드 간격 12 에 더해 섹션 간격 20 을 만든다.
private val SectionTopPadding = 8.dp

@Composable
internal fun FriendRecordScreen(
    uiState: FriendRecordUiState.Data,
    listState: LazyListState,
    onChallengeClick: (Route) -> Unit,
    onSendChallengeClick: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "hero") {
            FriendRecordScoreCard(friend = uiState.friend) {
                VerticalSpacer(16.dp)
                FriendRecordScoreBoard(
                    myWinsText = uiState.myWinsText,
                    friendWinsText = uiState.friendWinsText,
                    friendNickname = uiState.friend.nickname,
                    isMuted = uiState.isScoreMuted,
                    subLineText = uiState.scoreSubLineText,
                    contentDescription = uiState.scoreContentDescription,
                    onSendChallengeClick = onSendChallengeClick,
                )
            }
        }

        if (uiState.hasOngoing) {
            item(key = "ongoing-header") {
                FriendRecordSectionHeader(
                    title = "진행 중",
                    countText = uiState.ongoingCountText,
                    modifier = Modifier.padding(top = SectionTopPadding),
                )
            }
            items(
                items = uiState.record.ongoing,
                key = { challenge -> "ongoing-${challenge.challengeId}" },
            ) { challenge ->
                FriendOngoingChallengeCard(
                    challenge = challenge,
                    friendNickname = uiState.friend.nickname,
                    statusLabel = uiState.statusLabelOf(challenge),
                    deadlineText = uiState.deadlineTextOf(challenge),
                    isUrgent = uiState.isUrgent(challenge),
                    onClick = { onChallengeClick(uiState.routeOf(challenge)) },
                )
            }
        }

        item(key = "past-header") {
            FriendRecordSectionHeader(
                title = "지난 챌린지",
                modifier = Modifier.padding(top = SectionTopPadding),
            )
        }

        if (uiState.isPastEmpty) {
            item(key = "past-empty") { FriendPastChallengesEmptyText() }
        } else {
            items(
                items = uiState.pastChallenges,
                key = { challenge -> "past-${challenge.challengeId}" },
            ) { challenge ->
                FriendPastChallengeCard(
                    challenge = challenge,
                    friendNickname = uiState.friend.nickname,
                    dateText = uiState.dateTextOf(challenge),
                    onClick = { onChallengeClick(uiState.routeOf(challenge)) },
                )
            }
        }

        if (uiState.hasNextPage) {
            item(key = "next-page") { FriendRecordNextPageIndicator() }
        }
    }
}

@ThemePreviews
@Composable
private fun FriendRecordScreenPreview() {
    FriendRecordScreenPreviewContainer(friendRecordPreviewData())
}

@ThemePreviews
@Composable
private fun FriendRecordScreenNextPageLoadingPreview() {
    FriendRecordScreenPreviewContainer(friendRecordPreviewData(ongoing = emptyList(), isPageEnd = false))
}

@ThemePreviews
@Composable
private fun FriendRecordScreenNoCompletedPreview() {
    FriendRecordScreenPreviewContainer(
        friendRecordPreviewData(
            score = FriendScore(myWins = 0, friendWins = 0, draws = 0, bothLose = 0),
            ongoing = listOf(PreviewInProgressChallenge),
            pastChallenges = emptyList(),
        ),
    )
}

@Composable
private fun FriendRecordScreenPreviewContainer(uiState: FriendRecordUiState.Data) {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background.screen)) {
            FriendRecordScreen(
                uiState = uiState,
                listState = rememberLazyListState(),
                onChallengeClick = {},
                onSendChallengeClick = {},
            )
        }
    }
}
