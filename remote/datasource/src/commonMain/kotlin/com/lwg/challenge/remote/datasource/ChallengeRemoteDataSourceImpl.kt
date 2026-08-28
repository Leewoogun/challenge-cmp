package com.lwg.challenge.remote.datasource

import com.lwg.challenge.data.datasource.remote.ChallengeRemoteDataSource
import com.lwg.challenge.domain.model.challenge.ChallengeArchive
import com.lwg.challenge.domain.model.challenge.ChallengeCreateInput
import com.lwg.challenge.domain.model.challenge.ChallengeDetail
import com.lwg.challenge.domain.model.challenge.ReceivedChallenge
import com.lwg.challenge.domain.model.signature.Signature
import com.lwg.challenge.remote.api.ChallengeApi
import com.lwg.challenge.remote.mapper.SignatureCodec
import com.lwg.challenge.remote.mapper.toBody
import com.lwg.challenge.remote.mapper.toChallengeArchive
import com.lwg.challenge.remote.mapper.toChallengeDetail
import com.lwg.challenge.remote.mapper.toReceivedChallenges
import com.lwg.challenge.remote.model.challenge.AcceptChallengeBody
import com.lwg.challenge.remote.network.util.suspendOnFailureWithErrorHandling
import com.lwg.challenge.remote.network.util.suspendOnSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.YearMonth
import org.koin.core.annotation.Single

@Single(binds = [ChallengeRemoteDataSource::class])
class ChallengeRemoteDataSourceImpl(
    private val challengeApi: ChallengeApi,
) : ChallengeRemoteDataSource {

    override fun getReceivedChallenges(
        onError: (String) -> Unit,
    ): Flow<List<ReceivedChallenge>> = flow {
        challengeApi.getReceivedChallenges()
            .suspendOnSuccess {
                val mapped = response.toReceivedChallenges()
                // deadline 파싱 실패 항목은 매퍼가 걸렀다. 항목당 발화하면 스낵바가 연달아 떠서 1회만 알린다 (api-contract §3)
                if (mapped.droppedCount > 0) onError("일부 항목을 불러오지 못했어요")
                this@flow.emit(mapped.items)
            }
            .suspendOnFailureWithErrorHandling(onError)
    }

    override fun getChallengeArchive(
        month: YearMonth?,
        onError: (String) -> Unit,
    ): Flow<ChallengeArchive> = flow {
        // YearMonth.toString() 이 곧 wire 포맷이다 — ISO `yyyy-MM` zero-padded 라 서버가 요구하는 형태와
        // 같고, WireYearMonthSerializer.render 와도 같은 경로다(그 패딩은 직렬화기 테스트가 고정한다).
        challengeApi.getChallengeHistories(month?.toString())
            .suspendOnSuccess {
                val mapped = response.toChallengeArchive()
                // month 를 못 읽은 것과 항목 일부 드롭은 서로 다른 사건이다. 판별자는 onError 가 아니라
                // "emit 이 뒤따르는가" 다 — 여기서는 emit 하지 않아 화면이 전체 실패로 받는다.
                if (mapped == null) {
                    onError("보관함을 불러오지 못했어요")
                    return@suspendOnSuccess
                }
                // 날짜·결과를 못 읽은 항목은 매퍼가 걸렀다. 항목당 발화하면 스낵바가 연달아 떠서 1회만 알린다 (api-contract §3).
                // 위와 달리 emit 이 뒤따라서, 목록과 스낵바가 함께 나온다.
                if (mapped.droppedCount > 0) onError("일부 항목을 불러오지 못했어요")
                this@flow.emit(mapped.archive)
            }
            .suspendOnFailureWithErrorHandling(onError)
    }

    override suspend fun createChallenge(
        input: ChallengeCreateInput,
        onSuccess: (Long) -> Unit,
        onError: (String) -> Unit,
    ) {
        challengeApi.createChallenge(input.toBody())
            .suspendOnSuccess { onSuccess(response.data.challengeId) }
            .suspendOnFailureWithErrorHandling(onError)
    }

    override suspend fun getChallengeDetail(
        challengeId: Long,
        onSuccess: (ChallengeDetail) -> Unit,
        onError: (String) -> Unit,
    ) {
        challengeApi.getChallengeDetail(challengeId)
            .suspendOnSuccess {
                // deadline 이 없으면 매퍼가 null 을 준다. 계약서를 그릴 수 없으니 성공으로 위장하지 않는다.
                val detail = response.toChallengeDetail()
                if (detail == null) onError("챌린지 정보를 불러오지 못했어요") else onSuccess(detail)
            }
            .suspendOnFailureWithErrorHandling(onError)
    }

    override suspend fun acceptChallenge(
        challengeId: Long,
        myMission: String,
        signature: Signature,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        challengeApi.acceptChallenge(challengeId, AcceptChallengeBody(myMission, SignatureCodec.encode(signature)))
            .suspendOnSuccess { onSuccess() }
            .suspendOnFailureWithErrorHandling(onError)
    }

    override suspend fun rejectChallenge(
        challengeId: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        challengeApi.rejectChallenge(challengeId)
            .suspendOnSuccess { onSuccess() }
            .suspendOnFailureWithErrorHandling(onError)
    }

    override suspend fun cancelChallenge(
        challengeId: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        challengeApi.cancelChallenge(challengeId)
            .suspendOnSuccess { onSuccess() }
            .suspendOnFailureWithErrorHandling(onError)
    }
}
