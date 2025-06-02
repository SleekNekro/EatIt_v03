package com.github.SleekNekro.routes

import com.github.SleekNekro.data.DAO.UserDAO
import com.github.SleekNekro.model.request.FollowerRequest
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.collections.emptyList
import kotlin.collections.mapOf

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


        get("/{id}/followers/count") {
            try {
                val userId = call.parameters["id"]?.toLongOrNull()
                    ?: return@get call.respondInvalidId()

                val followersCount = UserDAO.getFollowers(userId) // 🔥 Ahora devuelve directamente un `Long`
                call.respond(HttpStatusCode.OK, mapOf("followers_count" to followersCount)) // ✅ Devuelve el número correcto
            } catch (e: Exception) {
                println("❌ Error en /followers/count: ${e.message}") // 🔥 Log para depurar
                call.respond(HttpStatusCode.InternalServerError, "Error al obtener seguidores: ${e.message}")
            }
        }


        get("/{id}/following/count") {
            try {
                val userId = call.parameters["id"]?.toLongOrNull()
                    ?: return@get call.respondInvalidId()

                val followingCount = UserDAO.getFollowing(userId) // 🔥 Ahora devuelve directamente un `Long`
                call.respond(HttpStatusCode.OK, mapOf("following_count" to followingCount)) // ✅ Devuelve el número correcto
            } catch (e: Exception) {
                println("❌ Error en /following/count: ${e.message}") // 🔥 Log para depurar
                call.respond(HttpStatusCode.InternalServerError, "Error al obtener cantidad de seguidos: ${e.message}")
            }
        }



    }
}