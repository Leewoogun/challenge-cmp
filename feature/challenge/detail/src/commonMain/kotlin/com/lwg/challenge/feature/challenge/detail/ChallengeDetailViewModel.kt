package com.lwg.challenge.feature.challenge.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lwg.challenge.domain.model.VerificationStatus
import com.lwg.challenge.domain.model.challenge.ChallengeDetail
import com.lwg.challenge.domain.model.challenge.Contract
import com.lwg.challenge.domain.model.challenge.SignatureSlotState
import com.lwg.challenge.domain.model.user.CacheStrategy
import com.lwg.challenge.domain.model.verification.ChallengeVerifications
import com.lwg.challenge.domain.repository.ChallengeRepository
import com.lwg.challenge.domain.repository.UserInfoRepository
import com.lwg.challenge.domain.repository.VerificationRepository
import com.lwg.challenge.feature.challenge.detail.contract.ChallengeDetailUiState
import com.lwg.challenge.feature.challenge.detail.contract.MY_LABEL
import com.lwg.challenge.feature.challenge.detail.contract.MY_MISSION_LABEL
import com.lwg.challenge.feature.challenge.detail.contract.SIGNATURE_NOT_SIGNED_HINT
import com.lwg.challenge.feature.challenge.detail.contract.SIGNATURE_UNREADABLE_HINT
import com.lwg.challenge.feature.challenge.detail.contract.STATUS_IN_PROGRESS
import com.lwg.challenge.feature.challenge.detail.contract.SignatureSection
import com.lwg.challenge.feature.challenge.detail.contract.VerificationSection
import com.lwg.challenge.feature.challenge.detail.contract.deadlineDisplayText
import com.lwg.challenge.feature.challenge.detail.contract.deadlineDisplayTone
import com.lwg.challenge.utils.datetime.deadlineAbsoluteText
import com.lwg.challenge.utils.datetime.nowKst
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
private val DEADLINE_TICK_INTERVAL = 1.minutes

@KoinViewModel
class ChallengeDetailViewModel(
    @InjectedParam private val challengeId: Long,
    private val challengeRepository: ChallengeRepository,
    private val userInfoRepository: UserInfoRepository,
    private val verificationRepository: VerificationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChallengeDetailUiState>(ChallengeDetailUiState.Loading)

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

    private var deadlineTicker: Job? = null
    private var loadJob: Job? = null

    fun retry() {
        load()
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

            // 상세를 먼저 그려 두고 인증 현황이 도착할 때 다시 갱신하면 canVerify 가 true -> false 로
            // 뒤집히며 손가락 아래에서 CTA 가 사라진다. 두 조회를 다 마친 뒤 한 번에 세운다.
            val verifications = loadVerificationSection(myUserId)
            _uiState.update { detail.toData(myUserId, verifications) }
            startDeadlineTicker(detail.deadline)
        }
    }

    /**
     * 남은 시간 표기를 [DEADLINE_TICK_INTERVAL] 주기로 다시 만든다. 화면이 오래 떠 있어도
     * "3시간 12분" 이 처음 그려진 값 그대로 굳지 않는다.
     *
     * 마감을 넘긴 회차까지는 반영하고(URGENT → NEUTRAL) 루프를 끝낸다. 그 뒤로는 "판정 대기 중" 에서
     * 더 바뀔 것이 없어, 계속 깨워 봐야 같은 값을 다시 계산할 뿐이다.
     */
    private fun startDeadlineTicker(deadline: LocalDateTime) {
        // retry() 로 두 번째 티커가 얹히면 같은 state 를 두 루프가 갱신한다.
        deadlineTicker?.cancel()
        deadlineTicker = viewModelScope.launch {
            while (nowKst() < deadline) {
                delay(DEADLINE_TICK_INTERVAL)
                refreshDeadlineDisplay(deadline)
            }
        }
    }

    private fun refreshDeadlineDisplay(deadline: LocalDateTime) {
        _uiState.update { current ->
            // retry() 직후 아직 살아 있는 티커가 여기로 들어온다. Loading/Error 에는 갱신할 마감
            // 표기 자체가 없다.
            if (current !is ChallengeDetailUiState.Data) return@update current

            val now = nowKst()
            val text = deadlineDisplayText(deadline, now)
            val tone = deadlineDisplayTone(deadline, now)

            // MutableStateFlow 가 같은 값을 흡수해 주지만 그 흡수에 기대지 않고 여기서 직접 거른다.
            // Data 에 람다처럼 구조적 동등성이 성립하지 않는 필드가 하나만 붙어도 흡수가 조용히
            // 풀려, 표기가 그대로인 분에도 화면 전체가 매분 다시 그려지기 시작한다.
            if (text == current.deadlineText && tone == current.deadlineTone) {
                current
            } else {
                current.copy(deadlineText = text, deadlineTone = tone)
            }
        }
    }

    /**
     * 인증 현황을 '나 / 상대' 로 세운다. 모르면 `null` 이고 `canVerify` 가 노출 쪽으로 폴백한다.
     *
     * 조회 실패를 [ChallengeDetailUiState.Error] 로 승격하지 않는 이유는 계약서만으로 화면이 이미
     * 성립하기 때문이다 — 없는 건 사진과 게이트 정보뿐이다.
     */
    private suspend fun loadVerificationSection(myUserId: Long?): VerificationSection? {
        // 관점을 모르면 어느 칸이 내 것인지 집을 수 없다. 받아 와도 '나/상대' 로 세울 수 없어 부르지 않는다.
        if (myUserId == null) return null

        var section: VerificationSection? = null
        verificationRepository.getVerifications(
            challengeId = challengeId,
            onSuccess = { verifications -> section = verifications.toVerificationSection(myUserId) },
            // 사용자가 할 수 있는 조치가 없어 알리지 않는다. 폴백이 흡수한다.
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
            opponentStatus = other.status,
            opponentPhotoUrl = other.photoUrl,
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

        val isMeOpponent = myUserId != null && myUserId == opponent.userId
        val isPerspectiveKnown = isMeOpponent || (myUserId != null && myUserId == challenger.userId)

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

        return ChallengeDetailUiState.Data(
            myLabel = myLabel,
            myMissionLabel = myMissionLabel,
            myMission = me.mission,
            opponentNickname = other.nickname,
            opponentMission = other.mission,
            betContent = betContent,
            deadlineText = deadlineDisplayText(deadline, now),
            deadlineTone = deadlineDisplayTone(deadline, now),
            deadlineAbsoluteText = deadlineAbsoluteText(challengeDate, deadline),
            // 계약서가 없으면(soul-oath 이전 생성) 서명 묶음 자체가 없다. 빈 값으로 채우면 있지도
            // 않은 맹세를 '서명 대기 중' 으로 그리게 된다.
            signatures = contract?.toSignatureSection(isMeOpponent),
            verifications = verifications,
            // 인증 여부를 모를 때(조회 실패·관점 미상) 숨기는 쪽으로 폴백하면, 아직 인증하지 않은
            // 사용자가 일시적 오류 때문에 주 액션 자체를 잃는다. 노출로 폴백하면 최악이 이미 인증한
            // 사용자의 중복 시도이고 서버가 정확히 거부한다 — 막는 실패보다 중복 시도 실패가 낫다.
            canVerify = status == STATUS_IN_PROGRESS &&
                verifications?.myStatus != VerificationStatus.VERIFIED,
        )
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
