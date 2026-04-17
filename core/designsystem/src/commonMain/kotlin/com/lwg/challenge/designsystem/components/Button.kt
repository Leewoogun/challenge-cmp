package com.lwg.challenge.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun NormalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = ChallengeTheme.colorScheme.ivory2,
    contentColor: Color = ChallengeTheme.colorScheme.brown2,
    disabledContainerColor: Color = ChallengeTheme.colorScheme.ivory2.copy(alpha = 0.6f),
    disabledContentColor: Color = ChallengeTheme.colorScheme.brown2.copy(alpha = 0.4f),
    textStyle: TextStyle = ChallengeTheme.typography.medium14,
    shape: Shape = RoundedCornerShape(14.dp),
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        color = if (enabled) containerColor else disabledContainerColor,
        contentColor = if (enabled) contentColor else disabledContentColor,
    ) {
        Box(
            modifier = Modifier.padding(contentPadding),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = textStyle,
            )
        }
    }
}

@Preview
@Composable
private fun NormalButtonPreview() {
    ChallengeTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            NormalButton(
                text = "기본 버튼",
                onClick = {},
            )

            NormalButton(
                text = "비활성화 버튼",
                onClick = {},
                enabled = false,
            )

            NormalButton(
                text = "커스텀 색상 버튼",
                onClick = {},
                containerColor = ChallengeTheme.colorScheme.orange1,
                contentColor = ChallengeTheme.colorScheme.white,
            )

            NormalButton(
                text = "Bold 스타일 버튼",
                onClick = {},
                textStyle = ChallengeTheme.typography.bold16,
            )
        }
    }
}