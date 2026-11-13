package com.lwg.challenge.feature.login.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.LabeledDivider
import com.lwg.challenge.designsystem.components.button.NormalButton
import com.lwg.challenge.designsystem.components.VerticalSpacer
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.feature.login.contract.TestAccount
import com.lwg.challenge.feature.login.contract.TestLoginUiState
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * debug 빌드 전용 테스트 계정 로그인 섹션. release 에서는 [TestLoginUiState.isVisible] 이
 * false 라 아무것도 렌더하지 않는다.
 */
@Composable
internal fun TestLoginSection(
    state: TestLoginUiState,
    isLoginInProgress: Boolean,
    onAccountClick: (TestAccount) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!state.isVisible) return

    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        LabeledDivider(
            text = "개발용",
            modifier = Modifier.fillMaxWidth(),
        )

        VerticalSpacer(height = 8.dp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TestAccount.entries.forEach { account ->
                NormalButton(
                    text = account.label,
                    onClick = { onAccountClick(account) },
                    enabled = state.isEnabled && !isLoginInProgress,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    containerColor = ChallengeTheme.colorScheme.background.field,
                    contentColor = ChallengeTheme.colorScheme.text.secondary,
                    shape = RoundedCornerShape(12.dp),
                    textStyle = ChallengeTheme.typography.medium14,
                )
            }
        }

        state.caption?.let { caption ->
            Text(
                text = caption,
                style = ChallengeTheme.typography.medium12,
                color = ChallengeTheme.colorScheme.text.secondary,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

@Preview
@Composable
private fun TestLoginSectionEnabledPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(24.dp),
        ) {
            TestLoginSection(
                state = TestLoginUiState(isVisible = true),
                isLoginInProgress = false,
                onAccountClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun TestLoginSectionUnavailablePreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(24.dp),
        ) {
            TestLoginSection(
                state = TestLoginUiState(isVisible = true, isUnavailable = true),
                isLoginInProgress = false,
                onAccountClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun TestLoginSectionHiddenPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(24.dp),
        ) {
            TestLoginSection(
                state = TestLoginUiState(isVisible = false),
                isLoginInProgress = false,
                onAccountClick = {},
            )
        }
    }
}
