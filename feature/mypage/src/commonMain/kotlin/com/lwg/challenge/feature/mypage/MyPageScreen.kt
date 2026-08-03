package com.lwg.challenge.feature.mypage

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.button.NormalButton
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.ui.components.PlaceholderScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun MyPageScreen(
    onLogoutClick: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        PlaceholderScreen(title = "MY")

        NormalButton(
            text = "로그아웃",
            onClick = onLogoutClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            contentColor = ChallengeTheme.colorScheme.error,
        )
    }
}

@Preview
@Composable
private fun MyPageScreenPreview() {
    ChallengeTheme {
        MyPageScreen(onLogoutClick = {})
    }
}
