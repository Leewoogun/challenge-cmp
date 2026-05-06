package com.lwg.challenge.feature.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.NormalButton
import com.lwg.challenge.designsystem.theme.BrandColors
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.feature.login.contract.LoginUiState

/**
 * 로그인 화면 UI.
 *
 * 배경: Lovable `--gradient-fire` (135deg 오렌지 그라데이션) — `ChallengeTheme.brushes.fire`.
 * 카카오 버튼: 카카오 공식 브랜드 색 — `BrandColors.KakaoYellow` / `BrandColors.KakaoLabel`.
 */
@Composable
internal fun LoginScreen(
    uiState: LoginUiState,
    onLoginClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ChallengeTheme.brushes.fire),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // TODO(design): 실제 로고 리소스로 교체.
            Text(
                text = "맹세",
                style = ChallengeTheme.typography.bold48,
                color = ChallengeTheme.colorScheme.onPrimary,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "말만 하지 말고, 걸어.",
                style = ChallengeTheme.typography.medium16,
                color = ChallengeTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(64.dp))

            // 카카오 공식 브랜드 색은 디자인 시스템 ColorScheme이 아닌 BrandColors에서 가져온다.
            NormalButton(
                text = if (uiState is LoginUiState.Loading) "로그인 중..." else "카카오로 시작하기",
                onClick = onLoginClick,
                enabled = uiState !is LoginUiState.Loading,
                modifier = Modifier.fillMaxWidth(),
                containerColor = BrandColors.KakaoYellow,
                contentColor = BrandColors.KakaoLabel,
                shape = RoundedCornerShape(14.dp),
                textStyle = ChallengeTheme.typography.bold16,
            )
        }

        if (uiState is LoginUiState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = ChallengeTheme.colorScheme.onPrimary)
            }
        }
    }
}
