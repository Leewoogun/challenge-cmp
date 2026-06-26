package com.lwg.challenge.feature.friends

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lwg.challenge.designsystem.components.ChallengeScaffold
import com.lwg.challenge.designsystem.components.friend.FriendsEmptyState
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.feature.friends.component.FriendsTopBar
import com.lwg.challenge.feature.friends.contract.FriendsUiState
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 친구 탭 화면 (1차 1단계 호환 — Task 6 에서 풀 UI 로 확장 예정).
 *
 * [uiState] 는 받은 요청 / 친구 목록을 갖지만, 현재 화면은 EmptyState 만 노출한다.
 * 풀 UI (받은 요청 카드 + 친구 목록) 는 후속 task.
 */
@Composable
internal fun FriendsScreen(
    uiState: FriendsUiState,
    onClickAddFriend: () -> Unit,
) {
    ChallengeScaffold(
        statusBarColor = ChallengeTheme.colorScheme.surface,
        topBar = { FriendsTopBar() },
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            FriendsEmptyState(
                title = "아직 친구가 없어요",
                subtitle = "친구를 추가하고 함께 챌린지를 시작해보세요",
                ctaLabel = "친구 추가하기",
                icon = Icons.Filled.Group,
                onClickCta = onClickAddFriend,
            )
        }
    }
}

@Preview
@Composable
private fun FriendsScreenPreview() {
    ChallengeTheme {
        FriendsScreen(
            uiState = FriendsUiState(
                receivedRequests = emptyList(),
                friends = emptyList(),
            ),
            onClickAddFriend = {},
        )
    }
}
