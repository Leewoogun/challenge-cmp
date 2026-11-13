package com.lwg.challenge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.ChallengeInputField
import com.lwg.challenge.designsystem.components.InputFieldType
import com.lwg.challenge.designsystem.components.VerticalSpacer
import com.lwg.challenge.designsystem.preview.ThemePreviews
import com.lwg.challenge.designsystem.theme.ChallengeTheme

/** 라벨 + 입력 필드 + 글자 수 카운터 한 세트. */
@Composable
fun LabeledInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    counterText: String? = null,
    counterAtMax: Boolean = false,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    type: InputFieldType = InputFieldType.Basic,
    imeAction: ImeAction = ImeAction.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    focusRequester: FocusRequester? = null,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = ChallengeTheme.typography.medium12,
            color = ChallengeTheme.colorScheme.text.secondary,
        )

        VerticalSpacer(6.dp)

        ChallengeInputField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            enabled = enabled,
            readOnly = readOnly,
            singleLine = singleLine,
            type = type,
            imeAction = imeAction,
            keyboardActions = keyboardActions,
            focusRequester = focusRequester,
        )

        if (counterText != null) {
            VerticalSpacer(6.dp)

            Text(
                text = counterText,
                style = ChallengeTheme.typography.medium12,
                color = if (counterAtMax) {
                    ChallengeTheme.colorScheme.warning
                } else {
                    ChallengeTheme.colorScheme.text.secondary
                },
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@ThemePreviews
@Composable
private fun LabeledInputFieldEmptyPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(24.dp),
        ) {
            LabeledInputField(
                label = "나의 미션",
                value = "",
                onValueChange = {},
                placeholder = "예) 오늘 운동 1시간 하기",
                counterText = "0/100",
                imeAction = ImeAction.Next,
            )
        }
    }
}

@ThemePreviews
@Composable
private fun LabeledInputFieldFilledPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(24.dp),
        ) {
            LabeledInputField(
                label = "내기 (진 사람이 해야 할 것)",
                value = "커피 사기",
                onValueChange = {},
                placeholder = "예) 커피 사기",
                counterText = "5/100",
                imeAction = ImeAction.Done,
            )
        }
    }
}

@ThemePreviews
@Composable
private fun LabeledInputFieldAtMaxPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(24.dp),
        ) {
            LabeledInputField(
                label = "나의 미션",
                value = "가".repeat(100),
                onValueChange = {},
                counterText = "100/100",
                counterAtMax = true,
            )
        }
    }
}

@ThemePreviews
@Composable
private fun LabeledInputFieldNoCounterPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(24.dp),
        ) {
            LabeledInputField(
                label = "닉네임",
                value = "우건",
                onValueChange = {},
            )
        }
    }
}
