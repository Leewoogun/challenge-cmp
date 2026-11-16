package com.lwg.challenge.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupPositionProvider
import com.lwg.challenge.designsystem.preview.ThemePreviews
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import kotlinx.coroutines.launch

/**
 * 툴팁 트리거 아이콘 종류.
 *
 * @property icon 표시할 아이콘
 * @property defaultContentDescription 호출부가 지정하지 않을 때 쓰는 접근성 설명
 */
enum class ToolTipType(
    val icon: ImageVector,
    val defaultContentDescription: String,
) {
    QUESTION(Icons.AutoMirrored.Outlined.HelpOutline, "도움말 보기"),
    INFO(Icons.Outlined.Info, "안내 보기"),
}

/** 앵커 기준 말풍선 위치. Material3 [TooltipAnchorPosition] 이 experimental 이라 호출부 opt-in 전파를 막으려 감싼다. */
enum class ToolTipPosition {
    ABOVE,
    BELOW,
}

enum class ToolTipCaretDirection {
    TOP,
    BOTTOM,
}

private val TOOLTIP_CARET_WIDTH = 12.dp
private val TOOLTIP_CORNER_RADIUS = 8.dp
private val TOOLTIP_SCREEN_MARGIN = 16.dp
private val TOOLTIP_ICON_SIZE = 16.dp

/**
 * 아이콘을 누르면 말풍선을 띄우는 툴팁. 말풍선은 화면 좌우 [TOOLTIP_SCREEN_MARGIN] 안으로 밀어 넣고
 * caret 은 아이콘 중심을 가리킨다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeToolTip(
    text: String,
    modifier: Modifier = Modifier,
    title: String? = null,
    toolTipType: ToolTipType = ToolTipType.QUESTION,
    contentDescription: String = toolTipType.defaultContentDescription,
    backgroundColor: Color = ChallengeTheme.colorScheme.point,
    textColor: Color = ChallengeTheme.colorScheme.text.onPoint,
    balloonLayoutAlignment: Alignment = Alignment.Center,
    position: ToolTipPosition = ToolTipPosition.BELOW,
) {
    val tooltipState = rememberTooltipState(isPersistent = true)
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val windowInfo = LocalWindowInfo.current
    val marginPx = with(density) { TOOLTIP_SCREEN_MARGIN.roundToPx() }
    val caretWidthPx = with(density) { TOOLTIP_CARET_WIDTH.toPx() }
    val cornerRadiusPx = with(density) { TOOLTIP_CORNER_RADIUS.toPx() }

    val baseProvider = TooltipDefaults.rememberTooltipPositionProvider(
        when (position) {
            ToolTipPosition.ABOVE -> TooltipAnchorPosition.Above
            ToolTipPosition.BELOW -> TooltipAnchorPosition.Below
        },
    )
    val caretCenterXState = remember { mutableStateOf<Float?>(null) }
    val tooltipWidthState = remember { mutableIntStateOf(0) }
    val positionProvider = remember(baseProvider, marginPx) {
        object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize,
            ): IntOffset {
                val base = baseProvider.calculatePosition(
                    anchorBounds,
                    windowSize,
                    layoutDirection,
                    popupContentSize,
                )
                val maxX = (windowSize.width - popupContentSize.width - marginPx)
                    .coerceAtLeast(marginPx)
                val x = base.x.coerceIn(marginPx, maxX)
                caretCenterXState.value = anchorBounds.center.x - x.toFloat()
                tooltipWidthState.intValue = popupContentSize.width
                return IntOffset(x, base.y)
            }
        }
    }

    TooltipBox(
        positionProvider = positionProvider,
        tooltip = {
            val caretCenterX = caretCenterXState.value
            val tooltipWidthPx = tooltipWidthState.intValue
            val caretStartPadding = if (caretCenterX != null && tooltipWidthPx > 0) {
                val maxStart = (tooltipWidthPx - caretWidthPx - cornerRadiusPx)
                    .coerceAtLeast(cornerRadiusPx)
                val start = (caretCenterX - caretWidthPx / 2f).coerceIn(cornerRadiusPx, maxStart)
                with(density) { start.toDp() }
            } else {
                TOOLTIP_CORNER_RADIUS
            }

            ChallengeToolTipBox(
                text = text,
                title = title,
                backgroundColor = backgroundColor,
                textColor = textColor,
                caretDirection = when (position) {
                    ToolTipPosition.ABOVE -> ToolTipCaretDirection.BOTTOM
                    ToolTipPosition.BELOW -> ToolTipCaretDirection.TOP
                },
                caretAlignment = Alignment.Start,
                startPadding = caretStartPadding,
                modifier = Modifier.widthIn(
                    max = with(density) { windowInfo.containerSize.width.toDp() } - TOOLTIP_SCREEN_MARGIN * 2,
                ),
            )
        },
        state = tooltipState,
    ) {
        Box(
            modifier = modifier,
            contentAlignment = balloonLayoutAlignment,
        ) {
            Icon(
                imageVector = toolTipType.icon,
                contentDescription = contentDescription,
                tint = ChallengeTheme.colorScheme.icon.secondary,
                modifier = Modifier
                    .size(TOOLTIP_ICON_SIZE)
                    .clip(CircleShape)
                    .clickable(
                        onClickLabel = contentDescription,
                        onClick = { scope.launch { tooltipState.show() } },
                    ),
            )
        }
    }
}

/** 툴팁 말풍선. [ChallengeToolTip] 밖에서 고정 안내로도 쓸 수 있게 분리해 둔다. */
@Composable
fun ChallengeToolTipBox(
    text: String,
    modifier: Modifier = Modifier,
    title: String? = null,
    backgroundColor: Color = ChallengeTheme.colorScheme.point,
    textColor: Color = ChallengeTheme.colorScheme.text.onPoint,
    startPadding: Dp = 8.dp,
    caretDirection: ToolTipCaretDirection = ToolTipCaretDirection.TOP,
    caretAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
) {
    val caretModifier = when (caretAlignment) {
        Alignment.Start -> Modifier.padding(start = startPadding)
        Alignment.End -> Modifier.padding(end = 8.dp)
        else -> Modifier
    }
    Column(
        modifier = modifier,
        horizontalAlignment = caretAlignment,
    ) {
        if (caretDirection == ToolTipCaretDirection.TOP) {
            ToolTipCaret(pointingUp = true, color = backgroundColor, modifier = caretModifier)
        }
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(TOOLTIP_CORNER_RADIUS))
                .background(backgroundColor)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (title != null) {
                Text(
                    text = title,
                    style = ChallengeTheme.typography.bold14,
                    color = textColor,
                )
            }
            Text(
                text = text,
                style = ChallengeTheme.typography.medium12,
                color = textColor,
            )
        }
        if (caretDirection == ToolTipCaretDirection.BOTTOM) {
            ToolTipCaret(pointingUp = false, color = backgroundColor, modifier = caretModifier)
        }
    }
}

@Composable
private fun ToolTipCaret(
    pointingUp: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    width: Dp = TOOLTIP_CARET_WIDTH,
    height: Dp = 6.dp,
) {
    Canvas(modifier = modifier.size(width, height)) {
        val path = Path().apply {
            if (pointingUp) {
                moveTo(size.width / 2f, 0f)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
            } else {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width / 2f, size.height)
            }
            close()
        }
        drawPath(path = path, color = color)
    }
}

@ThemePreviews
@Composable
private fun ChallengeToolTipBoxWithTitlePreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(24.dp),
        ) {
            ChallengeToolTipBox(
                title = "미션은 이렇게 적어줘",
                text = "사진으로 인증할 수 있는 미션을 적어줘",
            )
        }
    }
}

@ThemePreviews
@Composable
private fun ChallengeToolTipBoxPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(24.dp),
        ) {
            ChallengeToolTipBox(
                text = "사진으로 인증할 수 있는 미션을 적어줘",
                caretDirection = ToolTipCaretDirection.BOTTOM,
            )
        }
    }
}

@ThemePreviews
@Composable
private fun ChallengeToolTipPreview() {
    ChallengeTheme {
        Column(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(24.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "나의 미션",
                    style = ChallengeTheme.typography.medium12,
                    color = ChallengeTheme.colorScheme.text.secondary,
                )
                ChallengeToolTip(text = "사진으로 인증할 수 있는 미션을 적어줘")
                ChallengeToolTip(text = "안내 문구", toolTipType = ToolTipType.INFO)
            }
            // 팝업은 탭해야 뜨므로 정적 Preview 에서는 말풍선을 아래에 함께 그려 위치감을 보여준다.
            ChallengeToolTipBox(
                text = "사진으로 인증할 수 있는 미션을 적어줘",
                caretAlignment = Alignment.Start,
                startPadding = 50.dp,
            )
        }
    }
}
