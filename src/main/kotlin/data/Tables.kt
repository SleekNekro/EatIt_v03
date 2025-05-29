package com.github.SleekNekro.data

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.*

object Users : LongIdTable("users") {
    val username = varchar("username", 50)
    val email = varchar("email", 100).uniqueIndex()
    val password = varchar("password", 255)
    val profilePic = varchar("profile_pic", 255).nullable()
    val followers = integer("followers").default(0)
    val createdAt = long("created_at")
}

object Recipes : LongIdTable("recipes") {
    val userId = long("user_id").references(Users.id)
    val title = varchar("title", 100)
    val description = text("description")
    val servings = integer("servings")
    val imageUrl = varchar("image_url", 255).nullable()
    val createdAt = long("created_at")
}

object Ingredients : LongIdTable("ingredients") {
    val recipeId = long("recipe_id").references(Recipes.id)
    val name = varchar("name", 100)
    val quantity = varchar("quantity", 50)
}

object Comments : LongIdTable("comments") {
    val recipeId = long("recipe_id").references(Recipes.id)
    val userId = long("user_id").references(Users.id)
    val content = text("content")
    val createdAt = long("created_at")
}

object Likes : LongIdTable("likes") {
    val userId = long("user_id").references(Users.id)
    val recipeId = long("recipe_id").references(Recipes.id)
    val createdAt = long("created_at")
}

// Tables.kt
object Followers : Table() {
    val id = long("id").autoIncrement()
    val userId = long("user_id").references(Users.id)
    val followerId = long("follower_id").references(Users.id)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    
    override val primaryKey = PrimaryKey(id)
    init {
        uniqueIndex(userId, followerId) // Evita duplicados
    }
}