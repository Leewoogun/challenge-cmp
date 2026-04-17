package com.lwg.challenge.local.database.di

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.lwg.challenge.local.database.AppDatabase
import com.lwg.challenge.local.database.getDatabaseBuilder
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan("com.lwg.challenge.local.database")
class DatabaseModule {

    @Single
    internal fun provideDatabase(): AppDatabase {
        return getDatabaseBuilder()
            .setDriver(BundledSQLiteDriver())
            .build()
    }

    @Single
    internal fun provideSearchHistoryDao(database: AppDatabase) = database.searchHistoryDao()

    @Single
    internal fun provideFavoriteDao(database: AppDatabase) = database.favoriteDao()
}
