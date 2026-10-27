package com.lwg.challenge.feature.ranking

import com.lwg.challenge.domain.model.ranking.LoserRankingEntry
import com.lwg.challenge.domain.repository.RankingRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * 테스트용 [RankingRepository] 페이크.
 *
 * - `entries` 로 성공 목록 주입.
 * - `error` 가 non-null 이면 목록을 emit 하지 않고 onError 만 호출한다 (실제 RemoteDataSource 와 동일).
 */
class FakeRankingRepository : RankingRepository {

    var entries: List<LoserRankingEntry> = emptyList()
    var error: String? = null

    var callCount: Int = 0
        private set

    /** non-null 이면 이 신호를 기다렸다 응답한다. 응답 전 화면을 관찰할 때 쓴다. */
    var pendingResponse: CompletableDeferred<Unit>? = null

    override fun getLoserRanking(onError: (String) -> Unit): Flow<List<LoserRankingEntry>> = flow {
        callCount += 1
        pendingResponse?.await()
        val currentError = error
        if (currentError != null) onError(currentError) else emit(entries)
    }
}
