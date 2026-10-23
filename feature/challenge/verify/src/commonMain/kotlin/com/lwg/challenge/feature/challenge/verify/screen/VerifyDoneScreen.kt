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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.VerticalSpacer
import com.lwg.challenge.designsystem.components.button.NormalButton
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

private const val FIRST_SUBMIT_MESSAGE = "오늘의 미션 인증이 상대에게 전달돼요."
private const val REPLACED_MESSAGE = "사진이 교체됐어요. 상대는 최신 사진을 보게 돼요."

/**
 * 제출 확정 완료 화면. 완료 문구 두 개는 초안이라 확정이 필요하다.
 *
 * @param isReplace 교체 제출은 알림이 나가지 않아 "상대에게 전달돼요" 대신 다른 문구를 쓴다.
 */
@Composable
internal fun VerifyDoneScreen(
    isReplace: Boolean,
    onDoneClick: () -> Unit,
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
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = ChallengeTheme.colorScheme.success,
        )

        VerticalSpacer(20.dp)

        Text(
            text = "인증이 완료됐어요 🔥",
            style = ChallengeTheme.typography.bold20,
            color = ChallengeTheme.colorScheme.onBackground,
        )

        VerticalSpacer(8.dp)

        Text(
            text = if (isReplace) REPLACED_MESSAGE else FIRST_SUBMIT_MESSAGE,
            style = ChallengeTheme.typography.medium14,
            color = ChallengeTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        VerticalSpacer(28.dp)

        NormalButton(
            text = "확인",
            onClick = onDoneClick,
            modifier = Modifier.fillMaxWidth(),
            containerColor = ChallengeTheme.colorScheme.primary,
            contentColor = ChallengeTheme.colorScheme.onPrimary,
            textStyle = ChallengeTheme.typography.bold16,
        )
    }
}

@Preview
@Composable
private fun VerifyDoneScreenPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            VerifyDoneScreen(
                isReplace = false,
                onDoneClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun VerifyDoneScreenReplacePreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            VerifyDoneScreen(
                isReplace = true,
                onDoneClick = {},
            )
        }
    }
}
