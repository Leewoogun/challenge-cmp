package com.lwg.challenge.remote.datasource

import com.lwg.challenge.data.datasource.remote.RankingRemoteDataSource
import com.lwg.challenge.domain.model.ranking.LoserRankingEntry
import com.lwg.challenge.remote.api.RankingApi
import com.lwg.challenge.remote.mapper.toLoserRankingEntries
import com.lwg.challenge.remote.network.util.suspendOnFailureWithErrorHandling
import com.lwg.challenge.remote.network.util.suspendOnSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Single

@Single(binds = [RankingRemoteDataSource::class])
class RankingRemoteDataSourceImpl(
    private val rankingApi: RankingApi,
) : RankingRemoteDataSource {

    override fun getLoserRanking(onError: (String) -> Unit): Flow<List<LoserRankingEntry>> = flow {
        rankingApi.getLoserRanking()
            .suspendOnSuccess { this@flow.emit(response.toLoserRankingEntries()) }
            .suspendOnFailureWithErrorHandling(onError)
    }
}
