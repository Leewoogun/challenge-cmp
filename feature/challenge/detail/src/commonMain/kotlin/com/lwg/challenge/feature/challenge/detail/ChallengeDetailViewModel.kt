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
import com.lwg.challenge.utils.flow.updateWithData
import com.lwg.challenge.utils.flow.withData
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

/** 남은 시간 표기가 분 단위다. */
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

    // Nav3 가 백스택 entry 의 ViewModel 을 유지하므로, 인증 후 복귀 시 재조회되도록 init 대신 구독 시점에 로드한다.
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

    fun requestForfeit() {
        _modalEffect.update { ChallengeDetailModalEffect.ConfirmForfeit }
    }

    fun forfeit() {
        _uiState.withData<ChallengeDetailUiState.Data> { if (it.isForfeiting) return }
        markForfeiting()
        dismiss()

        viewModelScope.launch {
            challengeRepository.forfeitChallenge(
                challengeId = challengeId,
                onSuccess = {},
                onError = { message -> emitEffect(ChallengeDetailUiEffect.ShowMessage(message)) },
            )

            // 실패 사유 대부분(이미 끝남/이미 인증)은 화면이 낡았다는 뜻이라 성공·실패 모두 재조회한다.
            load()
        }
    }

    fun dismiss() {
        _modalEffect.update { ChallengeDetailModalEffect.Hidden }
    }

    /** 해제는 [load] 가 상세를 다시 세우며 한다. */
    private fun markForfeiting() {
        _uiState.updateWithData<ChallengeDetailUiState, ChallengeDetailUiState.Data> {
            it.copy(isForfeiting = true)
        }
    }

    private fun emitEffect(effect: ChallengeDetailUiEffect) {
        viewModelScope.launch {
            _uiEffect.emit(effect)
        }
    }

    private fun load() {
        // 늦게 끝난 이전 응답이 최신 결과를 덮어쓰지 않게 한다.
        loadJob?.cancel()

        // 재조회 중에도 보던 내용을 스피너로 지우지 않는다.
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

            // 나눠 그리면 하단 슬롯이 CTA → 사진으로 바뀌며 버튼이 사라지므로 두 조회 후 한 번에 세운다.
            val verifications = loadVerificationSection()
            _uiState.update { detail.toData(verifications) }
            startHeadlineTicker(detail.deadline, detail.myResult)
        }
    }

    /** 마감 이후엔 시계로 바뀔 표기가 없어 마감을 넘긴 회차까지만 돈다. */
    private fun startHeadlineTicker(deadline: LocalDateTime, result: ChallengeOutcome?) {
        headlineTicker?.cancel()
        headlineTicker = viewModelScope.launch {
            while (nowKst() < deadline) {
                delay(HEADLINE_TICK_INTERVAL)
                refreshHeadline(deadline, result)
            }
        }
    }

    private fun refreshHeadline(deadline: LocalDateTime, result: ChallengeOutcome?) {
        _uiState.updateWithData<ChallengeDetailUiState, ChallengeDetailUiState.Data> { current ->
            val headline = headlineDisplay(result, deadline, nowKst())

            // Data 의 동등성에 기대지 않고 직접 걸러 표기가 같으면 재구성을 막는다.
            if (headline.text == current.headlineText && headline.tone == current.headlineTone) {
                current
            } else {
                current.copy(headlineText = headline.text, headlineTone = headline.tone)
            }
        }
    }

    /** 계약서만으로 화면이 성립하므로 실패해도 Error 로 올리거나 알리지 않는다. */
    private suspend fun loadVerificationSection(): VerificationSection? {
        var section: VerificationSection? = null
        verificationRepository.getVerifications(
            challengeId = challengeId,
            onSuccess = { verifications -> section = verifications.toVerificationSection() },
            onError = {},
        )
        return section
    }

    private fun ChallengeVerifications.toVerificationSection(): VerificationSection = VerificationSection(
        myStatus = myVerification.status,
        myPhotoUrl = myVerification.photoUrl,
        myPhotoDeleted = myVerification.photoDeleted,
        myVerifiedAt = myVerification.verifiedAt,
        myAiReason = myVerification.aiReason,
        opponentStatus = opponentVerification.status,
        opponentPhotoUrl = opponentVerification.photoUrl,
        opponentPhotoDeleted = opponentVerification.photoDeleted,
        opponentVerifiedAt = opponentVerification.verifiedAt,
        opponentAiReason = opponentVerification.aiReason,
    )

    private fun ChallengeDetail.toData(verifications: VerificationSection?): ChallengeDetailUiState.Data {
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

    /** UNREADABLE 은 이미 서명한 칸이라 '서명 대기 중' 으로 쓰면 안 된다. */
    private fun signatureHint(state: SignatureSlotState): String = when (state) {
        SignatureSlotState.UNREADABLE -> SIGNATURE_UNREADABLE_HINT
        SignatureSlotState.NOT_SIGNED, SignatureSlotState.SIGNED -> SIGNATURE_NOT_SIGNED_HINT
    }
}
