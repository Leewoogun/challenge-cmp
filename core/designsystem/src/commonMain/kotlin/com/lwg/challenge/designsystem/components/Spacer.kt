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
