package com.lwg.challenge.data.repository

import com.lwg.challenge.data.datasource.remote.RankingRemoteDataSource
import com.lwg.challenge.domain.model.ranking.LoserRankingEntry
import com.lwg.challenge.domain.repository.RankingRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single(binds = [RankingRepository::class])
class RankingRepositoryImpl(
    private val rankingRemoteDataSource: RankingRemoteDataSource,
) : RankingRepository {

    override fun getLoserRanking(onError: (String) -> Unit): Flow<List<LoserRankingEntry>> =
        rankingRemoteDataSource.getLoserRanking(onError)
}
