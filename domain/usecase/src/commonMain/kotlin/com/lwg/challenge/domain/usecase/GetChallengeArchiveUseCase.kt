package com.lwg.challenge.domain.usecase

import com.lwg.challenge.domain.model.challenge.ChallengeArchive
import com.lwg.challenge.domain.repository.ChallengeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.YearMonth

/**
 * 계약서 보관함 한 달치.
 *
 * [month] 가 null 이면 **서버가 정한 이번 달**이 온다 — 앱이 기기 시계로 첫 달을 정하지 않는다.
 */
class GetChallengeArchiveUseCase(
    private val challengeRepository: ChallengeRepository,
) {
    operator fun invoke(month: YearMonth?, onError: (String) -> Unit): Flow<ChallengeArchive> =
        challengeRepository.getChallengeArchive(month, onError)
}
