package com.lwg.challenge.feature.challenge.detail.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.lwg.challenge.domain.model.VerificationStatus
import com.lwg.challenge.domain.model.signature.Signature
import kotlinx.datetime.LocalDateTime

/** 관점(내가 어느 쪽인지)을 판정했을 때 내 서명 칸에 붙는 라벨. */
internal const val MY_LABEL = "나"

/** 관점을 판정했을 때 계약서 내 미션 행에 붙는 라벨. */
internal const val MY_MISSION_LABEL = "나의 미션"

/** 아직 서명하지 않은 칸의 안내 문구. */
internal const val SIGNATURE_NOT_SIGNED_HINT = "서명 대기 중"

/** 서명은 했는데 데이터를 읽지 못한 칸의 안내 문구. */
internal const val SIGNATURE_UNREADABLE_HINT = "서명을 불러오지 못했어요"

/** 인증 진입점이 열리는 유일한 챌린지 상태. */
internal const val STATUS_IN_PROGRESS = "IN_PROGRESS"

/** 확인 다이얼로그 제목. */
internal const val FORFEIT_DIALOG_TITLE = "정말 기권하시겠어요?"

/** 확인 다이얼로그 본문. 되돌릴 수 없다는 것을 반드시 말한다 — 기권 취소 기능이 없다. */
internal const val FORFEIT_DIALOG_MESSAGE = "기권하면 이 챌린지는 바로 끝나고, 상대가 승리합니다. 되돌릴 수 없어요."

internal const val FORFEIT_CONFIRM_LABEL = "기권하기"

/** 하단 진입점 문구. */
internal const val FORFEIT_ENTRY_TEXT = "기권하기"

/** 내가 기권했을 때의 안내. */
internal const val FORFEIT_BY_ME_TEXT = "내가 기권했어요"

/** 기권자를 실명으로 말할 때의 접미. `"${nickname}$FORFEIT_BY_OTHER_SUFFIX"`. */
internal const val FORFEIT_BY_OTHER_SUFFIX = "님이 기권했어요"

/** 기권자가 양측 어느 쪽과도 맞지 않을 때. 아는 사실만 말한다. */
internal const val FORFEIT_UNKNOWN_ACTOR_TEXT = "기권으로 끝났어요"

/**
 * AI 반려 사유 앞에 붙는 접두. `"$VERIFICATION_REJECTED_PREFIX$aiReason"`.
 *
 * 스펙 원문은 합쇼체("인정되지 않았습니다")인데 해요체로 낮췄다 — 이 화면과 인증 카드의 사용자 노출
 * 문구가 전부 해요체라("아직 인증하지 않았어요" · "인증하지 못했어요" · "사진을 불러오지 못했어요"),
 * 한 줄만 합쇼체면 그 자리가 튄다.
 */
internal const val VERIFICATION_REJECTED_PREFIX = "인증이 인정되지 않았어요: "

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
     * @property myMission 아직 맹세하지 않았으면 `null` — 빈 문자열로 뭉개면 값 없는 행을 그린다.
     * @property opponentMission 상대가 아직 맹세하지 않았으면(`PENDING`) `null` 이고 행 자체가 빠진다.
     * @property headlineText 헤더 한 자리를 남은 시간 → "판정 대기 중" → 판정 결과가 차례로
     *   물려받는다. 앞 두 구간은 시간이 흐르면 값이 바뀌므로 ViewModel 의 분 단위 티커가 같은 자리를
     *   다시 채우고, 결과가 실린 뒤에는 시계로 더 바뀔 것이 없다. 화면은 받은 문자열을 그리기만 한다.
     * @property headlineTone [headlineText] 와 **같은 순간·같은 판정**에서 함께 만들어진다
     *   ([headlineDisplay] 가 둘을 한 묶음으로 돌려준다). 따로 갱신하면 "판정 대기 중" 이 촉구색으로
     *   남거나 패배 문구에 승리색이 붙는 조합이 생긴다.
     * @property deadlineAbsoluteText 절대 마감 시각("8/3 24:00"). 시간이 흘러도 변하지 않아 티커가
     *   건드리지 않는다.
     * @property canVerify 챌린지가 [STATUS_IN_PROGRESS] 인지만 뜻한다 — 내 인증 상태는 보지 않는다.
     *   재제출이 사진 교체로 허용되면서 `VERIFIED` 가 진입점을 닫을 근거가 아니게 됐다. 마감 뒤의
     *   교체는 서버가 여전히 거부하므로 그 조건만 남는다.
     * @property forfeitNoticeText 기권으로 끝난 챌린지에만 붙는 한 줄("내가 기권했어요" /
     *   "우건님이 기권했어요"). 기권이 아니면 `null` 이고 그 자리가 통째로 빠진다. 관점 판정이 이미
     *   끝난 값이라 화면이 다시 누가 기권했는지 묻지 않는다. [headlineText] 와 달리 시간이 흘러도
     *   변하지 않아 티커가 건드리지 않는다.
     * @property isForfeiting 기권 요청이 나가 있는 동안 참. 진입점을 잠그는 데만 쓴다. 전체 화면
     *   로딩으로 덮지 않는 이유는 이미 읽고 있던 계약서가 사라지기 때문이다
     *   (`MyPageUiState.isWithdrawing` 과 같은 판단).
     */
    @Immutable
    data class Data(
        val myLabel: String,
        val myMissionLabel: String,
        val myMission: String?,
        val opponentNickname: String,
        val opponentMission: String?,
        val betContent: String,
        val headlineText: String,
        val headlineTone: HeadlineTone,
        val deadlineAbsoluteText: String,
        /** 계약서가 없는 챌린지(soul-oath 이전 생성)면 null. 서명 블록을 통째로 그리지 않는다. */
        val signatures: SignatureSection?,
        /**
         * 인증 현황을 모르면(조회 실패·관점 미상) null. 사진도 뱃지도 없이 하단 슬롯은 [canVerify] 만
         * 따른다 — 진행 중이면 인증 CTA, 아니면 빈칸.
         */
        val verifications: VerificationSection?,
        val canVerify: Boolean,
        val forfeitNoticeText: String?,
        val isForfeiting: Boolean,
    ) : ChallengeDetailUiState {

        val opponentLabel: String get() = opponentNickname

        /** 상대 미션 카드 라벨. [myMissionLabel] 과 달리 관점 판정과 무관해 닉네임에서 바로 만든다. */
        val opponentMissionLabel: String get() = "${opponentNickname}의 미션"

        val myVerificationStatus: VerificationStatus? get() = verifications?.myStatus

        val myPhotoUrl: String? get() = verifications?.myPhotoUrl

        /** 현황을 모르면 삭제라고 단정하지 않는다 — 사진 없음의 이유를 날조하지 않는 쪽으로 둔다. */
        val myPhotoDeleted: Boolean get() = verifications?.myPhotoDeleted == true

        val opponentVerificationStatus: VerificationStatus? get() = verifications?.opponentStatus

        val opponentPhotoUrl: String? get() = verifications?.opponentPhotoUrl

        val opponentPhotoDeleted: Boolean get() = verifications?.opponentPhotoDeleted == true

        /**
         * AI 판정 사유. 현황을 모르면 `null`.
         *
         * non-null 이면 **그 인증은 AI 판별로 미인증 처리된 것**이다(API 계약 보장). 그래서
         * [myVerificationStatus] 를 함께 보지 않는다 — 같이 보면 '반려란 무엇인가' 의 판정 사본이
         * 앱에 생긴다. 통과·검증 불가·호출 실패 건의 사유는 서버가 컬럼에만 두고 wire 에는 싣지 않아
         * 여기까지 오지 않는다.
         */
        val myAiReason: String? get() = verifications?.myAiReason

        /** [myAiReason] 과 같은 계약이 그대로 적용된다. */
        val opponentAiReason: String? get() = verifications?.opponentAiReason

        /**
         * 내 카드에 그릴 반려 사유 문구. 사유가 없으면 `null` 이고 그 자리가 통째로 빠진다.
         *
         * 접두어 결합을 화면이 아니라 여기서 끝내는 이유는 [opponentMissionLabel] 과 같다 —
         * 두 카드가 같은 문장을 그려야 하는데 조립을 호출부에 맡기면 한쪽만 고쳐져 갈린다.
         */
        val myAiReasonText: String? get() = myAiReason?.let { "$VERIFICATION_REJECTED_PREFIX$it" }

        /** [myAiReasonText] 와 같은 규칙. 상대 칸이라고 문구가 달라지지 않는다. */
        val opponentAiReasonText: String?
            get() = opponentAiReason?.let { "$VERIFICATION_REJECTED_PREFIX$it" }

        /**
         * 내 인증 사진을 이미지 로더 캐시에서 식별할 키. 사진이 없으면 `null`.
         *
         * 사진을 교체해도 [myPhotoUrl] 은 그대로다. Coil 은 URL 을 키로 메모리·디스크 캐시를 따로
         * 들고 HTTP 캐시 헤더를 보지 않으므로, 키를 가르지 않으면 교체 뒤에도 옛 사진이 계속 그려진다.
         * 그 실패는 예외도 로그도 남기지 않아 화면을 직접 보기 전까지 아무도 모른다.
         *
         * 알려진 한계: [VerificationSection.myVerifiedAt] 이 초 단위라 같은 초에 두 번 교체하면 키가
         * 같아진다. 촬영 → 확인 → 제출 왕복이 1초 안에 두 번 끝날 수 없어 실사용상 도달하지 않는다.
         */
        val myPhotoCacheKey: String? get() = photoCacheKey(myPhotoUrl, verifications?.myVerifiedAt)

        /** [myPhotoCacheKey] 와 같은 이유·같은 규칙. 상대가 교체한 사진도 재진입 때 새 키로 잡힌다. */
        val opponentPhotoCacheKey: String?
            get() = photoCacheKey(opponentPhotoUrl, verifications?.opponentVerifiedAt)

        /**
         * 내 미션 카드 하단에 무엇이 들어가는지.
         *
         * [VerificationStatus.VERIFIED] 는 [canVerify] 를 가르는 대신 같은 칸의 모양만 가른다 —
         * 진행 중이면 사진 아래에 교체 액션을 함께 두고, 마감 뒤에는 사진만 남긴다.
         *
         * [VerificationStatus.FAILED] 를 [canVerify] 보다 먼저 보는 이유는 그대로다:
         * [STATUS_IN_PROGRESS] + `FAILED` 는 진입점이 열려 있는데도 시킬 것이 없는 조합이라, 내
         * 인증 상태를 먼저 걷어낸 뒤에야 게이트로 떨어뜨릴 수 있다.
         *
         * 다만 `FAILED` 가 **보여줄 것도 없다는 뜻은 아니다.** 마감까지 미제출한 `FAILED` 와 AI 가
         * 반려한 `FAILED` 를 가르는 것은 사진 유무뿐이고([VerificationStatus.FAILED] 참조),
         * 후자는 사용자가 실제로 올린 사진이 남아 있다. 그래서 사진이 있으면 그것만 남긴다.
         *
         * 사진이 있어도 [MyMissionSlot.MY_PHOTO_WITH_RETAKE] 가 아닌 이유: 반려는 자정 판정 배치가
         * 내리는 결론이고 그 시점이 곧 마감이라, `FAILED` 를 보는 순간 챌린지는 이미 닫혀 있고
         * [canVerify] 가 false 다. 다시 찍을 창이 구조적으로 존재하지 않는다.
         *
         * [verifications] 가 `null`(조회 실패·관점 미상) 이면 앞 분기들이 모두 빗나가 [canVerify] 로
         * 내려간다 — 의도한 경로다.
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

        /**
         * 하단 기권 진입점을 여는지.
         *
         * 노출 조건은 [STATUS_IN_PROGRESS] **이고 내 인증 없음**인데, 그건 [myMissionSlot] 의
         * [MyMissionSlot.VERIFY_CTA] 가지가 이미 뜻하는 것과 정확히 같다. 조건식을 새로 적으면 같은
         * 규칙이 두 벌이 되고, 한쪽만 고치는 날 둘이 갈린다.
         *
         * [MyMissionSlot.MY_PHOTO_WITH_RETAKE]("다시 찍기")와 **같은 `when` 의 다른 가지**라,
         * 기권 진입점과 다시 찍기가 동시에 보이는 조합이 구조적으로 존재하지 않는다. 이 배타성이
         * 이 파생의 존재 이유이고, 별도 가드로 지키지 않는 이유다.
         *
         * 인증 현황을 모르면([verifications] 이 `null` — 조회 실패·관점 미상) [myMissionSlot] 이
         * [MyMissionSlot.VERIFY_CTA] 로 떨어지므로 기권 진입점도 함께 열린다. 의도한 것이다 —
         * 인증 CTA 와 같은 근거로 열고, 실제 자격은 서버가 판정한다. 여기에만 가드를 더하면 두
         * 진입점이 서로 다른 근거로 갈린다.
         */
        val canForfeit: Boolean get() = myMissionSlot == MyMissionSlot.VERIFY_CTA
    }
}

/**
 * 사진 URL 은 그대로 두고 캐시 키만 **제출 확정 시각**으로 가른다 — URL 에 쿼리를 붙이면 서버 계약이
 * 정한 사진 경로 모양이 바뀌므로 요청은 손대지 않는다.
 *
 * 가르는 기준이 '인증 확정' 이 아니라 '제출 확정' 이어야 한다. 서버는 제출 시점에만 이 값을 쓰고
 * 이후 판정은 건드리지 않으므로, 인증이 인정됐는지와 무관하게 사진 바이트가 바뀐 순간만 이 값이
 * 움직인다 — 바로 그게 캐시를 갈라야 하는 순간이다.
 *
 * [verifiedAt] 이 없으면(아직 제출 전) 갈라놓을 대상 자체가 없어 URL 만 남기며, 이는 키를 지정하지
 * 않았을 때의 Coil 기본 동작과 같다.
 */
private fun photoCacheKey(photoUrl: String?, verifiedAt: LocalDateTime?): String? {
    val url = photoUrl ?: return null
    return if (verifiedAt == null) url else "$url|$verifiedAt"
}

/**
 * 내 미션 카드 하단 슬롯의 종류.
 *
 * 색·치수가 아니라 '무엇이 들어가는지' 라 표현 상태로 내릴 수 있다 — 화면이 상태를 다시 판별하지 않게
 * [ChallengeDetailUiState.Data.myMissionSlot] 에서 확정해 내려보낸다.
 */
enum class MyMissionSlot {

    /** 아직 인증하지 않았고, 지금 인증할 수 있다. */
    VERIFY_CTA,

    /**
     * 내가 낸 사진만 남는다 — 마감이 지나 이제 바꿀 수 없다.
     *
     * 인증이 인정된 경우와 AI 가 반려한 경우가 여기서 만난다. 둘 다 사진은 그대로 두고 결과만
     * 뱃지가 말하므로, 슬롯을 갈라야 할 이유가 없다.
     */
    MY_PHOTO,

    /** 인증을 마쳤지만 아직 진행 중이라 다시 찍어 교체할 수 있다. */
    MY_PHOTO_WITH_RETAKE,

    /** 챌린지가 닫혔거나 마감까지 사진을 내지 못해, 할 것도 보여줄 것도 없다. */
    NONE,
}

/**
 * 서명 2-up 한 묶음. 계약서가 있을 때만 존재한다.
 *
 * 다섯 값을 [ChallengeDetailUiState.Data] 에 평탄하게 두고 `hasContract` 를 따로 들면 일부만
 * 그리는 실수가 가능해진다 — 계약서가 없으면 이 다섯이 통째로 없으므로 묶음 자체를 nullable 로 둔다.
 *
 * @property mySignatureHint [mySignature] 가 `null` 일 때만 쓰인다. 미서명과 디코드 실패는
 *   시각이 같고 문구만 달라, 어느 쪽인지 판정한 ViewModel 이 문구를 확정해 내려보낸다.
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
 * 양측 인증 현황 한 묶음. '나 / 상대' 로 이미 세워져 있어 화면이 다시 뒤집지 않는다.
 *
 * 묶음 자체가 nullable 인 이유는 '모른다' 와 '아직 아무도 인증하지 않았다' 가 다른 상태이기 때문이다.
 * 후자는 두 칸 모두 [VerificationStatus.PENDING] 으로 채워져 온다.
 *
 * @property myPhotoUrl 인증 사진의 절대 URL. **제출한 적이 없으면** null — 미인증과 같은 말이
 *   아니다(AI 가 반려한 칸은 미인증 처리인데도 내가 올린 사진이 남아 있다 —
 *   [com.lwg.challenge.domain.model.verification.PartyVerification.photoUrl] 에 같은 계약이 있다).
 *   인증 헤더가 필요한 URL 이라 외부로 그대로 내보낼 수 없다.
 * @property myVerifiedAt 화면에 그리는 값이 아니라 사진 캐시 키의 재료다
 *   ([ChallengeDetailUiState.Data.myPhotoCacheKey]). 사진을 교체하면 [myPhotoUrl] 은 그대로인 채
 *   서버가 이 값을 갱신하므로, 이 값이 달라졌다는 건 같은 주소의 바이트가 바뀌었다는 뜻이다.
 * @property opponentVerifiedAt [myVerifiedAt] 과 같은 용도. 상대가 교체한 사진도 재진입 때 새 값이
 *   실려 오므로 같은 경로로 잡힌다.
 * @property opponentPhotoDeleted 탈퇴로 사진이 영구 삭제됨. 제출을 마친 칸(`VERIFIED`, 그리고
 *   AI 가 반려한 `FAILED`) + [opponentPhotoUrl] `null` 이 정상인 경우가 이것뿐이라, 이 값이 없으면
 *   데이터 이상과 구분되지 않는다
 *   ([com.lwg.challenge.domain.model.verification.PartyVerification.photoDeleted] 에 같은 계약이 있다).
 * @property myAiReason AI 판정 사유. non-null 이면 **그 인증이 AI 판별로 미인증 처리됐다**는
 *   뜻이다(API 계약 보장). 그래서 화면은 [myStatus] 를 함께 보지 않는다 — 같이 보면 '반려란
 *   무엇인가' 의 판정 사본이 앱에 생긴다. 통과·검증 불가·호출 실패 건의 사유는 서버가 컬럼에만 두고
 *   wire 에는 싣지 않는다
 *   ([com.lwg.challenge.domain.model.verification.PartyVerification.aiReason] 에 같은 계약이 있다).
 * @property opponentAiReason [myAiReason] 과 같다. 상대 칸이라고 계약이 달라지지 않는다.
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
