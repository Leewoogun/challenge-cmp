package com.lwg.challenge.feature.challenge.create.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.VerticalSpacer
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 라벨 + 단일 라인 입력 박스 + 글자 수 카운터 묶음. design.md §1.3.
 *
 * Material3 `TextField` 의 고정 높이·indicator 를 피하려고 [BasicTextField] + decorationBox 로 구성한다
 * (`FriendsSearchTopBar` 선례).
 *
 * @param counterText "{trim 길이}/100". 서버 검증이 trim 기준이라 카운터도 trim 으로 센다 — State 에서 계산해 받는다.
 * @param counterAtMax 100자 도달 여부. true 면 카운터를 warning 색으로 전환한다.
 */
@Composable
internal fun MissionInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    counterText: String,
    counterAtMax: Boolean,
    imeAction: ImeAction,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(12.dp)

    Column(modifier = modifier) {
        Text(
            text = label,
            style = ChallengeTheme.typography.medium12,
            color = ChallengeTheme.colorScheme.onSurfaceVariant,
        )

        VerticalSpacer(6.dp)

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .onFocusChanged { focused = it.isFocused },
            enabled = enabled,
            singleLine = true,
            textStyle = ChallengeTheme.typography.medium14.copy(
                color = ChallengeTheme.colorScheme.onBackground,
            ),
            keyboardOptions = KeyboardOptions(imeAction = imeAction),
            cursorBrush = SolidColor(ChallengeTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = ChallengeTheme.colorScheme.secondary, shape = shape)
                        .then(
                            if (focused) {
                                Modifier.border(
                                    width = 2.dp,
                                    color = ChallengeTheme.colorScheme.primary,
                                    shape = shape,
                                )
                            } else {
                                Modifier
                            },
                        )
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = ChallengeTheme.typography.medium14,
                            color = ChallengeTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    innerTextField()
                }
            },
        )

        VerticalSpacer(6.dp)

        Text(
            text = counterText,
            style = ChallengeTheme.typography.medium12,
            color = if (counterAtMax) {
                ChallengeTheme.colorScheme.warning
            } else {
                ChallengeTheme.colorScheme.onSurfaceVariant
            },
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview
@Composable
private fun MissionInputFieldEmptyPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            MissionInputField(
                label = "나의 미션",
                value = "",
                onValueChange = {},
                placeholder = "예) 오늘 운동 1시간 하기",
                counterText = "0/100",
                counterAtMax = false,
                imeAction = ImeAction.Next,
            )
        }
    }
}

@Preview
@Composable
private fun MissionInputFieldFilledPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            MissionInputField(
                label = "내기 (진 사람이 해야 할 것)",
                value = "커피 사기",
                onValueChange = {},
                placeholder = "예) 커피 사기",
                counterText = "5/100",
                counterAtMax = false,
                imeAction = ImeAction.Done,
            )
        }
    }
}

@Preview
@Composable
private fun MissionInputFieldAtMaxPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            MissionInputField(
                label = "나의 미션",
                value = "가".repeat(100),
                onValueChange = {},
                placeholder = "예) 오늘 운동 1시간 하기",
                counterText = "100/100",
                counterAtMax = true,
                imeAction = ImeAction.Next,
            )
        }
    }
}
