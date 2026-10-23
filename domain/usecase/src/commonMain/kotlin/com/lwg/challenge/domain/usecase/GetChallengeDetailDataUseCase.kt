package com.lwg.challenge.domain.usecase

import com.lwg.challenge.domain.model.challenge.ChallengeDetail
import com.lwg.challenge.domain.model.verification.ChallengeVerifications
import com.lwg.challenge.domain.repository.ChallengeRepository
import com.lwg.challenge.domain.repository.VerificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEmpty

class GetChallengeDetailDataUseCase(
    private val challengeRepository: ChallengeRepository,
    private val verificationRepository: VerificationRepository,
) {
    operator fun invoke(challengeId: Long, onError: (String) -> Unit): Flow<ChallengeDetailData> = combine(
        challengeRepository.getChallengeDetail(challengeId, onError),
        verificationRepository.getVerifications(challengeId, onError = {})
            .map<ChallengeVerifications, ChallengeVerifications?> { it }
            .onEmpty { emit(null) },
    ) { detail, verifications ->
        ChallengeDetailData(detail = detail, verifications = verifications)
    }
}

/**
 * 챌린지 상세 화면에 필요한 데이터 묶음 — 상세 + 양측 인증 현황.
 *
 * @property verifications 조회에 실패하면 null. 상세만으로 화면이 성립한다
 */
data class ChallengeDetailData(
    val detail: ChallengeDetail,
    val verifications: ChallengeVerifications?,
)
