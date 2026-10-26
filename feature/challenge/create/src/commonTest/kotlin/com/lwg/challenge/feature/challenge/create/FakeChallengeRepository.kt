package com.lwg.challenge.feature.challenge.create

import com.lwg.challenge.domain.model.challenge.ChallengeArchive
import com.lwg.challenge.domain.model.challenge.ChallengeCreateInput
import com.lwg.challenge.domain.model.challenge.ChallengeDetail
import com.lwg.challenge.domain.model.challenge.ReceivedChallenge
import com.lwg.challenge.domain.model.signature.Signature
import com.lwg.challenge.domain.repository.ChallengeRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.datetime.YearMonth

/**
 * 테스트용 [ChallengeRepository] 페이크. `CreateChallengeUseCase` 주입용.
 *
 * [createError] 로 성공/실패를 고르고, 전달된 [ChallengeCreateInput] 을 [createCalls] 에 기록한다.
 */
class FakeChallengeRepository : ChallengeRepository {

    /** createChallenge 실패 메시지. null 이면 [createdChallengeId] 로 성공. */
    var createError: String? = null

    var createdChallengeId: Long = 1L

    val createCalls = mutableListOf<ChallengeCreateInput>()

    /** 설정하면 complete 될 때까지 createChallenge 응답을 붙잡아 둔다. */
    var createGate: CompletableDeferred<Unit>? = null

    override fun getReceivedChallenges(
        onError: (String) -> Unit,
    ): Flow<List<ReceivedChallenge>> = emptyFlow()

    override fun getChallengeArchive(
        month: YearMonth,
        onError: (String) -> Unit,
    ): Flow<ChallengeArchive> = emptyFlow()

    override suspend fun createChallenge(
        input: ChallengeCreateInput,
        onSuccess: (challengeId: Long) -> Unit,
        onError: (String) -> Unit,
    ) {
        createCalls += input
        createGate?.await()
        createError.let { if (it == null) onSuccess(createdChallengeId) else onError(it) }
    }

    override fun getChallengeDetail(
        challengeId: Long,
        onError: (String) -> Unit,
    ): Flow<ChallengeDetail> = emptyFlow()

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

    override suspend fun forfeitChallenge(
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
