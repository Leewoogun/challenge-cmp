package com.lwg.challenge.feature.friends.search.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.feature.friends.search.component.SearchHintText
import org.jetbrains.compose.ui.tooling.preview.Preview

/** 입력이 2자 미만일 때의 입력 유도 화면. */
@Composable
internal fun FriendsSearchIdleScreen() {
    SearchHintText(text = "닉네임을 2자 이상 입력해주세요")
}

@Preview
@Composable
private fun FriendsSearchIdleScreenPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            FriendsSearchIdleScreen()
        }
    }
}
