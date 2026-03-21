package com.das.python.data.model.searcher

import kotlinx.serialization.Serializable


@Serializable
data class ChannelDetails (
    val name: String,
    val id: String,
    val link: String
)