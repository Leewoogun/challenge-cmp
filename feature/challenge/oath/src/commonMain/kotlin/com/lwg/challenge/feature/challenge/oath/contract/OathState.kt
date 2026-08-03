package com.lwg.challenge.feature.challenge.oath.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.lwg.challenge.domain.model.signature.Signature

/** 미션 입력 최대 길이. 서버 검증(trim 기준 1~100자)과 동일한 상한. */
const val MAX_OATH_MISSION_LENGTH = 100

@Stable
sealed interface OathUiState {

    @Immutable
    data object Loading : OathUiState

    /**
     * 첫 조회에 실패해 보여줄 계약서 자체가 없는 상태.
     *
     * 이미 [Data] 로 진입한 뒤의 재조회 실패는 이 상태로 내려오지 않는다 — 작성 중이던
     * 미션·서명이 화면째 날아가므로 스낵바로만 알린다.
     */
    @Immutable
    data class Error(val message: String) : OathUiState

    /**
     * @property challengerSignature 미서명이거나 디코드에 실패했으면 null — 서명 블록을 생략한다.
     *   챌린저 서명은 정서적 장치이지 맹세의 전제가 아니라, 없다고 진행을 막지 않는다.
     */
    @Immutable
    data class Data(
        val challengeId: Long,
        val challengerNickname: String,
        val challengerMission: String,
        val challengerSignature: Signature?,
        val betContent: String,
        val deadlineText: String,
        val myMission: String,
        val signature: Signature,
        val isSubmitting: Boolean,
        val showExitConfirm: Boolean,
    ) : OathUiState {

        /** 서버 검증이 trim 기준이라 카운터도 trim 으로 센다. */
        val myMissionCount: Int get() = myMission.trim().length

        val myMissionCounterText: String get() = "$myMissionCount/$MAX_OATH_MISSION_LENGTH"

        val isMyMissionAtMax: Boolean get() = myMissionCount >= MAX_OATH_MISSION_LENGTH

        val challengerMissionLabel: String get() = "${challengerNickname}의 미션"

        val challengerSignatureLabel: String get() = "${challengerNickname}의 서명"

        /**
         * 유효 서명 판정은 "획 ≥ 1개" 뿐이다 — 서버 검증과 규칙이 갈라지면 클라를 통과한 서명이
         * 서버에서 거부된다.
         */
        val canSubmit: Boolean
            get() = !isSubmitting &&
                myMissionCount in 1..MAX_OATH_MISSION_LENGTH &&
                !signature.isEmpty

        /** 나가면 잃을 게 있는가. 아무것도 안 쓴 채 나가는 사람에게까지 묻지 않기 위한 판정. */
        val isDirty: Boolean get() = myMission.isNotBlank() || !signature.isEmpty
    }
}
