package com.lwg.challenge.domain.repository

import com.lwg.challenge.domain.model.ranking.LoserRankingEntry
import kotlinx.coroutines.flow.Flow

interface RankingRepository {

    fun getLoserRanking(onError: (String) -> Unit): Flow<List<LoserRankingEntry>>
}
