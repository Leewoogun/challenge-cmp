package com.lwg.challenge.feature.friends.search.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lwg.challenge.designsystem.preview.ThemePreviews
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.feature.friends.search.component.SearchHintText

/** 2자 이상 입력했는데 결과가 없을 때의 안내 화면. */
@Composable
internal fun FriendsSearchEmptyScreen() {
    SearchHintText(text = "검색 결과가 없어요. 닉네임을 더 정확히 입력해보세요.")
}

@ThemePreviews
@Composable
private fun FriendsSearchEmptyScreenPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background.screen)) {
            FriendsSearchEmptyScreen()
        }
    }
}
