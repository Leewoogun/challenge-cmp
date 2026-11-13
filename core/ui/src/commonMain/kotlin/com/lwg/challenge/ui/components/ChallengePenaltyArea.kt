package com.lwg.challenge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.preview.ThemePreviews
import com.lwg.challenge.designsystem.theme.ChallengeTheme

@Composable
fun ChallengePenaltyArea(
    text: String,
    modifier: Modifier = Modifier,
) {
    val primary = ChallengeTheme.colorScheme.point
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(color = primary.copy(alpha = 0.05f))
            .border(
                width = 1.dp,
                color = primary.copy(alpha = 0.10f),
                shape = RoundedCornerShape(10.dp),
            )
            .padding(PaddingValues(horizontal = 12.dp, vertical = 8.dp)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.LocalFireDepartment,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = primary,
        )
        Text(
            text = text,
            style = ChallengeTheme.typography.medium12,
            color = primary,
        )
    }
}

@ThemePreviews
@Composable
private fun BetStripPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(24.dp),
        ) {
            ChallengePenaltyArea(text = "내기: 커피 사기 ☕")
        }
    }
}
