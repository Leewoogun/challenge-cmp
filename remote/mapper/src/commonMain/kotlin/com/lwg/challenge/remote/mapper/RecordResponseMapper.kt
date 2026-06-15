package com.lwg.challenge.remote.mapper

import com.lwg.challenge.domain.model.UserRecord
import com.lwg.challenge.remote.model.record.RecordResponse

/**
 * Record DTO → Domain 매핑.
 */
fun RecordResponse.toUserRecord(): UserRecord = UserRecord(
    win = data.win,
    lose = data.lose,
    draw = data.draw,
    currentStreak = data.currentStreak,
)
