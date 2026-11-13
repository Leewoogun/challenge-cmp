package com.lwg.challenge.feature.mypage.archive.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.challenge.ChallengeCardInfo
import com.lwg.challenge.domain.model.challenge.ChallengeHistory
import com.lwg.challenge.feature.mypage.archive.PreviewArchiveHistories
import com.lwg.challenge.ui.components.card.ChallengeCard
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun ArchiveChallengeList(
    challenges: List<ChallengeHistory>,
    onChallengeClick: (Long) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
    ) {
        items(items = challenges, key = { it.challengeId }) { history ->
            ChallengeCard(
                cardInfo = ChallengeCardInfo(history),
                onClick = { onChallengeClick(history.challengeId) },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
            )
        }
    }
}

@Preview
@Composable
private fun ArchiveChallengeListPreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background.screen)) {
            ArchiveChallengeList(challenges = PreviewArchiveHistories, onChallengeClick = {})
        }
    }
}
