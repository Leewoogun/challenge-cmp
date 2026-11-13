package com.lwg.challenge.feature.mypage.archive.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.VerticalSpacer
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun ArchiveEmptyMonth(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    val primary = ChallengeTheme.colorScheme.point

    // 가운데 정렬하면 월을 넘길 때마다 같은 빈 화면이 다른 높이에 나타나 바 아래 64dp 에 고정한다.
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 64.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(primary.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.EventBusy,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = primary,
            )
        }

        VerticalSpacer(16.dp)

        Text(
            text = title,
            style = ChallengeTheme.typography.bold16,
            color = ChallengeTheme.colorScheme.text.primary,
        )

        VerticalSpacer(4.dp)

        Text(
            text = subtitle,
            style = ChallengeTheme.typography.medium12,
            color = ChallengeTheme.colorScheme.text.secondary,
        )
    }
}

/** A형 — 기록 구간 안의 빈 달. 이 방향으로 더 가면 기록이 있는 달이 나온다. */
@Preview
@Composable
private fun ArchiveEmptyMonthPreview() {
    ArchiveEmptyMonthPreviewContainer {
        ArchiveEmptyMonth(
            title = "8월엔 끝낸 챌린지가 없어요",
            subtitle = "화살표로 다른 달을 볼 수 있어요",
        )
    }
}

/** B형 — 첫 기록보다 이전 달. 더 가도 아무것도 없다는 사실을 부제가 대신 말한다. */
@Preview
@Composable
private fun ArchiveEmptyMonthBeforeFirstRecordPreview() {
    ArchiveEmptyMonthPreviewContainer {
        ArchiveEmptyMonth(
            title = "3월엔 끝낸 챌린지가 없어요",
            subtitle = "첫 기록은 2026년 5월이에요",
        )
    }
}

@Composable
private fun ArchiveEmptyMonthPreviewContainer(content: @Composable () -> Unit) {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background.screen)) {
            content()
        }
    }
}
