package com.lwg.challenge.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lwg.challenge.local.database.dao.FavoriteDao
import com.lwg.challenge.local.database.dao.SearchHistoryDao
import com.lwg.challenge.local.database.entity.FavoriteEntity
import com.lwg.challenge.local.database.entity.SearchHistoryEntity

@Database(
    entities = [
        SearchHistoryEntity::class,
        FavoriteEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
internal abstract class AppDatabase : RoomDatabase() {

    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        const val DATABASE_NAME = "app_database"
    }
}
