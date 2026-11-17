package com.lwg.challenge.feature.friends.record.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.components.VerticalSpacer
import com.lwg.challenge.designsystem.components.button.IconTextButton
import com.lwg.challenge.designsystem.components.button.IconTextButtonStyle
import com.lwg.challenge.designsystem.preview.ThemePreviews
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.friend.FriendScore
import com.lwg.challenge.feature.friends.record.contract.FriendRecordUiState
import com.lwg.challenge.feature.friends.record.friendRecordPreviewData

/**
 * 스코어 행 + 보조 줄 + 재대결 CTA.
 * 친구 승은 내 패의 일부일 뿐이라 `error` 로 칠하지 않는다 — 빨간 숫자는 "N패" 로 읽힌다.
 *
 * @param subLineText null 이면 보조 줄을 그리지 않는다.
 */
@Composable
internal fun FriendRecordScoreBoard(
    myWinsText: String,
    friendWinsText: String,
    friendNickname: String,
    isMuted: Boolean,
    subLineText: String?,
    contentDescription: String,
    onSendChallengeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) { this.contentDescription = contentDescription },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ScoreCell(
                value = myWinsText,
                label = "나",
                color = if (isMuted) ChallengeTheme.colorScheme.text.secondary else ChallengeTheme.colorScheme.text.point,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = ":",
                style = ChallengeTheme.typography.bold30,
                color = ChallengeTheme.colorScheme.text.secondary,
            )
            ScoreCell(
                value = friendWinsText,
                label = friendNickname,
                color = if (isMuted) ChallengeTheme.colorScheme.text.secondary else ChallengeTheme.colorScheme.text.primary,
                modifier = Modifier.weight(1f),
            )
        }

        if (subLineText != null) {
            VerticalSpacer(12.dp)
            Text(
                text = subLineText,
                style = ChallengeTheme.typography.medium12,
                color = ChallengeTheme.colorScheme.text.secondary,
            )
        }

        VerticalSpacer(16.dp)

        IconTextButton(
            text = "도전장 보내기",
            icon = Icons.Filled.SportsKabaddi,
            onClick = onSendChallengeClick,
            modifier = Modifier.fillMaxWidth(),
            style = IconTextButtonStyle.Outlined,
        )
    }
}

@Composable
private fun ScoreCell(
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = ChallengeTheme.typography.bold48.copy(fontFeatureSettings = "tnum"),
            color = color,
        )
        Text(
            text = label,
            style = ChallengeTheme.typography.medium12,
            color = ChallengeTheme.colorScheme.text.secondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** 무·양측 패배 둘 다 있음 — `무 1 · 양측 패배 1`. */
@ThemePreviews
@Composable
private fun FriendRecordScoreBoardBothSubPreview() {
    FriendRecordScoreBoardPreview(friendRecordPreviewData(score = FriendScore(2, 1, 1, 1)))
}

/** 무만 — `무 2`. */
@ThemePreviews
@Composable
private fun FriendRecordScoreBoardDrawOnlyPreview() {
    FriendRecordScoreBoardPreview(friendRecordPreviewData(score = FriendScore(3, 1, 2, 0)))
}

/** 양측 패배만 — `양측 패배 3`. */
@ThemePreviews
@Composable
private fun FriendRecordScoreBoardBothLoseOnlyPreview() {
    FriendRecordScoreBoardPreview(friendRecordPreviewData(score = FriendScore(0, 2, 0, 3)))
}

/** 무·양측 패배 0 — 보조 줄 없이 CTA 가 스코어 행 바로 아래 붙는다. */
@ThemePreviews
@Composable
private fun FriendRecordScoreBoardNoSubPreview() {
    FriendRecordScoreBoardPreview(friendRecordPreviewData(score = FriendScore(4, 2, 0, 0)))
}

/** 끝낸 판 0 + 진행 중만 — `0 : 0` 이 점수처럼 보이지 않게 흐리게. */
@ThemePreviews
@Composable
private fun FriendRecordScoreBoardMutedPreview() {
    FriendRecordScoreBoardPreview(friendRecordPreviewData(score = FriendScore(0, 0, 0, 0), pastChallenges = emptyList()))
}

/** 긴 닉네임은 라벨만 줄이고 콜론은 가운데에 남는다. */
@ThemePreviews
@Composable
private fun FriendRecordScoreBoardLongNicknamePreview() {
    ChallengeTheme {
        Column(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.card)
                .padding(20.dp),
        ) {
            FriendRecordScoreBoard(
                myWinsText = "12",
                friendWinsText = "9",
                friendNickname = "오늘도지지않는불꽃같은도전자김준혁입니다",
                isMuted = false,
                subLineText = null,
                contentDescription = "",
                onSendChallengeClick = {},
            )
        }
    }
}

@Composable
private fun FriendRecordScoreBoardPreview(data: FriendRecordUiState.Data) {
    ChallengeTheme {
        Column(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background.card)
                .padding(20.dp),
        ) {
            FriendRecordScoreBoard(
                myWinsText = data.myWinsText,
                friendWinsText = data.friendWinsText,
                friendNickname = data.friend.nickname,
                isMuted = data.isScoreMuted,
                subLineText = data.scoreSubLineText,
                contentDescription = data.scoreContentDescription,
                onSendChallengeClick = {},
            )
        }
    }
}
