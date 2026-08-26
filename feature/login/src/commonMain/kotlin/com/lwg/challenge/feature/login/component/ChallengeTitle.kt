package com.lwg.challenge.feature.login.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.modifier.wiggle
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun ChallengeTitle() {
    val fireBrush = ChallengeTheme.brushes.fire
    val onBackground = ChallengeTheme.colorScheme.onBackground

    val titleStyle = ChallengeTheme.typography.bold48

    val annotated: AnnotatedString = buildAnnotatedString {
        withStyle(SpanStyle(brush = fireBrush, fontWeight = FontWeight.Bold)) {
            append("영혼")
        }
        withStyle(SpanStyle(color = onBackground, fontWeight = FontWeight.Bold)) {
            append("을 \n걸어라.")
        }
    }

    Box(contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = annotated,
                style = titleStyle,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "🔥",
                style = ChallengeTheme.typography.bold30,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .wiggle(),
            )
        }
    }
}

@Preview
@Composable
private fun ChallengeTitlePreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            ChallengeTitle()
        }
    }
}
