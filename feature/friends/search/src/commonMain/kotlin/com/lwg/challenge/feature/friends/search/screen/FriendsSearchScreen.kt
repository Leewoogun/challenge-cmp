package com.lwg.challenge.feature.friends.search.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.friend.Relation
import com.lwg.challenge.domain.model.friend.UserSearchResult
import com.lwg.challenge.feature.friends.search.component.FriendSearchItem
import com.lwg.challenge.feature.friends.search.contract.FriendsSearchUiState
import org.jetbrains.compose.ui.tooling.preview.Preview

/** 친구 검색 결과 화면 — relation 별 액션 버튼을 가진 결과 리스트. */
@Composable
internal fun FriendsSearchScreen(
    uiState: FriendsSearchUiState.Result,
    onClickAction: (UserSearchResult) -> Unit,
    onClickCancel: (UserSearchResult) -> Unit,
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
                enabled = uiState.isActionEnabled(item),
                onClickAction = { onClickAction(item) },
                onClickCancel = { onClickCancel(item) },
            )
        }
    }
}

@Preview
@Composable
private fun FriendsSearchScreenPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background.screen)) {
            FriendsSearchScreen(
                uiState = FriendsSearchUiState.Result(
                    items = listOf(
                        previewItem(1, "도전왕", Relation.NONE),
                        previewItem(2, "도전중", Relation.REQUEST_SENT, pendingRequestId = 100),
                        previewItem(3, "이미친구", Relation.FRIEND),
                        previewItem(4, "받은요청", Relation.REQUEST_RECEIVED, pendingRequestId = 101),
                        previewItem(5, "거절됨", Relation.REJECTED),
                    ),
                    requestingUserIds = setOf(4),
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
) = UserSearchResult(
    userId = userId,
    nickname = nickname,
    profileImageUrl = null,
    relation = relation,
    pendingRequestId = pendingRequestId,
)
