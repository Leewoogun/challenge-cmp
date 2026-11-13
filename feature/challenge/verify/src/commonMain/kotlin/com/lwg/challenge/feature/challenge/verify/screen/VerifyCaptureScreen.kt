package com.lwg.challenge.feature.challenge.verify.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.VerticalSpacer
import com.lwg.challenge.designsystem.components.button.NormalButton
import com.lwg.challenge.designsystem.preview.ThemePreviews
import com.lwg.challenge.designsystem.theme.ChallengeTheme

/** 촬영 전 안내. 갤러리 진입점은 없다 — 실시간 촬영만 허용한다. */
@Composable
internal fun VerifyCaptureScreen(
    onCaptureClick: () -> Unit,
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
            imageVector = Icons.Filled.PhotoCamera,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = ChallengeTheme.colorScheme.icon.point,
        )

        VerticalSpacer(20.dp)

        Text(
            text = "오늘의 미션을 인증해요",
            style = ChallengeTheme.typography.bold20,
            color = ChallengeTheme.colorScheme.text.primary,
        )

        VerticalSpacer(8.dp)

        Text(
            text = "인증 사진은 실시간 촬영으로만 올릴 수 있어요.\n촬영한 사진을 확인한 뒤 제출을 확정해요.",
            style = ChallengeTheme.typography.medium14,
            color = ChallengeTheme.colorScheme.text.secondary,
            textAlign = TextAlign.Center,
        )

        VerticalSpacer(28.dp)

        NormalButton(
            text = "촬영하기",
            onClick = onCaptureClick,
            modifier = Modifier.fillMaxWidth(),
            containerColor = ChallengeTheme.colorScheme.button.background,
            contentColor = ChallengeTheme.colorScheme.button.text,
            textStyle = ChallengeTheme.typography.bold16,
        )
    }
}

@ThemePreviews
@Composable
private fun VerifyCaptureScreenPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background.screen)) {
            VerifyCaptureScreen(onCaptureClick = {})
        }
    }
}
