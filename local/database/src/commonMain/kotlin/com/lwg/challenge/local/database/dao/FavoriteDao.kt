package com.lwg.challenge.local.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.lwg.challenge.local.database.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal interface FavoriteDao {

    @Query("SELECT * FROM favorites ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE itemId = :itemId)")
    fun observeIsFavorite(itemId: Int): Flow<Boolean>

    @Upsert
    suspend fun upsert(entity: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE itemId = :itemId")
    suspend fun deleteByItemId(itemId: Int)

    @Query("DELETE FROM favorites")
    suspend fun deleteAll()
}
