package com.lwg.challenge.feature.challenge.create.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.VerticalSpacer
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/** 위저드 각 단계 상단의 제목 + 서브 문구. design.md §1.2 / §1.3. */
@Composable
internal fun StepHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = ChallengeTheme.typography.bold20,
            color = ChallengeTheme.colorScheme.text.primary,
        )

        VerticalSpacer(4.dp)

        Text(
            text = subtitle,
            style = ChallengeTheme.typography.medium14,
            color = ChallengeTheme.colorScheme.text.secondary,
        )
    }
}

@Preview
@Composable
private fun StepHeaderPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(24.dp),
        ) {
            StepHeader(
                title = "누구에게 걸래? 🔥",
                subtitle = "대결 상대를 선택하세요",
            )
        }
    }
}
