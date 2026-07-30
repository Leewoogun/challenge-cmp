package com.lwg.challenge.feature.challenge.create.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.ui.components.ProfilePlaceholder
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 대결 상대 선택 항목 카드. design.md §4.1.
 *
 * 이미지 로딩 라이브러리 부재로 [profileImageUrl] 은 시그니처에만 받고 실제 로드는 하지 않는다.
 * 전적(승/패) 행은 렌더하지 않는다 — `GET /api/v1/friends` 응답에 전적 필드가 없다(design.md §1.2).
 *
 * @param profileImageUrl 원격 프로필 URL. 현재는 렌더에 사용하지 않는다(이미지 로더 도입 시 후속).
 * @param selected 선택 여부. 선택 시 2.dp primary border 로 강조한다.
 */
@Composable
internal fun FriendPickItem(
    profileImageUrl: String?,
    nickname: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = ChallengeTheme.colorScheme.surface,
        border = if (selected) {
            BorderStroke(width = 2.dp, color = ChallengeTheme.colorScheme.primary)
        } else {
            BorderStroke(width = 1.dp, color = ChallengeTheme.colorScheme.outline)
        },
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ProfilePlaceholder(nickname = nickname)

            Text(
                text = nickname,
                style = ChallengeTheme.typography.bold14,
                color = ChallengeTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = ChallengeTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview
@Composable
private fun FriendPickItemPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            FriendPickItem(
                profileImageUrl = null,
                nickname = "우건",
                selected = false,
                onClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun FriendPickItemSelectedPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            FriendPickItem(
                profileImageUrl = null,
                nickname = "우건",
                selected = true,
                onClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun FriendPickItemLongNicknamePreview() {
    ChallengeTheme {
        Column(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            FriendPickItem(
                profileImageUrl = null,
                nickname = "엄청나게긴닉네임을가진사용자가존재한다면어떻게보일까요테스트",
                selected = false,
                onClick = {},
            )
        }
    }
}
