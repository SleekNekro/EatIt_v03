package com.github.SleekNekro.data.DAO

import com.github.SleekNekro.data.Ingredients
import com.github.SleekNekro.data.clases.IngredientData
import com.github.SleekNekro.util.ConvertibleToDataClass
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction

class IngredientDAO(id: EntityID<Long>) : LongEntity(id), ConvertibleToDataClass<IngredientData> {
    companion object : LongEntityClass<IngredientDAO>(Ingredients) {
        fun createIngredient(recipeId: Long, name: String, quantity: String): IngredientDAO {
            return transaction { IngredientDAO.new {
                this.recipeId = recipeId
                this.name = name
                this.quantity = quantity
            } }
        }

        fun deleteIngredient(id: Long): Boolean {
            return transaction {
                val ingredient = IngredientDAO.findById(id) ?: return@transaction false
                ingredient.delete()
                true
            }
        }
    }

    var recipeId by Ingredients.recipeId
    var name by Ingredients.name
    var quantity by Ingredients.quantity

    override fun toDataClass(): IngredientData {
        return IngredientData(
            id = this.id.value,
            recipeId = this.recipeId,
            name = this.name,
            quantity = this.quantity
        )
    }
}
