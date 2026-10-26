package com.lwg.challenge.ui.components.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lwg.challenge.designsystem.theme.ChallengeTheme
import com.lwg.challenge.domain.model.VerificationStatus
import com.lwg.challenge.domain.model.challenge.ChallengeCardInfo
import com.lwg.challenge.domain.model.challenge.ChallengeOutcome
import com.lwg.challenge.ui.components.ChallengePenaltyArea
import com.lwg.challenge.ui.components.card.component.ChallengeCardHeader
import com.lwg.challenge.ui.components.card.component.ChallengeCardMyMissionRow
import com.lwg.challenge.ui.components.card.component.ChallengeCardOpponentMissionRow
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 홈·보관함의 챌린지 카드.
 *
 * @param deadlineTimeText 진행 중 카드의 남은 시간. 현재 시각으로 계산하는 값이라 [cardInfo] 밖에서 받는다.
 */
@Composable
fun ChallengeCard(
    cardInfo: ChallengeCardInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    deadlineTimeText: String? = null,
) {
    ChallengeCardContainer(
        cardInfo = cardInfo,
        onClick = onClick,
        modifier = modifier,
    ) {
        ChallengeCardHeader(deadlineTimeText = deadlineTimeText)
        ChallengeCardMyMissionRow()
        ChallengeCardOpponentMissionRow()
        ChallengePenaltyArea(text = "내기: ${cardInfo.penalty}")
    }
}

/** 홈 진행 중 — 남은 시간과 미션별 인증 상태. */
@Preview
@Composable
private fun ChallengeCardInProgressPreview() {
    ChallengeCardPreviewContainer {
        ChallengeCard(
            cardInfo = previewCardInfo(
                myVerification = VerificationStatus.VERIFIED,
                opponentVerification = VerificationStatus.PENDING,
            ),
            deadlineTimeText = "3시간 남음",
            onClick = {},
        )
        ChallengeCard(
            cardInfo = previewCardInfo(
                opponentNickname = "민지",
                myMission = "물 2L 마시기",
                opponentMission = "10000보 걷기",
                penalty = "점심 사기 🍚",
                myVerification = VerificationStatus.FAILED,
                opponentVerification = VerificationStatus.VERIFIED,
            ),
            deadlineTimeText = "곧 마감",
            onClick = {},
        )
    }
}

/** 홈 최근 결과 — 남은 시간 자리에 결과가 온다. */
@Preview
@Composable
private fun ChallengeCardCompletedPreview() {
    ChallengeCardPreviewContainer {
        ChallengeOutcome.entries.forEach { outcome ->
            ChallengeCard(
                cardInfo = previewCardInfo(
                    outcome = outcome,
                    myVerification = VerificationStatus.VERIFIED,
                    opponentVerification = VerificationStatus.FAILED,
                ),
                onClick = {},
            )
        }
    }
}

/** 보관함 — 날짜와 결과만 있고 인증 상태는 없다. */
@Preview
@Composable
private fun ChallengeCardArchivePreview() {
    ChallengeCardPreviewContainer {
        ChallengeCard(
            cardInfo = previewCardInfo(outcome = ChallengeOutcome.WIN, challengeDate = LocalDate(2026, 8, 14)),
            onClick = {},
        )
        // 탈퇴한 상대는 뱃지·톤다운 없이 닉네임만 "탈퇴한 사용자" 로 읽힌다.
        ChallengeCard(
            cardInfo = previewCardInfo(
                opponentNickname = "탈퇴한 사용자",
                outcome = ChallengeOutcome.LOSE,
                challengeDate = LocalDate(2026, 6, 12),
            ),
            onClick = {},
        )
    }
}

/** 긴 닉네임만 말줄임되고 날짜·결과는 밀리지 않는다. */
@Preview
@Composable
private fun ChallengeCardLongTextPreview() {
    ChallengeCardPreviewContainer {
        ChallengeCard(
            cardInfo = previewCardInfo(
                opponentNickname = "오늘도지지않는불꽃같은도전자김준혁입니다",
                myMission = "새벽 다섯 시에 일어나서 헬스장 가서 유산소 30분 웨이트 60분 하기",
                opponentMission = "퇴근하고 집에 오는 길에 편의점 들르지 않고 곧장 집으로 가기",
                penalty = "제일 비싼 한우 오마카세 사주기 🥩",
                outcome = ChallengeOutcome.LOSE,
                challengeDate = LocalDate(2026, 8, 31),
            ),
            onClick = {},
        )
    }
}

private fun previewCardInfo(
    opponentNickname: String = "우건",
    myMission: String = "오늘 운동 1시간 하기",
    opponentMission: String = "책 30페이지 읽기",
    penalty: String = "커피 사기 ☕",
    outcome: ChallengeOutcome? = null,
    challengeDate: LocalDate? = null,
    myVerification: VerificationStatus? = null,
    opponentVerification: VerificationStatus? = null,
) = ChallengeCardInfo(
    opponentNickname = opponentNickname,
    myMission = myMission,
    opponentMission = opponentMission,
    penalty = penalty,
    outcome = outcome,
    challengeDate = challengeDate,
    myVerification = myVerification,
    opponentVerification = opponentVerification,
)

@Composable
private fun ChallengeCardPreviewContainer(content: @Composable () -> Unit) {
    ChallengeTheme {
        Column(
            modifier = Modifier
                .background(ChallengeTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            content()
        }
    }
}
