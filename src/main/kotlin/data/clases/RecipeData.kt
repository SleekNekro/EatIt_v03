package com.github.SleekNekro.data.clases

import kotlinx.serialization.Serializable

@Serializable
data class RecipeData(
    val id: Long,
    val userId: Long,
    val title: String,
    val description: String,
    val servings: Int,
    val imageUrl: String?,
    val createdAt: Long
)
