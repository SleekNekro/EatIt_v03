package com.github.SleekNekro.dao


import com.github.SleekNekro.model.CommentData
import com.github.SleekNekro.model.PostData
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

object PostTable : IntIdTable() {
    val title = varchar("title", 255)
    val content = text("content")
    val userId = reference("user_id", UserTable)
    val createdAt = datetime(name = "created_at")
    val updatedAt = datetime(name = "updated_at")
    val imageUrl = varchar("imageUrl", 255).nullable()
}

class PostDAO (id: EntityID<Int>) : IntEntity(id), ConvertibleToDataClass<PostData> {
    companion object : IntEntityClass<PostDAO>(PostTable){
        fun createPost(
            title: String,
            content: String,
            imageUrl: String
        ): PostDAO {
            return transaction {
                PostDAO.new {
                    this.title = title
                    this.content = content
                    this.createdAt = LocalDateTime.now()
                    this.updatedAt = LocalDateTime.now()
                    this.imageUrl = imageUrl
                }
            }
        }

        fun getAllPosts(): List<PostDAO> {
            return transaction { PostDAO.all().toList() }
        }
        fun getAllPostsOfUser(id: Int): List<PostDAO> {
            return transaction {
                PostDAO.find { PostTable.userId eq id }.toList()
            }
        }

        fun getPostById(id: Int): PostDAO? {
            return transaction { PostDAO.findById(id) }
        }

        fun updatePost(
            id: Int,
            newTitle: String? = null,
            newContent: String? = null,
            newImageUrl: String? = null
        ): Boolean {
            return transaction {
                val post = PostDAO.findById(id) ?: return@transaction false
                newTitle?.let { post.title = newTitle }
                newContent?.let { post.content= newContent }
                newImageUrl?.let { post.imageUrl= newImageUrl }
                post.updatedAt = LocalDateTime.now()
                true

            }
        }

        fun deletePostById(id: Int): Boolean {
            return transaction {
                val post = PostDAO.findById(id) ?: return@transaction false
                post.delete()
                true
            }
        }
    }
    var title by PostTable.title
    var content by PostTable.content
    var userId by PostTable.userId
    var createdAt by PostTable.createdAt
    var updatedAt by PostTable.updatedAt
    var imageUrl by PostTable.imageUrl

    override fun toDataClass(): PostData {
        return PostData(
            id = id.value,
            title = title,
            content = content,
            createdAt = createdAt,
            updatedAt = updatedAt,
            imageUrl = imageUrl,
            userId = userId.value
        )
    }

}