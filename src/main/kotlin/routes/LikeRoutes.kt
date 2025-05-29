package com.github.SleekNekro.routes

import com.github.SleekNekro.data.DAO.LikeDAO
import com.github.SleekNekro.util.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.configureLikeRoutes() {
    get {
        val likes = LikeDAO.getAllLikes().map { it.toDataClass() }
        call.respond(HttpStatusCode.OK, likes)
    }

    get("/recipe/{recipeId}") {
        val recipeId = call.parameters["recipeId"]?.toLongOrNull()
            ?: return@get call.respondInvalidId()

        val likes = LikeDAO.getLikesByRecipe(recipeId).map { it.toDataClass() }
        call.respond(HttpStatusCode.OK, likes)
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
        call.respond(HttpStatusCode.Created, like.toDataClass())
    }

    delete {
        val params = call.receiveParameters()
        val likeData = params.extractLikeData()
            ?: return@delete call.respondInvalidParameters()

        if (LikeDAO.removeLike(likeData.userId, likeData.recipeId)) {
            call.respond(HttpStatusCode.OK, "Like eliminado correctamente")
        } else {
            call.respondNotFound("Like")
        }
    }
}