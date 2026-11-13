package com.lwg.challenge.feature.friends.list.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.ui.components.EmptyStateCard
import org.jetbrains.compose.ui.tooling.preview.Preview

/** 친구 탭 빈 상태 화면 — [EmptyStateCard] 단일 CTA 만 노출한다. */
@Composable
internal fun FriendsEmptyScreen(
    onClickAddFriend: () -> Unit,
    onClickInvite: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp),
    ) {
        EmptyStateCard(
            title = "아직 친구가 없어요",
            subtitle = "친구를 추가하고 함께 챌린지를 시작해보세요",
            ctaLabel = "친구 추가하기",
            icon = Icons.Filled.Group,
            onClickCta = onClickAddFriend,
            onClickInvite = onClickInvite,
        )
    }
}

@Preview
@Composable
private fun FriendsEmptyScreenPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background.screen)) {
            FriendsEmptyScreen(
                onClickAddFriend = {},
                onClickInvite = {},
            )
        }
    }
}
