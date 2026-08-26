package com.lwg.challenge.data.datasource.remote

import com.lwg.challenge.domain.model.ranking.LoserRankingEntry
import kotlinx.coroutines.flow.Flow

interface RankingRemoteDataSource {

    fun getLoserRanking(onError: (String) -> Unit): Flow<List<LoserRankingEntry>>
}
