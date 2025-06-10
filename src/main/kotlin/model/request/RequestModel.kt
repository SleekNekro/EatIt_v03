package com.github.SleekNekro.model.request

import com.github.SleekNekro.data.clases.RecipeData
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val username: String,
    val password: String,
    val email: String,
)

@Serializable
data class RecipeEvent(
    val type: String,
    val recipe: RecipeData
)

@Serializable
data class RecipeRequest(
    val userId: Long,
    val title: String,
    val description: String,
    val servings: Int,
    val imageUrl: String?
)

@Serializable
data class UpdateRecipeRequest(
    val title: String? = null,
    val description: String? = null,
    val servings: Int? = null,
    val imageUrl: String? = null
)

@Serializable
data class CommentRequest(
    val userId: Long,
    val recipeId: Long,
    val content: String
)

@Serializable
data class UpdateCommentRequest(
    val content: String
)

@Serializable
data class UpdateUserRequest(
    val username: String? = null,
    val email: String? = null,
    val password: String? = null,
    val profilePic: String? = null
)

@Serializable
data class FollowerRequest(
    val followerId: Long
)


