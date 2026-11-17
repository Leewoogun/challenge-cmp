@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.lwg.challenge.feature.friends.record.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.lwg.challenge.domain.model.friend.FriendChallengeStatus
import com.lwg.challenge.domain.model.friend.FriendOngoingChallenge
import com.lwg.challenge.domain.model.friend.FriendPastChallenge
import com.lwg.challenge.domain.model.friend.FriendRecord
import com.lwg.challenge.domain.model.friend.UserBrief
import com.lwg.challenge.navigation.Route
import com.lwg.challenge.ui.components.RESULT_BOTH_LOSE_TEXT
import com.lwg.challenge.utils.datetime.KST
import com.lwg.challenge.utils.datetime.toRelativeKoreanString
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.number
import kotlinx.datetime.toInstant
import kotlin.time.Duration.Companion.hours

@Stable
sealed interface FriendRecordUiState {

    @Immutable
    data object Loading : FriendRecordUiState

    @Immutable
    data class Error(val message: String) : FriendRecordUiState

    @Immutable
    data class Empty(val friend: UserBrief) : FriendRecordUiState {

        val subtitleText: String get() = "${friend.nickname}에게 첫 도전장을 보내보세요"
    }

    /**
     * @property pastChallenges 지금까지 받은 지난 챌린지 쪽의 누적. 서버 순서 그대로다
     * @property now 결과를 받을 때 잰 기준 시각. 진행 중 카드의 남은 시간이 이 한 시계로 계산된다
     */
    @Immutable
    data class Data(
        val record: FriendRecord,
        val pastChallenges: List<FriendPastChallenge>,
        val isPageEnd: Boolean,
        val now: LocalDateTime,
    ) : FriendRecordUiState {

        val friend: UserBrief get() = record.friend

        val myWinsText: String get() = record.score.myWins.toString()
        val friendWinsText: String get() = record.score.friendWins.toString()

        // 그리는 대상(스코어)과 같은 출처를 본다. 목록이 비었는지와는 다른 축이다.
        val isScoreMuted: Boolean
            get() = with(record.score) { myWins == 0 && friendWins == 0 && draws == 0 && bothLose == 0 }

        /** 0인 항목은 빼고, 둘 다 0이면 null(줄 없음). */
        val scoreSubLineText: String?
            get() = listOfNotNull(
                record.score.draws.takeIf { it > 0 }?.let { "무 $it" },
                record.score.bothLose.takeIf { it > 0 }?.let { "$RESULT_BOTH_LOSE_TEXT $it" },
            ).joinToString(" · ").ifEmpty { null }

        val scoreContentDescription: String
            get() = "나 ${record.score.myWins}승, ${friend.nickname} ${record.score.friendWins}승"

        val hasOngoing: Boolean get() = record.ongoing.isNotEmpty()
        val ongoingCountText: String get() = "${record.ongoing.size}개"

        val isPastEmpty: Boolean get() = pastChallenges.isEmpty()
        val hasNextPage: Boolean get() = !isPageEnd

        fun statusLabelOf(challenge: FriendOngoingChallenge): String = when {
            challenge.status == FriendChallengeStatus.IN_PROGRESS -> "진행 중"
            challenge.sentByMe -> "보낸 도전장"
            else -> "받은 도전장"
        }

        fun deadlineTextOf(challenge: FriendOngoingChallenge): String =
            challenge.deadline.toRelativeKoreanString(now)

        /** 마감 1시간 이하면 강조 색으로 바꾼다. */
        fun isUrgent(challenge: FriendOngoingChallenge): Boolean =
            (challenge.deadline.toInstant(KST) - now.toInstant(KST)) <= 1.hours

        // 받은 도전장은 수락+서명이 한 요청이라 맹세 화면을 거쳐야 시작된다. 방향은 미션 null 이 아니라 sentByMe 로 가른다.
        fun routeOf(challenge: FriendOngoingChallenge): Route =
            if (challenge.status == FriendChallengeStatus.PENDING && !challenge.sentByMe) {
                Route.Challenge.Oath(challenge.challengeId)
            } else {
                Route.Challenge.Detail(challenge.challengeId)
            }

        fun routeOf(challenge: FriendPastChallenge): Route = Route.Challenge.Detail(challenge.challengeId)

        fun dateTextOf(challenge: FriendPastChallenge): String =
            "${challenge.challengeDate.month.number}월 ${challenge.challengeDate.day}일"
    }
}
