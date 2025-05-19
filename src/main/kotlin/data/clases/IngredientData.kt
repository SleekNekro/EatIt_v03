package com.github.SleekNekro.data.clases

import kotlinx.serialization.Serializable

@Serializable
data class IngredientData(
    val id: Long,
    val recipeId: Long,
    val name: String,
    val quantity: String
)
