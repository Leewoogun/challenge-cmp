package com.lwg.challenge.data.repository

import com.lwg.challenge.domain.model.ActiveChallenge
import com.lwg.challenge.domain.repository.ActiveChallengeRepository
import com.lwg.challenge.remote.api.ActiveChallengeApi
import com.lwg.challenge.remote.mapper.toActiveChallenges
import com.lwg.challenge.remote.network.util.ApiResult
import com.lwg.challenge.remote.network.util.suspendOnFailureWithErrorHandling
import com.lwg.challenge.remote.network.util.suspendOnSuccess
import com.lwg.challenge.utils.AuthEventBus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Single

@Single(binds = [ActiveChallengeRepository::class])
class ActiveChallengeRepositoryImpl(
    private val activeChallengeApi: ActiveChallengeApi,
    private val authEventBus: AuthEventBus,
) : ActiveChallengeRepository {

    override fun getActiveChallenges(onError: (String) -> Unit): Flow<List<ActiveChallenge>> = flow {
        val result = activeChallengeApi.getActiveChallenges()
        if (result is ApiResult.Failure.CustomError && result.code == CODE_UNAUTHORIZED) {
            authEventBus.emitSessionExpired()
            return@flow
        }
        result
            .suspendOnSuccess {
                this@flow.emit(response.toActiveChallenges())
            }
            .suspendOnFailureWithErrorHandling(onError)
    }

    private companion object {
        const val CODE_UNAUTHORIZED = 401
    }
}
