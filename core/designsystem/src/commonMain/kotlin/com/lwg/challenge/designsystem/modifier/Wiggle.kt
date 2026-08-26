package com.lwg.challenge.designsystem.modifier

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * `-maxAngle` ~ `+maxAngle` 사이를 무한 왕복하는 회전 애니메이션.
 *
 * @param maxAngle 좌우 최대 회전 각도(도).
 * @param durationMillis 한쪽 끝에서 반대쪽 끝까지 걸리는 시간.
 */
@Composable
fun Modifier.wiggle(
    maxAngle: Float = 8f,
    durationMillis: Int = 1_500,
): Modifier {
    val transition = rememberInfiniteTransition(label = "wiggle")
    val rotation by transition.animateFloat(
        initialValue = -maxAngle,
        targetValue = maxAngle,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "wiggle-rotation",
    )

    return rotate(rotation)
}

@Preview
@Composable
private fun WigglePreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            Text(
                text = "🔥",
                style = ChallengeTheme.typography.bold30,
                modifier = Modifier.wiggle(),
            )
        }
    }
}
