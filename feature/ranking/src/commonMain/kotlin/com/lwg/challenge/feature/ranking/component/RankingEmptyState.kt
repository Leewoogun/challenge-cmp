package com.lwg.challenge.feature.ranking.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.preview.ThemePreviews
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.ui.components.EmptyStateCard

/**
 * 줄 세울 사람이 모자랄 때(참가자 3명 미만) 명단 아래에 붙는 카드.
 *
 * 보조 CTA(카카오톡 초대)는 노출하지 않는다 — 랭킹에서 필요한 행동은 친구 추가 하나다.
 */
@Composable
internal fun RankingEmptyState(
    onClickAddFriend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    EmptyStateCard(
        title = "아직 줄 세울 친구가 없어요",
        subtitle = "친구를 등록하면 누가 개돼지왕인지 가려져요",
        ctaLabel = "친구 추가하기",
        icon = Icons.Filled.Group,
        onClickCta = onClickAddFriend,
        modifier = modifier,
    )
}

@ThemePreviews
@Composable
private fun RankingEmptyStatePreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.screen)
                .padding(20.dp),
        ) {
            RankingEmptyState(onClickAddFriend = {})
        }
    }
}
