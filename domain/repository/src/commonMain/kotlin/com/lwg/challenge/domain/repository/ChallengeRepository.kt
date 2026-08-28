package com.lwg.challenge.domain.repository

import com.lwg.challenge.domain.model.challenge.ChallengeCreateInput
import com.lwg.challenge.domain.model.challenge.ChallengeDetail
import com.lwg.challenge.domain.model.challenge.ChallengeHistory
import com.lwg.challenge.domain.model.challenge.ReceivedChallenge
import com.lwg.challenge.domain.model.signature.Signature
import kotlinx.coroutines.flow.Flow

/**
 * 챌린지 신청 / 응답 Repository.
 */
interface ChallengeRepository {

    fun getReceivedChallenges(
        onError: (String) -> Unit,
    ): Flow<List<ReceivedChallenge>>

    /** 계약서 보관함 목록. 종료된 챌린지 전체가 `challengeDate` 내림차순으로 온다. */
    fun getChallengeHistories(
        onError: (String) -> Unit,
    ): Flow<List<ChallengeHistory>>

    /**
     * 생성된 `challengeId` 를 [onSuccess] 로 넘기는 유일한 mutation.
     * 보낸 도전장 조회 API 가 없어, 생성 직후 취소(언두)를 걸 수 있는 handle 이 이 값뿐이다.
     */
    suspend fun createChallenge(
        input: ChallengeCreateInput,
        onSuccess: (challengeId: Long) -> Unit,
        onError: (String) -> Unit,
    )

    /**
     * 화면 진입 시 1회 + 재시도만 있어 관찰 대상이 아니다. Flow 가 아니라 suspend + 콜백으로 둔다.
     */
    suspend fun getChallengeDetail(
        challengeId: Long,
        onSuccess: (ChallengeDetail) -> Unit,
        onError: (String) -> Unit,
    )

    suspend fun acceptChallenge(
        challengeId: Long,
        myMission: String,
        signature: Signature,
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
