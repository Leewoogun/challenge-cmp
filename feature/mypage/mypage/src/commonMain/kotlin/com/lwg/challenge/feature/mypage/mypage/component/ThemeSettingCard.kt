package com.lwg.challenge.feature.mypage.mypage.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.button.SelectedButton
import com.lwg.challenge.designsystem.preview.ThemePreviews
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.ThemeMode

/** 화면 테마(다크 / 라이트) 선택 카드. */
@Composable
internal fun ThemeSettingCard(
    isDarkSelected: Boolean,
    isLightSelected: Boolean,
    onThemeModeSelected: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ChallengeTheme.colorScheme.background.card,
        border = BorderStroke(width = 1.dp, color = ChallengeTheme.colorScheme.line.border),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(ChallengeTheme.colorScheme.background.field),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Palette,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = ChallengeTheme.colorScheme.icon.point,
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "화면 테마",
                        style = ChallengeTheme.typography.medium14,
                        color = ChallengeTheme.colorScheme.text.primary,
                    )
                    Text(
                        text = "앱 화면 색을 골라요",
                        style = ChallengeTheme.typography.medium10,
                        color = ChallengeTheme.colorScheme.text.secondary,
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SelectedButton(
                    text = "다크",
                    selected = isDarkSelected,
                    onClick = { onThemeModeSelected(ThemeMode.DARK) },
                    leadingIcon = Icons.Filled.DarkMode,
                    modifier = Modifier.weight(1f),
                )
                SelectedButton(
                    text = "라이트",
                    selected = isLightSelected,
                    onClick = { onThemeModeSelected(ThemeMode.LIGHT) },
                    leadingIcon = Icons.Filled.LightMode,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@ThemePreviews
@Composable
private fun ThemeSettingCardPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(20.dp),
        ) {
            ThemeSettingCard(isDarkSelected = true, isLightSelected = false, onThemeModeSelected = {})
        }
    }
}
