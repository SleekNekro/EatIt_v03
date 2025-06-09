package com.github.SleekNekro.data.clases

import kotlinx.serialization.Serializable

@Serializable
data class LikeStatus(val userId: Long, val recipeId: Long, val hasLiked: Boolean)
