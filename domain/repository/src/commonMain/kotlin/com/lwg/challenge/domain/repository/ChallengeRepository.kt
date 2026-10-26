package com.lwg.challenge.domain.repository

import com.lwg.challenge.domain.model.challenge.ChallengeArchive
import com.lwg.challenge.domain.model.challenge.ChallengeCreateInput
import com.lwg.challenge.domain.model.challenge.ChallengeDetail
import com.lwg.challenge.domain.model.challenge.ReceivedChallenge
import com.lwg.challenge.domain.model.signature.Signature
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.YearMonth

/**
 * 챌린지 신청 / 응답 Repository.
 */
interface ChallengeRepository {

    fun getReceivedChallenges(
        onError: (String) -> Unit,
    ): Flow<List<ReceivedChallenge>>

    /**
     * 계약서 보관함 한 달치. 그 달의 종료된 챌린지가 `challengeDate` 내림차순으로 온다.
     */
    fun getChallengeArchive(
        month: YearMonth,
        onError: (String) -> Unit,
    ): Flow<ChallengeArchive>

    /**
     * 생성된 `challengeId` 를 [onSuccess] 로 넘기는 유일한 mutation.
     * 보낸 도전장 조회 API 가 없어, 생성 직후 취소(언두)를 걸 수 있는 handle 이 이 값뿐이다.
     */
    suspend fun createChallenge(
        input: ChallengeCreateInput,
        onSuccess: (challengeId: Long) -> Unit,
        onError: (String) -> Unit,
    )

    fun getChallengeDetail(
        challengeId: Long,
        onError: (String) -> Unit,
    ): Flow<ChallengeDetail>

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

    /**
     * 기권. 성공하면 챌린지가 즉시 끝나고 요청자가 패, 상대가 승이다.
     *
     * 🔴 **멱등이 아니다** — 일회성 전이라, 두 번째 요청은 챌린지가 이미 `COMPLETED` 라서 서버가 거부한다.
     */
    suspend fun forfeitChallenge(
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
