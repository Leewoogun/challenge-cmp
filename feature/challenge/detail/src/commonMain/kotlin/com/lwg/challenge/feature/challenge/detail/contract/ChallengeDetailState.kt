package com.lwg.challenge.feature.challenge.detail.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.lwg.challenge.domain.model.signature.Signature

/** 관점(내가 어느 쪽인지)을 판정했을 때 내 서명 칸에 붙는 라벨. */
internal const val MY_LABEL = "나"

/** 관점을 판정했을 때 계약서 내 미션 행에 붙는 라벨. */
internal const val MY_MISSION_LABEL = "나의 미션"

/** 아직 서명하지 않은 칸의 안내 문구. */
internal const val SIGNATURE_NOT_SIGNED_HINT = "서명 대기 중"

/** 서명은 했는데 데이터를 읽지 못한 칸의 안내 문구. */
internal const val SIGNATURE_UNREADABLE_HINT = "서명을 불러오지 못했어요"

@Stable
sealed interface ChallengeDetailUiState {

    @Immutable
    data object Loading : ChallengeDetailUiState

    /** 읽기 전용 화면이라 부분 실패가 없다 — 상세를 못 받으면 보여줄 계약서 자체가 없다. */
    @Immutable
    data class Error(val message: String) : ChallengeDetailUiState

    /**
     * @property myLabel 보통 [MY_LABEL] 이지만, 내가 어느 쪽인지 판정하지 못하면 챌린저 닉네임이 온다.
     *   관점을 모르는 채 "나" 라고 부르면 계약서를 잘못 읽게 되므로 그럴 땐 역할 그대로 보여준다.
     * @property myMissionLabel [myLabel] 과 같은 판정에서 함께 나온다. 서명 라벨만 실명으로 낮추고
     *   계약서 행은 "나의 미션" 으로 두면 한 카드 안에서 관점을 안다/모른다가 엇갈린다.
     * @property mySignatureHint [mySignature] 가 `null` 일 때만 쓰인다. 미서명과 디코드 실패는
     *   시각이 같고 문구만 달라, 어느 쪽인지 판정한 ViewModel 이 문구를 확정해 내려보낸다.
     * @property myMission 아직 맹세하지 않았으면 `null` — 빈 문자열로 뭉개면 값 없는 행을 그린다.
     * @property opponentMission 상대가 아직 맹세하지 않았으면(`PENDING`) `null` 이고 행 자체가 빠진다.
     */
    @Immutable
    data class Data(
        val myLabel: String,
        val myMissionLabel: String,
        val myMission: String?,
        val mySignature: Signature?,
        val mySignatureHint: String,
        val opponentNickname: String,
        val opponentMission: String?,
        val opponentSignature: Signature?,
        val opponentSignatureHint: String,
        val betContent: String,
        val deadlineText: String,
        val isFinalized: Boolean,
    ) : ChallengeDetailUiState {

        val opponentLabel: String get() = opponentNickname
    }
}
