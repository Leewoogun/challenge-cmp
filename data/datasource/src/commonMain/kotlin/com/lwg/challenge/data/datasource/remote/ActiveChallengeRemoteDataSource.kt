package com.lwg.challenge.data.datasource.remote

import com.lwg.challenge.domain.model.ActiveChallenge
import kotlinx.coroutines.flow.Flow

interface ActiveChallengeRemoteDataSource {

    fun getActiveChallenges(onError: (String) -> Unit): Flow<List<ActiveChallenge>>
}
