package com.das.mediaHub.data.databased.room.repository

import com.das.mediaHub.data.databased.room.dao.SearchDao
import com.das.mediaHub.data.databased.room.dataclass.SearchData

class SearchRepo(private val dao: SearchDao) {
    suspend fun getAllSearches(): List<SearchData> {
        return dao.getAllSearches()
    }

    suspend fun insert(searchData: SearchData): Long {
        return dao.insert(searchData)
    }

    suspend fun update(searchData: SearchData){
        dao.updateDate(searchData)
    }


    suspend fun delete(searchId: String) {
        dao.removeById(searchId)
    }


}