package com.lwg.challenge.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 아이콘 + 텍스트 CTA 버튼. 빈 상태 카드(친구/홈) 등 "아이콘이 앞에 붙는 버튼"을 위한 공용 컴포넌트.
 *
 * [IconTextButtonStyle.Filled] (강조 배경) / [IconTextButtonStyle.Outlined] (테두리) 두 스타일 지원.
 * 단순 텍스트 버튼은 [NormalButton] 을 사용한다.
 *
 * @param text 버튼 라벨.
 * @param icon 텍스트 좌측 16.dp 아이콘.
 * @param onClick 클릭 콜백.
 * @param style 채움/외곽선 스타일.
 * @param textStyle 라벨 타이포(기본 `medium14`).
 */
@Composable
fun IconTextButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: IconTextButtonStyle = IconTextButtonStyle.Filled,
    enabled: Boolean = true,
    containerColor: Color = ChallengeTheme.colorScheme.primary,
    contentColor: Color = when (style) {
        IconTextButtonStyle.Filled -> ChallengeTheme.colorScheme.onPrimary
        IconTextButtonStyle.Outlined -> ChallengeTheme.colorScheme.onSurface
    },
    textStyle: TextStyle = ChallengeTheme.typography.medium14,
    shape: Shape = RoundedCornerShape(12.dp),
    height: Dp = 52.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp),
) {
    val sizedModifier = modifier.height(height)

    when (style) {
        IconTextButtonStyle.Filled -> Button(
            onClick = onClick,
            modifier = sizedModifier,
            enabled = enabled,
            shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = containerColor,
                contentColor = contentColor,
            ),
            contentPadding = contentPadding,
        ) {
            IconTextContent(icon = icon, text = text, tint = contentColor, textStyle = textStyle)
        }

        IconTextButtonStyle.Outlined -> OutlinedButton(
            onClick = onClick,
            modifier = sizedModifier,
            enabled = enabled,
            shape = shape,
            border = BorderStroke(width = 1.dp, color = ChallengeTheme.colorScheme.outline),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = contentColor),
            contentPadding = contentPadding,
        ) {
            IconTextContent(icon = icon, text = text, tint = contentColor, textStyle = textStyle)
        }
    }
}

enum class IconTextButtonStyle { Filled, Outlined }

@Composable
private fun IconTextContent(
    icon: ImageVector,
    text: String,
    tint: Color,
    textStyle: TextStyle,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = tint,
        )
        Text(
            text = text,
            style = textStyle,
            color = tint,
        )
    }
}

@Preview
@Composable
private fun IconTextButtonPreview() {
    ChallengeTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            IconTextButton(
                text = "친구 추가하기",
                icon = Icons.Filled.PersonAdd,
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
            )

            IconTextButton(
                text = "친구 등록",
                icon = Icons.Filled.PersonAdd,
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                style = IconTextButtonStyle.Outlined,
            )

            IconTextButton(
                text = "챌린지 만들기",
                icon = Icons.Filled.SportsKabaddi,
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
            )
        }
    }
}
