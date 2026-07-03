package com.lwg.challenge.feature.friends.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.friend.Relation
import com.lwg.challenge.domain.model.friend.UserSearchResult
import com.lwg.challenge.feature.friends.search.component.FriendSearchItem
import com.lwg.challenge.feature.friends.search.contract.FriendSearchItemState
import com.lwg.challenge.feature.friends.search.contract.FriendsSearchUiState
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 친구 검색 결과 화면. design.md §B — relation 별 액션 버튼을 가진 결과 리스트.
 */
@Composable
internal fun FriendsSearchScreen(
    uiState: FriendsSearchUiState.Result,
    onClickAction: (FriendSearchItemState) -> Unit,
    onClickCancel: (FriendSearchItemState) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(uiState.items, key = { it.userId }) { item ->
            FriendSearchItem(
                profileImageUrl = item.profileImageUrl,
                nickname = item.nickname,
                relation = item.relation,
                onClickAction = { onClickAction(item) },
                onClickCancel = if (item.showCancel) ({ onClickCancel(item) }) else null,
            )
        }
    }
}

@Composable
internal fun FriendsSearchLoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.padding(top = 64.dp),
            color = ChallengeTheme.colorScheme.primary,
        )
    }
}

/**
 * 검색 안내 화면 — Idle(입력 유도) / Empty(결과 없음) 문구 노출.
 */
@Composable
internal fun FriendsSearchHintScreen(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Text(
            text = text,
            style = ChallengeTheme.typography.medium14,
            color = ChallengeTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 64.dp, start = 20.dp, end = 20.dp),
        )
    }
}

@Preview
@Composable
private fun FriendsSearchHintScreenIdlePreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            FriendsSearchHintScreen("닉네임을 2자 이상 입력해주세요")
        }
    }
}

@Preview
@Composable
private fun FriendsSearchHintScreenEmptyPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            FriendsSearchHintScreen("검색 결과가 없어요. 닉네임을 더 정확히 입력해보세요.")
        }
    }
}

@Preview
@Composable
private fun FriendsSearchLoadingScreenPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            FriendsSearchLoadingScreen()
        }
    }
}

@Preview
@Composable
private fun FriendsSearchScreenResultPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            FriendsSearchScreen(
                uiState = FriendsSearchUiState.Result(
                    items = listOf(
                        previewItem(1, "도전왕", Relation.NONE),
                        previewItem(2, "도전중", Relation.REQUEST_SENT, pendingRequestId = 100),
                        previewItem(3, "이미친구", Relation.FRIEND),
                        previewItem(4, "받은요청", Relation.REQUEST_RECEIVED, pendingRequestId = 101),
                        previewItem(5, "거절됨", Relation.REJECTED),
                    ),
                ),
                onClickAction = {},
                onClickCancel = {},
            )
        }
    }
}

private fun previewItem(
    userId: Long,
    nickname: String,
    relation: Relation,
    pendingRequestId: Long? = null,
): FriendSearchItemState = FriendSearchItemState(
    UserSearchResult(
        userId = userId,
        nickname = nickname,
        profileImageUrl = null,
        relation = relation,
        pendingRequestId = pendingRequestId,
    ),
)
