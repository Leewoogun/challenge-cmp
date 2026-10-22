package com.lwg.challenge.feature.challenge.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lwg.challenge.domain.model.challenge.ChallengeDetail
import com.lwg.challenge.domain.model.challenge.ChallengeOutcome
import com.lwg.challenge.domain.model.challenge.ChallengeSide
import com.lwg.challenge.domain.model.challenge.Contract
import com.lwg.challenge.domain.model.challenge.SignatureSlotState
import com.lwg.challenge.domain.model.verification.ChallengeVerifications
import com.lwg.challenge.domain.repository.ChallengeRepository
import com.lwg.challenge.domain.repository.VerificationRepository
import com.lwg.challenge.feature.challenge.detail.contract.ChallengeDetailModalEffect
import com.lwg.challenge.feature.challenge.detail.contract.ChallengeDetailUiEffect
import com.lwg.challenge.feature.challenge.detail.contract.ChallengeDetailUiState
import com.lwg.challenge.feature.challenge.detail.contract.FORFEIT_BY_ME_TEXT
import com.lwg.challenge.feature.challenge.detail.contract.FORFEIT_BY_OTHER_SUFFIX
import com.lwg.challenge.feature.challenge.detail.contract.SIGNATURE_NOT_SIGNED_HINT
import com.lwg.challenge.feature.challenge.detail.contract.SIGNATURE_UNREADABLE_HINT
import com.lwg.challenge.feature.challenge.detail.contract.STATUS_IN_PROGRESS
import com.lwg.challenge.feature.challenge.detail.contract.SignatureSection
import com.lwg.challenge.feature.challenge.detail.contract.VerificationSection
import com.lwg.challenge.feature.challenge.detail.contract.headlineDisplay
import com.lwg.challenge.utils.datetime.deadlineAbsoluteText
import com.lwg.challenge.utils.datetime.nowKst
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam
import kotlin.time.Duration.Companion.minutes

/** 남은 시간 표기가 분 단위라, 더 자주 깨워도 같은 문자열을 다시 만들 뿐이다. */
private val HEADLINE_TICK_INTERVAL = 1.minutes

@KoinViewModel
class ChallengeDetailViewModel(
    @InjectedParam private val challengeId: Long,
    private val challengeRepository: ChallengeRepository,
    private val verificationRepository: VerificationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChallengeDetailUiState>(ChallengeDetailUiState.Loading)

    private val _modalEffect = MutableStateFlow<ChallengeDetailModalEffect>(ChallengeDetailModalEffect.Hidden)
    val modalEffect: StateFlow<ChallengeDetailModalEffect> get() = _modalEffect

    private val _uiEffect = MutableSharedFlow<ChallengeDetailUiEffect>()
    val uiEffect: SharedFlow<ChallengeDetailUiEffect> = _uiEffect.asSharedFlow()

    /**
     * 구독 시점에 조회한다. 인증을 마치고 돌아오면 상세 entry 가 다시 구독되며 재조회가 걸려,
     * 방금 올린 인증이 CTA·사진·뱃지에 반영된다 — Nav3 는 백스택에 남은 entry 의 ViewModel 을
     * 유지하므로 `init` 로드로는 돌아왔을 때 갱신 지점이 없다.
     */
    val uiState: StateFlow<ChallengeDetailUiState> = _uiState
        .onStart { load() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(0),
            initialValue = ChallengeDetailUiState.Loading,
        )

    private var headlineTicker: Job? = null
    private var loadJob: Job? = null

    fun retry() {
        load()
    }

    /** 다이얼로그를 여는 것이 전부다 — 확인 없이는 기권이 발생하지 않아야 한다. */
    fun requestForfeit() {
        _modalEffect.update { ChallengeDetailModalEffect.ConfirmForfeit }
    }

    fun forfeit() {
        if (isForfeiting()) return
        markForfeiting()
        dismiss()

        viewModelScope.launch {
            challengeRepository.forfeitChallenge(
                challengeId = challengeId,
                onSuccess = {},
                onError = { message -> emitEffect(ChallengeDetailUiEffect.ShowMessage(message)) },
            )

            // 성공 응답 body 로 부분 갱신하지 않는다 — 상세 화면을 세우는 경로가 두 벌이 되고, 두
            // 경로가 갈리는 날 컴파일러도 테스트도 잡지 못한다. forfeitedBy 도 어차피 상세 재조회로만 온다.
            //
            // 실패에도 다시 부르는 이유: 이 화면은 진입 시점 스냅샷을 들고 오래 떠 있는다(그래서
            // 헤드라인 티커가 있다). 서버가 거부하는 사유 대부분 — 이미 끝났다 / 이미 인증했다 — 은
            // 그 스냅샷이 낡았다는 뜻이다. 다시 받지 않으면 사라졌어야 할 기권 진입점이 그대로 남아
            // 사용자가 같은 실패를 반복한다. 성공·실패를 한 경로로 두는 편이 분기도 하나 적다.
            //
            // isForfeiting 을 따로 되돌리지 않는 것도 같은 이유다 — load() 가 상세를 다시 세우면서
            // false 로 확정된다.
            load()
        }
    }

    fun dismiss() {
        _modalEffect.update { ChallengeDetailModalEffect.Hidden }
    }

    private fun isForfeiting(): Boolean =
        (_uiState.value as? ChallengeDetailUiState.Data)?.isForfeiting == true

    /** 되돌리는 짝이 없다 — [load] 가 상세를 다시 세우면서 `false` 로 확정한다. */
    private fun markForfeiting() {
        _uiState.update { current ->
            if (current is ChallengeDetailUiState.Data) current.copy(isForfeiting = true) else current
        }
    }

    private fun emitEffect(effect: ChallengeDetailUiEffect) {
        viewModelScope.launch {
            _uiEffect.emit(effect)
        }
    }

    private fun load() {
        // 재구독마다 새 로드가 얹히면 늦게 끝난 이전 응답이 최신 결과를 덮어쓴다.
        loadJob?.cancel()

        // 재조회 중에도 이미 세운 계약서는 지우지 않는다 — 갱신 때마다 화면이 스피너로 비워지면
        // 앱을 잠깐 벗어났다 돌아오는 것만으로도 읽던 내용이 사라진다.
        _uiState.update { current ->
            if (current is ChallengeDetailUiState.Data) current else ChallengeDetailUiState.Loading
        }

        loadJob = viewModelScope.launch {
            var loaded: ChallengeDetail? = null
            challengeRepository.getChallengeDetail(
                challengeId = challengeId,
                onSuccess = { loaded = it },
                onError = { message -> _uiState.update { ChallengeDetailUiState.Error(message) } },
            )
            val detail = loaded ?: return@launch

            // 상세를 먼저 그려 두고 인증 현황이 도착할 때 다시 갱신하면 하단 슬롯이 CTA 에서 사진으로
            // 바뀌며 손가락 아래에서 버튼이 사라진다. 두 조회를 다 마친 뒤 한 번에 세운다.
            val verifications = loadVerificationSection()
            _uiState.update { detail.toData(verifications) }
            startHeadlineTicker(detail.deadline, detail.myResult)
        }
    }

    /**
     * 헤더 표기를 [HEADLINE_TICK_INTERVAL] 주기로 다시 만든다. 화면이 오래 떠 있어도 "3시간 12분" 이
     * 처음 그려진 값 그대로 굳지 않는다.
     *
     * 마감을 넘긴 회차까지는 반영하고(URGENT → NEUTRAL) 루프를 끝낸다. 그 뒤로 이 자리에 남는 것은
     * "판정 대기 중" 아니면 이미 실려 있는 결과인데, 둘 다 시계로는 더 바뀌지 않는다 — 판정이 나중에
     * 붙는 것은 티커가 아니라 재구독 재조회가 가져온다.
     *
     * @param result 매 회차 같은 값을 그대로 다시 넘긴다. 빠뜨리면 티커가 한 번 도는 것만으로 결과
     *   문구가 시간 표기로 되돌아간다.
     */
    private fun startHeadlineTicker(deadline: LocalDateTime, result: ChallengeOutcome?) {
        // retry() 로 두 번째 티커가 얹히면 같은 state 를 두 루프가 갱신한다.
        headlineTicker?.cancel()
        headlineTicker = viewModelScope.launch {
            while (nowKst() < deadline) {
                delay(HEADLINE_TICK_INTERVAL)
                refreshHeadline(deadline, result)
            }
        }
    }

    private fun refreshHeadline(deadline: LocalDateTime, result: ChallengeOutcome?) {
        _uiState.update { current ->
            // retry() 직후 아직 살아 있는 티커가 여기로 들어온다. Loading/Error 에는 갱신할 헤더
            // 표기 자체가 없다.
            if (current !is ChallengeDetailUiState.Data) return@update current

            val headline = headlineDisplay(result, deadline, nowKst())

            // MutableStateFlow 가 같은 값을 흡수해 주지만 그 흡수에 기대지 않고 여기서 직접 거른다.
            // Data 에 람다처럼 구조적 동등성이 성립하지 않는 필드가 하나만 붙어도 흡수가 조용히
            // 풀려, 표기가 그대로인 분에도 화면 전체가 매분 다시 그려지기 시작한다.
            if (headline.text == current.headlineText && headline.tone == current.headlineTone) {
                current
            } else {
                current.copy(headlineText = headline.text, headlineTone = headline.tone)
            }
        }
    }

    /**
     * 모르면 `null` 이고 하단 슬롯은 [ChallengeDetailUiState.Data.canVerify] 만 따른다.
     * 계약서만으로 화면이 성립하므로 조회 실패를 [ChallengeDetailUiState.Error] 로 올리지 않는다.
     */
    private suspend fun loadVerificationSection(): VerificationSection? {
        var section: VerificationSection? = null
        verificationRepository.getVerifications(
            challengeId = challengeId,
            onSuccess = { verifications -> section = verifications.toVerificationSection() },
            // 사용자가 할 수 있는 조치가 없어 알리지 않는다.
            onError = {},
        )
        return section
    }

    private fun ChallengeVerifications.toVerificationSection(): VerificationSection = VerificationSection(
        myStatus = me.status,
        myPhotoUrl = me.photoUrl,
        myPhotoDeleted = me.photoDeleted,
        myVerifiedAt = me.verifiedAt,
        myAiReason = me.aiReason,
        opponentStatus = opponent.status,
        opponentPhotoUrl = opponent.photoUrl,
        opponentPhotoDeleted = opponent.photoDeleted,
        opponentVerifiedAt = opponent.verifiedAt,
        opponentAiReason = opponent.aiReason,
    )

    private fun ChallengeDetail.toData(verifications: VerificationSection?): ChallengeDetailUiState.Data {
        // 시계는 ViewModel 에서만 읽는다 — 여기서 1회, 이후로는 티커가 1분마다 1회.
        val headline = headlineDisplay(myResult, deadline, nowKst())

        return ChallengeDetailUiState.Data(
            myMission = me.mission,
            opponentNickname = opponent.nickname,
            opponentMission = opponent.mission,
            betContent = betContent,
            headlineText = headline.text,
            headlineTone = headline.tone,
            deadlineAbsoluteText = deadlineAbsoluteText(challengeDate, deadline),
            signatures = contract?.toSignatureSection(),
            verifications = verifications,
            canVerify = status == STATUS_IN_PROGRESS,
            forfeitNoticeText = forfeitNotice(),
            isForfeiting = false,
        )
    }

    private fun ChallengeDetail.forfeitNotice(): String? = when (forfeitedBy) {
        null -> null
        ChallengeSide.ME -> FORFEIT_BY_ME_TEXT
        ChallengeSide.OPPONENT -> "${opponent.nickname}$FORFEIT_BY_OTHER_SUFFIX"
    }

    private fun Contract.toSignatureSection(): SignatureSection = SignatureSection(
        mySignature = mySignature,
        mySignatureHint = signatureHint(mySignatureState),
        opponentSignature = opponentSignature,
        opponentSignatureHint = signatureHint(opponentSignatureState),
        isFinalized = isFinalized,
    )

    /** [SignatureSlotState.UNREADABLE] 은 서명을 한 사람이라 '서명 대기 중' 으로 그리면 거짓이 된다. */
    private fun signatureHint(state: SignatureSlotState): String = when (state) {
        SignatureSlotState.UNREADABLE -> SIGNATURE_UNREADABLE_HINT
        SignatureSlotState.NOT_SIGNED, SignatureSlotState.SIGNED -> SIGNATURE_NOT_SIGNED_HINT
    }
}
