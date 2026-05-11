package com.lwg.challenge.feature.login.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "영혼을 걸어라. 🔥" 타이틀.
 *
 * - "영혼" 부분만 `brushes.fire` brush 텍스트
 * - 나머지는 `onBackground`
 * - 🔥 이모지에 wiggle 애니메이션 (좌우 -8° ~ +8°)
 */
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

    val infinite = rememberInfiniteTransition(label = "title-wiggle")
    val rotation by infinite.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_500),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "wiggle-rotation",
    )

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
                    .rotate(rotation),
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
