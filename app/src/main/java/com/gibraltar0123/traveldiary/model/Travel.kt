package com.gibraltar0123.traveldiary.model

data class Travel(
    val id: Int,
    val userId: String,
    val title: String,
    val completed: Boolean,
    val description: String?,
    val imageUrl: String?,

)

