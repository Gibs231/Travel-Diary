package com.gibraltar0123.traveldiary.model

data class ApiResponse(
    val status: Int,
    val message: String,
    val travels: List<Travel>
)