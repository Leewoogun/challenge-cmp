package com.lwg.challenge.feature.home

import com.lwg.challenge.domain.model.UserRecord
import com.lwg.challenge.domain.repository.RecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeRecordRepository : RecordRepository {

    var record: UserRecord? = null
    var error: String? = null

    var callCount: Int = 0
        private set

    override fun getMyRecord(onError: (String) -> Unit): Flow<UserRecord> = flow {
        callCount += 1
        val current = record
        val currentError = error
        when {
            current != null -> emit(current)
            currentError != null -> onError(currentError)
        }
    }
}
