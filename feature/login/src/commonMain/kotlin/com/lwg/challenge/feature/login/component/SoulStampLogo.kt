package com.lwg.challenge.feature.login.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun SoulStampLogo(
    modifier: Modifier = Modifier,
) {
    val infinite = rememberInfiniteTransition(label = "soul-stamp")

    val stampAlpha by infinite.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2_000),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "stamp-alpha",
    )

    val sparkleAlpha1 by infinite.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_400),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "sparkle1-alpha",
    )
    val sparkleAlpha2 by infinite.animateFloat(
        initialValue = 1.0f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_400),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "sparkle2-alpha",
    )

    val onPrimary = ChallengeTheme.colorScheme.onPrimary
    val primary = ChallengeTheme.colorScheme.primary
    val fireBrush = ChallengeTheme.brushes.fire

    Box(modifier = modifier.size(140.dp)) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .alpha(stampAlpha)
                .clip(CircleShape)
                .background(brush = fireBrush),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .size(124.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = onPrimary.copy(alpha = 0.30f),
                        shape = CircleShape,
                    ),
            )
            Icon(
                imageVector = Icons.Filled.LocalFireDepartment,
                contentDescription = null,
                tint = onPrimary,
                modifier = Modifier.size(56.dp),
            )
        }

        Icon(
            imageVector = Icons.Filled.AutoAwesome,
            contentDescription = null,
            tint = primary,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 8.dp, y = (-8).dp)
                .size(20.dp)
                .alpha(sparkleAlpha1),
        )
        Icon(
            imageVector = Icons.Filled.AutoAwesome,
            contentDescription = null,
            tint = primary,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-8).dp, y = 4.dp)
                .size(14.dp)
                .alpha(sparkleAlpha2),
        )
    }
}

@Preview
@Composable
private fun SoulStampLogoPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(32.dp),
        ) {
            SoulStampLogo()
        }
    }
}
