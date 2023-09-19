package com.maou.busapp.domain.model

data class Bus(
    val id: String,
    val name: String,
    val address: String,
    val destination: String,
    val timeArrives: String
)
