package com.lwg.challenge.feature.home.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.lwg.challenge.domain.model.ActiveChallenge
import com.lwg.challenge.domain.model.UserRecord
import com.lwg.challenge.domain.model.user.UserInfo

/** 수락 다이얼로그 미션 입력 최대 길이. 서버 검증(trim 기준 1~100자)과 동일한 상한. */
const val MAX_ACCEPT_MISSION_LENGTH = 100

@Stable
sealed interface HomeUiState {

    @Immutable
    data object Loading : HomeUiState

    @Immutable
    data class Data(
        val record: UserRecord,
        val challenges: List<ActiveChallenge>,
        val userInfo: UserInfo? = null,
        val receivedChallenges: List<ReceivedChallengeItemState> = emptyList(),
        val acceptTarget: ReceivedChallengeItemState? = null,
        val acceptMission: String = "",
        val isSubmittingAccept: Boolean = false,
        val inFlightIds: Set<Long> = emptySet(),
    ) : HomeUiState {
        val emptyType: HomeEmptyType
            get() = when {
                // 받은 도전장이 떠 있는데 "친구를 등록하고 첫 약속을 걸어보세요" 는 사실과 어긋난다.
                record.isEmpty && challenges.isEmpty() && receivedChallenges.isEmpty() ->
                    HomeEmptyType.FIRST_USER
                challenges.isEmpty() -> HomeEmptyType.NO_ACTIVE_CHALLENGE
                else -> HomeEmptyType.NONE
            }

        /** 서버 검증이 trim 기준이라 카운터도 trim 으로 센다. */
        val acceptMissionCount: Int get() = acceptMission.trim().length
        val acceptMissionCounterText: String
            get() = "$acceptMissionCount/$MAX_ACCEPT_MISSION_LENGTH"
        val isAcceptMissionAtMax: Boolean get() = acceptMissionCount >= MAX_ACCEPT_MISSION_LENGTH
        val canSubmitAccept: Boolean
            get() = !isSubmittingAccept && acceptMissionCount in 1..MAX_ACCEPT_MISSION_LENGTH

        fun isActionable(item: ReceivedChallengeItemState): Boolean =
            item.challengeId !in inFlightIds
    }
}

enum class HomeEmptyType {
    NONE,
    FIRST_USER,
    NO_ACTIVE_CHALLENGE,
}
