package com.github.SleekNekro.data.clases

import kotlinx.serialization.Serializable

@Serializable
data class LikeData(
    val id: Long,
    val userId: Long,
    val recipeId: Long,
    val createdAt: Long
)

