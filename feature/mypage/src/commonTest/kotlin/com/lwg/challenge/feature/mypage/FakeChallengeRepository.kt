package com.lwg.challenge.feature.mypage

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

    /**
     * 서버가 정한 **이번 달**. `month = null` 요청의 응답이 에코하는 달이라, 화면의 이동 상한이
     * 이 값에서 나온다. 기기 시계를 읽으면 실제 이번 달이 바뀔 때마다 테스트가 흔들리므로 고정값이다.
     */
    var thisMonth: YearMonth = YearMonth(2026, 8)

    /** 계정 전역 하한. null 이면 종료한 챌린지가 하나도 없는 사용자다. */
    var firstArchivedMonth: YearMonth? = YearMonth(2025, 1)

    /**
     * 조회된 달을 요청 순서대로 기록한다. `null` 은 파라미터 없이 나간 요청이다 —
     * "첫 요청은 month 를 붙이지 않는다" 를 이 목록으로 단언한다.
     */
    val requestedMonths: MutableList<YearMonth?> = mutableListOf()

    val callCount: Int get() = requestedMonths.size

    /** 달별 지정이 없을 때 쓰이는 기본 응답. `null` 이면 emit 도 onError 도 없이 끝난다. */
    var histories: List<ChallengeHistory>? = emptyList()
    var error: String? = null

    /** 항목 일부만 매핑에 실패한 경우. [error] 와 달리 알린 뒤에도 성공분 목록이 그대로 온다. */
    var partialError: String? = null

    private val monthResponses = mutableMapOf<YearMonth, MonthResponse>()
    private val pendingMonths = mutableSetOf<YearMonth>()

    /**
     * 특정 달에만 다른 응답을 준다. 월을 넘나드는 테스트가 달마다 다른 결과를 심기 위한 것이다.
     *
     * @param echo 응답이 스스로 말하는 달. 기본값은 조회된 달 그대로다. **일부러 어긋나게** 주면
     *   늦게 도착한 stale 응답이 재현되고, 그 응답이 폐기되는지 검증할 수 있다.
     */
    fun respondTo(
        month: YearMonth,
        histories: List<ChallengeHistory>? = emptyList(),
        error: String? = null,
        partialError: String? = null,
        echo: YearMonth = month,
    ) {
        monthResponses[month] = MonthResponse(histories, error, partialError, echo)
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
    //
    // onError 하나가 두 사건(전체 실패 / 부분 실패)을 나르고 emit 이 뒤따르는지로만 갈린다는 것도
    // 실제 구현 계약이다 (ChallengeRemoteDataSourceImpl.getChallengeArchive) —
    // 그 갈림을 여기서 그대로 재현한다.
    override fun getChallengeArchive(
        month: YearMonth?,
        onError: (String) -> Unit,
    ): Flow<ChallengeArchive> = flow {
        requestedMonths += month
        // 요청이 null 이면 서버가 이번 달을 골라 준다. 그 선택을 여기서 재현해야 응답의 에코도
        // 실제와 같은 값이 된다.
        val resolved = month ?: thisMonth
        if (resolved in pendingMonths) awaitCancellation()

        val response = monthResponses[resolved]
            ?: MonthResponse(histories, error, partialError, echo = resolved)

        when {
            response.error != null -> onError(response.error)
            response.histories != null -> {
                response.partialError?.let(onError)
                emit(
                    ChallengeArchive(
                        month = response.echo,
                        firstArchivedMonth = firstArchivedMonth,
                        histories = response.histories,
                    ),
                )
            }
        }
    }

    private class MonthResponse(
        val histories: List<ChallengeHistory>?,
        val error: String?,
        val partialError: String?,
        val echo: YearMonth,
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
