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
import com.lwg.challenge.designsystem.components.button.IconTextButton
import com.lwg.challenge.designsystem.components.button.IconTextButtonStyle
import com.lwg.challenge.designsystem.preview.ThemePreviews
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.friend.Relation
import com.lwg.challenge.ui.components.ProfileImage

/**
 * 검색 결과 1행 — 프로필 + 닉네임 + relation 5종 액션.
 *
 * @param enabled 요청이 나가 있는 동안 false — 액션과 취소를 모두 잠근다.
 * @param onClickCancel 요청 취소. 취소 버튼은 [Relation.REQUEST_SENT] 일 때만 그린다.
 */
@Composable
internal fun FriendSearchItem(
    profileImageUrl: String?,
    nickname: String,
    relation: Relation,
    enabled: Boolean,
    onClickAction: () -> Unit,
    onClickCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ChallengeTheme.colorScheme.background.card,
        border = BorderStroke(width = 1.dp, color = ChallengeTheme.colorScheme.line.border),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ProfileImage(imageUrl = profileImageUrl, nickname = nickname, size = 44.dp)

            Text(
                text = nickname,
                style = ChallengeTheme.typography.bold14,
                color = ChallengeTheme.colorScheme.text.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )

            RelationAction(
                relation = relation,
                enabled = enabled,
                onClickAction = onClickAction,
                onClickCancel = onClickCancel,
            )
        }
    }
}

@Composable
private fun RelationAction(
    relation: Relation,
    enabled: Boolean,
    onClickAction: () -> Unit,
    onClickCancel: () -> Unit,
) {
    when (relation) {
        Relation.NONE -> IconTextButton(
            text = "친구 요청",
            icon = Icons.Filled.PersonAdd,
            onClick = onClickAction,
            enabled = enabled,
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
                    .clickable(enabled = enabled, onClick = onClickCancel),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "요청 취소",
                    modifier = Modifier.size(14.dp),
                    tint = ChallengeTheme.colorScheme.icon.secondary,
                )
            }
        }

        Relation.REQUEST_RECEIVED -> IconTextButton(
            text = "수락",
            icon = Icons.Filled.Check,
            onClick = onClickAction,
            enabled = enabled,
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
            enabled = enabled,
            style = IconTextButtonStyle.Filled,
            containerColor = ChallengeTheme.colorScheme.background.field,
            contentColor = ChallengeTheme.colorScheme.text.primary,
            textStyle = ChallengeTheme.typography.bold14,
            shape = RoundedCornerShape(12.dp),
            height = 36.dp,
            contentPadding = PaddingValues(horizontal = 12.dp),
        )
    }
}

/** 정적 relation 뱃지. Disabled Button 의 자동 alpha(0.38) 를 피하려고 클릭 없는 [Surface] 로 그린다. */
@Composable
private fun RelationBadge(
    text: String,
    withBorder: Boolean,
    textStyle: androidx.compose.ui.text.TextStyle = ChallengeTheme.typography.bold14,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = ChallengeTheme.colorScheme.background.field,
        border = if (withBorder) {
            BorderStroke(width = 1.dp, color = ChallengeTheme.colorScheme.line.border)
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
                color = ChallengeTheme.colorScheme.text.secondary,
            )
        }
    }
}

@ThemePreviews
@Composable
private fun FriendSearchItemNonePreview() {
    PreviewContainer {
        FriendSearchItem(
            profileImageUrl = null,
            nickname = "우건",
            relation = Relation.NONE,
            enabled = true,
            onClickAction = {},
            onClickCancel = {},
        )
    }
}

@ThemePreviews
@Composable
private fun FriendSearchItemRequestSentPreview() {
    PreviewContainer {
        FriendSearchItem(
            profileImageUrl = null,
            nickname = "도전왕",
            relation = Relation.REQUEST_SENT,
            enabled = true,
            onClickAction = {},
            onClickCancel = {},
        )
    }
}

@ThemePreviews
@Composable
private fun FriendSearchItemRequestReceivedPreview() {
    PreviewContainer {
        FriendSearchItem(
            profileImageUrl = null,
            nickname = "도전자",
            relation = Relation.REQUEST_RECEIVED,
            enabled = true,
            onClickAction = {},
            onClickCancel = {},
        )
    }
}

@ThemePreviews
@Composable
private fun FriendSearchItemFriendPreview() {
    PreviewContainer {
        FriendSearchItem(
            profileImageUrl = null,
            nickname = "절친",
            relation = Relation.FRIEND,
            enabled = true,
            onClickAction = {},
            onClickCancel = {},
        )
    }
}

@ThemePreviews
@Composable
private fun FriendSearchItemRejectedPreview() {
    PreviewContainer {
        FriendSearchItem(
            profileImageUrl = null,
            nickname = "다시도전",
            relation = Relation.REJECTED,
            enabled = true,
            onClickAction = {},
            onClickCancel = {},
        )
    }
}

/** 요청이 나가 있는 행 — 버튼이 잠긴다. */
@ThemePreviews
@Composable
private fun FriendSearchItemRequestingPreview() {
    PreviewContainer {
        FriendSearchItem(
            profileImageUrl = null,
            nickname = "우건",
            relation = Relation.NONE,
            enabled = false,
            onClickAction = {},
            onClickCancel = {},
        )
    }
}

@Composable
private fun PreviewContainer(content: @Composable () -> Unit) {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(20.dp),
        ) {
            content()
        }
    }
}
