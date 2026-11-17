package com.lwg.challenge.feature.friends.record.screen

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
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.preview.ThemePreviews
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.feature.friends.record.PreviewFriend
import com.lwg.challenge.feature.friends.record.component.FriendRecordScoreCard
import com.lwg.challenge.feature.friends.record.contract.FriendRecordUiState
import com.lwg.challenge.ui.components.EmptyStateCard

@Composable
internal fun FriendRecordEmptyScreen(
    uiState: FriendRecordUiState.Empty,
    onSendChallengeClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        FriendRecordScoreCard(friend = uiState.friend)

        EmptyStateCard(
            title = "아직 붙어본 적 없어요",
            subtitle = uiState.subtitleText,
            ctaLabel = "도전장 보내기",
            icon = Icons.Filled.SportsKabaddi,
            onClickCta = onSendChallengeClick,
            ctaIcon = Icons.Filled.SportsKabaddi,
        )
    }
}

@ThemePreviews
@Composable
private fun FriendRecordEmptyScreenPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background.screen)) {
            FriendRecordEmptyScreen(
                uiState = FriendRecordUiState.Empty(friend = PreviewFriend),
                onSendChallengeClick = {},
            )
        }
    }
}
