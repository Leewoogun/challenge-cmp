package com.lwg.challenge.domain.repository

import com.lwg.challenge.domain.model.challenge.ChallengeCreateInput
import com.lwg.challenge.domain.model.challenge.ReceivedChallenge
import kotlinx.coroutines.flow.Flow

/**
 * 챌린지 신청 / 응답 Repository.
 */
interface ChallengeRepository {

    fun getReceivedChallenges(
        onError: (String) -> Unit,
    ): Flow<List<ReceivedChallenge>>

    /**
     * 생성된 `challengeId` 를 [onSuccess] 로 넘기는 유일한 mutation.
     * 보낸 도전장 조회 API 가 없어, 생성 직후 취소(언두)를 걸 수 있는 handle 이 이 값뿐이다.
     */
    suspend fun createChallenge(
        input: ChallengeCreateInput,
        onSuccess: (challengeId: Long) -> Unit,
        onError: (String) -> Unit,
    )

    suspend fun acceptChallenge(
        challengeId: Long,
        myMission: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    )

    suspend fun rejectChallenge(
        challengeId: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    )

    suspend fun cancelChallenge(
        challengeId: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    )
}
