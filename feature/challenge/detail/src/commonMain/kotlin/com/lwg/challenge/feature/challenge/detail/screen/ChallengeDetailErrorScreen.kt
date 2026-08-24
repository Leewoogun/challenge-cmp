package com.lwg.challenge.feature.challenge.detail.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.VerticalSpacer
import com.lwg.challenge.designsystem.components.button.IconTextButton
import com.lwg.challenge.designsystem.components.button.IconTextButtonStyle
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/** 계약서를 한 번도 못 받아온 상태. 보여줄 계약이 없으므로 화면 전체를 차지한다. */
@Composable
internal fun ChallengeDetailErrorScreen(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "계약서를 불러오지 못했어요",
            style = ChallengeTheme.typography.bold16,
            color = ChallengeTheme.colorScheme.onBackground,
        )

        VerticalSpacer(8.dp)

        Text(
            text = message,
            style = ChallengeTheme.typography.medium14,
            color = ChallengeTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        VerticalSpacer(20.dp)

        IconTextButton(
            text = "다시 시도",
            icon = Icons.Filled.Refresh,
            onClick = onRetry,
            style = IconTextButtonStyle.Outlined,
        )
    }
}

@Preview
@Composable
private fun ChallengeDetailErrorScreenPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            ChallengeDetailErrorScreen(
                message = "네트워크 오류가 발생하였습니다.",
                onRetry = {},
            )
        }
    }
}
