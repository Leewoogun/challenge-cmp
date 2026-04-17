package com.lwg.challenge.local.database.datasource

import com.lwg.challenge.local.database.dao.SearchHistoryDao
import com.lwg.challenge.local.database.entity.SearchHistoryEntity
import com.lwg.challenge.local.database.util.currentTimeMillis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single
internal class SearchHistoryLocalDataSource(
    private val searchHistoryDao: SearchHistoryDao,
) {

    fun observeRecentSearches(limit: Int = 20): Flow<List<String>> {
        return searchHistoryDao.observeRecentSearches(limit).map { entities ->
            entities.map { it.query }
        }
    }

    suspend fun addSearch(query: String) {
        searchHistoryDao.insert(
            SearchHistoryEntity(
                query = query,
                timestamp = currentTimeMillis(),
            ),
        )
    }

    suspend fun deleteSearch(id: Long) {
        searchHistoryDao.deleteById(id)
    }

    suspend fun clearAll() {
        searchHistoryDao.deleteAll()
    }
}
