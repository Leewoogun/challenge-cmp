package com.lwg.challenge.feature.challenge.detail.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.lwg.challenge.domain.model.VerificationStatus
import com.lwg.challenge.domain.model.challenge.ChallengeDetail
import com.lwg.challenge.domain.model.challenge.ChallengeSide
import com.lwg.challenge.domain.model.challenge.SignatureSlotState
import com.lwg.challenge.domain.model.verification.ChallengeVerifications
import com.lwg.challenge.domain.model.verification.VerificationResult
import com.lwg.challenge.utils.datetime.deadlineAbsoluteText
import kotlinx.datetime.LocalDateTime

internal const val MY_LABEL = "나"

internal const val MY_MISSION_LABEL = "나의 미션"

internal const val SIGNATURE_NOT_SIGNED_HINT = "서명 대기 중"

internal const val SIGNATURE_UNREADABLE_HINT = "서명을 불러오지 못했어요"

/** 인증 진입점이 열리는 유일한 챌린지 상태. */
internal const val STATUS_IN_PROGRESS = "IN_PROGRESS"

internal const val FORFEIT_DIALOG_TITLE = "정말 기권하시겠어요?"

/** 기권 취소 기능이 없으므로 되돌릴 수 없다는 것을 반드시 말한다. */
internal const val FORFEIT_DIALOG_MESSAGE = "기권하면 이 챌린지는 바로 끝나고, 상대가 승리합니다. 되돌릴 수 없어요."

internal const val FORFEIT_CONFIRM_LABEL = "기권하기"

internal const val FORFEIT_ENTRY_TEXT = "기권하기"

internal const val FORFEIT_BY_ME_TEXT = "내가 기권했어요"

internal const val FORFEIT_BY_OTHER_SUFFIX = "님이 기권했어요"

/** 스펙 원문은 합쇼체지만 화면의 다른 문구에 맞춰 해요체로 쓴다. */
internal const val VERIFICATION_REJECTED_PREFIX = "인증이 인정되지 않았어요: "

@Stable
sealed interface ChallengeDetailUiState {

    @Immutable
    data object Loading : ChallengeDetailUiState

    @Immutable
    data class Error(val message: String) : ChallengeDetailUiState

    /**
     * @property verifications 인증 현황 조회에 실패하면 null. 이때 하단 슬롯은 [canVerify] 만 따른다.
     * @property headline 시간에 따라 바뀌어 ViewModel 티커가 갱신한다.
     * @property isForfeiting 진입점만 잠근다. 전체 로딩으로 덮으면 보던 계약서가 사라진다.
     */
    @Immutable
    data class Data(
        val detail: ChallengeDetail,
        val verifications: ChallengeVerifications?,
        val headline: HeadlineDisplay,
        val isForfeiting: Boolean,
    ) : ChallengeDetailUiState {

        val myLabel: String get() = MY_LABEL

        val myMissionLabel: String get() = MY_MISSION_LABEL

        val opponentMissionLabel: String get() = "${detail.opponent.nickname}의 미션"

        val deadlineAbsoluteText: String get() = deadlineAbsoluteText(detail.challengeDate, detail.deadline)

        /** 사진 교체가 허용돼 내 인증 상태는 보지 않고 챌린지가 진행 중인지만 본다. */
        val canVerify: Boolean get() = detail.status == STATUS_IN_PROGRESS

        val forfeitNoticeText: String?
            get() = when (detail.forfeitedBy) {
                null -> null
                ChallengeSide.ME -> FORFEIT_BY_ME_TEXT
                ChallengeSide.OPPONENT -> "${detail.opponent.nickname}$FORFEIT_BY_OTHER_SUFFIX"
            }

        val mySignatureHint: String get() = signatureHint(detail.contract?.mySignatureState)

        val opponentSignatureHint: String get() = signatureHint(detail.contract?.opponentSignatureState)

        private val myVerification: VerificationResult? get() = verifications?.myVerification

        private val opponentVerification: VerificationResult? get() = verifications?.opponentVerification

        /** 이미 인증했으면 인증 화면을 사진 교체로 연다. */
        val isMyVerified: Boolean get() = myVerification?.status == VerificationStatus.VERIFIED

        /** 현황을 모르면 삭제라고 단정하지 않는다. */
        val myPhotoDeleted: Boolean get() = myVerification?.photoDeleted == true

        val opponentPhotoDeleted: Boolean get() = opponentVerification?.photoDeleted == true

        val myAiReasonText: String? get() = myVerification?.aiReason?.let { "$VERIFICATION_REJECTED_PREFIX$it" }

        val opponentAiReasonText: String?
            get() = opponentVerification?.aiReason?.let { "$VERIFICATION_REJECTED_PREFIX$it" }

        /** 사진을 교체해도 URL 이 같아 Coil 캐시가 옛 사진을 그리므로 제출 시각으로 키를 가른다. */
        val myPhotoCacheKey: String?
            get() = photoCacheKey(myVerification?.photoUrl, myVerification?.verifiedAt)

        val opponentPhotoCacheKey: String?
            get() = photoCacheKey(opponentVerification?.photoUrl, opponentVerification?.verifiedAt)

        /**
         * `FAILED` 를 [canVerify] 보다 먼저 본다 — 진행 중이어도 실패한 인증엔 시킬 것이 없다.
         * AI 반려로 `FAILED` 가 되면 이미 마감 뒤라 사진만 남기고 다시 찍기는 두지 않는다.
         */
        val myMissionSlot: MyMissionSlot
            get() = when {
                isMyVerified ->
                    if (canVerify) MyMissionSlot.MY_PHOTO_WITH_RETAKE else MyMissionSlot.MY_PHOTO

                myVerification?.status == VerificationStatus.FAILED ->
                    if (myVerification?.photoUrl != null) MyMissionSlot.MY_PHOTO else MyMissionSlot.NONE

                canVerify -> MyMissionSlot.VERIFY_CTA
                else -> MyMissionSlot.NONE
            }

        /** 인증 CTA 와 같은 조건이라 [myMissionSlot] 에서 파생해 다시 찍기와 동시에 보이지 않게 한다. */
        val canForfeit: Boolean get() = myMissionSlot == MyMissionSlot.VERIFY_CTA
    }
}

/** UNREADABLE 은 이미 서명한 칸이라 '서명 대기 중' 으로 쓰면 안 된다. */
private fun signatureHint(state: SignatureSlotState?): String =
    if (state == SignatureSlotState.UNREADABLE) SIGNATURE_UNREADABLE_HINT else SIGNATURE_NOT_SIGNED_HINT

/** URL 에 쿼리를 붙이지 않고 캐시 키만 제출 시각으로 가른다. */
private fun photoCacheKey(photoUrl: String?, verifiedAt: LocalDateTime?): String? {
    val url = photoUrl ?: return null
    return if (verifiedAt == null) url else "$url|$verifiedAt"
}

/** 내 미션 카드 하단 슬롯의 종류. */
enum class MyMissionSlot {

    /** 아직 인증하지 않았고, 지금 인증할 수 있다. */
    VERIFY_CTA,

    /** 마감이 지나 내가 낸 사진만 남는다. 인정·반려 여부는 뱃지가 보여준다. */
    MY_PHOTO,

    /** 인증을 마쳤지만 아직 진행 중이라 다시 찍을 수 있다. */
    MY_PHOTO_WITH_RETAKE,

    /** 할 것도 보여줄 것도 없다. */
    NONE,
}
