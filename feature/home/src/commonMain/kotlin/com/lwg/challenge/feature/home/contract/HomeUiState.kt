package com.lwg.challenge.feature.home.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.lwg.challenge.domain.model.ActiveChallenge
import com.lwg.challenge.domain.model.ActiveChallengeStatus
import com.lwg.challenge.domain.model.UserRecord
import com.lwg.challenge.domain.model.user.UserInfo
import com.lwg.challenge.utils.datetime.toRelativeKoreanString
import com.lwg.challenge.utils.isDebug
import kotlinx.datetime.LocalDateTime

@Stable
sealed interface HomeUiState {

    @Immutable
    data object Loading : HomeUiState

    @Immutable
    data class Data(
        val record: UserRecord,
        val challenges: List<ActiveChallenge>,
        val now: LocalDateTime,
        val userInfo: UserInfo? = null,
        val receivedChallenges: List<ReceivedChallengeItemState> = emptyList(),
        val unreadCount: Int = 0,
        val inFlightIds: Set<Long> = emptySet(),
    ) : HomeUiState {

        val inProgressChallenges: List<ActiveChallenge> =
            challenges.filter { it.status == ActiveChallengeStatus.IN_PROGRESS }

        val completedChallenges: List<ActiveChallenge> =
            challenges.filter { it.status == ActiveChallengeStatus.COMPLETED }

        val isInProgressEmpty: Boolean get() = inProgressChallenges.isEmpty()

        val hasUnreadNotification: Boolean get() = unreadCount > 0

        val isRecordMuted: Boolean get() = record.isEmpty
        val streakText: String
            get() = if (record.currentStreak > 0) "${record.currentStreak}🔥" else "0"

        fun deadlineTextOf(challenge: ActiveChallenge): String =
            challenge.deadline.toRelativeKoreanString(now)

        fun isActionable(item: ReceivedChallengeItemState): Boolean =
            item.challengeId !in inFlightIds

        /** debug 빌드에서만 노출하는 현재 로그인 계정 표기. release 에서는 null. */
        val debugAccountLabel: String? get() = if (isDebug) userInfo?.nickname else null
    }
}
