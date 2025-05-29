package com.github.SleekNekro.util

import io.ktor.http.*

data class RecipeData(
    val userId: Long,
    val title: String,
    val description: String,
    val servings: Int,
    val imageUrl: String?
)

data class CommentData(
    val recipeId: Long,
    val userId: Long,
    val content: String
)

data class LikeData(
    val userId: Long,
    val recipeId: Long
)

data class IngredientData(
    val recipeId: Long,
    val name: String,
    val quantity: String
)

fun Parameters.extractLikeData(): LikeData? {
    val userId = this["userId"]?.toLongOrNull() ?: return null
    val recipeId = this["recipeId"]?.toLongOrNull() ?: return null
    return LikeData(userId, recipeId)
}

fun Parameters.extractIngredientData(): IngredientData? {
    val recipeId = this["recipeId"]?.toLongOrNull() ?: return null
    val name = this["name"] ?: return null
    val quantity = this["quantity"] ?: return null

    if (name.isBlank() || quantity.isBlank()) return null

    return IngredientData(recipeId, name, quantity)
}

fun Parameters.extractRecipeData(): RecipeData? {
    val userId = this["userId"]?.toLongOrNull() ?: return null
    val title = this["title"] ?: return null
    val description = this["description"] ?: return null
    val servings = this["servings"]?.toIntOrNull() ?: return null
    val imageUrl = this["imageUrl"]

    if (title.isBlank() || description.isBlank()) return null

    return RecipeData(userId, title, description, servings, imageUrl)
}

fun Parameters.extractCommentData(): CommentData? {
    val recipeId = this["recipeId"]?.toLongOrNull() ?: return null
    val userId = this["userId"]?.toLongOrNull() ?: return null
    val content = this["content"] ?: return null

    if (content.isBlank()) return null

    return CommentData(recipeId, userId, content)
}