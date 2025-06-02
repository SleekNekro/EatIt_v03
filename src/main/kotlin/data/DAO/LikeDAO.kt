package com.github.SleekNekro.data.DAO

import com.github.SleekNekro.data.Likes
import com.github.SleekNekro.data.clases.LikeData
import com.github.SleekNekro.util.ConvertibleToDataClass
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction


class LikeDAO(id: EntityID<Long>) : LongEntity(id), ConvertibleToDataClass<LikeData> {
    companion object : LongEntityClass<LikeDAO>(Likes) {

        // Obtener todos los likes
        fun getAllLikes(): List<LikeDAO> {
            return transaction {
                LikeDAO.all().toList()
            }
        }
        fun getLikeExists(userId: Long, recipeId: Long): Boolean {
            return transaction {
                LikeDAO.find {
                    (Likes.userId eq userId) and (Likes.recipeId eq recipeId)
                }.count() > 0
            }
        }


        // Obtener los likes de una receta
        fun getLikesByRecipe(recipeId: Long): List<LikeDAO> {
            return transaction {
                LikeDAO.find { Likes.recipeId eq recipeId }.toList()
            }
        }

        // Obtener los likes de un usuario
        fun getLikesByUser(userId: Long): List<LikeDAO> {
            return transaction {
                LikeDAO.find { Likes.userId eq userId }.toList()
            }
        }

        // Agregar un like
        fun addLike(userId: Long, recipeId: Long): LikeDAO? {
            return transaction {
                // Verificar que el usuario existe
                val userExists = UserDAO.findById(userId) != null
                if (!userExists) {
                    return@transaction null
                }
                
                // Verificar que la receta existe
                val recipeExists = RecipeDAO.findById(recipeId) != null
                if (!recipeExists) {
                    return@transaction null
                }

                // Crear el like solo si ambos existen
                LikeDAO.new {
                    this.userId = userId
                    this.recipeId = recipeId
                    this.createdAt = System.currentTimeMillis()
                }
            }
        }

        // Eliminar un like
        fun removeLike(userId: Long, recipeId: Long): Boolean {
            return transaction {
                val like = LikeDAO.find { (Likes.userId eq userId) and (Likes.recipeId eq recipeId) }
                    .singleOrNull()

                if (like != null) {
                    like.delete()
                    true
                } else {
                    false
                }
            }
        }
    }

    var userId by Likes.userId
    var recipeId by Likes.recipeId
    var createdAt by Likes.createdAt

    override fun toDataClass(): LikeData {
        return LikeData(
            id = this.id.value,
            userId = this.userId,
            recipeId = this.recipeId,
            createdAt = this.createdAt
        )
    }
}