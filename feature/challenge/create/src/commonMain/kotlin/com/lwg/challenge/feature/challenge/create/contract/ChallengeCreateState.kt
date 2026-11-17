package com.lwg.challenge.feature.challenge.create.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.lwg.challenge.domain.model.friend.Friend
import com.lwg.challenge.domain.model.signature.Signature
import com.lwg.challenge.utils.datetime.kstDeadlineHintText
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

/** 미션 / 내기 입력 최대 길이. */
const val MAX_INPUT_LENGTH = 100

@Stable
sealed interface ChallengeCreateUiState {

    val step: ChallengeCreateStep

    @Immutable
    data object Loading : ChallengeCreateUiState {
        override val step: ChallengeCreateStep
            get() = ChallengeCreateStep.FRIEND_PICK
    }

    @Immutable
    data class FriendPick(
        val friends: List<Friend>,
        val selectedFriendId: Long?,
        val loadFailed: Boolean,
    ) : ChallengeCreateUiState {

        override val step: ChallengeCreateStep
            get() = ChallengeCreateStep.FRIEND_PICK

        val isFriendsEmpty: Boolean get() = friends.isEmpty() && !loadFailed

        fun isSelected(friend: Friend): Boolean = friend.userId == selectedFriendId
    }

    @Immutable
    data class MissionInput(
        val myMission: String,
        val penalty: String,
        val challengeDate: LocalDate,
        val today: LocalDate,
        val selectableDates: ClosedRange<LocalDate>,
    ) : ChallengeCreateUiState {

        override val step: ChallengeCreateStep
            get() = ChallengeCreateStep.MISSION_INPUT

        val tomorrow: LocalDate get() = today.plus(1, DateTimeUnit.DAY)

        val selectedDeadlineOption: DeadlineOption
            get() = when (challengeDate) {
                today -> DeadlineOption.TODAY
                tomorrow -> DeadlineOption.TOMORROW
                else -> DeadlineOption.CUSTOM
            }

        val todayHintText: String get() = kstDeadlineHintText(today)
        val tomorrowHintText: String get() = kstDeadlineHintText(tomorrow)
        val customHintText: String
            get() = when (selectedDeadlineOption) {
                DeadlineOption.CUSTOM -> kstDeadlineHintText(challengeDate)
                DeadlineOption.TODAY, DeadlineOption.TOMORROW -> "최대 3개월"
            }

        val canProceedToOath: Boolean
            get() = myMissionCount in 1..MAX_INPUT_LENGTH &&
                penaltyCount in 1..MAX_INPUT_LENGTH

        val myMissionCount: Int get() = myMission.trim().length
        val penaltyCount: Int get() = penalty.trim().length

        val myMissionCounterText: String get() = "$myMissionCount/$MAX_INPUT_LENGTH"
        val penaltyCounterText: String get() = "$penaltyCount/$MAX_INPUT_LENGTH"

        val isMyMissionAtMax: Boolean get() = myMissionCount >= MAX_INPUT_LENGTH
        val isPenaltyAtMax: Boolean get() = penaltyCount >= MAX_INPUT_LENGTH
    }

    @Immutable
    data class Oath(
        val opponent: Friend,
        val myMission: String,
        val penalty: String,
        val deadlineText: String,
        val signature: Signature,
        val isSubmitting: Boolean,
    ) : ChallengeCreateUiState {

        override val step: ChallengeCreateStep
            get() = ChallengeCreateStep.OATH

        /**
         * 유효 서명 판정은 "획 ≥ 1개" 뿐이다 — 서버 검증과 규칙이 갈라지면 클라를 통과한 서명이
         * 서버에서 거부된다.
         */
        val canSubmit: Boolean get() = !isSubmitting && !signature.isEmpty
    }
}

/** 위저드에서 사용자가 입력한 값. 단계를 오가도 유지되도록 ViewModel 이 들고 있는다. */
internal data class UserInputState(
    val step: ChallengeCreateStep = ChallengeCreateStep.FRIEND_PICK,
    val selectedFriend: Friend? = null,
    val myMission: String = "",
    val penalty: String = "",
    val today: LocalDate,
    val challengeDate: LocalDate,
    val signature: Signature = Signature.Empty,
    val isSubmitting: Boolean = false,
    /** 진입 시 지정된 상대. 친구 목록과 대조하기 전까지만 값이 있다. */
    val pendingOpponentId: Long? = null,
    val isFriendPickSkipped: Boolean = false,
) {
    val selectableDates: ClosedRange<LocalDate> get() = selectableDatesFrom(today)

    val canProceedToOath: Boolean
        get() = !isSubmitting &&
            myMission.trim().length in 1..MAX_INPUT_LENGTH &&
            penalty.trim().length in 1..MAX_INPUT_LENGTH
}

/** 서버와 같은 규칙 — 월 단위 덧셈은 말일을 clamp 한다(11/30 + 3개월 = 2/28). */
internal fun selectableDatesFrom(today: LocalDate): ClosedRange<LocalDate> =
    today..today.plus(3, DateTimeUnit.MONTH)

/** 마감 카드 3종. 선택 강조는 선택한 날짜에서 파생한다. */
enum class DeadlineOption(val label: String) {
    TODAY("오늘 자정"),
    TOMORROW("내일 자정"),
    CUSTOM("날짜 선택"),
}

enum class ChallengeCreateStep {
    FRIEND_PICK,
    MISSION_INPUT,
    OATH,
}
