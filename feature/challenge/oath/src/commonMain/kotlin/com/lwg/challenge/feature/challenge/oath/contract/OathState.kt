package com.lwg.challenge.feature.challenge.oath.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.lwg.challenge.domain.model.challenge.ChallengeDetail
import com.lwg.challenge.domain.model.signature.Signature
import com.lwg.challenge.utils.datetime.toRelativeKoreanString
import kotlinx.datetime.LocalDateTime

/** 미션 입력 최대 길이. 서버 검증(trim 기준 1~100자)과 동일한 상한. */
const val MAX_OATH_MISSION_LENGTH = 100

@Stable
sealed interface OathUiState {

    @Immutable
    data object Loading : OathUiState

    @Immutable
    data class Error(val message: String) : OathUiState

    @Immutable
    data class Data(
        val detail: ChallengeDetail,
        val now: LocalDateTime,
        val myMission: String,
        val signature: Signature,
        val isSubmitting: Boolean,
    ) : OathUiState {

        val deadlineText: String get() = detail.deadline.toRelativeKoreanString(now)

        val opponentMissionLabel: String get() = "${detail.opponent.nickname}의 미션"

        val opponentSignatureLabel: String get() = "${detail.opponent.nickname}의 서명"

        val myMissionCount: Int get() = myMission.trim().length

        val myMissionCounterText: String get() = "$myMissionCount/$MAX_OATH_MISSION_LENGTH"

        val isMyMissionAtMax: Boolean get() = myMissionCount >= MAX_OATH_MISSION_LENGTH

        /** 서명 유효성은 서버와 같은 "획 ≥ 1개" 만 본다. */
        val canSubmit: Boolean
            get() = !isSubmitting &&
                myMissionCount in 1..MAX_OATH_MISSION_LENGTH &&
                !signature.isEmpty

        /** 아무것도 안 쓴 채 나가는 사람에게까지 이탈 확인을 묻지 않는다. */
        val isDirty: Boolean get() = myMission.isNotBlank() || !signature.isEmpty
    }
}
