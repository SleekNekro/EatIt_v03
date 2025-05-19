package com.github.SleekNekro.data.clases

import kotlinx.serialization.Serializable

@Serializable

data class CommentData(
    val id: Long,
    val recipeId: Long,
    val userId: Long,
    val content: String,
    val createdAt: Long
)
