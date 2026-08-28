package com.lwg.challenge.domain.usecase

import com.lwg.challenge.domain.model.challenge.ChallengeHistory
import com.lwg.challenge.domain.repository.ChallengeRepository
import kotlinx.coroutines.flow.Flow

/**
 * 계약서 보관함 목록.
 *
 * 월 그룹핑은 여기서 하지 않는다 — 월 라벨·건수 문구는 표시용 파생값이라 화면이 소유한다.
 */
class GetChallengeHistoriesUseCase(
    private val challengeRepository: ChallengeRepository,
) {
    operator fun invoke(onError: (String) -> Unit): Flow<List<ChallengeHistory>> =
        challengeRepository.getChallengeHistories(onError)
}
