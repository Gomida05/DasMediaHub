package com.das.mediaHub.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "search_data")
data class SearchData(
    @PrimaryKey

    val id: String,
    val value: String
)