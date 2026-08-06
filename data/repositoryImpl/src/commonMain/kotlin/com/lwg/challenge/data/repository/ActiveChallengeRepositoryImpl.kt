package com.lwg.challenge.data.repository

import com.lwg.challenge.data.datasource.remote.ActiveChallengeRemoteDataSource
import com.lwg.challenge.domain.model.ActiveChallenge
import com.lwg.challenge.domain.repository.ActiveChallengeRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single(binds = [ActiveChallengeRepository::class])
class ActiveChallengeRepositoryImpl(
    private val activeChallengeRemoteDataSource: ActiveChallengeRemoteDataSource,
) : ActiveChallengeRepository {

    override fun getActiveChallenges(onError: (String) -> Unit): Flow<List<ActiveChallenge>> =
        activeChallengeRemoteDataSource.getActiveChallenges(onError)
}
