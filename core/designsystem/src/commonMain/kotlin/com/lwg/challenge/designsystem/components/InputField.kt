package com.lwg.challenge.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * [ChallengeInputField] 의 높이 동작.
 *
 * - [Basic] : 한 줄 높이([ChallengeInputFieldDefaults.MinHeight])에 맞추고 세로 중앙 정렬.
 * - [FixedHeight] : dp 가 아니라 줄 수로 높이를 고정하고(minLines = maxLines) 상단 정렬.
 *   여러 줄 입력 전제라 내부에서 `singleLine` 을 끈다.
 */
@Stable
sealed interface InputFieldType {

    @Immutable
    data object Basic : InputFieldType

    @Immutable
    data class FixedHeight(val lines: Int) : InputFieldType
}

object ChallengeInputFieldDefaults {
    val MinHeight = 48.dp
    val Shape = RoundedCornerShape(12.dp)
    val HorizontalPadding = 16.dp
    val VerticalPadding = 12.dp
    val FocusBorderWidth = 2.dp
    val ContentSpacing = 8.dp
}

/**
 * 공용 텍스트 입력 필드.
 *
 * Material3 `TextField` 는 고정 높이와 indicator 를 강제해 디자인과 맞지 않아
 * [BasicTextField] + decorationBox 로 직접 구성한다.
 *
 * 포커스 상태는 내부에서 관리한다 — 테두리 강조만을 위해 호출부가 상태를 들고 있을 이유가 없다.
 * 값 갱신 이외의 포커스 반응이 필요하면 [onFocusChanged] 로 받는다.
 *
 * @param readOnly 입력만 막고 포커스·커서는 유지한다(제출 in-flight 등).
 *   포커스 자체를 막으려면 [enabled] 를 쓴다.
 */
@Composable
fun ChallengeInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    type: InputFieldType = InputFieldType.Basic,
    textStyle: TextStyle = ChallengeTheme.typography.medium14,
    placeholderStyle: TextStyle = ChallengeTheme.typography.medium14,
    imeAction: ImeAction = ImeAction.Default,
    keyboardOptions: KeyboardOptions = KeyboardOptions(imeAction = imeAction),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    focusRequester: FocusRequester? = null,
    onFocusChanged: (Boolean) -> Unit = {},
    startContent: @Composable (() -> Unit)? = null,
    endContent: @Composable (() -> Unit)? = null,
) {
    var focused by remember { mutableStateOf(false) }

    val (minLines, maxLines) = when (type) {
        InputFieldType.Basic -> 1 to if (singleLine) 1 else Int.MAX_VALUE
        is InputFieldType.FixedHeight -> type.lines to type.lines
    }
    val alignTop = type is InputFieldType.FixedHeight

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .then(focusRequester?.let { Modifier.focusRequester(it) } ?: Modifier)
            .onFocusChanged {
                focused = it.isFocused
                onFocusChanged(it.isFocused)
            },
        enabled = enabled,
        readOnly = readOnly,
        singleLine = type is InputFieldType.Basic && singleLine,
        minLines = minLines,
        maxLines = maxLines,
        textStyle = textStyle.copy(
            color = if (enabled) {
                ChallengeTheme.colorScheme.onBackground
            } else {
                ChallengeTheme.colorScheme.onSurfaceVariant
            },
        ),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        cursorBrush = SolidColor(ChallengeTheme.colorScheme.primary),
        decorationBox = { innerTextField ->
            InputFieldBox(focused = focused) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = if (alignTop) Alignment.Top else Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(ChallengeInputFieldDefaults.ContentSpacing),
                ) {
                    startContent?.invoke()

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = if (alignTop) Alignment.TopStart else Alignment.CenterStart,
                    ) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = placeholderStyle,
                                color = ChallengeTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        innerTextField()
                    }

                    endContent?.invoke()
                }
            }
        },
    )
}

/**
 * 입력 필드 외곽 박스. 편집 불가 입력(선택형·클릭형)도 같은 시각을 갖도록 분리해 둔다.
 */
@Composable
fun InputFieldBox(
    modifier: Modifier = Modifier,
    focused: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = ChallengeInputFieldDefaults.MinHeight)
            .clip(ChallengeInputFieldDefaults.Shape)
            .background(
                color = ChallengeTheme.colorScheme.secondary,
                shape = ChallengeInputFieldDefaults.Shape,
            )
            .then(
                if (focused) {
                    Modifier.border(
                        width = ChallengeInputFieldDefaults.FocusBorderWidth,
                        color = ChallengeTheme.colorScheme.primary,
                        shape = ChallengeInputFieldDefaults.Shape,
                    )
                } else {
                    Modifier
                },
            )
            .padding(
                horizontal = ChallengeInputFieldDefaults.HorizontalPadding,
                vertical = ChallengeInputFieldDefaults.VerticalPadding,
            ),
        contentAlignment = Alignment.CenterStart,
        content = content,
    )
}

@Preview
@Composable
private fun ChallengeInputFieldEmptyPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            ChallengeInputField(
                value = "",
                onValueChange = {},
                placeholder = "예) 오늘 운동 1시간 하기",
            )
        }
    }
}

@Preview
@Composable
private fun ChallengeInputFieldFilledPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            ChallengeInputField(
                value = "커피 사기",
                onValueChange = {},
                placeholder = "예) 커피 사기",
            )
        }
    }
}

@Preview
@Composable
private fun ChallengeInputFieldFixedHeightPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            ChallengeInputField(
                value = "여러 줄 입력이 필요한 경우 줄 수로 높이를 고정한다.",
                onValueChange = {},
                singleLine = false,
                type = InputFieldType.FixedHeight(lines = 3),
            )
        }
    }
}

@Preview
@Composable
private fun InputFieldBoxFocusedPreview() {
    ChallengeTheme {
        Column(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            InputFieldBox(focused = true) {
                Text(
                    text = "포커스",
                    style = ChallengeTheme.typography.medium14,
                    color = ChallengeTheme.colorScheme.onBackground,
                )
            }
            InputFieldBox {
                Text(
                    text = "기본",
                    style = ChallengeTheme.typography.medium14,
                    color = ChallengeTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
