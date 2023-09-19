package com.maou.busapp.data.source.response

data class GeneralResponse<T>(
    val statusCode: Int,
    val body: T
)