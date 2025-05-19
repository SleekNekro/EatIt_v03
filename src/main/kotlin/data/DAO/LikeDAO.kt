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
        fun addLike(userId: Long, recipeId: Long): LikeDAO {
            return transaction { LikeDAO.new {
                this.userId = userId
                this.recipeId = recipeId
                this.createdAt = System.currentTimeMillis()
            } }
        }

        fun removeLike(userId: Long, recipeId: Long): Boolean {
            return transaction {
                val like = LikeDAO.find {
                    (Likes.userId eq userId) and (Likes.recipeId eq recipeId)
                }.singleOrNull()

                like?.let {
                    it.delete()
                    return@transaction true
                } ?: false
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
