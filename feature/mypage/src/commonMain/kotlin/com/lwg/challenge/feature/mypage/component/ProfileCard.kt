package com.lwg.challenge.feature.mypage.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.UserRecord
import com.lwg.challenge.ui.components.ProfileImage
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 마이페이지 프로필 카드. design.md §1.2.
 *
 * - 아바타 radius 는 20dp — 랭킹 아바타(16dp)와 다르다 (§1.2.1)
 * - 승/패/무 색은 앱의 승패무 단일 출처(`challengeOutcomeVisualOf`)와 같은 축이다 (§1.2.4)
 *
 * @param record `null` 이면 전적 조회 실패다. 승/패/무 행을 아예 그리지 않고 [summary] 만 남긴다 —
 *   모르는 값을 0 으로 그리면 muted 여도 사실 주장이 되어 "이 0 은 진짜 0" 이라고 거짓말한다 (§1.7.3).
 * @param summary 캡션 문구. 분기 판정은 State 책임이다 (§1.2.5 · §1.7.3).
 * @param muted 전적이 없는 신규 사용자. 승/패/무 3개를 전부 muted 톤으로 내린다.
 */
@Composable
internal fun ProfileCard(
    nickname: String,
    profileImageUrl: String?,
    summary: String,
    record: UserRecord?,
    muted: Boolean,
    modifier: Modifier = Modifier,
) {
    val mutedColor = ChallengeTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ChallengeTheme.colorScheme.surface,
        border = BorderStroke(width = 1.dp, color = ChallengeTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ProfileImage(
                imageUrl = profileImageUrl,
                nickname = nickname,
                size = 64.dp,
                shape = RoundedCornerShape(20.dp),
                textStyle = ChallengeTheme.typography.medium24,
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nickname,
                    style = ChallengeTheme.typography.bold18,
                    color = ChallengeTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = summary,
                    style = ChallengeTheme.typography.medium12,
                    color = mutedColor,
                )

                if (record != null) {
                    Row(
                        modifier = Modifier.padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        RecordCount(
                            text = "${record.win}승",
                            color = if (muted) mutedColor else ChallengeTheme.colorScheme.primary,
                        )
                        RecordCount(
                            text = "${record.lose}패",
                            color = if (muted) mutedColor else ChallengeTheme.colorScheme.error,
                        )
                        RecordCount(
                            text = "${record.draw}무",
                            color = if (muted) mutedColor else ChallengeTheme.colorScheme.onBackground,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordCount(
    text: String,
    color: Color,
) {
    Text(
        text = text,
        style = ChallengeTheme.typography.medium12,
        color = color,
    )
}

@Preview
@Composable
private fun ProfileCardEmptyRecordPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(20.dp),
        ) {
            ProfileCard(
                nickname = "이우건",
                profileImageUrl = null,
                summary = "아직 기록이 없어요",
                record = UserRecord.Empty,
                muted = true,
            )
        }
    }
}

@Preview
@Composable
private fun ProfileCardRecordFailedPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(20.dp),
        ) {
            ProfileCard(
                nickname = "이우건",
                profileImageUrl = null,
                summary = "전적을 불러오지 못했어요",
                record = null,
                muted = false,
            )
        }
    }
}

@Preview
@Composable
private fun ProfileCardAllDrawPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(20.dp),
        ) {
            ProfileCard(
                nickname = "이우건",
                profileImageUrl = null,
                summary = "총 3챌린지 · 승률 0%",
                record = UserRecord(win = 0, lose = 0, draw = 3, currentStreak = 0),
                muted = false,
            )
        }
    }
}

@Preview
@Composable
private fun ProfileCardPreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(20.dp),
        ) {
            ProfileCard(
                nickname = "이우건",
                profileImageUrl = null,
                summary = "총 12챌린지 · 승률 58%",
                record = UserRecord(win = 7, lose = 3, draw = 2, currentStreak = 3),
                muted = false,
            )
        }
    }
}

@Preview
@Composable
private fun ProfileCardLongNicknamePreview() {
    ChallengeTheme {
        Box(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(20.dp),
        ) {
            ProfileCard(
                nickname = "오늘도지지않는불꽃같은도전자이우건입니다",
                profileImageUrl = null,
                summary = "총 12챌린지 · 승률 58%",
                record = UserRecord(win = 7, lose = 3, draw = 2, currentStreak = 3),
                muted = false,
            )
        }
    }
}
