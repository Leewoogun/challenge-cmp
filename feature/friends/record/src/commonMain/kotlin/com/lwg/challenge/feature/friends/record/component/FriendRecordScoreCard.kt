package com.lwg.challenge.feature.friends.record.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.VerticalSpacer
import com.lwg.challenge.designsystem.preview.ThemePreviews
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.friend.UserBrief
import com.lwg.challenge.feature.friends.record.PreviewFriend
import com.lwg.challenge.feature.friends.record.friendRecordPreviewData
import com.lwg.challenge.ui.components.ProfileImage

/**
 * 히어로 카드. 프로필과 닉네임 아래 [scoreContent] 를 붙인다.
 * 빈 상태는 스코어 없이 프로필만 그린다 — `0 : 0` 위에 "붙어본 적 없다" 를 쓰면 같은 말을 두 번 한다.
 */
@Composable
internal fun FriendRecordScoreCard(
    friend: UserBrief,
    modifier: Modifier = Modifier,
    scoreContent: @Composable ColumnScope.() -> Unit = {},
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ChallengeTheme.colorScheme.background.card,
        border = BorderStroke(width = 1.dp, color = ChallengeTheme.colorScheme.line.border),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ProfileImage(
                imageUrl = friend.profileImageUrl,
                nickname = friend.nickname,
                size = 64.dp,
                shape = RoundedCornerShape(16.dp),
            )

            VerticalSpacer(12.dp)

            Text(
                text = friend.nickname,
                style = ChallengeTheme.typography.bold18,
                color = ChallengeTheme.colorScheme.text.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            scoreContent()
        }
    }
}

@ThemePreviews
@Composable
private fun FriendRecordScoreCardPreview() {
    val data = friendRecordPreviewData()
    FriendRecordScoreCardPreviewContainer {
        FriendRecordScoreCard(friend = data.friend) {
            VerticalSpacer(16.dp)
            FriendRecordScoreBoard(
                myWinsText = data.myWinsText,
                friendWinsText = data.friendWinsText,
                friendNickname = data.friend.nickname,
                isMuted = data.isScoreMuted,
                subLineText = data.scoreSubLineText,
                contentDescription = data.scoreContentDescription,
                onSendChallengeClick = {},
            )
        }
    }
}

/** 빈 상태 — 프로필과 닉네임만. */
@ThemePreviews
@Composable
private fun FriendRecordScoreCardProfileOnlyPreview() {
    FriendRecordScoreCardPreviewContainer {
        FriendRecordScoreCard(friend = PreviewFriend)
    }
}

@Composable
private fun FriendRecordScoreCardPreviewContainer(content: @Composable () -> Unit) {
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
