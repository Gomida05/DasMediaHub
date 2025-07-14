package com.das.forui.data.databased.room.dataclass

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "search_data")
data class SearchData(
    @PrimaryKey val id: String,
    val value: String
)