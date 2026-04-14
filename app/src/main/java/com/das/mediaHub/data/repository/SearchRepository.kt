package com.das.mediaHub.data.repository

import com.das.mediaHub.data.local.dao.SearchHistoryDao
import com.das.mediaHub.data.model.SearchData
import kotlinx.coroutines.flow.Flow

class SearchRepository(
    private val dao: SearchHistoryDao
) {
    fun getAllSearches(): Flow<List<SearchData>> = dao.getAllSearches()

    suspend fun insert(value: String) {
        dao.insert(
            SearchData(
                id = System.currentTimeMillis().toString(),
                value = value
            )
        )
    }

    suspend fun deleteById(id: String) {
        dao.deleteById(id)
    }

    suspend fun clearAll() {
        dao.clearAll()
    }
}