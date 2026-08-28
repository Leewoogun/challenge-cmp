package com.lwg.challenge.feature.challenge.detail

import com.lwg.challenge.domain.model.challenge.ChallengeArchive
import com.lwg.challenge.domain.model.challenge.ChallengeCreateInput
import com.lwg.challenge.domain.model.challenge.ChallengeDetail
import com.lwg.challenge.domain.model.challenge.ReceivedChallenge
import com.lwg.challenge.domain.model.signature.Signature
import com.lwg.challenge.domain.repository.ChallengeRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.datetime.YearMonth

/**
 * 테스트용 [ChallengeRepository] 페이크.
 *
 * 모듈 간 테스트 소스는 공유되지 않아 다른 feature 의 같은 이름 페이크와 별개다.
 * 이쪽은 상세 조회만 쓴다.
 */
class FakeChallengeRepository : ChallengeRepository {

    /** 상세 조회 실패 메시지. null 이면 [detail] 로 성공. */
    var detailError: String? = null

    var detail: ChallengeDetail? = null

    var detailCallCount: Int = 0

    /** 조회가 끝나기 전 상태를 관찰하려는 테스트용. 0 이면 즉시 응답한다. */
    var detailDelayMillis: Long = 0

    override fun getReceivedChallenges(
        onError: (String) -> Unit,
    ): Flow<List<ReceivedChallenge>> = emptyFlow()

    override fun getChallengeArchive(
        month: YearMonth?,
        onError: (String) -> Unit,
    ): Flow<ChallengeArchive> = emptyFlow()

    override suspend fun createChallenge(
        input: ChallengeCreateInput,
        onSuccess: (challengeId: Long) -> Unit,
        onError: (String) -> Unit,
    ) = Unit

    override suspend fun getChallengeDetail(
        challengeId: Long,
        onSuccess: (ChallengeDetail) -> Unit,
        onError: (String) -> Unit,
    ) {
        detailCallCount++
        if (detailDelayMillis > 0) delay(detailDelayMillis)
        val error = detailError
        val current = detail
        when {
            error != null -> onError(error)
            current != null -> onSuccess(current)
            else -> onError("상세 픽스처가 설정되지 않았습니다.")
        }
    }

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
