package com.github.SleekNekro.dao

import com.github.SleekNekro.model.CommentData
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

object CommentTable : IntIdTable() {
    val content = text("content")
    val userId = reference("user_id", UserTable)
    val postId = reference("post_id", PostTable)
    val createdAt = datetime(name = "created_at")
    val updatedAt = datetime(name = "updated_at")
}
class CommentDAO(id: EntityID<Int>) : IntEntity(id),ConvertibleToDataClass<CommentData> {
    companion object : IntEntityClass<CommentDAO>(CommentTable){
        fun createComment(
            content: String,
        ): Boolean{
            return transaction {
                CommentDAO.new {
                    this.content= content
                    this.userId= userId
                    this.postId = postId
                    this.createdAt = LocalDateTime.now()
                    this.updatedAt = LocalDateTime.now()
                }
                true
            }
        }
        fun getAllComments(): List<CommentDAO> {
            return transaction {
                CommentDAO.all().toList()
            }
        }

        fun getCommentsByPostID(id: Int): List<CommentDAO> {
            return transaction {
                CommentDAO.find{ CommentTable.postId eq id }.toList()
            }
        }

        fun getCommentById(id: Int): CommentDAO? {
            return transaction {
                CommentDAO.findById(id)
            }
        }

        fun updateComment(
            id: Int,
            newContent: String? = null
        ): Boolean{
            return transaction {
                val com= CommentDAO.findById(id) ?: return@transaction false
                newContent?.let { com.content= newContent }
                true
            }
        }

        fun deleteComment(id: Int): Boolean{
            return transaction {
                val com =CommentDAO.findById(id) ?: return@transaction false
                com.delete()
                true
            }
        }

    }

    var content by CommentTable.content
    var userId by CommentTable.userId
    var postId by CommentTable.postId
    var createdAt by CommentTable.createdAt
    var updatedAt by CommentTable.updatedAt


    override fun toDataClass(): CommentData {
        return CommentData(
            id = id.value,
            content = content,
            userId = userId.value,
            postId = postId.value,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }
}
