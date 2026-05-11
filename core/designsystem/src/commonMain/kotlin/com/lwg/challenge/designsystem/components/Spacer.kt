package com.lwg.challenge.designsystem.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

/**
 * 디자인 시스템 표준 Spacer.
 *
 * - Dp 버전: 고정 크기 여백
 * - Float 버전 (Row/ColumnScope 내): `weight()` 기반 가변 여백
 *
 * Compose 의 `Spacer + Modifier.height/width` 조합 대신 이 컴포넌트를 사용하여
 * UI 코드의 가독성과 일관성을 확보한다.
 */
@Composable
fun VerticalSpacer(
    height: Dp,
) {
    Spacer(modifier = Modifier.height(height))
}

@Composable
fun HorizontalSpacer(
    width: Dp,
) {
    Spacer(modifier = Modifier.width(width))
}

@Composable
fun RowScope.HorizontalSpacer(
    width: Float,
) {
    Spacer(modifier = Modifier.weight(width))
}

@Composable
fun ColumnScope.VerticalSpacer(
    height: Float,
) {
    Spacer(modifier = Modifier.weight(height))
}
