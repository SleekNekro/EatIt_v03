package com.github.SleekNekro.routes

import com.github.SleekNekro.data.DAO.LikeDAO
import com.github.SleekNekro.data.clases.LikeStatus
import com.github.SleekNekro.util.SseBroadcaster
import com.github.SleekNekro.util.extractLikeData
import com.github.SleekNekro.util.respondInvalidId
import com.github.SleekNekro.util.respondInvalidParameters
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.transactions.transaction

private val likeBroadcaster = SseBroadcaster()

fun Route.configureLikeRoutes() {
    // Endpoint SSE para actualizaciones en tiempo real
    get("/events") {
        likeBroadcaster.addClient(call)
    }

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
        val likeStatus = LikeStatus(userId, recipeId, hasLiked)
        
        // Enviar el estado inicial
        likeBroadcaster.broadcast(
            "like_status",
            Json.encodeToString(likeStatus)
        )
        
        call.respond(HttpStatusCode.OK, likeStatus)
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

        val count = LikeDAO.getLikesByRecipe(recipeId).size
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
            // Enviar actualización SSE cuando se añade un like
            val likeCount = LikeDAO.getLikesByRecipe(likeData.recipeId).size
            likeBroadcaster.broadcast(
                "like_update",
                Json.encodeToString(
                    mapOf(
                        "type" to "add",
                        "recipeId" to likeData.recipeId,
                        "userId" to likeData.userId,
                        "likesCount" to likeCount
                    )
                )
            )
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
            // Enviar actualización SSE cuando se elimina un like
            val likeCount = LikeDAO.getLikesByRecipe(recipeId).size
            likeBroadcaster.broadcast(
                "like_update",
                Json.encodeToString(
                    mapOf(
                        "type" to "remove",
                        "recipeId" to recipeId,
                        "userId" to userId,
                        "likesCount" to likeCount
                    )
                )
            )
            call.respond(HttpStatusCode.OK, "Like eliminado correctamente")
        } else {
            call.respond(HttpStatusCode.NotFound, "Like no encontrado")
        }
    }
}