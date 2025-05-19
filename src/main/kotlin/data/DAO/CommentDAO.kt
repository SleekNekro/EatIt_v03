package com.github.SleekNekro.data.DAO

import com.github.SleekNekro.data.Comments
import com.github.SleekNekro.data.clases.CommentData
import com.github.SleekNekro.util.ConvertibleToDataClass
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction

class CommentDAO(id: EntityID<Long>) : LongEntity(id), ConvertibleToDataClass<CommentData> {
    companion object : LongEntityClass<CommentDAO>(Comments) {

        // Obtener todos los comentarios
        fun getAllComments(): List<CommentDAO> {
            return transaction {
                CommentDAO.all().toList()
            }
        }

        // Obtener un comentario por ID
        fun getCommentById(id: Long): CommentDAO? {
            return transaction {
                CommentDAO.findById(id)
            }
        }

        // Obtener comentarios de una receta específica
        fun getCommentsByRecipeId(recipeId: Long): List<CommentDAO> {
            return transaction {
                CommentDAO.find { Comments.recipeId eq recipeId }.toList()
            }
        }

        // Crear un nuevo comentario
        fun createComment(recipeId: Long, userId: Long, content: String): CommentDAO {
            return transaction {
                CommentDAO.new {
                    this.recipeId = recipeId
                    this.userId = userId
                    this.content = content
                    this.createdAt = System.currentTimeMillis()
                }
            }
        }

        // Actualizar un comentario
        fun updateComment(id: Long, newContent: String): Boolean {
            return transaction {
                val comment = CommentDAO.findById(id) ?: return@transaction false
                comment.content = newContent
                true
            }
        }

        // Eliminar un comentario
        fun deleteComment(id: Long): Boolean {
            return transaction {
                val comment = CommentDAO.findById(id) ?: return@transaction false
                comment.delete()
                true
            }
        }
    }

    var recipeId by Comments.recipeId
    var userId by Comments.userId
    var content by Comments.content
    var createdAt by Comments.createdAt

    override fun toDataClass(): CommentData {
        return CommentData(
            id = this.id.value,
            recipeId = this.recipeId,
            userId = this.userId,
            content = this.content,
            createdAt = this.createdAt
        )
    }
}
