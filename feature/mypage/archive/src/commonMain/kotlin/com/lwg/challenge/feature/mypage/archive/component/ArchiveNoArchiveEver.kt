package com.lwg.challenge.feature.mypage.archive.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.preview.ThemePreviews
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.ui.components.EmptyStateCard

/** 종료한 챌린지가 계정 전체에 하나도 없는 상태. */
@Composable
internal fun ArchiveNoArchiveEver(onCreateChallengeClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        EmptyStateCard(
            title = "아직 끝낸 챌린지가 없어요",
            subtitle = "챌린지가 끝나면 계약서가 여기 쌓여요",
            ctaLabel = "챌린지 만들기",
            icon = Icons.Filled.Description,
            onClickCta = onCreateChallengeClick,
            ctaIcon = Icons.Filled.SportsKabaddi,
        )
    }
}

@ThemePreviews
@Composable
private fun ArchiveNoArchiveEverPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background.screen)) {
            ArchiveNoArchiveEver(onCreateChallengeClick = {})
        }
    }
}
