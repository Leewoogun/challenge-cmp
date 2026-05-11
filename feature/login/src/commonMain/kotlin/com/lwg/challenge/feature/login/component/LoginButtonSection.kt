package com.lwg.challenge.feature.login.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.VerticalSpacer
import com.lwg.challenge.designsystem.components.LabeledDivider
import com.lwg.challenge.designsystem.components.NormalButton
import com.lwg.challenge.designsystem.theme.BrandColors
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import kotlinx.coroutines.delay
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 로그인 CTA — LabeledDivider + 카카오 버튼 + 약관 풋터.
 *
 * 진입 시 fadeIn + slideUp (200ms delay).
 */
@Composable
internal fun LoginButtonSection(
    isLoading: Boolean,
    onLoginClick: () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(200)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 400)) +
                slideInVertically(
                    animationSpec = tween(durationMillis = 400),
                    initialOffsetY = { it / 4 },
                ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LabeledDivider(
                text = "한 번 서명하면 무를 수 없음",
                modifier = Modifier.fillMaxWidth(),
            )

            NormalButton(
                text = "카카오로 시작하기",
                onClick = onLoginClick,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                containerColor = BrandColors.KakaoYellow,
                contentColor = BrandColors.KakaoLabel,
                shape = RoundedCornerShape(16.dp),
                textStyle = ChallengeTheme.typography.bold16,
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp),
            )

            VerticalSpacer(height = 4.dp)

            FooterAgreementText()
        }
    }
}

@Preview
@Composable
private fun LoginButtonSectionPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            LoginButtonSection(
                isLoading = false,
                onLoginClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun LoginButtonSectionLoadingPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            LoginButtonSection(
                isLoading = true,
                onLoginClick = {},
            )
        }
    }
}
