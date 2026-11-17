package com.lwg.challenge.feature.friends.record.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.preview.ThemePreviews
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.feature.friends.record.contract.FriendRecordUiState
import com.lwg.challenge.ui.components.EmptyStateCard

@Composable
internal fun FriendRecordErrorScreen(
    uiState: FriendRecordUiState.Error,
    onRetryClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        EmptyStateCard(
            title = "전적을 불러오지 못했어요",
            subtitle = uiState.message,
            ctaLabel = "다시 시도",
            icon = Icons.Filled.CloudOff,
            onClickCta = onRetryClick,
            ctaIcon = Icons.Filled.Refresh,
        )
    }
}

@ThemePreviews
@Composable
private fun FriendRecordErrorScreenPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background.screen)) {
            FriendRecordErrorScreen(
                uiState = FriendRecordUiState.Error(message = "친구를 찾을 수 없어요"),
                onRetryClick = {},
            )
        }
    }
}
