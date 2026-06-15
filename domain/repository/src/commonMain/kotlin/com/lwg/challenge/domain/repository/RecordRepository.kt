package com.lwg.challenge.domain.repository

import com.lwg.challenge.domain.model.UserRecord
import kotlinx.coroutines.flow.Flow

interface RecordRepository {

    fun getMyRecord(onError: (String) -> Unit): Flow<UserRecord>
}
