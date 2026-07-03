package com.lwg.challenge.designsystem.components.friend

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 친구 목록 항목 카드. design.md §A.4/§C.406 — 친구 1명을 가로 1줄로 표시.
 *
 * 이미지 로딩 라이브러리 부재로 [profileImageUrl] 은 시그니처에만 받고 실제 로드는 하지 않는다.
 * 프로필 자리는 닉네임 첫 글자 이니셜 placeholder(secondary 배경)로 렌더한다.
 *
 * @param profileImageUrl 원격 프로필 URL. 현재는 렌더에 사용하지 않는다(이미지 로더 도입 시 후속).
 * @param nickname 친구 닉네임(`bold14` + `onBackground`, 1줄 truncate).
 */
@Composable
fun FriendListItem(
    profileImageUrl: String?,
    nickname: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ChallengeTheme.colorScheme.surface,
        border = BorderStroke(width = 1.dp, color = ChallengeTheme.colorScheme.outline),
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
        }
    }
}

@Composable
internal fun ProfilePlaceholder(
    nickname: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(48.dp)
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

@Preview
@Composable
private fun FriendListItemPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            FriendListItem(
                profileImageUrl = null,
                nickname = "우건",
            )
        }
    }
}

@Preview
@Composable
private fun FriendListItemLongNicknamePreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            FriendListItem(
                profileImageUrl = null,
                nickname = "엄청나게긴닉네임을가진사용자가존재한다면어떻게보일까요테스트",
            )
        }
    }
}

@Preview
@Composable
private fun FriendListItemNoProfilePreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            FriendListItem(
                profileImageUrl = null,
                nickname = "도전자",
            )
        }
    }
}
