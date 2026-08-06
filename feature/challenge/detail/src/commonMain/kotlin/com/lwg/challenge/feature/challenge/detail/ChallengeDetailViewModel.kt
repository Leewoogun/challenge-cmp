package com.lwg.challenge.feature.challenge.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lwg.challenge.domain.model.challenge.ChallengeDetail
import com.lwg.challenge.domain.model.challenge.Contract
import com.lwg.challenge.domain.model.challenge.SignatureSlotState
import com.lwg.challenge.domain.model.user.CacheStrategy
import com.lwg.challenge.domain.repository.ChallengeRepository
import com.lwg.challenge.domain.repository.UserInfoRepository
import com.lwg.challenge.feature.challenge.detail.contract.ChallengeDetailUiState
import com.lwg.challenge.feature.challenge.detail.contract.MY_LABEL
import com.lwg.challenge.feature.challenge.detail.contract.MY_MISSION_LABEL
import com.lwg.challenge.feature.challenge.detail.contract.SIGNATURE_NOT_SIGNED_HINT
import com.lwg.challenge.feature.challenge.detail.contract.SIGNATURE_UNREADABLE_HINT
import com.lwg.challenge.feature.challenge.detail.contract.SignatureSection
import com.lwg.challenge.utils.datetime.nowKst
import com.lwg.challenge.utils.datetime.toRelativeKoreanString
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam

@KoinViewModel
class ChallengeDetailViewModel(
    @InjectedParam private val challengeId: Long,
    private val challengeRepository: ChallengeRepository,
    private val userInfoRepository: UserInfoRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChallengeDetailUiState>(ChallengeDetailUiState.Loading)
    val uiState: StateFlow<ChallengeDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun retry() {
        load()
    }

    private fun load() {
        _uiState.update { ChallengeDetailUiState.Loading }

        viewModelScope.launch {
            val myUserId = myUserId()

            challengeRepository.getChallengeDetail(
                challengeId = challengeId,
                onSuccess = { detail -> _uiState.update { detail.toData(myUserId) } },
                onError = { message -> _uiState.update { ChallengeDetailUiState.Error(message) } },
            )
        }
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
    private fun ChallengeDetail.toData(myUserId: Long?): ChallengeDetailUiState.Data {
        // 시계는 매핑 시점 1회만 읽는다. Composable 에서 읽으면 recomposition 마다 값이 흔들린다.
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
            deadlineText = deadline.toRelativeKoreanString(now),
            // 계약서가 없으면(soul-oath 이전 생성) 서명 묶음 자체가 없다. 빈 값으로 채우면 있지도
            // 않은 맹세를 '서명 대기 중' 으로 그리게 된다.
            signatures = contract?.toSignatureSection(isMeOpponent),
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
