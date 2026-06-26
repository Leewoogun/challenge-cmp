package com.lwg.challenge.feature.mypage

import androidx.compose.runtime.Composable
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.ui.components.PlaceholderScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun MyPageScreen() {
    PlaceholderScreen(title = "MY")
}

@Preview
@Composable
private fun MyPageScreenPreview() {
    ChallengeTheme {
        MyPageScreen()
    }
}
