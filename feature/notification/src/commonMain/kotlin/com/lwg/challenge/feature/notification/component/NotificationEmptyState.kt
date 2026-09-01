package com.lwg.challenge.feature.notification.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsNone
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

/**
 * 알림이 0건일 때의 본문. design.md §5.2.
 *
 * `EmptyStateCard` 를 검토했고 기각했다. CTA 가 필수 파라미터인데 **빈 알림함에 놓을 CTA 가 없다** —
 * "챌린지 만들기"·"친구 등록" 은 홈이 이미 갖고 있고, 이 사용자는 방금 그 홈에서 벨을 눌러 왔다.
 * 알림함에서 친구 목록으로 내보내는 것은 확인하러 온 사람을 밖으로 내보내는 것이다.
 * `EmptyStateCard` 에 nullable CTA 를 여는 선택도 하지 않는다 — 출시된 공용 컴포넌트를 두 번째
 * 사용처 하나 때문에 일반화하지 않는다(`ArchiveEmptyMonth` 가 첫 번째).
 *
 * 카드 크롬도 벗었다. `EmptyStateCard` 는 종착지의 어휘인데 **빈 알림함은 사건이 아니라 "아직
 * 아무 일도 안 일어났다" 는 담담한 사실**이다. 일러스트 문법(아이콘 박스·간격·타이포)만 그대로
 * 두어 새 방언이 아니라 같은 어휘의 가벼운 판임을 유지한다.
 *
 * 부제에 *"알림을 켜세요"* 같은 설정 유도를 넣지 않는다 — 알림 권한과 알림 목록은 다른 축이고,
 * row 는 권한 없이도 쌓인다.
 */
@Composable
internal fun NotificationEmptyState(modifier: Modifier = Modifier) {
    val primary = ChallengeTheme.colorScheme.primary

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
            // outline 벨이라야 한다 — filled Notifications 는 모르는 타입 폴백이 쓰므로,
            // 같은 아이콘이면 "알림" 과 "알림 없음" 이 같은 기호가 된다. NotificationsOff 는
            // 끄지 않았는데 꺼진 것처럼 보여 기각했다.
            Icon(
                imageVector = Icons.Filled.NotificationsNone,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = primary,
            )
        }

        VerticalSpacer(16.dp)

        Text(
            text = "아직 알림이 없어요",
            style = ChallengeTheme.typography.bold16,
            color = ChallengeTheme.colorScheme.onBackground,
        )

        VerticalSpacer(4.dp)

        Text(
            text = "챌린지가 오가면 여기에 쌓여요",
            style = ChallengeTheme.typography.medium12,
            color = ChallengeTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview
@Composable
private fun NotificationEmptyStatePreview() {
    ChallengeTheme {
        Box(modifier = Modifier.background(ChallengeTheme.colorScheme.background)) {
            NotificationEmptyState()
        }
    }
}
