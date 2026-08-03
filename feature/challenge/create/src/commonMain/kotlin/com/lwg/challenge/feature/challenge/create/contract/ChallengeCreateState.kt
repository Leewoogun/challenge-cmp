package com.lwg.challenge.feature.challenge.create.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.lwg.challenge.domain.model.challenge.DeadlineType
import com.lwg.challenge.domain.model.friend.Friend
import com.lwg.challenge.domain.model.signature.Signature

/** 미션 / 내기 입력 최대 길이. 서버 검증(trim 기준 1~100자)과 동일한 상한. */
const val MAX_INPUT_LENGTH = 100

/** 챌린지 생성 위저드 단계. 선언 순서가 곧 진행 순서다 — `WizardProgressBar` 가 ordinal 로 진행도를 그린다. */
enum class ChallengeCreateStep {
    FRIEND_PICK,
    MISSION_INPUT,
    OATH,
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
        val signature: Signature,
        val isSubmitting: Boolean,
        val loadFailed: Boolean,
    ) : ChallengeCreateUiState {

        /** 미션·내기 입력이 끝나 맹세 step 으로 넘어갈 수 있는가. */
        val canProceedToOath: Boolean
            get() = !isSubmitting &&
                myMissionCount in 1..MAX_INPUT_LENGTH &&
                betContentCount in 1..MAX_INPUT_LENGTH

        /**
         * 실제 제출 가능 여부. 입력 조건 + 서명 유효.
         *
         * 유효 서명 판정은 "획 ≥ 1개" 뿐이다 — 서버 검증과 규칙이 갈라지면 클라를 통과한 서명이
         * 서버에서 거부된다.
         */
        val canSubmit: Boolean get() = canProceedToOath && !signature.isEmpty

        val isFriendsEmpty: Boolean get() = friends.isEmpty() && !loadFailed

        val selectedFriend: Friend? get() = friends.firstOrNull { it.userId == selectedFriendId }

        val opponentNicknameText: String get() = selectedFriend?.nickname.orEmpty()

        val deadlineHintText: String
            get() = when (deadlineType) {
                DeadlineType.TODAY -> todayDeadlineHint
                DeadlineType.TOMORROW -> tomorrowDeadlineHint
            }

        val myMissionCount: Int get() = myMission.trim().length
        val betContentCount: Int get() = betContent.trim().length

        val myMissionCounterText: String get() = "$myMissionCount/$MAX_INPUT_LENGTH"
        val betContentCounterText: String get() = "$betContentCount/$MAX_INPUT_LENGTH"

        val isMyMissionAtMax: Boolean get() = myMissionCount >= MAX_INPUT_LENGTH
        val isBetContentAtMax: Boolean get() = betContentCount >= MAX_INPUT_LENGTH
    }
}
