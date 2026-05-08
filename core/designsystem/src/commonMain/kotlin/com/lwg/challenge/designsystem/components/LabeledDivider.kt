package com.lwg.challenge.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme

/**
 * 양쪽에 1px 구분선이 있고 가운데 짧은 라벨 텍스트가 들어가는 시각 패턴.
 *
 * Material 3에는 라벨 포함 Divider가 없어 별도 컴포넌트로 둔다. 로그인 화면의
 * "한 번 서명하면 무를 수 없음" 같은 강조 캡션에 사용한다.
 */
@Composable
fun LabeledDivider(
    text: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = ChallengeTheme.typography.bold12,
    textColor: Color = ChallengeTheme.colorScheme.onSurfaceVariant,
    dividerColor: Color = ChallengeTheme.colorScheme.outline,
    horizontalSpacing: androidx.compose.ui.unit.Dp = 12.dp,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        HorizontalDivider(
            modifier = Modifier
                .weight(1f)
                .height(1.dp),
            color = dividerColor,
        )
        Text(
            text = text,
            style = textStyle,
            color = textColor,
            modifier = Modifier.padding(horizontal = horizontalSpacing),
        )
        HorizontalDivider(
            modifier = Modifier
                .weight(1f)
                .height(1.dp),
            color = dividerColor,
        )
    }
}
