package com.lwg.challenge.local.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private object AndroidDatabaseFactory : KoinComponent {
    val context: Context by inject()
}

internal actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val context = AndroidDatabaseFactory.context
    val dbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME)
    return Room.databaseBuilder<AppDatabase>(
        context = context,
        name = dbFile.absolutePath,
    )
}
