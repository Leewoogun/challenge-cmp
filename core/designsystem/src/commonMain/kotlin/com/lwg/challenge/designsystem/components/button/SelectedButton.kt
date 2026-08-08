package com.lwg.challenge.designsystem.components.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

object SelectedButtonDefaults {
    val MinHeight = 60.dp
    val Shape = RoundedCornerShape(12.dp)
    val ContentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
    val IconSize = 14.dp
    val IconSpacing = 6.dp
    val TextSpacing = 2.dp

    /** 선택 시 배경 — primary 를 그대로 깔면 텍스트가 묻혀 옅게 깐다. */
    const val SelectedContainerAlpha = 0.10f
    const val SelectedSupportingAlpha = 0.80f
    const val UnselectedSupportingAlpha = 0.70f
}

/**
 * 선택 상태를 가지는 버튼.
 */
@Composable
fun SelectedButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true,
    role: Role = Role.RadioButton,
    contentPadding: PaddingValues = SelectedButtonDefaults.ContentPadding,
) {
    val primary = ChallengeTheme.colorScheme.primary
    val contentColor = if (selected) primary else ChallengeTheme.colorScheme.onSurfaceVariant
    val supportingColor = if (selected) {
        primary.copy(alpha = SelectedButtonDefaults.SelectedSupportingAlpha)
    } else {
        ChallengeTheme.colorScheme.onSurfaceVariant.copy(
            alpha = SelectedButtonDefaults.UnselectedSupportingAlpha,
        )
    }

    Box(
        modifier = modifier
            .heightIn(min = SelectedButtonDefaults.MinHeight)
            .clip(SelectedButtonDefaults.Shape)
            .background(
                if (selected) {
                    primary.copy(alpha = SelectedButtonDefaults.SelectedContainerAlpha)
                } else {
                    ChallengeTheme.colorScheme.secondary
                },
            )
            .then(
                if (selected) {
                    Modifier.border(
                        border = BorderStroke(1.dp, primary),
                        shape = SelectedButtonDefaults.Shape,
                    )
                } else {
                    Modifier
                },
            )
            .clickable(enabled = enabled, onClick = onClick)
            .semantics {
                this.role = role
                this.selected = selected
            }
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SelectedButtonDefaults.TextSpacing),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SelectedButtonDefaults.IconSpacing),
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(SelectedButtonDefaults.IconSize),
                        tint = contentColor,
                    )
                }

                Text(
                    text = text,
                    style = ChallengeTheme.typography.medium14,
                    color = contentColor,
                )
            }

            if (supportingText != null) {
                Text(
                    text = supportingText,
                    style = ChallengeTheme.typography.medium12,
                    color = supportingColor,
                )
            }
        }
    }
}

@Preview
@Composable
private fun SelectedButtonSelectedPreview() {
    ChallengeTheme {
        Row(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SelectedButton(
                text = "오늘",
                supportingText = "7/28 24:00",
                leadingIcon = Icons.Filled.Schedule,
                selected = true,
                onClick = {},
                modifier = Modifier.weight(1f),
            )
            SelectedButton(
                text = "내일",
                supportingText = "7/29 24:00",
                leadingIcon = Icons.Filled.Schedule,
                selected = false,
                onClick = {},
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Preview
@Composable
private fun SelectedButtonTextOnlyPreview() {
    ChallengeTheme {
        Row(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SelectedButton(
                text = "전체",
                selected = true,
                onClick = {},
                modifier = Modifier.weight(1f),
            )
            SelectedButton(
                text = "진행 중",
                selected = false,
                onClick = {},
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Preview
@Composable
private fun SelectedButtonDisabledPreview() {
    ChallengeTheme {
        Row(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SelectedButton(
                text = "오늘",
                supportingText = "7/28 24:00",
                leadingIcon = Icons.Filled.Schedule,
                selected = true,
                enabled = false,
                onClick = {},
                modifier = Modifier.weight(1f),
            )
        }
    }
}
