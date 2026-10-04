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

        /**
         * 빈 상태 카드를 띄울지, 어느 문구로 띄울지.
         *
         * 세는 대상이 [challenges] 가 아니라 [inProgressChallenges] 인 것이 핵심이다. 판정이 끝난
         * 건이 같은 배열에 섞여 오므로 전체로 세면, 진행 중이 0건인데도 최근 결과가 있다는 이유만으로
         * 빈 상태 카드가 사라진다. 그런데 이 카드의 문구 2종은 **둘 다 "진행 중인 챌린지가 없어요"** 다 —
         * 진행 중만 세면 문구를 한 글자도 바꾸지 않고 그대로 정확해진다.
         *
         * [HomeEmptyType.FIRST_USER] 는 받은 도전장에 더해 **최근 결과**도 없어야 한다. 판정된 결과가
         * 이미 있는 사람에게 "친구를 등록하고 첫 약속을 걸어보세요" 는 틀린 말이다.
         */
        val emptyType: HomeEmptyType
            get() = when {
                record.isEmpty && inProgressChallenges.isEmpty() &&
                    receivedChallenges.isEmpty() && completedChallenges.isEmpty() ->
                    HomeEmptyType.FIRST_USER
                inProgressChallenges.isEmpty() -> HomeEmptyType.NO_ACTIVE_CHALLENGE
                else -> HomeEmptyType.NONE
            }

        /**
         * 벨 뱃지 점등 여부.
         *
         * 서버에 boolean 을 따로 요구하지 않고 개수를 앱에서 접는다 — 계약을 boolean 으로 좁혀두면
         * 나중에 뱃지에 숫자를 찍을 때 계약을 다시 열어야 한다.
         */
        val hasUnreadNotification: Boolean get() = unreadCount > 0

        /** 전적 4개 값이 모두 0이면 강조 색을 죽인다. */
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

enum class HomeEmptyType {
    NONE,
    FIRST_USER,
    NO_ACTIVE_CHALLENGE,
}
