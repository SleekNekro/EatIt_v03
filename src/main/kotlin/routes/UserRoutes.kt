package com.github.SleekNekro.routes

import com.github.SleekNekro.data.DAO.UserDAO
import com.github.SleekNekro.model.request.UpdateUserRequest
import com.github.SleekNekro.model.request.FollowerRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

// Funciones de extensión de utilidad
suspend fun ApplicationCall.respondInvalidId() {
    respond(HttpStatusCode.BadRequest, "ID inválido")
}

suspend fun ApplicationCall.respondNotFound(entity: String) {
    respond(HttpStatusCode.NotFound, "$entity no encontrado")
}

suspend fun ApplicationCall.respondInvalidFormat(field: String) {
    respond(HttpStatusCode.BadRequest, "Formato inválido para $field")
}

fun Route.configureUserRoutes() {
    route("/users") {
        get {
            try {
                val users = UserDAO.getAllUsers().map { it.toDataClass() }
                call.respond(HttpStatusCode.OK, users)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error al obtener usuarios: ${e.message}")
            }
        }

        get("/{id}") {
            try {
                val id = call.parameters["id"]?.toLongOrNull()
                    ?: return@get call.respondInvalidId()
                
                val user = UserDAO.getUserById(id)?.toDataClass()
                    ?: return@get call.respondNotFound("Usuario")
                
                call.respond(HttpStatusCode.OK, user)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error al obtener usuario: ${e.message}")
            }
        }

        get("/email/{email}") {
            try {
                val email = call.parameters["email"]
                    ?: return@get call.respondInvalidFormat("email")
                
                val user = UserDAO.getUserByEmail(email)?.toDataClass()
                    ?: return@get call.respondNotFound("Usuario")
                
                call.respond(HttpStatusCode.OK, user)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error al obtener usuario: ${e.message}")
            }
        }

        patch("/{id}") {
            try {
                val id = call.parameters["id"]?.toLongOrNull()
                    ?: return@patch call.respondInvalidId()
                
                val updateRequest = call.receive<UpdateUserRequest>()

                val updated = UserDAO.updateUser(
                    id = id,
                    newUsername = updateRequest.username,
                    newEmail = updateRequest.email,
                    newPassword = updateRequest.password,
                    newProfilePic = updateRequest.profilePic
                )

                if (updated) {
                    call.respond(HttpStatusCode.OK, "Usuario actualizado correctamente")
                } else {
                    call.respondNotFound("Usuario")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error al actualizar usuario: ${e.message}")
            }
        }

        delete("/{id}") {
            try {
                val id = call.parameters["id"]?.toLongOrNull()
                    ?: return@delete call.respondInvalidId()

                if (UserDAO.deleteUser(id)) {
                    call.respond(HttpStatusCode.OK, "Usuario eliminado correctamente")
                } else {
                    call.respondNotFound("Usuario")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error al eliminar usuario: ${e.message}")
            }
        }
    }
}