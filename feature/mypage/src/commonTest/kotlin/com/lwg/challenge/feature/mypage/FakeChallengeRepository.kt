package com.lwg.challenge.feature.mypage

import com.lwg.challenge.domain.model.challenge.ChallengeCreateInput
import com.lwg.challenge.domain.model.challenge.ChallengeDetail
import com.lwg.challenge.domain.model.challenge.ChallengeHistory
import com.lwg.challenge.domain.model.challenge.ReceivedChallenge
import com.lwg.challenge.domain.model.signature.Signature
import com.lwg.challenge.domain.repository.ChallengeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow

class FakeChallengeRepository : ChallengeRepository {

    var histories: List<ChallengeHistory>? = null
    var error: String? = null

    /** 항목 일부만 매핑에 실패한 경우. [error] 와 달리 알린 뒤에도 성공분 목록이 그대로 온다. */
    var partialError: String? = null

    var callCount: Int = 0
        private set

    // 전체 실패에 emit 하지 않는 것이 실제 구현 계약이다. 빈 목록을 흘리면 조회 실패가 "0건" 으로
    // 둔갑해, 실패를 빈 상태로 그리지 않는다는 판정이 테스트에서 통과해 버린다.
    //
    // onError 하나가 두 사건을 나르고 emit 이 뒤따르는지로만 갈린다는 것도 실제 구현 계약이다
    // (ChallengeRemoteDataSourceImpl.getChallengeHistories) — 그 갈림을 여기서 그대로 재현한다.
    override fun getChallengeHistories(
        onError: (String) -> Unit,
    ): Flow<List<ChallengeHistory>> = flow {
        callCount += 1
        val current = histories
        val currentError = error
        when {
            currentError != null -> onError(currentError)
            current != null -> {
                partialError?.let(onError)
                emit(current)
            }
        }
    }

    override fun getReceivedChallenges(
        onError: (String) -> Unit,
    ): Flow<List<ReceivedChallenge>> = emptyFlow()

    override suspend fun createChallenge(
        input: ChallengeCreateInput,
        onSuccess: (challengeId: Long) -> Unit,
        onError: (String) -> Unit,
    ) = Unit

    override suspend fun getChallengeDetail(
        challengeId: Long,
        onSuccess: (ChallengeDetail) -> Unit,
        onError: (String) -> Unit,
    ) = Unit

    override suspend fun acceptChallenge(
        challengeId: Long,
        myMission: String,
        signature: Signature,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) = Unit

    override suspend fun rejectChallenge(
        challengeId: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) = Unit

    override suspend fun cancelChallenge(
        challengeId: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) = Unit
}
