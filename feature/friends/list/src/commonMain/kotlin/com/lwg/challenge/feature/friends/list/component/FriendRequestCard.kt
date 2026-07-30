package com.lwg.challenge.feature.friends.list.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.IconTextButton
import com.lwg.challenge.designsystem.components.IconTextButtonStyle
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.ui.components.ProfilePlaceholder
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 친구 요청 카드. design.md §A.4/§C.419-423 — 받은 친구 요청 1건을 수락/거절 액션과 함께 표시.
 *
 * 이미지 로딩 라이브러리 부재로 [profileImageUrl] 은 시그니처에만 받고 실제 로드는 하지 않는다.
 * 프로필 자리는 닉네임 첫 글자 이니셜 placeholder(secondary 배경)로 렌더한다.
 *
 * @param profileImageUrl 원격 프로필 URL. 현재는 렌더에 사용하지 않는다(이미지 로더 도입 시 후속).
 * @param nickname 요청자 닉네임(`bold14` + `onBackground`, 1줄 truncate).
 * @param onAccept 수락 버튼 콜백.
 * @param onReject 거절 버튼 콜백.
 */
@Composable
internal fun FriendRequestCard(
    profileImageUrl: String?,
    nickname: String,
    onAccept: () -> Unit,
    onReject: () -> Unit,
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
            ProfilePlaceholder(nickname = nickname)

            Text(
                text = nickname,
                style = ChallengeTheme.typography.bold14,
                color = ChallengeTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconTextButton(
                    text = "수락",
                    icon = Icons.Filled.Check,
                    onClick = onAccept,
                    style = IconTextButtonStyle.Filled,
                    textStyle = ChallengeTheme.typography.bold14,
                    shape = RoundedCornerShape(12.dp),
                    height = 36.dp,
                    contentPadding = PaddingValues(horizontal = 12.dp),
                )
                IconTextButton(
                    text = "거절",
                    icon = Icons.Filled.Close,
                    onClick = onReject,
                    style = IconTextButtonStyle.Outlined,
                    textStyle = ChallengeTheme.typography.bold14,
                    shape = RoundedCornerShape(12.dp),
                    height = 36.dp,
                    contentPadding = PaddingValues(horizontal = 12.dp),
                )
            }
        }
    }
}

@Preview
@Composable
private fun FriendRequestCardPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            FriendRequestCard(
                profileImageUrl = null,
                nickname = "도전왕",
                onAccept = {},
                onReject = {},
            )
        }
    }
}
