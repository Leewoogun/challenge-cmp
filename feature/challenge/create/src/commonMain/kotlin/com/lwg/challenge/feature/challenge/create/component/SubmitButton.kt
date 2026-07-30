package com.lwg.challenge.feature.challenge.create.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.IconTextButton
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * "챌린지 걸기" CTA. design.md §1.3.
 *
 * [IconTextButton] 에는 content 슬롯이 없어 인디케이터를 넣을 수 없다. 그래서 [loading] 일 때만
 * 같은 시각 스펙(52.dp / R12 / primary)의 [Button] 으로 대체 렌더한다.
 */
@Composable
internal fun SubmitButton(
    text: String,
    enabled: Boolean,
    loading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (loading) {
        Button(
            onClick = {},
            modifier = modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = false,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                disabledContainerColor = ChallengeTheme.colorScheme.primary,
                disabledContentColor = ChallengeTheme.colorScheme.onPrimary,
            ),
            contentPadding = PaddingValues(horizontal = 12.dp),
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = ChallengeTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp,
            )
        }
    } else {
        IconTextButton(
            text = text,
            icon = Icons.Filled.SportsKabaddi,
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            enabled = enabled,
            textStyle = ChallengeTheme.typography.bold14,
        )
    }
}

@Preview
@Composable
private fun SubmitButtonEnabledPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            SubmitButton(
                text = "챌린지 걸기",
                enabled = true,
                loading = false,
                onClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun SubmitButtonDisabledPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            SubmitButton(
                text = "챌린지 걸기",
                enabled = false,
                loading = false,
                onClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun SubmitButtonLoadingPreview() {
    ChallengeTheme {
        Column(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            SubmitButton(
                text = "챌린지 걸기",
                enabled = false,
                loading = true,
                onClick = {},
            )
        }
    }
}
