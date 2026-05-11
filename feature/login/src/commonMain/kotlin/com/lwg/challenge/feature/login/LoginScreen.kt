package com.lwg.challenge.feature.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.VerticalSpacer
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.feature.login.component.BackgroundDecor
import com.lwg.challenge.feature.login.component.LoginButtonSection
import com.lwg.challenge.feature.login.component.ChallengeSection
import com.lwg.challenge.feature.login.contract.LoginUiState
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 로그인 화면 UI.
 *
 * 디자인 결정 (2026-05-08, `docs/features/auth-kakao/design.md`):
 * - 다크 베이스 + 상단 글로우 후광 + 하단 fire 블롭
 * - Hero: 스탬프 로고 + "SOUL CONTRACT" 뱃지 + 타이틀("영혼" 부분 fire-gradient) + 서브 카피
 * - CTA: LabeledDivider + 카카오 버튼(텍스트 only) + 약관 풋터
 * - Hero/CTA 진입 시 slide-up 애니메이션 (CTA 0.2s delay)
 *
 * 화면 전용 컴포넌트는 `component/` 패키지로 분리되어 있다.
 */
@Composable
internal fun LoginScreen(
    uiState: LoginUiState,
    onLoginClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ChallengeTheme.colorScheme.background),
    ) {
        BackgroundDecor()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            VerticalSpacer(height = 1f)

            ChallengeSection()

            VerticalSpacer(height = 1f)

            LoginButtonSection(
                isLoading = uiState is LoginUiState.Loading,
                onLoginClick = onLoginClick,
            )

            VerticalSpacer(height = 8.dp)
        }

        if (uiState is LoginUiState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.30f)),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = ChallengeTheme.colorScheme.primary)
            }
        }
    }
}

@Preview
@Composable
private fun LoginScreenIdlePreview() {
    ChallengeTheme {
        LoginScreen(
            uiState = LoginUiState.Idle,
            onLoginClick = {},
        )
    }
}

@Preview
@Composable
private fun LoginScreenLoadingPreview() {
    ChallengeTheme {
        LoginScreen(
            uiState = LoginUiState.Loading,
            onLoginClick = {},
        )
    }
}
