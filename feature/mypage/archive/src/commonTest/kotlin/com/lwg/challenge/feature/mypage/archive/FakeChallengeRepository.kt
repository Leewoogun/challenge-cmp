package com.lwg.challenge.feature.mypage.archive

import com.lwg.challenge.domain.model.challenge.ChallengeArchive
import com.lwg.challenge.domain.model.challenge.ChallengeCreateInput
import com.lwg.challenge.domain.model.challenge.ChallengeDetail
import com.lwg.challenge.domain.model.challenge.ChallengeHistory
import com.lwg.challenge.domain.model.challenge.ReceivedChallenge
import com.lwg.challenge.domain.model.signature.Signature
import com.lwg.challenge.domain.repository.ChallengeRepository
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.YearMonth

class FakeChallengeRepository : ChallengeRepository {

    /** 계정 전역 하한. null 이면 종료한 챌린지가 하나도 없는 사용자다. */
    var firstArchivedMonth: YearMonth? = YearMonth(2025, 1)

    /** 조회된 달을 요청 순서대로 기록한다. */
    val requestedMonths: MutableList<YearMonth> = mutableListOf()

    val callCount: Int get() = requestedMonths.size

    /** 달별 지정이 없을 때 쓰이는 기본 응답. `null` 이면 emit 도 onError 도 없이 끝난다. */
    var histories: List<ChallengeHistory>? = emptyList()
    var error: String? = null

    private val monthResponses = mutableMapOf<YearMonth, MonthResponse>()
    private val pendingMonths = mutableSetOf<YearMonth>()

    /** 특정 달에만 다른 응답을 준다. 월을 넘나드는 테스트가 달마다 다른 결과를 심기 위한 것이다. */
    fun respondTo(
        month: YearMonth,
        histories: List<ChallengeHistory>? = emptyList(),
        error: String? = null,
    ) {
        monthResponses[month] = MonthResponse(histories, error)
    }

    /**
     * 그 달의 응답을 영원히 보류한다. 응답이 도착하기 **전**의 화면(직전 달 잔상이 남았는지)을
     * 관찰하려면 응답이 오지 않는 구간이 필요하다.
     */
    fun neverRespondTo(month: YearMonth) {
        pendingMonths += month
    }

    // 전체 실패에 emit 하지 않는 것이 실제 구현 계약이다. 빈 목록을 흘리면 조회 실패가 "0건" 으로
    // 둔갑해, 실패를 빈 상태로 그리지 않는다는 판정이 테스트에서 통과해 버린다.
    override fun getChallengeArchive(
        month: YearMonth,
        onError: (String) -> Unit,
    ): Flow<ChallengeArchive> = flow {
        requestedMonths += month
        if (month in pendingMonths) awaitCancellation()

        val response = monthResponses[month] ?: MonthResponse(histories, error)
        when {
            response.error != null -> onError(response.error)
            response.histories != null -> emit(
                ChallengeArchive(firstArchivedMonth = firstArchivedMonth, histories = response.histories),
            )
        }
    }

    private class MonthResponse(
        val histories: List<ChallengeHistory>?,
        val error: String?,
    )

    override fun getReceivedChallenges(
        onError: (String) -> Unit,
    ): Flow<List<ReceivedChallenge>> = emptyFlow()

    override suspend fun createChallenge(
        input: ChallengeCreateInput,
        onSuccess: (challengeId: Long) -> Unit,
        onError: (String) -> Unit,
    ) = Unit

    override fun getChallengeDetail(
        challengeId: Long,
        onError: (String) -> Unit,
    ): Flow<ChallengeDetail> = emptyFlow()

    override suspend fun acceptChallenge(
        challengeId: Long,
        myMission: String,
        signature: Signature,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) = Unit

    override suspend fun rejectChallenge(
        challengeId: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) = Unit

    override suspend fun forfeitChallenge(
        challengeId: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) = Unit

    override suspend fun cancelChallenge(
        challengeId: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) = Unit
}
