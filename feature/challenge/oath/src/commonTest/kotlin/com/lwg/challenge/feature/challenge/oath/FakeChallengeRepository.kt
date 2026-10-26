package com.lwg.challenge.feature.challenge.oath

import com.lwg.challenge.domain.model.challenge.ChallengeArchive
import com.lwg.challenge.domain.model.challenge.ChallengeCreateInput
import com.lwg.challenge.domain.model.challenge.ChallengeDetail
import com.lwg.challenge.domain.model.challenge.ReceivedChallenge
import com.lwg.challenge.domain.model.signature.Signature
import com.lwg.challenge.domain.repository.ChallengeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.YearMonth

/** [ChallengeRepository.acceptChallenge] 에 실제로 전달된 인자. */
data class AcceptCall(
    val challengeId: Long,
    val myMission: String,
    val signature: Signature,
)

/**
 * 테스트용 [ChallengeRepository] 페이크.
 *
 * 모듈 간 테스트 소스는 공유되지 않아 `:feature:challenge:create` 의 같은 이름 페이크와 별개다.
 * 이쪽은 상세 조회 / 수락만 쓴다.
 */
class FakeChallengeRepository : ChallengeRepository {

    /** 상세 조회 실패 메시지. null 이면 [detail] 로 성공. */
    var detailError: String? = null

    var detail: ChallengeDetail? = null

    var detailCallCount: Int = 0

    /** 수락 실패 메시지. null 이면 성공. */
    var acceptError: String? = null

    /** true 면 호출만 기록하고 콜백을 부르지 않는다 — in-flight 상태를 붙잡아 두기 위한 스위치. */
    var acceptPending: Boolean = false

    val acceptCalls = mutableListOf<AcceptCall>()

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
    ) = Unit

    override fun getChallengeDetail(
        challengeId: Long,
        onError: (String) -> Unit,
    ): Flow<ChallengeDetail> = flow {
        detailCallCount++
        val error = detailError
        val current = detail
        when {
            error != null -> onError(error)
            current != null -> emit(current)
            else -> onError("상세 픽스처가 설정되지 않았습니다.")
        }
    }

    override suspend fun acceptChallenge(
        challengeId: Long,
        myMission: String,
        signature: Signature,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        acceptCalls += AcceptCall(challengeId, myMission, signature)
        if (acceptPending) return
        acceptError.let { if (it == null) onSuccess() else onError(it) }
    }

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
