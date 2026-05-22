package com.das.downloader.exception

class NetworkRequestException(
    val code: Int,
    override val message: String
) : Exception()