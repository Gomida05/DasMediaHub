package com.das.python.data.model

import kotlinx.serialization.Serializable

@Serializable
data class FewVideoDetails(
    val title: String,
    val description: String,
    val viewNumber: String,
    val date: String,
    val channelName: String
)