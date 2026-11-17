package com.lwg.challenge.feature.friends.record.component

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
import com.lwg.challenge.designsystem.preview.ThemePreviews
import com.lwg.challenge.designsystem.theme.ChallengeTheme

/** @param countText null 이면 건수를 쓰지 않는다 — 지난 목록은 쪽 단위로 받아 전체 건수를 모른다. */
@Composable
internal fun FriendRecordSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    countText: String? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = ChallengeTheme.typography.bold16,
            color = ChallengeTheme.colorScheme.text.primary,
        )
        if (countText != null) {
            Text(
                text = countText,
                style = ChallengeTheme.typography.medium12,
                color = ChallengeTheme.colorScheme.text.secondary,
            )
        }
    }
}

@ThemePreviews
@Composable
private fun FriendRecordSectionHeaderPreview() {
    ChallengeTheme {
        Column(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            FriendRecordSectionHeader(title = "진행 중", countText = "3개")
            FriendRecordSectionHeader(title = "지난 챌린지")
        }
    }
}
