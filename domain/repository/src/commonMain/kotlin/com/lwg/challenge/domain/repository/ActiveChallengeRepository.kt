package com.lwg.challenge.domain.repository

import com.lwg.challenge.domain.model.ActiveChallenge
import kotlinx.coroutines.flow.Flow

interface ActiveChallengeRepository {

    fun getActiveChallenges(onError: (String) -> Unit): Flow<List<ActiveChallenge>>
}
