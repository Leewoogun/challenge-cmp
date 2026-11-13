package com.lwg.challenge.ui.components

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.button.IconTextButton
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/** 아이콘 + 텍스트 CTA 에 in-flight 인디케이터를 더한 버튼. */
@Composable
fun LoadingIconButton(
    text: String,
    icon: ImageVector,
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
                disabledContainerColor = ChallengeTheme.colorScheme.button.background,
                disabledContentColor = ChallengeTheme.colorScheme.button.text,
            ),
            contentPadding = PaddingValues(horizontal = 12.dp),
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = ChallengeTheme.colorScheme.text.onPoint,
                strokeWidth = 2.dp,
            )
        }
    } else {
        IconTextButton(
            text = text,
            icon = icon,
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            enabled = enabled,
            textStyle = ChallengeTheme.typography.bold14,
        )
    }
}

@Preview
@Composable
private fun LoadingIconButtonEnabledPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(24.dp),
        ) {
            LoadingIconButton(
                text = "챌린지 걸기",
                icon = Icons.Filled.SportsKabaddi,
                enabled = true,
                loading = false,
                onClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun LoadingIconButtonDisabledPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(24.dp),
        ) {
            LoadingIconButton(
                text = "챌린지 걸기",
                icon = Icons.Filled.SportsKabaddi,
                enabled = false,
                loading = false,
                onClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun LoadingIconButtonLoadingPreview() {
    ChallengeTheme {
        Column(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(24.dp),
        ) {
            LoadingIconButton(
                text = "챌린지 걸기",
                icon = Icons.Filled.SportsKabaddi,
                enabled = false,
                loading = true,
                onClick = {},
            )
        }
    }
}
