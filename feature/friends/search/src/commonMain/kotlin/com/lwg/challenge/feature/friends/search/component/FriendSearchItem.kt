package com.lwg.challenge.feature.friends.search.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.IconTextButton
import com.lwg.challenge.designsystem.components.IconTextButtonStyle
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.friend.Relation
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 검색 결과 1행. design.md §B.3/§B.5/§C — 프로필 + 닉네임 + relation 5종 액션.
 *
 * 이미지 로더 부재로 [profileImageUrl] 은 시그니처에만 받고 닉네임 이니셜 placeholder 로 렌더한다.
 *
 * @param onClickCancel `relation == REQUEST_SENT` 일 때만 non-null (요청 취소).
 */
@Composable
internal fun FriendSearchItem(
    profileImageUrl: String?,
    nickname: String,
    relation: Relation,
    onClickAction: () -> Unit,
    onClickCancel: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ChallengeTheme.colorScheme.surface,
        border = BorderStroke(width = 1.dp, color = ChallengeTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SearchProfilePlaceholder(nickname = nickname)

            Text(
                text = nickname,
                style = ChallengeTheme.typography.bold14,
                color = ChallengeTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )

            RelationAction(
                relation = relation,
                onClickAction = onClickAction,
                onClickCancel = onClickCancel,
            )
        }
    }
}

@Composable
private fun SearchProfilePlaceholder(
    nickname: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(ChallengeTheme.colorScheme.secondary),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = nickname.trim().firstOrNull()?.uppercase() ?: "",
            style = ChallengeTheme.typography.medium20,
            color = ChallengeTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun RelationAction(
    relation: Relation,
    onClickAction: () -> Unit,
    onClickCancel: (() -> Unit)?,
) {
    when (relation) {
        Relation.NONE -> IconTextButton(
            text = "친구 요청",
            icon = Icons.Filled.PersonAdd,
            onClick = onClickAction,
            style = IconTextButtonStyle.Filled,
            textStyle = ChallengeTheme.typography.bold14,
            shape = RoundedCornerShape(12.dp),
            height = 36.dp,
            contentPadding = PaddingValues(horizontal = 12.dp),
        )

        Relation.REQUEST_SENT -> Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            RelationBadge(text = "요청 보냄", withBorder = false)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onClickCancel?.invoke() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "요청 취소",
                    modifier = Modifier.size(14.dp),
                    tint = ChallengeTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Relation.REQUEST_RECEIVED -> IconTextButton(
            text = "수락",
            icon = Icons.Filled.Check,
            onClick = onClickAction,
            style = IconTextButtonStyle.Filled,
            textStyle = ChallengeTheme.typography.bold14,
            shape = RoundedCornerShape(12.dp),
            height = 36.dp,
            contentPadding = PaddingValues(horizontal = 12.dp),
        )

        Relation.FRIEND -> RelationBadge(
            text = "이미 친구",
            withBorder = true,
            textStyle = ChallengeTheme.typography.bold12,
        )

        Relation.REJECTED -> IconTextButton(
            text = "다시 요청",
            icon = Icons.Filled.Refresh,
            onClick = onClickAction,
            style = IconTextButtonStyle.Filled,
            containerColor = ChallengeTheme.colorScheme.secondary,
            contentColor = ChallengeTheme.colorScheme.onSurface,
            textStyle = ChallengeTheme.typography.bold14,
            shape = RoundedCornerShape(12.dp),
            height = 36.dp,
            contentPadding = PaddingValues(horizontal = 12.dp),
        )
    }
}

/**
 * 정적(비클릭) relation 뱃지. design.md §B.5 — `REQUEST_SENT`/`FRIEND` 의 disabled 시각.
 *
 * Disabled Button 의 자동 alpha(0.38) 를 회피하기 위해 클릭 없는 [Surface] 로 구현한다.
 */
@Composable
private fun RelationBadge(
    text: String,
    withBorder: Boolean,
    textStyle: androidx.compose.ui.text.TextStyle = ChallengeTheme.typography.bold14,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = ChallengeTheme.colorScheme.secondary,
        border = if (withBorder) {
            BorderStroke(width = 1.dp, color = ChallengeTheme.colorScheme.outline)
        } else {
            null
        },
    ) {
        Box(
            modifier = Modifier
                .height(36.dp)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = textStyle,
                color = ChallengeTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview
@Composable
private fun FriendSearchItemNonePreview() {
    PreviewContainer {
        FriendSearchItem(
            profileImageUrl = null,
            nickname = "우건",
            relation = Relation.NONE,
            onClickAction = {},
            onClickCancel = null,
        )
    }
}

@Preview
@Composable
private fun FriendSearchItemRequestSentPreview() {
    PreviewContainer {
        FriendSearchItem(
            profileImageUrl = null,
            nickname = "도전왕",
            relation = Relation.REQUEST_SENT,
            onClickAction = {},
            onClickCancel = {},
        )
    }
}

@Preview
@Composable
private fun FriendSearchItemRequestReceivedPreview() {
    PreviewContainer {
        FriendSearchItem(
            profileImageUrl = null,
            nickname = "도전자",
            relation = Relation.REQUEST_RECEIVED,
            onClickAction = {},
            onClickCancel = null,
        )
    }
}

@Preview
@Composable
private fun FriendSearchItemFriendPreview() {
    PreviewContainer {
        FriendSearchItem(
            profileImageUrl = null,
            nickname = "절친",
            relation = Relation.FRIEND,
            onClickAction = {},
            onClickCancel = null,
        )
    }
}

@Preview
@Composable
private fun FriendSearchItemRejectedPreview() {
    PreviewContainer {
        FriendSearchItem(
            profileImageUrl = null,
            nickname = "다시도전",
            relation = Relation.REJECTED,
            onClickAction = {},
            onClickCancel = null,
        )
    }
}

@Composable
private fun PreviewContainer(content: @Composable () -> Unit) {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(20.dp),
        ) {
            content()
        }
    }
}
