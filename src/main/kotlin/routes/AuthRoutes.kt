package com.github.SleekNekro.routes

import com.github.SleekNekro.data.DAO.UserDAO
import com.github.SleekNekro.model.request.*
import com.github.SleekNekro.security.JwtConfig
import com.github.SleekNekro.security.generateToken
import com.github.SleekNekro.security.hashPassword
import com.github.SleekNekro.security.verifyPassword
import com.github.SleekNekro.util.*
import io.ktor.http.*
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File
import java.util.UUID

fun Route.configureAuthRoutes(jwtConfig: JwtConfig) {
    post("/register") {
        try {
            val multipart = call.receiveMultipart()
            var username: String? = null
            var email: String? = null
            var password: String? = null
            var profilePicUrl: String? = null

            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        when (part.name) {
                            "username" -> username = part.value
                            "email" -> email = part.value
                            "password" -> password = part.value
                        }
                    }
                    is PartData.FileItem -> {
                        if (part.name == "profilePic") {
                            val fileBytes = part.streamProvider().readBytes()
                            val fileName = "uploads/profile_${UUID.randomUUID()}.jpg"
                            File(fileName).writeBytes(fileBytes)

                            // URL accesible
                            profilePicUrl = "https://eatitv03-production.up.railway.app/$fileName"
                        }
                    }
                    else -> {}
                }
                part.dispose()
            }

            // Validaciones
            if (email == null || !email.contains("@")) {
                call.respondInvalidFormat("email")
                return@post
            }

            if (password == null || password.length < 6) {
                call.respond(HttpStatusCode.BadRequest, "La contraseña debe tener al menos 6 caracteres")
                return@post
            }

            if (UserDAO.getUserByEmail(email) != null) {
                call.respond(HttpStatusCode.Conflict, "$email ya está en uso")
                return@post
            }

            val hashedPassword = hashPassword(password)

            val userId = UserDAO.createUser(username!!, email!!, hashedPassword, profilePicUrl ?: "")

            // **Usamos RegisterResponse**
            call.respond(HttpStatusCode.Created, RegisterResponse(
                user = userId.toDataClass(),
                profilePicUrl = profilePicUrl ?: "",
                message = "$username registrado correctamente"
            ))

        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "Error al registrar usuario: ${e.message}")
        }
    }



    post("/login") {
        try {
            val loginRequest = call.receive<LoginRequest>()
            val userEntity = UserDAO.getUserByEmail(loginRequest.email)
                ?: return@post call.respondInvalidCredentials()

            val user = userEntity.toDataClass()
            val passwordMatch = verifyPassword(loginRequest.password, user.password)

            if (passwordMatch) {
                val token = generateToken(user, jwtConfig.secret, jwtConfig.domain, jwtConfig.audience)
                token?.let {
                    call.respond(HttpStatusCode.OK, LoginResponse(
                        token = it,
                        user = UserResponse.fromUser(user)
                    ))
                }
            } else {
                call.respondInvalidCredentials()
            }
        } catch (e: Exception) {
            call.respondServerError(e)
        }
    }
}