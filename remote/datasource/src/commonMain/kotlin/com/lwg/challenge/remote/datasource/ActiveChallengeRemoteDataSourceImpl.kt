package com.lwg.challenge.remote.datasource

import com.lwg.challenge.data.datasource.remote.ActiveChallengeRemoteDataSource
import com.lwg.challenge.domain.model.ActiveChallenge
import com.lwg.challenge.remote.api.ActiveChallengeApi
import com.lwg.challenge.remote.mapper.toActiveChallenges
import com.lwg.challenge.remote.network.util.suspendOnFailureWithErrorHandling
import com.lwg.challenge.remote.network.util.suspendOnSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Single

@Single(binds = [ActiveChallengeRemoteDataSource::class])
class ActiveChallengeRemoteDataSourceImpl(
    private val activeChallengeApi: ActiveChallengeApi,
) : ActiveChallengeRemoteDataSource {

    override fun getActiveChallenges(onError: (String) -> Unit): Flow<List<ActiveChallenge>> = flow {
        activeChallengeApi.getActiveChallenges()
            .suspendOnSuccess {
                val mapped = response.toActiveChallenges()
                // deadline 파싱 실패 항목은 매퍼가 걸렀다. 항목당 발화하면 스낵바가 연달아 떠서 1회만 알린다 (api-contract §3)
                if (mapped.droppedCount > 0) onError("일부 항목을 불러오지 못했어요")
                this@flow.emit(mapped.items)
            }
            .suspendOnFailureWithErrorHandling(onError)
    }
}
