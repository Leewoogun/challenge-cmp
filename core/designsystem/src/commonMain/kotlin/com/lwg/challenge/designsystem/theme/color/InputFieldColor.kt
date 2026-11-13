package com.lwg.challenge.designsystem.theme.color

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * 입력창 색.
 *
 * @property background 입력창 배경
 * @property text 입력 글씨
 * @property hint placeholder 글씨
 * @property cursor 커서
 * @property focusedBorder 포커스 시 테두리
 */
@Immutable
data class InputFieldColor(
    val background: Color,
    val text: Color,
    val hint: Color,
    val cursor: Color,
    val focusedBorder: Color,
)
