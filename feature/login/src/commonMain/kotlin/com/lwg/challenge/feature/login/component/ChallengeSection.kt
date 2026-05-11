package com.lwg.challenge.feature.login.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.VerticalSpacer
import com.lwg.challenge.designsystem.components.StatusPillBadge
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 로그인 Hero — 스탬프 로고 + "SOUL CONTRACT" 뱃지 + 타이틀 + 서브카피.
 *
 * 진입 시 fadeIn + slideUp 애니메이션 (400ms).
 */
@Composable
internal fun ChallengeSection() {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 400)) +
                slideInVertically(
                    animationSpec = tween(durationMillis = 400),
                    initialOffsetY = { it / 6 },
                ),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            SoulStampLogo()

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatusPillBadge(text = "SOUL CONTRACT")

                ChallengeTitle()

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "말만 번지르르한 친구에게 던지는",
                        style = ChallengeTheme.typography.light14,
                        color = ChallengeTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    VerticalSpacer(height = 2.dp)
                    Text(
                        text = "피할 수 없는 1:1 도전장",
                        style = ChallengeTheme.typography.bold14,
                        color = ChallengeTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun ChallengeSectionPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            ChallengeSection()
        }
    }
}
