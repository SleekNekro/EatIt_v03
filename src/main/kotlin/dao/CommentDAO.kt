package com.github.SleekNekro.dao

import com.github.SleekNekro.model.CommentData
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime
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

    }

    var content by CommentTable.content
    var userId by CommentTable.userId
    var postId by CommentTable.postId


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
