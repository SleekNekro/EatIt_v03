package com.github.SleekNekro.routes

import com.github.SleekNekro.data.DAO.UserDAO
import com.github.SleekNekro.model.request.FollowerRequest
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.configureFollowerRoutes() {
    route("/follower") {
        post("/{id}/follow") {
            try {
                val userId = call.parameters["id"]?.toLongOrNull()
                    ?: return@post call.respondInvalidId()

                val followerRequest = call.receive<FollowerRequest>()

                if (UserDAO.followUser(userId, followerRequest.followerId)) {
                    call.respond(HttpStatusCode.OK, "Usuario seguido correctamente")
                } else {
                    call.respond(HttpStatusCode.BadRequest, "No se pudo seguir al usuario")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error al seguir usuario: ${e.message}")
            }
        }

        delete("/{id}/unfollow/{followerId}") {
            try {
                val userId = call.parameters["id"]?.toLongOrNull()
                    ?: return@delete call.respondInvalidId()
                val followerId = call.parameters["followerId"]?.toLongOrNull()
                    ?: return@delete call.respondInvalidId()

                if (UserDAO.unfollowUser(userId, followerId)) {
                    call.respond(HttpStatusCode.OK, "Usuario dejado de seguir")
                } else {
                    call.respond(HttpStatusCode.BadRequest, "No se pudo dejar de seguir")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error al dejar de seguir: ${e.message}")
            }
        }

        get("/{id}/followers") {
            try {
                val userId = call.parameters["id"]?.toLongOrNull()
                    ?: return@get call.respondInvalidId()

                val followers = UserDAO.getFollowers(userId)
                call.respond(HttpStatusCode.OK, followers)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error al obtener seguidores: ${e.message}")
            }
        }

        get("/{id}/followers") {
            try {
                val userId = call.parameters["id"]?.toLongOrNull()
                    ?: return@get call.respondInvalidId()

                val followers = UserDAO.getFollowers(userId)
                call.respond(HttpStatusCode.OK, followers)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error al obtener seguidores: ${e.message}")
            }
        }

    }
}