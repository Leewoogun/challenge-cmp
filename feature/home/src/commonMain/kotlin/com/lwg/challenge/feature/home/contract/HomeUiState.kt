@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.lwg.challenge.feature.home.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.lwg.challenge.domain.model.ActiveChallenge
import com.lwg.challenge.domain.model.ActiveChallengeStatus
import com.lwg.challenge.domain.model.UserRecord
import com.lwg.challenge.domain.model.challenge.ReceivedChallenge
import com.lwg.challenge.domain.model.user.UserInfo
import com.lwg.challenge.utils.datetime.KST
import com.lwg.challenge.utils.datetime.toRelativeKoreanString
import com.lwg.challenge.utils.isDebug
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toInstant
import kotlin.time.Duration.Companion.hours

@Stable
sealed interface HomeUiState {

    val hasUnreadNotification: Boolean get() = false

    /** debug 빌드에서만 노출하는 현재 로그인 계정 표기. release 에서는 null. */
    val debugAccountLabel: String? get() = null

    @Immutable
    data object Loading : HomeUiState

    @Immutable
    data class Data(
        val record: UserRecord,
        val challenges: List<ActiveChallenge>,
        val now: LocalDateTime,
        val userInfo: UserInfo? = null,
        val receivedChallenges: List<ReceivedChallenge> = emptyList(),
        val unreadCount: Int = 0,
        val inFlightIds: Set<Long> = emptySet(),
    ) : HomeUiState {

        val inProgressChallenges: List<ActiveChallenge> =
            challenges.filter { it.status == ActiveChallengeStatus.IN_PROGRESS }

        val completedChallenges: List<ActiveChallenge> =
            challenges.filter { it.status == ActiveChallengeStatus.COMPLETED }

        val isInProgressEmpty: Boolean get() = inProgressChallenges.isEmpty()

        override val hasUnreadNotification: Boolean get() = unreadCount > 0

        val isRecordMuted: Boolean get() = record.isEmpty
        val streakText: String
            get() = if (record.currentStreak > 0) "${record.currentStreak}🔥" else "0"

        fun deadlineTextOf(challenge: ActiveChallenge): String =
            challenge.deadline.toRelativeKoreanString(now)

        fun deadlineTextOf(challenge: ReceivedChallenge): String =
            challenge.deadline.toRelativeKoreanString(now)

        /** 마감 1시간 이하면 강조 색으로 바꾼다. */
        fun isUrgent(challenge: ReceivedChallenge): Boolean =
            (challenge.deadline.toInstant(KST) - now.toInstant(KST)) <= 1.hours

        fun isActionable(challenge: ReceivedChallenge): Boolean =
            challenge.challengeId !in inFlightIds

        override val debugAccountLabel: String? get() = if (isDebug) userInfo?.nickname else null
    }
}
