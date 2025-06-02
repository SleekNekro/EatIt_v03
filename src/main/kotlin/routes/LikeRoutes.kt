package com.github.SleekNekro.routes

import com.github.SleekNekro.data.DAO.LikeDAO
import com.github.SleekNekro.data.Likes
import com.github.SleekNekro.util.*
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.configureLikeRoutes() {
    get {
        val likes = LikeDAO.getAllLikes().map { it.toDataClass() }
        call.respond(HttpStatusCode.OK, likes)
    }
    get("/status/{userId}/{recipeId}") {
        val userId = call.parameters["userId"]?.toLongOrNull()
            ?: return@get call.respondInvalidId()

        val recipeId = call.parameters["recipeId"]?.toLongOrNull()
            ?: return@get call.respondInvalidId()

        application.environment.log.info("Ruta `/like/status/` ejecutada con userId=$userId y recipeId=$recipeId")

        val hasLiked = LikeDAO.getLikeExists(userId, recipeId)
        call.respond(HttpStatusCode.OK, mapOf("hasLiked" to hasLiked))
    }


    get("/recipe/{recipeId}") {
        val recipeId = call.parameters["recipeId"]?.toLongOrNull()
            ?: return@get call.respondInvalidId()

        val likes = LikeDAO.getLikesByRecipe(recipeId).map { it.toDataClass() }
        call.respond(HttpStatusCode.OK, likes)
    }
    get("/recipe/{recipeId}/likesCount") {
        val recipeId = call.parameters["recipeId"]?.toLongOrNull()
            ?: return@get call.respond(HttpStatusCode.BadRequest, "Id inválido")

        // Cuenta la cantidad de likes para la receta.
        val count = LikeDAO.getLikesByRecipe(recipeId).size
        // Responde con un objeto JSON que contenga el contador.
        call.respond(HttpStatusCode.OK, mapOf("likesCount" to count))
    }


    get("/user/{userId}") {
        val userId = call.parameters["userId"]?.toLongOrNull()
            ?: return@get call.respondInvalidId()

        val likes = LikeDAO.getLikesByUser(userId).map { it.toDataClass() }
        call.respond(HttpStatusCode.OK, likes)
    }

    post {
        val params = call.receiveParameters()
        val likeData = params.extractLikeData()
            ?: return@post call.respondInvalidParameters()

        val like = LikeDAO.addLike(
            userId = likeData.userId,
            recipeId = likeData.recipeId
        )
        
        if (like != null) {
            call.respond(HttpStatusCode.Created, like.toDataClass())
        } else {
            call.respond(HttpStatusCode.NotFound, "Usuario o receta no encontrados")
        }
    }

    put {
        val params = call.receiveParameters()
        val userId = params["userId"]?.toLongOrNull()
        val recipeId = params["recipeId"]?.toLongOrNull()

        if (userId == null || recipeId == null) {
            return@put call.respond(HttpStatusCode.BadRequest, "Parámetros inválidos")
        }

        val likeEliminado = transaction { LikeDAO.removeLike(userId, recipeId) }

        if (likeEliminado) {
            call.respond(HttpStatusCode.OK, "Like eliminado correctamente")
        } else {
            call.respond(HttpStatusCode.NotFound, "Like no encontrado")
        }
    }




}