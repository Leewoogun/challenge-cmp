package com.lwg.challenge.local.database.datasource

import com.lwg.challenge.domain.model.Favorite
import com.lwg.challenge.local.database.dao.FavoriteDao
import com.lwg.challenge.local.database.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.lwg.challenge.local.database.util.currentTimeMillis
import org.koin.core.annotation.Single

@Single
internal class FavoriteLocalDataSource(
    private val favoriteDao: FavoriteDao,
) {

    fun observeAll(): Flow<List<Favorite>> {
        return favoriteDao.observeAll().map { entities ->
            entities.map { it.toFavorite() }
        }
    }

    fun observeIsFavorite(itemId: Int): Flow<Boolean> {
        return favoriteDao.observeIsFavorite(itemId)
    }

    suspend fun addFavorite(favorite: Favorite) {
        favoriteDao.upsert(favorite.toEntity())
    }

    suspend fun removeFavorite(itemId: Int) {
        favoriteDao.deleteByItemId(itemId)
    }

    suspend fun clearAll() {
        favoriteDao.deleteAll()
    }
}

private fun FavoriteEntity.toFavorite() = Favorite(
    itemId = itemId,
    title = title,
    imageUrl = imageUrl,
)

private fun Favorite.toEntity() = FavoriteEntity(
    itemId = itemId,
    title = title,
    imageUrl = imageUrl,
    createdAt = currentTimeMillis(),
)
