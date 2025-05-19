package com.github.SleekNekro.data.DAO

import com.github.SleekNekro.data.Recipes
import com.github.SleekNekro.data.clases.RecipeData
import com.github.SleekNekro.util.ConvertibleToDataClass
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction

class RecipeDAO(id: EntityID<Long>) : LongEntity(id), ConvertibleToDataClass<RecipeData> {
    companion object : LongEntityClass<RecipeDAO>(Recipes) {
        fun createRecipe(userId: Long, title: String, description: String, servings: Int, imageUrl: String?): RecipeDAO {
            return transaction { RecipeDAO.new {
                this.userId = userId
                this.title = title
                this.description = description
                this.servings = servings
                this.imageUrl = imageUrl
                this.createdAt = System.currentTimeMillis()
            } }
        }

        fun updateRecipe(id: Long, newTitle: String? = null, newDescription: String? = null, newServings: Int? = null, newImageUrl: String? = null): Boolean {
            return transaction {
                val recipe = RecipeDAO.findById(id) ?: return@transaction false
                newTitle?.let { recipe.title = it }
                newDescription?.let { recipe.description = it }
                newServings?.let { recipe.servings = it }
                newImageUrl?.let { recipe.imageUrl = it }
                true
            }
        }
        fun getAllRecipes(): List<RecipeDAO> {
            return transaction {
                RecipeDAO.all().toList()
            }
        }
        fun getRecipeById(id: Long): RecipeDAO? {
            return transaction {
                RecipeDAO.findById(id)
            }
        }
        fun getRecipesByUser(userId: Long): List<RecipeDAO> {
            return transaction {
                RecipeDAO.find { Recipes.userId eq userId }.toList()
            }
        }


        fun deleteRecipe(id: Long): Boolean {
            return transaction {
                val recipe = RecipeDAO.findById(id) ?: return@transaction false
                recipe.delete()
                true
            }
        }
    }

    var userId by Recipes.userId
    var title by Recipes.title
    var description by Recipes.description
    var servings by Recipes.servings
    var imageUrl by Recipes.imageUrl
    var createdAt by Recipes.createdAt

    override fun toDataClass(): RecipeData {
        return RecipeData(
            id = this.id.value,
            userId = this.userId,
            title = this.title,
            description = this.description,
            servings = this.servings,
            imageUrl = this.imageUrl,
            createdAt = this.createdAt
        )
    }
}
