package com.lwg.challenge.domain.model.challenge

import com.lwg.challenge.domain.model.signature.Signature
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

/**
 * 챌린지 상세 + 계약서 1건.
 *
 * [challenger] / [opponent] 는 서버가 준 **역할 그대로**다 — '나 / 상대' 관점 변환은 하지 않는다.
 * 내가 어느 쪽인지 판별은 내 userId 를 아는 화면 계층의 몫이다.
 *
 * @property status 챌린지 진행 상태 문자열. 계약 완결 여부는 이 값이 아니라 [Contract.isFinalized] 가 정한다
 * @property result 판정 결과. `COMPLETED` 되기 전까지 null 이며, 서버 불변식상
 *   `status == "COMPLETED"` 와 `result != null` 은 동치다. [challenger] / [opponent] 와 마찬가지로
 *   **역할 기준**이다 — 내 시점 변환은 [ChallengeResult.outcomeFor] 로 화면 계층이 한다
 * @property forfeitedBy 기권으로 끝난 챌린지면 기권한 사람의 userId, 아니면 null.
 *   [challenger] / [opponent] 와 마찬가지로 **역할 변환 없는 원본**이라, 내가 기권했는지 판별은
 *   내 userId 를 아는 화면 계층의 몫이다
 * @property challengeDate 챌린지 기준 날짜 (KST). 포맷이 어긋나면 null
 * @property deadline 마감 시각 (KST)
 * @property contract soul-oath 도입 이전에 만들어진 챌린지는 계약서가 없어 null 이다. 정상 상태이며
 *   계약 §3.1 nullable 표에 등재돼 있다 — 챌린지 자체는 계약서 없이도 온전하다
 */
data class ChallengeDetail(
    val challengeId: Long,
    val status: String,
    val result: ChallengeResult?,
    val forfeitedBy: Long?,
    val challengeDate: LocalDate?,
    val deadline: LocalDateTime,
    val betContent: String,
    val challenger: ContractParty,
    val opponent: ContractParty,
    val contract: Contract?,
)

/**
 * 계약 당사자 1명.
 *
 * @property profileImageUrl 카카오 프로필 미설정 시 null
 * @property mission 이 사람이 스스로에게 건 미션. 아직 맹세하지 않았으면 null —
 *   `PENDING` 구간의 정상 상태이지 오류가 아니다
 */
data class ContractParty(
    val userId: Long,
    val nickname: String,
    val profileImageUrl: String?,
    val mission: String?,
)

/**
 * 양측 서명이 붙는 계약서.
 *
 * 서명 문자열 디코드는 매퍼가 하며, 실패해도 `signedAt` 은 지우지 않는다.
 * 그래야 [SignatureSlotState.UNREADABLE] 과 [SignatureSlotState.NOT_SIGNED] 을 구분할 수 있다.
 *
 * @property isFinalized 양측 서명이 모두 확정돼 계약이 완결됐는지. 서버가 판정한다
 */
data class Contract(
    val content: String,
    val isFinalized: Boolean,
    val challengerSignature: Signature?,
    val challengerSignedAt: LocalDateTime?,
    val opponentSignature: Signature?,
    val opponentSignedAt: LocalDateTime?,
) {

    val challengerSignatureState: SignatureSlotState
        get() = slotState(challengerSignature, challengerSignedAt)

    val opponentSignatureState: SignatureSlotState
        get() = slotState(opponentSignature, opponentSignedAt)

    private fun slotState(signature: Signature?, signedAt: LocalDateTime?): SignatureSlotState = when {
        signedAt == null -> SignatureSlotState.NOT_SIGNED
        signature != null -> SignatureSlotState.SIGNED
        else -> SignatureSlotState.UNREADABLE
    }
}

/**
 * 서명 슬롯 1칸의 표시 상태.
 *
 * [UNREADABLE] 을 [NOT_SIGNED] 과 합치지 않는 이유: 서명은 했는데 디코드만 실패한 경우를
 * '서명 대기 중'으로 그리면 사용자에게 거짓을 보여주게 된다. 파싱 실패는 숨기지 않는다.
 */
enum class SignatureSlotState {

    /** 아직 서명하지 않았다 (`signedAt == null`). */
    NOT_SIGNED,

    /** 서명했고 정상적으로 읽었다. */
    SIGNED,

    /** 서명 시각은 있는데 서명 데이터를 읽지 못했다. */
    UNREADABLE,
}
