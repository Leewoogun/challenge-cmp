package com.lwg.challenge.feature.challenge.create.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.lwg.challenge.domain.model.challenge.DeadlineType
import com.lwg.challenge.domain.model.friend.Friend
import com.lwg.challenge.domain.model.signature.Signature

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
        val deadlineType: DeadlineType,
        val todayDeadlineHint: String,
        val tomorrowDeadlineHint: String,
    ) : ChallengeCreateUiState {

        override val step: ChallengeCreateStep
            get() = ChallengeCreateStep.MISSION_INPUT

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
    val deadlineType: DeadlineType = DeadlineType.TODAY,
    val signature: Signature = Signature.Empty,
    val isSubmitting: Boolean = false,
) {
    val canProceedToOath: Boolean
        get() = !isSubmitting &&
            myMission.trim().length in 1..MAX_INPUT_LENGTH &&
            penalty.trim().length in 1..MAX_INPUT_LENGTH
}

enum class ChallengeCreateStep {
    FRIEND_PICK,
    MISSION_INPUT,
    OATH,
}
