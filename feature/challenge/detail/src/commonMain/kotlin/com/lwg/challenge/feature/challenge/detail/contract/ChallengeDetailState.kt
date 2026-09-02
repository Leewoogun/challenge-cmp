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
         * [STATUS_IN_PROGRESS] + `FAILED` 는 진입점이 열려 있는데도 보여줄 것도 시킬 것도 없는
         * 조합이라, 내 인증 상태를 먼저 걷어낸 뒤에야 게이트로 떨어뜨릴 수 있다.
         *
         * [verifications] 가 `null`(조회 실패·관점 미상) 이면 앞 두 분기가 모두 빗나가 [canVerify] 로
         * 내려간다 — 의도한 경로다.
         */
        val myMissionSlot: MyMissionSlot
            get() = when {
                myVerificationStatus == VerificationStatus.VERIFIED ->
                    if (canVerify) MyMissionSlot.MY_PHOTO_WITH_RETAKE else MyMissionSlot.MY_PHOTO

                myVerificationStatus == VerificationStatus.FAILED -> MyMissionSlot.NONE
                canVerify -> MyMissionSlot.VERIFY_CTA
                else -> MyMissionSlot.NONE
            }
    }
}

/**
 * 사진 URL 은 그대로 두고 캐시 키만 인증 확정 시각으로 가른다 — URL 에 쿼리를 붙이면 서버 계약이
 * 정한 사진 경로 모양이 바뀌므로 요청은 손대지 않는다.
 *
 * [verifiedAt] 이 없으면(아직 인증 전) 갈라놓을 대상 자체가 없어 URL 만 남기며, 이는 키를 지정하지
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

    /** 인증을 마쳤고 마감이 지나 이제 바꿀 수 없다 — 내 사진만 남는다. */
    MY_PHOTO,

    /** 인증을 마쳤지만 아직 진행 중이라 다시 찍어 교체할 수 있다. */
    MY_PHOTO_WITH_RETAKE,

    /** 인증이 끝났거나(실패) 챌린지가 닫혀 할 것도 보여줄 것도 없다. */
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
 * @property myPhotoUrl 인증 사진의 절대 URL. 미인증이면 null. 인증 헤더가 필요한 URL 이라 외부로
 *   그대로 내보낼 수 없다.
 * @property myVerifiedAt 화면에 그리는 값이 아니라 사진 캐시 키의 재료다
 *   ([ChallengeDetailUiState.Data.myPhotoCacheKey]). 사진을 교체하면 [myPhotoUrl] 은 그대로인 채
 *   서버가 이 값을 갱신하므로, 이 값이 달라졌다는 건 같은 주소의 바이트가 바뀌었다는 뜻이다.
 * @property opponentVerifiedAt [myVerifiedAt] 과 같은 용도. 상대가 교체한 사진도 재진입 때 새 값이
 *   실려 오므로 같은 경로로 잡힌다.
 * @property opponentPhotoDeleted 탈퇴로 사진이 영구 삭제됨. `VERIFIED` + [opponentPhotoUrl] `null`
 *   조합이 정상인 유일한 경우라, 이 값이 없으면 데이터 이상과 구분되지 않는다.
 */
@Immutable
data class VerificationSection(
    val myStatus: VerificationStatus,
    val myPhotoUrl: String?,
    val myPhotoDeleted: Boolean,
    val myVerifiedAt: LocalDateTime?,
    val opponentStatus: VerificationStatus,
    val opponentPhotoUrl: String?,
    val opponentPhotoDeleted: Boolean,
    val opponentVerifiedAt: LocalDateTime?,
)
