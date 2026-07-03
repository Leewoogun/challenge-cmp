package com.lwg.challenge.feature.friends.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.IconTextButton
import com.lwg.challenge.designsystem.components.IconTextButtonStyle
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 친구 추가/초대 2-up 액션 행. design.md §A.2/§A.4 — primary [친구 추가] + outline [친구 초대].
 */
@Composable
internal fun FriendsActionRow(
    onClickAddFriend: () -> Unit,
    onClickInvite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        IconTextButton(
            text = "친구 추가",
            icon = Icons.Filled.PersonAdd,
            onClick = onClickAddFriend,
            modifier = Modifier.weight(1f),
            style = IconTextButtonStyle.Filled,
            textStyle = ChallengeTheme.typography.bold14,
            height = 48.dp,
            contentPadding = PaddingValues(horizontal = 12.dp),
        )
        IconTextButton(
            text = "친구 초대",
            icon = Icons.AutoMirrored.Filled.Send,
            onClick = onClickInvite,
            modifier = Modifier.weight(1f),
            style = IconTextButtonStyle.Outlined,
            textStyle = ChallengeTheme.typography.bold14,
            height = 48.dp,
            contentPadding = PaddingValues(horizontal = 12.dp),
        )
    }
}

@Preview
@Composable
private fun FriendsActionRowPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(20.dp),
        ) {
            FriendsActionRow(
                onClickAddFriend = {},
                onClickInvite = {},
            )
        }
    }
}
