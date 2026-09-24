package com.lwg.challenge.feature.challenge.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lwg.challenge.domain.model.challenge.ChallengeDetail
import com.lwg.challenge.domain.model.challenge.Contract
import com.lwg.challenge.domain.model.challenge.SignatureSlotState
import com.lwg.challenge.domain.model.challenge.outcomeFor
import com.lwg.challenge.domain.model.user.CacheStrategy
import com.lwg.challenge.domain.model.verification.ChallengeVerifications
import com.lwg.challenge.domain.repository.ChallengeRepository
import com.lwg.challenge.domain.repository.UserInfoRepository
import com.lwg.challenge.domain.repository.VerificationRepository
import com.lwg.challenge.feature.challenge.detail.contract.ChallengeDetailModalEffect
import com.lwg.challenge.feature.challenge.detail.contract.ChallengeDetailUiEffect
import com.lwg.challenge.feature.challenge.detail.contract.ChallengeDetailUiState
import com.lwg.challenge.feature.challenge.detail.contract.FORFEIT_BY_ME_TEXT
import com.lwg.challenge.feature.challenge.detail.contract.FORFEIT_BY_OTHER_SUFFIX
import com.lwg.challenge.feature.challenge.detail.contract.FORFEIT_UNKNOWN_ACTOR_TEXT
import com.lwg.challenge.feature.challenge.detail.contract.MY_LABEL
import com.lwg.challenge.feature.challenge.detail.contract.MY_MISSION_LABEL
import com.lwg.challenge.feature.challenge.detail.contract.ResultView
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
import kotlinx.coroutines.flow.firstOrNull
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
    private val userInfoRepository: UserInfoRepository,
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
            val myUserId = myUserId()

            var loaded: ChallengeDetail? = null
            challengeRepository.getChallengeDetail(
                challengeId = challengeId,
                onSuccess = { loaded = it },
                onError = { message -> _uiState.update { ChallengeDetailUiState.Error(message) } },
            )
            val detail = loaded ?: return@launch

            // 상세를 먼저 그려 두고 인증 현황이 도착할 때 다시 갱신하면 하단 슬롯이 CTA 에서 사진으로
            // 바뀌며 손가락 아래에서 버튼이 사라진다. 두 조회를 다 마친 뒤 한 번에 세운다.
            val verifications = loadVerificationSection(myUserId)
            _uiState.update { detail.toData(myUserId, verifications) }
            startHeadlineTicker(detail.deadline, detail.resultView(myUserId))
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
    private fun startHeadlineTicker(deadline: LocalDateTime, result: ResultView?) {
        // retry() 로 두 번째 티커가 얹히면 같은 state 를 두 루프가 갱신한다.
        headlineTicker?.cancel()
        headlineTicker = viewModelScope.launch {
            while (nowKst() < deadline) {
                delay(HEADLINE_TICK_INTERVAL)
                refreshHeadline(deadline, result)
            }
        }
    }

    private fun refreshHeadline(deadline: LocalDateTime, result: ResultView?) {
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
     * 인증 현황을 '나 / 상대' 로 세운다. 모르면 `null` 이고 하단 슬롯은 [ChallengeDetailUiState.Data.canVerify] 만
     * 따른다 — 진행 중이면 인증 CTA, 아니면 빈칸.
     *
     * 조회 실패를 [ChallengeDetailUiState.Error] 로 승격하지 않는 이유는 계약서만으로 화면이 이미
     * 성립하기 때문이다 — 없는 건 사진과 뱃지뿐이다.
     */
    private suspend fun loadVerificationSection(myUserId: Long?): VerificationSection? {
        // 관점을 모르면 어느 칸이 내 것인지 집을 수 없다. 받아 와도 '나/상대' 로 세울 수 없어 부르지 않는다.
        if (myUserId == null) return null

        var section: VerificationSection? = null
        verificationRepository.getVerifications(
            challengeId = challengeId,
            onSuccess = { verifications -> section = verifications.toVerificationSection(myUserId) },
            // 사용자가 할 수 있는 조치가 없어 알리지 않는다.
            onError = {},
        )
        return section
    }

    private fun ChallengeVerifications.toVerificationSection(myUserId: Long): VerificationSection? {
        val mine = partyOf(myUserId) ?: return null
        val other = if (mine.userId == challenger.userId) opponent else challenger

        return VerificationSection(
            myStatus = mine.status,
            myPhotoUrl = mine.photoUrl,
            myPhotoDeleted = mine.photoDeleted,
            myVerifiedAt = mine.verifiedAt,
            myAiReason = mine.aiReason,
            opponentStatus = other.status,
            opponentPhotoUrl = other.photoUrl,
            opponentPhotoDeleted = other.photoDeleted,
            opponentVerifiedAt = other.verifiedAt,
            opponentAiReason = other.aiReason,
        )
    }

    /**
     * 내 userId. 실패하면 `null` 이고 화면은 그대로 진행한다 — 관점 미상 fallback 이 흡수한다.
     *
     * 조회 실패를 스낵바로 알리지 않는 이유는 사용자가 할 수 있는 게 없기 때문이다. 계약서는
     * 여전히 정확하게(역할 그대로) 보인다.
     */
    private suspend fun myUserId(): Long? =
        userInfoRepository
            .getUserInfo(onError = {}, cacheStrategy = CacheStrategy.CACHE_FIRST)
            .firstOrNull()
            ?.id

    /**
     * 서버는 [ChallengeDetail.challenger] / [ChallengeDetail.opponent] 를 역할 그대로 준다.
     * 화면은 '나 / 상대' 로 보여줘야 하므로 내 userId 로 어느 쪽이 나인지 여기서 판정한다.
     */
    private fun ChallengeDetail.toData(
        myUserId: Long?,
        verifications: VerificationSection?,
    ): ChallengeDetailUiState.Data {
        // 시계는 ViewModel 에서만 읽는다 — 여기서 1회, 이후로는 티커가 1분마다 1회.
        // Composable 에서 읽으면 recomposition 마다 시계를 보게 돼 갱신 주기가 렌더 사정에 끌려간다.
        // 티커는 그 지점을 ViewModel 에 묶어 둔 채 주기만 명시적으로 고정한 것이다.
        val now = nowKst()

        val isMeOpponent = isMeOpponent(myUserId)
        val isPerspectiveKnown = isPerspectiveKnown(myUserId)

        val me = if (isMeOpponent) opponent else challenger
        val other = if (isMeOpponent) challenger else opponent

        // 관점을 모르면(userInfo 실패·양쪽 어디와도 불일치) 챌린저를 "나" 라고 부르지 않는다.
        // 서명 아래 라벨이 "나" 인데 실제로는 상대 서명이면 계약서를 잘못 읽게 만든다.
        // 계약이 역할 기준으로 내려오는 것과 같은 이유로, 모를 땐 역할 그대로 실명으로 보여준다.
        // 서명 라벨과 계약서 미션 라벨은 한 카드에 같이 놓이므로 한 분기에서 같이 확정한다 —
        // 따로 계산하면 한쪽만 고쳐서 "우건" 서명 위에 "나의 미션" 이 다시 붙는다.
        val (myLabel, myMissionLabel) = if (isPerspectiveKnown) {
            MY_LABEL to MY_MISSION_LABEL
        } else {
            challenger.nickname to "${challenger.nickname}의 미션"
        }

        val headline = headlineDisplay(resultView(myUserId), deadline, now)

        return ChallengeDetailUiState.Data(
            myLabel = myLabel,
            myMissionLabel = myMissionLabel,
            myMission = me.mission,
            opponentNickname = other.nickname,
            opponentMission = other.mission,
            betContent = betContent,
            headlineText = headline.text,
            headlineTone = headline.tone,
            deadlineAbsoluteText = deadlineAbsoluteText(challengeDate, deadline),
            // 계약서가 없으면(soul-oath 이전 생성) 서명 묶음 자체가 없다. 빈 값으로 채우면 있지도
            // 않은 맹세를 '서명 대기 중' 으로 그리게 된다.
            signatures = contract?.toSignatureSection(isMeOpponent),
            verifications = verifications,
            canVerify = status == STATUS_IN_PROGRESS,
            forfeitNoticeText = forfeitNotice(myUserId),
            isForfeiting = false,
        )
    }

    /**
     * 기권으로 끝난 챌린지에 붙일 안내 한 줄. 기권이 아니면 `null` 이고 그 자리가 통째로 빠진다.
     *
     * [myUserId] 비교를 먼저 두는 것이 계약이다 — 관점을 알 때만 "내가" 로 말하고, 관점을 모르면
     * (`myUserId == null`) 자연히 실명 분기로 떨어진다. 라벨·헤드라인이 관점을 모를 때 "나" 를 버리고
     * 실명으로 낮추는 것과 같은 판단이다.
     */
    private fun ChallengeDetail.forfeitNotice(myUserId: Long?): String? = when (forfeitedBy) {
        null -> null
        myUserId -> FORFEIT_BY_ME_TEXT
        challenger.userId -> "${challenger.nickname}$FORFEIT_BY_OTHER_SUFFIX"
        opponent.userId -> "${opponent.nickname}$FORFEIT_BY_OTHER_SUFFIX"

        // 양측 어느 userId 와도 맞지 않으면 닉네임을 지어낼 수 없다. 없는 이름을 날조하는 대신
        // 아는 사실만 말한다.
        else -> FORFEIT_UNKNOWN_ACTOR_TEXT
    }

    /**
     * 관점 판정. 라벨·서명·인증 칸·결과 문구가 모두 이 하나에 매달려 있어 식을 따로 적지 않는다 —
     * 한쪽만 고치면 "우건" 서명 위에 "나의 미션" 이 붙는 것과 같은 어긋남이 결과 표기에서도 난다.
     */
    private fun ChallengeDetail.isMeOpponent(myUserId: Long?): Boolean =
        myUserId != null && myUserId == opponent.userId

    private fun ChallengeDetail.isPerspectiveKnown(myUserId: Long?): Boolean =
        isMeOpponent(myUserId) || (myUserId != null && myUserId == challenger.userId)

    /**
     * 판정 결과를 어느 관점으로 말할지 정한다. 아직 판정 전이면 `null` 이고 헤더는 시간 표기로 남는다.
     *
     * 관점을 모르면 승패를 내 것으로 말할 수 없어 역할 실명으로 낮춘다 — 서명·미션 라벨이 같은
     * 상황에서 "나" 를 버리는 것과 같은 판단이다.
     */
    private fun ChallengeDetail.resultView(myUserId: Long?): ResultView? {
        val result = this.result ?: return null

        return if (isPerspectiveKnown(myUserId)) {
            ResultView.Mine(result.outcomeFor(isMeOpponent(myUserId)))
        } else {
            ResultView.ByRole(
                result = result,
                challengerNickname = challenger.nickname,
                opponentNickname = opponent.nickname,
            )
        }
    }

    private fun Contract.toSignatureSection(isMeOpponent: Boolean): SignatureSection = SignatureSection(
        mySignature = if (isMeOpponent) opponentSignature else challengerSignature,
        mySignatureHint = signatureHint(
            if (isMeOpponent) opponentSignatureState else challengerSignatureState,
        ),
        opponentSignature = if (isMeOpponent) challengerSignature else opponentSignature,
        opponentSignatureHint = signatureHint(
            if (isMeOpponent) challengerSignatureState else opponentSignatureState,
        ),
        isFinalized = isFinalized,
    )

    /**
     * 서명이 비어 보이는 이유를 문구로 확정한다.
     *
     * [SignatureSlotState.UNREADABLE] 은 **서명을 한 사람**이다. 이걸 '서명 대기 중' 으로 그리면
     * 사용자에게 거짓을 보여주는 것이라 문구를 갈라 놓는다.
     */
    private fun signatureHint(state: SignatureSlotState): String = when (state) {
        SignatureSlotState.UNREADABLE -> SIGNATURE_UNREADABLE_HINT
        SignatureSlotState.NOT_SIGNED, SignatureSlotState.SIGNED -> SIGNATURE_NOT_SIGNED_HINT
    }
}
