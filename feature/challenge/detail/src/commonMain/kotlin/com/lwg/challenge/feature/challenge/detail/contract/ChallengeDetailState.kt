package com.lwg.challenge.feature.challenge.detail.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.lwg.challenge.domain.model.VerificationStatus
import com.lwg.challenge.domain.model.signature.Signature
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
     * @property myMission 아직 맹세하지 않았으면 `null`.
     * @property opponentMission 상대가 아직 맹세하지 않았으면 `null` 이고 행이 빠진다.
     * @property headlineText 남은 시간 → "판정 대기 중" → 판정 결과 순으로 바뀌며, ViewModel 티커가 갱신한다.
     * @property headlineTone [headlineText] 와 어긋나지 않도록 [headlineDisplay] 가 함께 만든다.
     * @property canVerify 챌린지가 진행 중인지만 본다. 사진 교체가 허용돼 내 인증 상태는 보지 않는다.
     * @property forfeitNoticeText 기권으로 끝난 챌린지에만 있다.
     * @property isForfeiting 진입점만 잠근다. 전체 로딩으로 덮으면 보던 계약서가 사라진다.
     */
    @Immutable
    data class Data(
        val myMission: String?,
        val opponentNickname: String,
        val opponentMission: String?,
        val betContent: String,
        val headlineText: String,
        val headlineTone: HeadlineTone,
        val deadlineAbsoluteText: String,
        /** 계약서가 없는 챌린지(soul-oath 이전 생성)면 null. */
        val signatures: SignatureSection?,
        /** 인증 현황 조회에 실패하면 null. 이때 하단 슬롯은 [canVerify] 만 따른다. */
        val verifications: VerificationSection?,
        val canVerify: Boolean,
        val forfeitNoticeText: String?,
        val isForfeiting: Boolean,
    ) : ChallengeDetailUiState {

        val myLabel: String get() = MY_LABEL

        val myMissionLabel: String get() = MY_MISSION_LABEL

        val opponentLabel: String get() = opponentNickname

        val opponentMissionLabel: String get() = "${opponentNickname}의 미션"

        val myVerificationStatus: VerificationStatus? get() = verifications?.myStatus

        val myPhotoUrl: String? get() = verifications?.myPhotoUrl

        /** 현황을 모르면 삭제라고 단정하지 않는다. */
        val myPhotoDeleted: Boolean get() = verifications?.myPhotoDeleted == true

        val opponentVerificationStatus: VerificationStatus? get() = verifications?.opponentStatus

        val opponentPhotoUrl: String? get() = verifications?.opponentPhotoUrl

        val opponentPhotoDeleted: Boolean get() = verifications?.opponentPhotoDeleted == true

        val myAiReason: String? get() = verifications?.myAiReason

        val opponentAiReason: String? get() = verifications?.opponentAiReason

        val myAiReasonText: String? get() = myAiReason?.let { "$VERIFICATION_REJECTED_PREFIX$it" }

        val opponentAiReasonText: String?
            get() = opponentAiReason?.let { "$VERIFICATION_REJECTED_PREFIX$it" }

        /** 사진을 교체해도 URL 이 같아 Coil 캐시가 옛 사진을 그리므로 제출 시각으로 키를 가른다. */
        val myPhotoCacheKey: String? get() = photoCacheKey(myPhotoUrl, verifications?.myVerifiedAt)

        val opponentPhotoCacheKey: String?
            get() = photoCacheKey(opponentPhotoUrl, verifications?.opponentVerifiedAt)

        /**
         * `FAILED` 를 [canVerify] 보다 먼저 본다 — 진행 중이어도 실패한 인증엔 시킬 것이 없다.
         * AI 반려로 `FAILED` 가 되면 이미 마감 뒤라 사진만 남기고 다시 찍기는 두지 않는다.
         */
        val myMissionSlot: MyMissionSlot
            get() = when {
                myVerificationStatus == VerificationStatus.VERIFIED ->
                    if (canVerify) MyMissionSlot.MY_PHOTO_WITH_RETAKE else MyMissionSlot.MY_PHOTO

                myVerificationStatus == VerificationStatus.FAILED ->
                    if (myPhotoUrl != null) MyMissionSlot.MY_PHOTO else MyMissionSlot.NONE

                canVerify -> MyMissionSlot.VERIFY_CTA
                else -> MyMissionSlot.NONE
            }

        /** 인증 CTA 와 같은 조건이라 [myMissionSlot] 에서 파생해 다시 찍기와 동시에 보이지 않게 한다. */
        val canForfeit: Boolean get() = myMissionSlot == MyMissionSlot.VERIFY_CTA
    }
}

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

/**
 * 서명 2-up 한 묶음. 계약서가 있을 때만 존재한다.
 *
 * @property mySignatureHint [mySignature] 가 `null` 일 때 미서명/디코드 실패 문구.
 */
@Immutable
data class SignatureSection(
    val mySignature: Signature?,
    val mySignatureHint: String,
    val opponentSignature: Signature?,
    val opponentSignatureHint: String,
    val isFinalized: Boolean,
)

/**
 * 양측 인증 현황 한 묶음.
 *
 * @property myPhotoUrl 제출한 적이 없으면 null. AI 가 반려한 칸에도 사진은 남아 있다.
 * @property myVerifiedAt 화면에 그리지 않고 사진 캐시 키에만 쓴다.
 * @property opponentPhotoDeleted 탈퇴로 사진이 영구 삭제됨.
 * @property myAiReason non-null 이면 AI 판별로 반려된 것이다.
 */
@Immutable
data class VerificationSection(
    val myStatus: VerificationStatus,
    val myPhotoUrl: String?,
    val myPhotoDeleted: Boolean,
    val myVerifiedAt: LocalDateTime?,
    val myAiReason: String?,
    val opponentStatus: VerificationStatus,
    val opponentPhotoUrl: String?,
    val opponentPhotoDeleted: Boolean,
    val opponentVerifiedAt: LocalDateTime?,
    val opponentAiReason: String?,
)
