package com.lwg.challenge.feature.challenge.create.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.lwg.challenge.domain.model.challenge.DeadlineType
import com.lwg.challenge.domain.model.friend.Friend

/** 미션 / 내기 입력 최대 길이. 서버 검증(trim 기준 1~100자)과 동일한 상한. */
const val MAX_INPUT_LENGTH = 100

/** 챌린지 생성 위저드 단계. */
enum class ChallengeCreateStep {
    FRIEND_PICK,
    MISSION_INPUT,
}

@Stable
sealed interface ChallengeCreateUiState {

    /** TopBar 뒤로가기 분기와 progress 렌더가 Loading 중에도 필요해 공통 프로퍼티로 노출한다. */
    val step: ChallengeCreateStep

    @Immutable
    data object Loading : ChallengeCreateUiState {
        override val step: ChallengeCreateStep get() = ChallengeCreateStep.FRIEND_PICK
    }

    @Immutable
    data class Data(
        override val step: ChallengeCreateStep,
        val friends: List<Friend>,
        val selectedFriendId: Long?,
        val myMission: String,
        val betContent: String,
        val deadlineType: DeadlineType,
        val todayDeadlineHint: String,
        val tomorrowDeadlineHint: String,
        val isSubmitting: Boolean,
        val loadFailed: Boolean,
    ) : ChallengeCreateUiState {

        val canSubmit: Boolean
            get() = !isSubmitting &&
                myMissionCount in 1..MAX_INPUT_LENGTH &&
                betContentCount in 1..MAX_INPUT_LENGTH

        val isFriendsEmpty: Boolean get() = friends.isEmpty() && !loadFailed

        val myMissionCount: Int get() = myMission.trim().length
        val betContentCount: Int get() = betContent.trim().length

        val myMissionCounterText: String get() = "$myMissionCount/$MAX_INPUT_LENGTH"
        val betContentCounterText: String get() = "$betContentCount/$MAX_INPUT_LENGTH"

        val isMyMissionAtMax: Boolean get() = myMissionCount >= MAX_INPUT_LENGTH
        val isBetContentAtMax: Boolean get() = betContentCount >= MAX_INPUT_LENGTH
    }
}
