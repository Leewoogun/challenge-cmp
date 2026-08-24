package com.lwg.challenge.feature.challenge.verify.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.VerticalSpacer
import com.lwg.challenge.designsystem.components.button.NormalButton
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/** 제출 확정 완료 화면. */
@Composable
internal fun VerifyDoneSection(
    onDoneClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = ChallengeTheme.colorScheme.success,
        )

        VerticalSpacer(20.dp)

        Text(
            text = "인증이 완료됐어요 🔥",
            style = ChallengeTheme.typography.bold20,
            color = ChallengeTheme.colorScheme.onBackground,
        )

        VerticalSpacer(8.dp)

        Text(
            text = "오늘의 미션 인증이 상대에게 전달돼요.",
            style = ChallengeTheme.typography.medium14,
            color = ChallengeTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        VerticalSpacer(28.dp)

        NormalButton(
            text = "확인",
            onClick = onDoneClick,
            modifier = Modifier.fillMaxWidth(),
            containerColor = ChallengeTheme.colorScheme.primary,
            contentColor = ChallengeTheme.colorScheme.onPrimary,
            textStyle = ChallengeTheme.typography.bold16,
        )
    }
}

@Preview
@Composable
private fun VerifyDoneSectionPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            VerifyDoneSection(onDoneClick = {})
        }
    }
}
