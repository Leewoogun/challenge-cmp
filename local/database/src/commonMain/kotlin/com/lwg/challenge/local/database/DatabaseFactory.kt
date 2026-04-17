package com.lwg.challenge.local.database

import androidx.room.RoomDatabase

internal expect fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase>
