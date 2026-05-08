package com.lwg.challenge.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme

/**
 * 점(dot) + 텍스트로 구성된 캡슐 형태의 상태/카테고리 뱃지.
 *
 * Lovable의 "SOUL CONTRACT" 뱃지와 같은 시각 패턴 — 반투명 채움 + 가는 보더 + 작은 점.
 *
 * 기본 색상은 `primary` 톤. 다른 톤이 필요하면 [accentColor] 로 오버라이드한다.
 */
@Composable
fun StatusPillBadge(
    text: String,
    modifier: Modifier = Modifier,
    accentColor: Color = ChallengeTheme.colorScheme.primary,
    textStyle: TextStyle = ChallengeTheme.typography.bold12,
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = accentColor.copy(alpha = 0.10f),
        contentColor = accentColor,
        border = BorderStroke(width = 1.dp, color = accentColor.copy(alpha = 0.20f)),
    ) {
        Row(
            modifier = Modifier.padding(contentPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(color = accentColor, shape = CircleShape),
            )
            Text(
                text = text,
                style = textStyle,
            )
        }
    }
}
